package tech.algofinserve.recommendation.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tech.algofinserve.recommendation.model.domain.StockAlert;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis-backed replacement for ChartInkAlertFactory's static ConcurrentHashMaps.
 *
 * Key layout (Redis Lists):
 *   chartink:buy:stock:{stockCode}  → list of JSON-serialized StockAlert
 *   chartink:buy:scan:{scanName}    → list of JSON-serialized StockAlert
 *   chartink:sell:stock:{stockCode} → list of JSON-serialized StockAlert
 *   chartink:sell:scan:{scanName}   → list of JSON-serialized StockAlert
 *
 * All keys have a 25-hour TTL so stale data auto-expires if clearAll() is not called.
 */
@Component
public class RedisAlertCache {

    private static final Logger log = LoggerFactory.getLogger(RedisAlertCache.class);

    private static final String BUY_STOCK_PREFIX  = "chartink:buy:stock:";
    private static final String BUY_SCAN_PREFIX   = "chartink:buy:scan:";
    private static final String SELL_STOCK_PREFIX = "chartink:sell:stock:";
    private static final String SELL_SCAN_PREFIX  = "chartink:sell:scan:";

    private static final long TTL_HOURS = 25;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // In-memory fallback maps — used when Redis is unavailable (same structure as original ChartInkAlertFactory)
    private final Map<String, List<StockAlert>> buyStockFallback  = new ConcurrentHashMap<>();
    private final Map<String, List<StockAlert>> buyScanFallback   = new ConcurrentHashMap<>();
    private final Map<String, List<StockAlert>> sellStockFallback = new ConcurrentHashMap<>();
    private final Map<String, List<StockAlert>> sellScanFallback  = new ConcurrentHashMap<>();

