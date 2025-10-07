package tech.algofinserve.recommendation.messaging;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;


@Service
public class TelegramSenderPool {
    private final ExecutorService executor;
    private final List<TelegramMessaging> workers;
    private final AtomicInteger roundRobin = new AtomicInteger(0);

    public TelegramSenderPool() {
        int poolSize = Math.max(3, Runtime.getRuntime().availableProcessors()); // choose as needed
        this.executor = Executors.newFixedThreadPool(poolSize, r -> {
            Thread t = new Thread(r);
            t.setName("telegram-sender-" + t.getId());
            t.setDaemon(true);
            return t;
        });

        // create worker instances - each can have its own default chat id or same one
        this.workers = new ArrayList<>(poolSize);
        for (int i = 0; i < poolSize; i++) {
            workers.add(new TelegramMessaging()); // or pass channel from config
        }
    }

    /**
     * Submit send and return a Future<Boolean>.
     */
    public Future<Boolean> sendAsync(String chatId,String text) {
        // pick a worker instance round-robin (not strictly required; TelegramMessaging is thread-safe)
        int idx = roundRobin.getAndUpdate(i -> (i + 1) % workers.size());
        TelegramMessaging worker = workers.get(idx);

        return executor.submit(() -> StringUtils.isEmpty(chatId)?
                worker.sendMessage(text) :
                worker.sendMessage(chatId,text));
    }

    /**
     * Submit and wait up to timeoutMillis. Returns boolean result, or false on exception/timeout.
     */
    public boolean sendAndWait(String chatId,String text, long timeoutMillis) {
        Future<Boolean> future = sendAsync(chatId,text);
        try {
            return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            int idx = roundRobin.getAndUpdate(i -> (i + 1) % workers.size());
            TelegramMessaging worker = workers.get(idx);
            executor.submit(() -> worker.sendMessage(chatId,text));
            return false;
        } catch (ExecutionException | TimeoutException e) {
            future.cancel(true);
            e.printStackTrace();
            int idx = roundRobin.getAndUpdate(i -> (i + 1) % workers.size());
            TelegramMessaging worker = workers.get(idx);
            executor.submit(() -> worker.sendMessage(chatId,text));
            return false;
        }
    }

    public void shutdown() {
        executor.shutdown();
    }
}
