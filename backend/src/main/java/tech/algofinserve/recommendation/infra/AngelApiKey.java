package tech.algofinserve.recommendation.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AngelApiKey {
    
    @Value("${angel.api.key:your_api_key}")
    private String apiKey;
    
    @Value("${angel.client.id:your_client_id}")
    private String clientId;
    
    @Value("${angel.password:your_password}")
    private String password;
    
    @Value("${angel.totp:your_totp}")
    private String totp;
    
    // Additional keys following marketdata pattern
    @Value("${angel.market.api.key:your_market_api_key}")
    private String marketApiKey;
    
    @Value("${angel.market.secret.key:your_market_secret_key}")
    private String marketSecretKey;
    
    @Value("${angel.historical.api.key:your_historical_api_key}")
    private String historicalApiKey;
    
    @Value("${angel.historical.secret.key:your_historical_secret_key}")
    private String historicalSecretKey;
    
    public AngelApiKey() {
        // Default values will be set via @Value annotations
    }
    
    public AngelApiKey(String apiKey, String clientId, String password, String totp) {
        this.apiKey = apiKey;
        this.clientId = clientId;
        this.password = password;
        this.totp = totp;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getTotp() {
        return totp;
    }
    
    public void setTotp(String totp) {
        this.totp = totp;
    }
    
    public String getMarketApiKey() {
        return marketApiKey;
    }
    
    public void setMarketApiKey(String marketApiKey) {
        this.marketApiKey = marketApiKey;
    }
    
    public String getMarketSecretKey() {
        return marketSecretKey;
    }
    
    public void setMarketSecretKey(String marketSecretKey) {
        this.marketSecretKey = marketSecretKey;
    }
    
    public String getHistoricalApiKey() {
        return historicalApiKey;
    }
    
    public void setHistoricalApiKey(String historicalApiKey) {
        this.historicalApiKey = historicalApiKey;
    }
    
    public String getHistoricalSecretKey() {
        return historicalSecretKey;
    }
    
    public void setHistoricalSecretKey(String historicalSecretKey) {
        this.historicalSecretKey = historicalSecretKey;
    }
}