    public RedisAlertCache(StringRedisTemplate redisTemplate,
                           @Qualifier("redisObjectMapper") ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    // ── BUY STOCK ────────────────────────────────────────────────────────────

    public boolean buyStockExists(String stockCode) {
        try {
            Long size = redisTemplate.opsForList().size(BUY_STOCK_PREFIX + stockCode);
            return size != null && size > 0;
        } catch (Exception e) {
            log.warn("Redis unavailable for buyStockExists, using fallback");
            return buyStockFallback.containsKey(stockCode);
        }
    }

    public void addToBuyStockAlerts(String stockCode, StockAlert alert) {
        try {
            push(BUY_STOCK_PREFIX + stockCode, alert);
        } catch (Exception e) {
            log.warn("Redis unavailable for addToBuyStockAlerts, using fallback");
            buyStockFallback.computeIfAbsent(stockCode, k -> new CopyOnWriteArrayList<>()).add(alert);
        }
    }

    public List<StockAlert> getBuyStockAlerts(String stockCode) {
        try {
            return fetchAll(BUY_STOCK_PREFIX + stockCode);
        } catch (Exception e) {
            log.warn("Redis unavailable for getBuyStockAlerts, using fallback");
            return buyStockFallback.getOrDefault(stockCode, new ArrayList<>());
        }
    }

    // ── BUY SCAN ─────────────────────────────────────────────────────────────

    public boolean buyScanExists(String scanName) {
        try {
            Long size = redisTemplate.opsForList().size(BUY_SCAN_PREFIX + scanName);
            return size != null && size > 0;
        } catch (Exception e) {
            log.warn("Redis unavailable for buyScanExists, using fallback");
            return buyScanFallback.containsKey(scanName);
        }
    }

    public void addToBuyScanAlerts(String scanName, StockAlert alert) {
        try {
            push(BUY_SCAN_PREFIX + scanName, alert);
        } catch (Exception e) {
            log.warn("Redis unavailable for addToBuyScanAlerts, using fallback");
            buyScanFallback.computeIfAbsent(scanName, k -> new CopyOnWriteArrayList<>()).add(alert);
        }
    }

    // ── SELL STOCK ───────────────────────────────────────────────────────────

    public boolean sellStockExists(String stockCode) {
        try {
            Long size = redisTemplate.opsForList().size(SELL_STOCK_PREFIX + stockCode);
            return size != null && size > 0;
        } catch (Exception e) {
            log.warn("Redis unavailable for sellStockExists, using fallback");
            return sellStockFallback.containsKey(stockCode);
        }
    }

    public void addToSellStockAlerts(String stockCode, StockAlert alert) {
        try {
            push(SELL_STOCK_PREFIX + stockCode, alert);
        } catch (Exception e) {
            log.warn("Redis unavailable for addToSellStockAlerts, using fallback");
            sellStockFallback.computeIfAbsent(stockCode, k -> new CopyOnWriteArrayList<>()).add(alert);
        }
    }

    public List<StockAlert> getSellStockAlerts(String stockCode) {
        try {
            return fetchAll(SELL_STOCK_PREFIX + stockCode);
        } catch (Exception e) {
            log.warn("Redis unavailable for getSellStockAlerts, using fallback");
            return sellStockFallback.getOrDefault(stockCode, new ArrayList<>());
        }
    }

    // ── SELL SCAN ────────────────────────────────────────────────────────────

    public boolean sellScanExists(String scanName) {
        try {
            Long size = redisTemplate.opsForList().size(SELL_SCAN_PREFIX + scanName);
            return size != null && size > 0;
        } catch (Exception e) {
            log.warn("Redis unavailable for sellScanExists, using fallback");
            return sellScanFallback.containsKey(scanName);
        }
    }

    public void addToSellScanAlerts(String scanName, StockAlert alert) {
        try {
            push(SELL_SCAN_PREFIX + scanName, alert);
        } catch (Exception e) {
            log.warn("Redis unavailable for addToSellScanAlerts, using fallback");
            sellScanFallback.computeIfAbsent(scanName, k -> new CopyOnWriteArrayList<>()).add(alert);
        }
    }

    // ── Report helpers ───────────────────────────────────────────────────────

    /**
     * Returns all buy-stock alerts grouped by stock code.
     * Used by StockAlertOutputHelper when generating the daily report.
     */
    public Map<String, List<StockAlert>> getAllBuyStockAlerts() {
        try {
            return fetchAllByPrefix(BUY_STOCK_PREFIX);
        } catch (Exception e) {
            log.warn("Redis unavailable for getAllBuyStockAlerts, using fallback");
            return new LinkedHashMap<>(buyStockFallback);
        }
    }

    // ── Clear ────────────────────────────────────────────────────────────────

    /**
     * Clears all ChartInk alert data from Redis. Called at EOD after report generation.
     */
    public void clearAll() {
        try {
            deleteByPrefix(BUY_STOCK_PREFIX);
            deleteByPrefix(BUY_SCAN_PREFIX);
            deleteByPrefix(SELL_STOCK_PREFIX);
            deleteByPrefix(SELL_SCAN_PREFIX);
            log.info("RedisAlertCache cleared all chartink keys");
        } catch (Exception e) {
            log.warn("Redis unavailable during clearAll, only clearing in-memory fallback");
        }
        buyStockFallback.clear();
        buyScanFallback.clear();
        sellStockFallback.clear();
        sellScanFallback.clear();
    }

    // ── Internal helpers ─────────────────────────────────────────────────────

    private void push(String key, StockAlert alert) {
        try {
            String json = objectMapper.writeValueAsString(alert);
            redisTemplate.opsForList().rightPush(key, json);
            redisTemplate.expire(key, TTL_HOURS, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize StockAlert for key {}: {}", key, e.getMessage());
        }
        // Redis connection exceptions are NOT caught here — they propagate up
        // so the caller (addTo* methods) can route to in-memory fallback
    }

    private List<StockAlert> fetchAll(String key) {
        List<String> jsonList = redisTemplate.opsForList().range(key, 0, -1);
        if (jsonList == null || jsonList.isEmpty()) return new ArrayList<>();

        List<StockAlert> result = new ArrayList<>();
        for (String json : jsonList) {
            try {
                result.add(objectMapper.readValue(json, StockAlert.class));
            } catch (JsonProcessingException e) {
                log.warn("Failed to deserialize StockAlert from key {}: {}", key, e.getMessage());
            }
        }
        return result;
    }

    private Map<String, List<StockAlert>> fetchAllByPrefix(String prefix) {
        Set<String> keys = redisTemplate.keys(prefix + "*");
        Map<String, List<StockAlert>> result = new LinkedHashMap<>();
        if (keys == null) return result;

        for (String key : keys) {
            String stockCode = key.substring(prefix.length());
            result.put(stockCode, fetchAll(key));
        }
        return result;
    }

    private void deleteByPrefix(String prefix) {
        Set<String> keys = redisTemplate.keys(prefix + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
