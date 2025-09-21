package tech.algofinserve.recommendation.trade.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import tech.algofinserve.recommendation.trade.util.SupertrendUtil;

import java.util.List;
import java.util.Map;

/**
 * Adapter to fetch market data from the market-data project
 * This acts as a bridge between recommendation-2-0 and market-data projects
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MarketDataAdapter {

    private final RestTemplate restTemplate;

    @Value("${marketdata.api.base-url:http://localhost:8081}")
    private String marketDataBaseUrl;

    /**
     * Get previous candle low for a symbol and timeframe
     */
    public Double getPrevCandleLow(String symbol, String timeframe) {
        try {
            String url = String.format("%s/api/ohlc/%s/%s/previous", marketDataBaseUrl, symbol, timeframe);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("low")) {
                return ((Number) response.get("low")).doubleValue();
            }
        } catch (RestClientException e) {
            log.warn("Failed to fetch previous candle low for {} {}: {}", symbol, timeframe, e.getMessage());
        }
        return null;
    }

    /**
     * Get previous candle high for a symbol and timeframe
     */
    public Double getPrevCandleHigh(String symbol, String timeframe) {
        try {
            String url = String.format("%s/api/ohlc/%s/%s/previous", marketDataBaseUrl, symbol, timeframe);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("high")) {
                return ((Number) response.get("high")).doubleValue();
            }
        } catch (RestClientException e) {
            log.warn("Failed to fetch previous candle high for {} {}: {}", symbol, timeframe, e.getMessage());
        }
        return null;
    }

    /**
     * Get current LTP (Last Traded Price) for a symbol
     */
    public Double getCurrentLTP(String symbol) {
        try {
            String url = String.format("%s/api/ltp/%s", marketDataBaseUrl, symbol);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("ltp")) {
                return ((Number) response.get("ltp")).doubleValue();
            }
        } catch (RestClientException e) {
            log.warn("Failed to fetch LTP for {}: {}", symbol, e.getMessage());
        }
        return null;
    }

    /**
     * Get historical OHLC data for Supertrend calculation
     */
    public List<Map<String, Object>> getHistoricalOHLC(String symbol, String timeframe, int limit) {
        try {
            String url = String.format("%s/api/ohlc/%s/%s?limit=%d", marketDataBaseUrl, symbol, timeframe, limit);
            List<Map<String, Object>> response = restTemplate.getForObject(url, List.class);
            return response;
        } catch (RestClientException e) {
            log.warn("Failed to fetch historical OHLC for {} {}: {}", symbol, timeframe, e.getMessage());
            return List.of();
        }
    }

    /**
     * Get Supertrend value for a symbol and timeframe
     * This will be calculated using the SupertrendUtil
     */
    public Double getSupertrend(String symbol, String timeframe, int atrPeriod, int multiplier) {
        try {
            // Get historical data for Supertrend calculation
            List<Map<String, Object>> ohlcData = getHistoricalOHLC(symbol, timeframe, 50);
            
            if (ohlcData.isEmpty()) {
                log.warn("No OHLC data available for Supertrend calculation: {} {}", symbol, timeframe);
                return null;
            }

            // Convert to OHLC format for Supertrend calculation
            // This will be implemented in SupertrendUtil
            return SupertrendUtil.calculateSupertrend(ohlcData, atrPeriod, multiplier);
            
        } catch (Exception e) {
            log.warn("Failed to calculate Supertrend for {} {}: {}", symbol, timeframe, e.getMessage());
            return null;
        }
    }

    /**
     * Get ATR (Average True Range) for a symbol and timeframe
     */
    public Double getATR(String symbol, String timeframe, int period) {
        try {
            List<Map<String, Object>> ohlcData = getHistoricalOHLC(symbol, timeframe, period + 10);
            
            if (ohlcData.isEmpty()) {
                log.warn("No OHLC data available for ATR calculation: {} {}", symbol, timeframe);
                return null;
            }

            return SupertrendUtil.calculateATR(ohlcData, period);
            
        } catch (Exception e) {
            log.warn("Failed to calculate ATR for {} {}: {}", symbol, timeframe, e.getMessage());
            return null;
        }
    }

    /**
     * Check if market data service is available
     */
    public boolean isMarketDataAvailable() {
        try {
            String url = String.format("%s/health", marketDataBaseUrl);
            restTemplate.getForObject(url, String.class);
            return true;
        } catch (RestClientException e) {
            log.debug("Market data service not available: {}", e.getMessage());
            return false;
        }
    }
}
