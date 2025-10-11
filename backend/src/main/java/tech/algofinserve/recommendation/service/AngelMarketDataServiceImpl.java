package tech.algofinserve.recommendation.service;

import com.angelbroking.smartapi.SmartConnect;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tech.algofinserve.recommendation.constants.ExchSeg;
import tech.algofinserve.recommendation.infra.AngelApiKey;
import tech.algofinserve.recommendation.infra.AngelBrokerConnector;
import tech.algofinserve.recommendation.model.domain.Ticker;

import java.util.HashMap;
import java.util.Map;

@Service
public class AngelMarketDataServiceImpl implements AngelMarketDataService {
    
    private static final Logger logger = LoggerFactory.getLogger(AngelMarketDataServiceImpl.class);
    
    @Autowired
    private AngelApiKey angelApiKey;
    
    @Autowired
    private MetaDataService metaDataService;
    
    @Override
    public Double getLTPForTicker(SmartConnect smartConnect, Ticker ticker) {
        try {
            if (smartConnect != null && ticker != null) {
                logger.info("Getting real LTP for ticker: " + ticker.getStockSymbol());
                
                try {
                    // Use SmartAPI getLTP method with correct parameters
                    // Format: getLTP(exchange, symbol, token)
                    String exchange = ticker.getExchSeg().toString(); // NSE
                    String symbol = ticker.getStockSymbol(); // NIFTY or BANKNIFTY
                    String token = ticker.getToken(); // Token from instrument master
                    
                    logger.info("Calling SmartAPI getLTP with exchange: {}, symbol: {}, token: {}", 
                               exchange, symbol, token);
                    
                    // Call the actual SmartAPI getLTP method
                    JSONObject result = smartConnect.getLTP(exchange, symbol, token);
                    
                    if (result != null && result.has("data")) {
                        JSONObject data = result.getJSONObject("data");
                        if (data.has("ltp")) {
                            double ltp = data.getDouble("ltp");
                            logger.info("Got REAL LTP from SmartAPI for {}: {}", symbol, ltp);
                            return ltp;
                        } else {
                            logger.warn("SmartAPI response missing 'ltp' field for {}", symbol);
                        }
                    } else {
                        logger.warn("SmartAPI response missing 'data' field for {}", symbol);
                    }
                    
                } catch (Exception smartApiException) {
                    logger.error("SmartAPI getLTP failed for {}: {}", ticker.getStockSymbol(), smartApiException.getMessage());
                    logger.error("SmartAPI error details: ", smartApiException);
                }
            } else {
                logger.warn("SmartConnect or Ticker is null - SmartConnect: {}, Ticker: {}", 
                           smartConnect != null, ticker != null);
            }
        } catch (Exception e) {
            logger.error("Error getting LTP for ticker: " + (ticker != null ? ticker.getStockSymbol() : "null"), e);
        }
        return null;
    }
    
    
    // Removed unused method - will be implemented when SmartAPI methods are clarified
    
    @Override
    public Double getLivePrice(String symbol, ExchSeg exchSeg) {
        try {
            logger.info("Attempting to get live price for symbol: " + symbol);
            
            // Try direct HTTP approach first (simpler and more reliable)
            Double directPrice = getPriceFromDirectAPI(symbol);
            if (directPrice != null) {
                logger.info("Got price from direct API for " + symbol + ": " + directPrice);
                return directPrice;
            }
            
            // Fallback to SmartAPI approach
            SmartConnect smartConnect = AngelBrokerConnector.getSmartConnectSession(angelApiKey);
            
            if (smartConnect == null) {
                logger.error("SmartConnect is null - cannot fetch live price for " + symbol);
                return null;
            }
            
            logger.info("SmartConnect obtained successfully for " + symbol);
            
            Ticker ticker = metaDataService.getInstrumentTickerForStockName(symbol, exchSeg);
            if (ticker == null) {
                logger.error("Ticker is null for symbol: " + symbol);
                return null;
            }
            
            logger.info("Ticker obtained for " + symbol + " - Token: " + ticker.getToken() + ", Exchange: " + ticker.getExchSeg());
            
            Double ltp = getLTPForTicker(smartConnect, ticker);
            
            if (ltp != null) {
                logger.info("Successfully got live price for " + symbol + ": " + ltp);
                return ltp;
            } else {
                logger.warn("LTP is null for symbol: " + symbol);
            }
            
            logger.warn("Failed to get live price for symbol: " + symbol);
            return null;
            
        } catch (Exception e) {
            logger.error("Error fetching live price for symbol: " + symbol, e);
            return null;
        }
    }
    
    /**
     * Get real market data from Yahoo Finance API
     */
    private Double getPriceFromDirectAPI(String symbol) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            // Yahoo Finance API for Indian indices
            String yahooSymbol = getYahooSymbol(symbol);
            String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + yahooSymbol;
            
            logger.info("Fetching real price from Yahoo Finance for " + symbol + " using symbol: " + yahooSymbol);
            
            // Set headers to avoid blocking
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
                    logger.info("Got REAL price from Yahoo Finance for " + symbol + ": " + price);
                    return price;
                } else if (meta.has("previousClose")) {
                    double price = meta.getDouble("previousClose");
                    logger.info("Got previous close from Yahoo Finance for " + symbol + ": " + price);
                    return price;
                }
            }
            
            logger.warn("Yahoo Finance API failed for " + symbol);
            return null;
            
        } catch (Exception e) {
            logger.error("Error fetching real price from Yahoo Finance for " + symbol, e);
            return null;
        }
    }
    
    /**
     * Convert symbol to Yahoo Finance format
     */
    private String getYahooSymbol(String symbol) {
        switch (symbol.toUpperCase()) {
            case "NIFTY":
                return "^NSEI"; // Yahoo Finance symbol for Nifty 50
            case "BANKNIFTY":
                return "^NSEBANK"; // Yahoo Finance symbol for Bank Nifty
            default:
                return symbol + ".NS"; // Default for Indian stocks
        }
    }
}
