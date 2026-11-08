package tech.algofinserve.recommendation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tech.algofinserve.recommendation.constants.ExchSeg;
import tech.algofinserve.recommendation.model.dto.StockFundamentalsDto;

import java.util.HashMap;
import java.util.Map;

@Service
public class StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AngelMarketDataService angelMarketDataService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public StockFundamentalsDto getStockFundamentals(String symbol) {
        try {
            Double ltp = getLiveMarketPrice(symbol);
            Map<String, Double> fundamentals = getFundamentalData(symbol);

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
            return angelMarketDataService.getLivePrice(symbol, ExchSeg.NSE);
        } catch (Exception e) {
            logger.error("Error fetching live market price for symbol: " + symbol, e);
            return null;
        }
    }

    private Map<String, Double> getFundamentalData(String symbol) {
        Map<String, Double> fundamentals = fetchFromYahooFinance(symbol);

        if (fundamentals.isEmpty()) {
            logger.warn("Yahoo Finance failed, trying Alpha Vantage for: " + symbol);
            fundamentals = fetchFromAlphaVantage(symbol);
        }

        if (fundamentals.isEmpty()) {
            logger.warn("Alpha Vantage failed, trying NSE India for: " + symbol);
            fundamentals = fetchFromNSEIndia(symbol);
        }

        if (fundamentals.isEmpty()) {
            throw new RuntimeException("Unable to fetch real fundamental data for: " + symbol);
        }

        return fundamentals;
    }

    private Map<String, Double> fetchFromYahooFinance(String symbol) {
        Map<String, Double> fundamentals = new HashMap<>();
        try {
            String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + symbol + ".NS";
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            if (response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode chart = root.path("chart").path("result");
                if (chart.isArray() && chart.size() > 0) {
                    JsonNode meta = chart.get(0).path("meta");
                    fundamentals.put("peRatio", getDoubleValue(meta, "trailingPE"));
                    fundamentals.put("marketCap", getDoubleValue(meta, "marketCap"));
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
            String url = "https://www.alphavantage.co/query?function=OVERVIEW&symbol=" + symbol + ".BSE&apikey=demo";
            String response = restTemplate.getForObject(url, String.class);

            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                if (!root.has("Error Message")) {
                    fundamentals.put("peRatio",    getDoubleValue(root, "PERatio"));
                    fundamentals.put("roe",         getDoubleValue(root, "ReturnOnEquityTTM"));
                    fundamentals.put("bookValue",   getDoubleValue(root, "BookValue"));
                    fundamentals.put("marketCap",   getDoubleValue(root, "MarketCapitalization"));
                    fundamentals.put("sales",       getDoubleValue(root, "RevenueTTM"));
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
            String url = "https://www.nseindia.com/api/quote-equity?symbol=" + symbol;
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.set("Accept", "application/json");

            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            if (response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode info = root.path("info");
                fundamentals.put("peRatio",   getDoubleValue(info, "pe"));
                fundamentals.put("bookValue", getDoubleValue(info, "bookValue"));
                fundamentals.put("marketCap", getDoubleValue(info, "marketCap"));
            }
        } catch (Exception e) {
            logger.error("NSE India API failed for: " + symbol, e);
        }
        return fundamentals;
    }

    private Double getDoubleValue(JsonNode node, String fieldName) {
        JsonNode field = node.path(fieldName);
        if (!field.isMissingNode() && !field.isNull()) {
            return field.asDouble();
        }
        return null;
    }
}
