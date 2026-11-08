package tech.algofinserve.recommendation.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Redis-backed BlockingQueue — zero polling overhead design.
 *
 * How it works:
 *   - localQueue (LinkedBlockingQueue) is the PRIMARY queue — consumers always block here
 *   - Redis List is used ONLY for persistence — written on put(), never polled
 *   - On startup: unprocessed messages are restored from Redis → localQueue
 *   - On take(): message is removed from Redis (ack) after being taken from localQueue
 *
 * Redis commands used:
 *   - put()     → 1 LPUSH  (persist message)
 *   - take()    → 1 RPOP   (ack/remove from Redis)
 *   - startup   → 1 LRANGE per queue (restore unprocessed messages)
 *   - idle      → 0        (no polling at all)
 *
 * If Redis is unavailable:
 *   - put()  falls back silently — message still goes to localQueue (processed normally)
 *   - take() ack failure is ignored — message is already in localQueue
 */
public class RedisBlockingQueue<T> extends AbstractQueue<T> implements BlockingQueue<T> {

    private static final Logger log = LoggerFactory.getLogger(RedisBlockingQueue.class);
    private static final int LOCAL_CAPACITY = 10000;

    private final LinkedBlockingQueue<T> localQueue = new LinkedBlockingQueue<>(LOCAL_CAPACITY);
    private final StringRedisTemplate redisTemplate;
    private final String key;
    private final Class<T> elementType;
    private final ObjectMapper objectMapper;

    public RedisBlockingQueue(StringRedisTemplate redisTemplate, String key,
                              Class<T> elementType, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.key = key;
        this.elementType = elementType;
        this.objectMapper = objectMapper;
        restoreFromRedis();
    }

    // ── Writes ──────────────────────────────────────────────────────────────

    @Override
    public void put(T t) throws InterruptedException {
        persistToRedis(serialize(t)); // persist FIRST — so RPOP in ack always finds the item
        localQueue.put(t);            // then make available to consumers
    }

    @Override
    public boolean offer(T t) {
        boolean added = localQueue.offer(t);
        if (added) persistToRedis(serialize(t));
        return added;
    }

    @Override
    public boolean offer(T t, long timeout, TimeUnit unit) throws InterruptedException {
        boolean added = localQueue.offer(t, timeout, unit);
        if (added) persistToRedis(serialize(t));
        return added;
    }

    // ── Reads ───────────────────────────────────────────────────────────────

    /**
     * Blocks on localQueue — zero Redis commands while waiting.
     * Acks (removes) from Redis after taking.
     */
    @Override
    public T take() throws InterruptedException {
        T item = localQueue.take();  // blocks here with no Redis polling
        ackFromRedis();
        return item;
    }

    @Override
    public T poll() {
        T item = localQueue.poll();
        if (item != null) ackFromRedis();
        return item;
    }

    @Override
    public T poll(long timeout, TimeUnit unit) throws InterruptedException {
        T item = localQueue.poll(timeout, unit);
        if (item != null) ackFromRedis();
        return item;
    }

    @Override
    public T peek() {
        return localQueue.peek();
    }

    // ── Metadata ─────────────────────────────────────────────────────────────

    @Override
    public int size() {
        return localQueue.size();
    }

    @Override
    public int remainingCapacity() {
        return localQueue.remainingCapacity();
    }

    @Override
    public Iterator<T> iterator() {
        return localQueue.iterator();
    }

    @Override
    public int drainTo(Collection<? super T> c) {
        return localQueue.drainTo(c);
    }

    @Override
    public int drainTo(Collection<? super T> c, int maxElements) {
        return localQueue.drainTo(c, maxElements);
    }

    // ── Redis persistence helpers ─────────────────────────────────────────────

    /**
     * Called on startup — loads any unprocessed messages from Redis back into localQueue.
     * This handles the case where the app was restarted with messages still pending.
     */
    private void restoreFromRedis() {
        try {
            List<String> pending = redisTemplate.opsForList().range(key, 0, -1);
            if (pending == null || pending.isEmpty()) return;

            // Redis List is LPUSH (newest at head), so reverse to restore in FIFO order
            Collections.reverse(pending);
            int restored = 0;
            for (String json : pending) {
                try {
                    if (localQueue.offer(deserialize(json))) restored++;
                } catch (Exception e) {
                    log.warn("Skipping undeserializable message from Redis queue {}: {}", key, e.getMessage());
                }
            }
            // Clear Redis now — messages are safely in localQueue
            // Without this, every restart would re-send the same messages
            redisTemplate.delete(key);
            if (restored > 0) {
                log.info("Restored {} unprocessed message(s) from Redis into localQueue for key '{}', Redis key cleared", restored, key);
            }
        } catch (Exception e) {
            log.warn("Could not restore from Redis for queue '{}' (Redis may be unavailable): {}", key, e.getMessage());
        }
    }

    /**
     * LPUSH to Redis — called on put(). Best-effort, never blocks or throws.
     */
    private void persistToRedis(String json) {
        try {
            redisTemplate.opsForList().leftPush(key, json);
        } catch (Exception e) {
            log.warn("Redis persist failed for queue '{}', message is still in localQueue: {}", key, e.getMessage());
        }
    }

    /**
     * RPOP from Redis — called on take()/poll(). Removes the oldest persisted entry.
     * Best-effort, never blocks or throws.
     */
    private void ackFromRedis() {
        try {
            redisTemplate.opsForList().rightPop(key);
        } catch (Exception e) {
            log.warn("Redis ack failed for queue '{}': {}", key, e.getMessage());
        }
    }

    // ── Serialization ────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private String serialize(T t) {
        if (elementType == String.class) return (String) t;
        try {
            return objectMapper.writeValueAsString(t);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize for Redis queue key=" + key, e);
        }
    }

    @SuppressWarnings("unchecked")
    private T deserialize(String json) {
        if (elementType == String.class) return (T) json;
        try {
            return objectMapper.readValue(json, elementType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize from Redis queue key=" + key, e);
        }
    }
}
