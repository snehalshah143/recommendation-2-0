package tech.algofinserve.recommendation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.algofinserve.recommendation.constants.ExchSeg;
import tech.algofinserve.recommendation.model.dto.MarketIndicesDto;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class MarketIndicesServiceImpl implements MarketIndicesService {
    
    private static final Logger logger = LoggerFactory.getLogger(MarketIndicesServiceImpl.class);
    
    @Autowired
    private AngelMarketDataService angelMarketDataService;
    
    // Market timings (IST)
    private static final LocalTime MARKET_OPEN_TIME = LocalTime.of(9, 15); // 9:15 AM
    private static final LocalTime MARKET_CLOSE_TIME = LocalTime.of(15, 30); // 3:30 PM
    
    // Fallback prices when real-time data is not available (updated to more realistic 2024 levels)
    private static final Double FALLBACK_NIFTY_PRICE = 24500.0;
    private static final Double FALLBACK_BANKNIFTY_PRICE = 52000.0;
    
    @Override
    public MarketIndicesDto getMarketIndices() {
        try {
            logger.info("Fetching market indices data");
            
            // Get current prices and previous closes from Yahoo Finance
            PriceAndPreviousClose niftyData = getYahooFinancePriceAndPreviousClose("^NSEI");
            PriceAndPreviousClose bankNiftyData = getYahooFinancePriceAndPreviousClose("^NSEBANK");
            
            Double niftyPrice, niftyPreviousClose, bankNiftyPrice, bankNiftyPreviousClose;
            
            if (niftyData != null) {
                niftyPrice = niftyData.getCurrentPrice();
                niftyPreviousClose = niftyData.getPreviousClose();
                logger.info("Using REAL Yahoo Finance Nifty data - Current: {}, Previous Close: {}", niftyPrice, niftyPreviousClose);
            } else {
                // Fallback to individual calls
                niftyPrice = getNiftyPrice();
                niftyPreviousClose = getPreviousDayNiftyClose();
                logger.warn("Using fallback Nifty data - Current: {}, Previous Close: {}", niftyPrice, niftyPreviousClose);
            }
            
            if (bankNiftyData != null) {
                bankNiftyPrice = bankNiftyData.getCurrentPrice();
                bankNiftyPreviousClose = bankNiftyData.getPreviousClose();
                logger.info("Using REAL Yahoo Finance Bank Nifty data - Current: {}, Previous Close: {}", bankNiftyPrice, bankNiftyPreviousClose);
            } else {
                // Fallback to individual calls
                bankNiftyPrice = getBankNiftyPrice();
                bankNiftyPreviousClose = getPreviousDayBankNiftyClose();
                logger.warn("Using fallback Bank Nifty data - Current: {}, Previous Close: {}", bankNiftyPrice, bankNiftyPreviousClose);
            }
            
            // Use fallback prices if real-time data is not available
            if (niftyPrice == null) {
                niftyPrice = FALLBACK_NIFTY_PRICE;
                niftyPreviousClose = FALLBACK_NIFTY_PRICE;
                logger.warn("Using fallback Nifty price: " + niftyPrice);
            }
            
            if (bankNiftyPrice == null) {
                bankNiftyPrice = FALLBACK_BANKNIFTY_PRICE;
                bankNiftyPreviousClose = FALLBACK_BANKNIFTY_PRICE;
                logger.warn("Using fallback Bank Nifty price: " + bankNiftyPrice);
            }
            
            // Check market status
            boolean marketOpen = isMarketOpen();
            
            // Create response
            MarketIndicesDto response = new MarketIndicesDto();
            response.setNifty(niftyPrice);
            response.setBanknifty(bankNiftyPrice);
            response.setMarketOpen(marketOpen);
            response.setLastUpdated(LocalDateTime.now(ZoneId.of("Asia/Kolkata"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            // Calculate change values based on real previous day's close from Yahoo Finance
            // (niftyPreviousClose and bankNiftyPreviousClose are already set above)
            
            // Calculate Nifty change
            double niftyChange = niftyPrice - niftyPreviousClose;
            double niftyChangePercent = (niftyChange / niftyPreviousClose) * 100;
            
            // Calculate Bank Nifty change
            double bankNiftyChange = bankNiftyPrice - bankNiftyPreviousClose;
            double bankNiftyChangePercent = (bankNiftyChange / bankNiftyPreviousClose) * 100;
            
            response.setNiftyChange(Math.round(niftyChange * 100.0) / 100.0);
            response.setNiftyChangePercent(Math.round(niftyChangePercent * 100.0) / 100.0);
            response.setBankniftyChange(Math.round(bankNiftyChange * 100.0) / 100.0);
            response.setBankniftyChangePercent(Math.round(bankNiftyChangePercent * 100.0) / 100.0);
            
            logger.info("Market indices data fetched successfully - Nifty: {}, Bank Nifty: {}, Market Open: {}", 
                    niftyPrice, bankNiftyPrice, marketOpen);
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error fetching market indices data", e);
            
            // Return fallback data on error
            MarketIndicesDto fallbackResponse = new MarketIndicesDto();
            fallbackResponse.setNifty(FALLBACK_NIFTY_PRICE);
            fallbackResponse.setBanknifty(FALLBACK_BANKNIFTY_PRICE);
            fallbackResponse.setMarketOpen(isMarketOpen());
            fallbackResponse.setLastUpdated(LocalDateTime.now(ZoneId.of("Asia/Kolkata"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            fallbackResponse.setNiftyChange(0.0);
            fallbackResponse.setNiftyChangePercent(0.0);
            fallbackResponse.setBankniftyChange(0.0);
            fallbackResponse.setBankniftyChangePercent(0.0);
            
            return fallbackResponse;
        }
    }
    
    @Override
    public boolean isMarketOpen() {
        try {
            LocalTime currentTime = LocalTime.now(ZoneId.of("Asia/Kolkata"));
            LocalDateTime currentDateTime = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
            
            // Check if it's a weekday (Monday to Friday)
            int dayOfWeek = currentDateTime.getDayOfWeek().getValue();
            if (dayOfWeek < 1 || dayOfWeek > 5) {
                return false; // Weekend
            }
            
            // Check if current time is within market hours
            return !currentTime.isBefore(MARKET_OPEN_TIME) && !currentTime.isAfter(MARKET_CLOSE_TIME);
            
        } catch (Exception e) {
            logger.error("Error checking market status", e);
            return false; // Default to closed on error
        }
    }
    
    @Override
    public Double getNiftyPrice() {
        try {
            logger.info("Fetching Nifty 50 price");
            return getNiftyPriceWithFallback();
        } catch (Exception e) {
            logger.error("Error fetching Nifty price", e);
            return getPreviousDayNiftyClose();
        }
    }
    
    @Override
    public Double getBankNiftyPrice() {
        try {
            logger.info("Fetching Bank Nifty price");
            return getBankNiftyPriceWithFallback();
        } catch (Exception e) {
            logger.error("Error fetching Bank Nifty price", e);
            return getPreviousDayBankNiftyClose();
        }
    }
    
    /**
     * Get previous day's closing price for Nifty
     * This should ideally fetch from a database or external API
     */
    private Double getPreviousDayNiftyClose() {
        try {
            // TODO: Implement actual previous day close fetching from database or API
            // For now, return a realistic previous close based on current market levels
            logger.info("Fetching previous day Nifty close");
            
            // This should be replaced with actual database query or API call
            // Example: return historicalDataService.getPreviousClose("NIFTY", LocalDate.now().minusDays(1));
            
            // More realistic previous close for Nifty (as of September 2024)
            return 24500.0;
            
        } catch (Exception e) {
            logger.error("Error fetching previous day Nifty close", e);
            return FALLBACK_NIFTY_PRICE;
        }
    }
    
    /**
     * Get previous day's closing price for Bank Nifty
     * This should ideally fetch from a database or external API
     */
    private Double getPreviousDayBankNiftyClose() {
        try {
            // TODO: Implement actual previous day close fetching from database or API
            // For now, return a realistic previous close based on current market levels
            logger.info("Fetching previous day Bank Nifty close");
            
            // This should be replaced with actual database query or API call
            // Example: return historicalDataService.getPreviousClose("BANKNIFTY", LocalDate.now().minusDays(1));
            
            // More realistic previous close for Bank Nifty (as of September 2024)
            return 54100.0;
            
        } catch (Exception e) {
            logger.error("Error fetching previous day Bank Nifty close", e);
            return FALLBACK_BANKNIFTY_PRICE;
        }
    }
    
    /**
     * Get Nifty price - either live or previous day's close
     */
    private Double getNiftyPriceWithFallback() {
        // Try Yahoo Finance first (most reliable)
        Double yahooPrice = getYahooFinancePrice("^NSEI");
        if (yahooPrice != null) {
            logger.info("Using REAL Yahoo Finance Nifty price: " + yahooPrice);
            return yahooPrice;
        }
        
        // Try Angel SmartAPI as fallback
        Double angelPrice = angelMarketDataService.getLivePrice("NIFTY", ExchSeg.NSE);
        if (angelPrice != null) {
            logger.info("Using REAL Angel SmartAPI Nifty price: " + angelPrice);
            return angelPrice;
        }
        
        // If live price not available, use previous day's close
        logger.warn("All live price sources failed, using previous day's close");
        return getPreviousDayNiftyClose();
    }
    
    /**
     * Get Bank Nifty price - either live or previous day's close
     */
    private Double getBankNiftyPriceWithFallback() {
        // Try Yahoo Finance first (most reliable)
        Double yahooPrice = getYahooFinancePrice("^NSEBANK");
        if (yahooPrice != null) {
            logger.info("Using REAL Yahoo Finance Bank Nifty price: " + yahooPrice);
            return yahooPrice;
        }
        
        // Try Angel SmartAPI as fallback
        Double angelPrice = angelMarketDataService.getLivePrice("BANKNIFTY", ExchSeg.NSE);
        if (angelPrice != null) {
            logger.info("Using REAL Angel SmartAPI Bank Nifty price: " + angelPrice);
            return angelPrice;
        }
        
        // If live price not available, use previous day's close
        logger.warn("All live price sources failed, using previous day's close");
        return getPreviousDayBankNiftyClose();
    }
    
    /**
     * Get real-time price from Yahoo Finance API
     */
    private Double getYahooFinancePrice(String yahooSymbol) {
        try {
            logger.info("Fetching real price from Yahoo Finance for " + yahooSymbol);
            
            java.net.URL url = new java.net.URL("https://query1.finance.yahoo.com/v8/finance/chart/" + yahooSymbol);
            java.net.HttpURLConnection con = (java.net.HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            con.setConnectTimeout(5000);
            con.setReadTimeout(10000);
            
            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(con.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                // Parse JSON response
                String jsonResponse = response.toString();
                int priceStart = jsonResponse.indexOf("\"regularMarketPrice\":") + 21;
                if (priceStart > 20) {
                    int priceEnd = jsonResponse.indexOf(",", priceStart);
                    if (priceEnd == -1) priceEnd = jsonResponse.indexOf("}", priceStart);
                    
                    if (priceEnd > priceStart) {
                        String priceStr = jsonResponse.substring(priceStart, priceEnd);
                        double price = Double.parseDouble(priceStr);
                        logger.info("Got REAL price from Yahoo Finance: " + price);
                        return price;
                    }
                }
                
                // Try previous close if regular market price not available
                int prevCloseStart = jsonResponse.indexOf("\"previousClose\":") + 16;
                if (prevCloseStart > 15) {
                    int prevCloseEnd = jsonResponse.indexOf(",", prevCloseStart);
                    if (prevCloseEnd == -1) prevCloseEnd = jsonResponse.indexOf("}", prevCloseStart);
                    
                    if (prevCloseEnd > prevCloseStart) {
                        String priceStr = jsonResponse.substring(prevCloseStart, prevCloseEnd);
                        double price = Double.parseDouble(priceStr);
                        logger.info("Got previous close from Yahoo Finance: " + price);
                        return price;
                    }
                }
            }
            
            logger.warn("Yahoo Finance API failed for " + yahooSymbol + " - HTTP " + responseCode);
            return null;
            
        } catch (Exception e) {
            logger.error("Error fetching price from Yahoo Finance for " + yahooSymbol, e);
            return null;
        }
    }
    
    /**
     * Get both current price and previous close from Yahoo Finance
     */
    private PriceAndPreviousClose getYahooFinancePriceAndPreviousClose(String yahooSymbol) {
        try {
            logger.info("Fetching real price and previous close from Yahoo Finance for " + yahooSymbol);
            
            java.net.URL url = new java.net.URL("https://query1.finance.yahoo.com/v8/finance/chart/" + yahooSymbol);
            java.net.HttpURLConnection con = (java.net.HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            con.setConnectTimeout(5000);
            con.setReadTimeout(10000);
            
            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(con.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                // Parse JSON response
                String jsonResponse = response.toString();
                
                // Get current price
                Double currentPrice = null;
                int priceStart = jsonResponse.indexOf("\"regularMarketPrice\":") + 21;
                if (priceStart > 20) {
                    int priceEnd = jsonResponse.indexOf(",", priceStart);
                    if (priceEnd == -1) priceEnd = jsonResponse.indexOf("}", priceStart);
                    
                    if (priceEnd > priceStart) {
                        String priceStr = jsonResponse.substring(priceStart, priceEnd);
                        currentPrice = Double.parseDouble(priceStr);
                        logger.info("Got REAL current price from Yahoo Finance: " + currentPrice);
                    }
                }
                
                // Get previous close
                Double previousClose = null;
                int prevCloseStart = jsonResponse.indexOf("\"previousClose\":") + 16;
                if (prevCloseStart > 15) {
                    int prevCloseEnd = jsonResponse.indexOf(",", prevCloseStart);
                    if (prevCloseEnd == -1) prevCloseEnd = jsonResponse.indexOf("}", prevCloseStart);
                    
                    if (prevCloseEnd > prevCloseStart) {
                        String priceStr = jsonResponse.substring(prevCloseStart, prevCloseEnd);
                        previousClose = Double.parseDouble(priceStr);
                        logger.info("Got REAL previous close from Yahoo Finance: " + previousClose);
                    }
                }
                
                if (currentPrice != null && previousClose != null) {
                    return new PriceAndPreviousClose(currentPrice, previousClose);
                } else if (currentPrice != null) {
                    // If we have current price but no previous close, use current price as both
                    return new PriceAndPreviousClose(currentPrice, currentPrice);
                }
            }
            
            logger.warn("Yahoo Finance API failed for " + yahooSymbol + " - HTTP " + responseCode);
            return null;
            
        } catch (Exception e) {
            logger.error("Error fetching price and previous close from Yahoo Finance for " + yahooSymbol, e);
            return null;
        }
    }
    
    /**
     * Helper class to hold both current price and previous close
     */
    private static class PriceAndPreviousClose {
        private final Double currentPrice;
        private final Double previousClose;
        
        public PriceAndPreviousClose(Double currentPrice, Double previousClose) {
            this.currentPrice = currentPrice;
            this.previousClose = previousClose;
        }
        
        public Double getCurrentPrice() { return currentPrice; }
        public Double getPreviousClose() { return previousClose; }
    }
}
