import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import org.json.JSONObject;

public class TestYahooFinance {
    public static void main(String[] args) {
        try {
            // Test NIFTY
            String niftyUrl = "https://query1.finance.yahoo.com/v8/finance/chart/^NSEI";
            String niftyPrice = getPrice(niftyUrl);
            System.out.println("NIFTY Price: " + niftyPrice);
            
            // Test BANKNIFTY
            String bankNiftyUrl = "https://query1.finance.yahoo.com/v8/finance/chart/^NSEBANK";
            String bankNiftyPrice = getPrice(bankNiftyUrl);
            System.out.println("BANKNIFTY Price: " + bankNiftyPrice);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String getPrice(String url) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .build();
            
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            JSONObject json = new JSONObject(response.body());
            JSONObject chart = json.getJSONObject("chart");
            JSONObject result = chart.getJSONArray("result").getJSONObject(0);
            JSONObject meta = result.getJSONObject("meta");
            
            if (meta.has("regularMarketPrice")) {
                return String.valueOf(meta.getDouble("regularMarketPrice"));
            } else if (meta.has("previousClose")) {
                return String.valueOf(meta.getDouble("previousClose"));
            }
        }
        
        return "N/A";
    }
}
