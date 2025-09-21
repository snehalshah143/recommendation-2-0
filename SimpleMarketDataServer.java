import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import com.sun.net.httpserver.*;

public class SimpleMarketDataServer {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8082), 0);
        
        // Create endpoints
        server.createContext("/api/indices", new IndicesHandler());
        server.createContext("/api/test/prices", new TestPricesHandler());
        
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8082");
    }
    
    static class IndicesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                try {
                    // Get real prices from Yahoo Finance
                    double niftyPrice = getYahooPrice("^NSEI");
                    double bankNiftyPrice = getYahooPrice("^NSEBANK");
                    
                    // Calculate changes (simplified)
                    double niftyChange = niftyPrice - 25423.6; // Previous close
                    double bankNiftyChange = bankNiftyPrice - 55727.45; // Previous close
                    
                    String response = String.format(
                        "{\"nifty\":%.2f,\"banknifty\":%.2f,\"marketOpen\":true,\"lastUpdated\":\"%s\"," +
                        "\"niftyChange\":%.2f,\"niftyChangePercent\":%.2f," +
                        "\"bankniftyChange\":%.2f,\"bankniftyChangePercent\":%.2f}",
                        niftyPrice, bankNiftyPrice, java.time.LocalDateTime.now(),
                        niftyChange, (niftyChange/25423.6)*100,
                        bankNiftyChange, (bankNiftyChange/55727.45)*100
                    );
                    
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                    exchange.sendResponseHeaders(200, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } catch (Exception e) {
                    String errorResponse = "{\"error\":\"" + e.getMessage() + "\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(500, errorResponse.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(errorResponse.getBytes());
                    os.close();
                }
            }
        }
    }
    
    static class TestPricesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                try {
                    double niftyPrice = getYahooPrice("^NSEI");
                    double bankNiftyPrice = getYahooPrice("^NSEBANK");
                    
                    String response = String.format(
                        "{\"nifty\":%.2f,\"banknifty\":%.2f,\"nifty_source\":\"Yahoo Finance\"," +
                        "\"banknifty_source\":\"Yahoo Finance\",\"timestamp\":%d,\"status\":\"success\"}",
                        niftyPrice, bankNiftyPrice, System.currentTimeMillis()
                    );
                    
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                    exchange.sendResponseHeaders(200, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } catch (Exception e) {
                    String errorResponse = "{\"error\":\"" + e.getMessage() + "\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(500, errorResponse.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(errorResponse.getBytes());
                    os.close();
                }
            }
        }
    }
    
    private static double getYahooPrice(String symbol) throws Exception {
        String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + symbol;
        
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        
        int responseCode = con.getResponseCode();
        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            // Parse JSON response (simplified)
            String jsonResponse = response.toString();
            int priceStart = jsonResponse.indexOf("\"regularMarketPrice\":") + 21;
            int priceEnd = jsonResponse.indexOf(",", priceStart);
            if (priceEnd == -1) priceEnd = jsonResponse.indexOf("}", priceStart);
            
            String priceStr = jsonResponse.substring(priceStart, priceEnd);
            return Double.parseDouble(priceStr);
        }
        
        throw new Exception("Failed to get price for " + symbol);
    }
}
