package tech.algofinserve.recommendation.service;

import com.angelbroking.smartapi.SmartConnect;
// Removed LTP import as it doesn't exist in SmartAPI library
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tech.algofinserve.recommendation.constants.ExchSeg;
import tech.algofinserve.recommendation.infra.AngelApiKey;
import tech.algofinserve.recommendation.infra.AngelBrokerConnector;
import tech.algofinserve.recommendation.model.domain.Ticker;
import tech.algofinserve.recommendation.model.dto.StockFundamentalsDto;

import java.util.HashMap;
import java.util.Map;

@Service
public class StockService {
    
    private static final Logger logger = LoggerFactory.getLogger(StockService.class);
    
    @Autowired
    private WebClient.Builder webClientBuilder;
    
    @Autowired
    private AngelMarketDataService angelMarketDataService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public StockFundamentalsDto getStockFundamentals(String symbol) {
        try {
            // Get live market price from Angel One
            Double ltp = getLiveMarketPrice(symbol);
            
            // Get fundamental data from Yahoo Finance
            Map<String, Double> fundamentals = getFundamentalData(symbol);
            
            // Merge the data
            return new StockFundamentalsDto(
                symbol,
                fundamentals.get("peRatio"),
                fundamentals.get("roe"),
                fundamentals.get("roc"),
                fundamentals.get("bookValue"),
                fundamentals.get("marketCap"),
                fundamentals.get("sales"),
                ltp
            );
            
        } catch (Exception e) {
            logger.error("Error fetching stock fundamentals for symbol: " + symbol, e);
            throw new RuntimeException("Failed to fetch real fundamental data for: " + symbol, e);
        }
    }
    
    private Double getLiveMarketPrice(String symbol) {
        try {
            // Use the AngelMarketDataService following marketdata patterns
            return angelMarketDataService.getLivePrice(symbol, ExchSeg.NSE);
        } catch (Exception e) {
            logger.error("Error fetching live market price for symbol: " + symbol, e);
            return null;
        }
    }
    
    private Map<String, Double> getFundamentalData(String symbol) {
        Map<String, Double> fundamentals = new HashMap<>();
        
        try {
            // Try multiple data sources for real fundamental data
            fundamentals = fetchFromYahooFinance(symbol);
            
            if (fundamentals.isEmpty()) {
                logger.warn("Yahoo Finance failed, trying Alpha Vantage for: " + symbol);
                fundamentals = fetchFromAlphaVantage(symbol);
            }
            
            if (fundamentals.isEmpty()) {
                logger.warn("Alpha Vantage failed, trying NSE India for: " + symbol);
                fundamentals = fetchFromNSEIndia(symbol);
            }
            
            if (fundamentals.isEmpty()) {
                logger.error("All external APIs failed for symbol: " + symbol);
                throw new RuntimeException("Unable to fetch real fundamental data for: " + symbol);
            }
            
        } catch (Exception e) {
            logger.error("Error fetching fundamental data for symbol: " + symbol, e);
            throw new RuntimeException("Failed to fetch real data for: " + symbol, e);
        }
        
        return fundamentals;
    }
    
    private Map<String, Double> fetchFromYahooFinance(String symbol) {
        Map<String, Double> fundamentals = new HashMap<>();
        
        try {
            String yahooSymbol = symbol + ".NS";
            String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + yahooSymbol;
            
            logger.info("Fetching from Yahoo Finance: " + yahooSymbol);
            
            String response = webClientBuilder.build()
                .get()
                .uri(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .retrieve()
                .bodyToMono(String.class)
                .block();
            
            if (response != null && !response.isEmpty()) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode chart = root.path("chart").path("result");
                
                if (chart.isArray() && chart.size() > 0) {
                    JsonNode meta = chart.get(0).path("meta");
                    
                    Double peRatio = getDoubleValue(meta, "trailingPE");
                    Double marketCap = getDoubleValue(meta, "marketCap");
                    
                    if (peRatio != null || marketCap != null) {
                        fundamentals.put("peRatio", peRatio);
                        fundamentals.put("marketCap", marketCap);
                        logger.info("Successfully fetched from Yahoo Finance: " + symbol);
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Yahoo Finance API failed for: " + symbol, e);
        }
        
        return fundamentals;
    }
    
    private Map<String, Double> fetchFromAlphaVantage(String symbol) {
        Map<String, Double> fundamentals = new HashMap<>();
        
        try {
            // Alpha Vantage API (you'll need to get a free API key)
            String apiKey = "demo"; // Replace with actual API key
            String url = "https://www.alphavantage.co/query?function=OVERVIEW&symbol=" + symbol + ".BSE&apikey=" + apiKey;
            
            logger.info("Fetching from Alpha Vantage: " + symbol);
            
            String response = webClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();
            
            if (response != null && !response.isEmpty()) {
                JsonNode root = objectMapper.readTree(response);
                
                if (!root.has("Error Message")) {
                    fundamentals.put("peRatio", getDoubleValue(root, "PERatio"));
                    fundamentals.put("roe", getDoubleValue(root, "ReturnOnEquityTTM"));
                    fundamentals.put("bookValue", getDoubleValue(root, "BookValue"));
                    fundamentals.put("marketCap", getDoubleValue(root, "MarketCapitalization"));
                    fundamentals.put("sales", getDoubleValue(root, "RevenueTTM"));
                    
                    logger.info("Successfully fetched from Alpha Vantage: " + symbol);
                }
            }
            
        } catch (Exception e) {
            logger.error("Alpha Vantage API failed for: " + symbol, e);
        }
        
        return fundamentals;
    }
    
    private Map<String, Double> fetchFromNSEIndia(String symbol) {
        Map<String, Double> fundamentals = new HashMap<>();
        
        try {
            // NSE India API
            String url = "https://www.nseindia.com/api/quote-equity?symbol=" + symbol;
            
            logger.info("Fetching from NSE India: " + symbol);
            
            String response = webClientBuilder.build()
                .get()
                .uri(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(String.class)
                .block();
            
            if (response != null && !response.isEmpty()) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode info = root.path("info");
                
                fundamentals.put("peRatio", getDoubleValue(info, "pe"));
                fundamentals.put("bookValue", getDoubleValue(info, "bookValue"));
                fundamentals.put("marketCap", getDoubleValue(info, "marketCap"));
                
                logger.info("Successfully fetched from NSE India: " + symbol);
            }
            
        } catch (Exception e) {
            logger.error("NSE India API failed for: " + symbol, e);
        }
        
        return fundamentals;
    }
    
    // Removed mock data methods - now only returns real data
    
    private Double getDoubleValue(JsonNode node, String fieldName) {
        JsonNode field = node.path(fieldName);
        if (!field.isMissingNode() && !field.isNull()) {
            return field.asDouble();
        }
        return null;
    }
    
    // Removed mock data fallback - now only returns real data or throws exception
}
