import java.net.*;
import java.io.*;

public class QuickTest {
    public static void main(String[] args) {
        try {
            System.out.println("Testing Yahoo Finance API for NIFTY and BANKNIFTY...");
            
            // Test NIFTY
            double niftyPrice = getYahooPrice("^NSEI");
            System.out.println("NIFTY Price: " + niftyPrice);
            
            // Test BANKNIFTY
            double bankNiftyPrice = getYahooPrice("^NSEBANK");
            System.out.println("BANKNIFTY Price: " + bankNiftyPrice);
            
            // Create JSON response
            String jsonResponse = String.format(
                "{\"nifty\":%.2f,\"banknifty\":%.2f,\"marketOpen\":true,\"lastUpdated\":\"%s\"," +
                "\"niftyChange\":%.2f,\"niftyChangePercent\":%.2f," +
                "\"bankniftyChange\":%.2f,\"bankniftyChangePercent\":%.2f}",
                niftyPrice, bankNiftyPrice, java.time.LocalDateTime.now(),
                niftyPrice - 25423.6, ((niftyPrice - 25423.6)/25423.6)*100,
                bankNiftyPrice - 55727.45, ((bankNiftyPrice - 55727.45)/55727.45)*100
            );
            
            System.out.println("\nJSON Response:");
            System.out.println(jsonResponse);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static double getYahooPrice(String symbol) throws Exception {
        String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + symbol;
        
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        con.setConnectTimeout(5000);
        con.setReadTimeout(10000);
        
        int responseCode = con.getResponseCode();
        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            
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
                    return Double.parseDouble(priceStr);
                }
            }
            
            // Try previous close if regular market price not available
            int prevCloseStart = jsonResponse.indexOf("\"previousClose\":") + 16;
            if (prevCloseStart > 15) {
                int prevCloseEnd = jsonResponse.indexOf(",", prevCloseStart);
                if (prevCloseEnd == -1) prevCloseEnd = jsonResponse.indexOf("}", prevCloseStart);
                
                if (prevCloseEnd > prevCloseStart) {
                    String priceStr = jsonResponse.substring(prevCloseStart, prevCloseEnd);
                    return Double.parseDouble(priceStr);
                }
            }
        }
        
        throw new Exception("Failed to get price for " + symbol);
    }
}
