package tech.algofinserve.recommendation.service;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class RealTimeMarketDataService {
    
    private static final Logger logger = LoggerFactory.getLogger(RealTimeMarketDataService.class);
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    // Cache for storing prices to avoid too many API calls
    private final Map<String, PriceData> priceCache = new HashMap<>();
    private static final long CACHE_DURATION_MINUTES = 1; // Cache for 1 minute
    
    private static class PriceData {
        private final double price;
        private final long timestamp;
        
        public PriceData(double price, long timestamp) {
            this.price = price;
            this.timestamp = timestamp;
        }
        
        public double getPrice() { return price; }
        
        public boolean isExpired(long cacheDurationMs) {
            return (System.currentTimeMillis() - timestamp) > cacheDurationMs;
        }
    }
    
    /**
     * Get real-time Nifty 50 price
     */
    public Double getNiftyPrice() {
        return getRealTimePrice("NIFTY", "NSE");
    }
    
    /**
     * Get real-time Bank Nifty price
     */
    public Double getBankNiftyPrice() {
        return getRealTimePrice("BANKNIFTY", "NSE");
    }
    
    /**
     * Get real-time price for any symbol
     */
    private Double getRealTimePrice(String symbol, String exchange) {
        String cacheKey = symbol + "_" + exchange;
        
        // Check cache first
        PriceData cachedData = priceCache.get(cacheKey);
        if (cachedData != null && !cachedData.isExpired(CACHE_DURATION_MINUTES * 60 * 1000)) {
            logger.info("Using cached price for {}: {}", symbol, cachedData.getPrice());
            return cachedData.getPrice();
        }
        
        try {
            logger.info("Fetching real-time price for {} from external API", symbol);
            
            // Try multiple APIs for better reliability
            Double price = tryYahooFinanceAPI(symbol);
            if (price != null) {
                cachePrice(cacheKey, price);
                return price;
            }
            
            price = tryAlphaVantageAPI(symbol);
            if (price != null) {
                cachePrice(cacheKey, price);
                return price;
            }
            
            // If all APIs fail, try a simple web scraping approach
            price = tryWebScraping(symbol);
            if (price != null) {
                cachePrice(cacheKey, price);
                return price;
            }
            
        } catch (Exception e) {
            logger.error("Error fetching real-time price for " + symbol, e);
        }
        
        logger.warn("All real-time price sources failed for {}", symbol);
        return null;
    }
    
    /**
     * Try Yahoo Finance API (free tier)
     */
    private Double tryYahooFinanceAPI(String symbol) {
        try {
            // Yahoo Finance API endpoint
            String yahooSymbol = getYahooSymbol(symbol);
            String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + yahooSymbol;
            
            // Set timeout and headers to avoid blocking
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            org.springframework.http.ResponseEntity<String> response = restTemplate.exchange(
                url, org.springframework.http.HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JSONObject json = new JSONObject(response.getBody());
                JSONObject chart = json.getJSONObject("chart");
                JSONObject result = chart.getJSONArray("result").getJSONObject(0);
                JSONObject meta = result.getJSONObject("meta");
                
                if (meta.has("regularMarketPrice")) {
                    double price = meta.getDouble("regularMarketPrice");
                    logger.info("Got price from Yahoo Finance for {}: {}", symbol, price);
                    return price;
                }
            }
        } catch (Exception e) {
            logger.debug("Yahoo Finance API failed for {}: {}", symbol, e.getMessage());
        }
        return null;
    }
    
    /**
     * Try Alpha Vantage API (free tier with API key)
     */
    private Double tryAlphaVantageAPI(String symbol) {
        try {
            // Note: You would need to add your Alpha Vantage API key here
            String apiKey = "YOUR_ALPHA_VANTAGE_API_KEY"; // Replace with actual API key
            String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + 
                        getAlphaVantageSymbol(symbol) + "&apikey=" + apiKey;
            
            String response = restTemplate.getForObject(url, String.class);
            if (response != null) {
                JSONObject json = new JSONObject(response);
                if (json.has("Global Quote")) {
                    JSONObject quote = json.getJSONObject("Global Quote");
                    if (quote.has("05. price")) {
                        double price = Double.parseDouble(quote.getString("05. price"));
                        logger.info("Got price from Alpha Vantage for {}: {}", symbol, price);
                        return price;
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Alpha Vantage API failed for {}: {}", symbol, e.getMessage());
        }
        return null;
    }
    
    /**
     * Try web scraping approach (fallback)
     */
    private Double tryWebScraping(String symbol) {
        try {
            // This is a simplified approach - in production, you'd use proper web scraping
            // For now, return null to trigger fallback to previous day's close
            logger.debug("Web scraping not implemented for {}", symbol);
            return null;
        } catch (Exception e) {
            logger.debug("Web scraping failed for {}: {}", symbol, e.getMessage());
        }
        return null;
    }
    
    /**
     * Convert symbol to Yahoo Finance format
     */
    private String getYahooSymbol(String symbol) {
        switch (symbol.toUpperCase()) {
            case "NIFTY":
                return "^NSEI"; // Nifty 50 index
            case "BANKNIFTY":
                return "^NSEBANK"; // Bank Nifty index
            default:
                return symbol + ".NS"; // NSE stocks
        }
    }
    
    /**
     * Convert symbol to Alpha Vantage format
     */
    private String getAlphaVantageSymbol(String symbol) {
        switch (symbol.toUpperCase()) {
            case "NIFTY":
                return "NSEI"; // Nifty 50 index
            case "BANKNIFTY":
                return "NSEBANK"; // Bank Nifty index
            default:
                return symbol + ".NSE"; // NSE stocks
        }
    }
    
    /**
     * Cache the price data
     */
    private void cachePrice(String cacheKey, Double price) {
        priceCache.put(cacheKey, new PriceData(price, System.currentTimeMillis()));
    }
    
    /**
     * Clear expired cache entries
     */
    public void clearExpiredCache() {
        priceCache.entrySet().removeIf(entry -> 
            entry.getValue().isExpired(CACHE_DURATION_MINUTES * 60 * 1000));
    }
}
