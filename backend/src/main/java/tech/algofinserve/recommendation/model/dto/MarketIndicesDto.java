package tech.algofinserve.recommendation.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MarketIndicesDto {
    
    @JsonProperty("nifty")
    private Double nifty;
    
    @JsonProperty("banknifty")
    private Double banknifty;
    
    @JsonProperty("marketOpen")
    private Boolean marketOpen;
    
    @JsonProperty("lastUpdated")
    private String lastUpdated;
    
    @JsonProperty("niftyChange")
    private Double niftyChange;
    
    @JsonProperty("niftyChangePercent")
    private Double niftyChangePercent;
    
    @JsonProperty("bankniftyChange")
    private Double bankniftyChange;
    
    @JsonProperty("bankniftyChangePercent")
    private Double bankniftyChangePercent;
    
    // Constructors
    public MarketIndicesDto() {}
    
    public MarketIndicesDto(Double nifty, Double banknifty, Boolean marketOpen, String lastUpdated) {
        this.nifty = nifty;
        this.banknifty = banknifty;
        this.marketOpen = marketOpen;
        this.lastUpdated = lastUpdated;
    }
    
    // Getters and Setters
    public Double getNifty() {
        return nifty;
    }
    
    public void setNifty(Double nifty) {
        this.nifty = nifty;
    }
    
    public Double getBanknifty() {
        return banknifty;
    }
    
    public void setBanknifty(Double banknifty) {
        this.banknifty = banknifty;
    }
    
    public Boolean getMarketOpen() {
        return marketOpen;
    }
    
    public void setMarketOpen(Boolean marketOpen) {
        this.marketOpen = marketOpen;
    }
    
    public String getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public Double getNiftyChange() {
        return niftyChange;
    }
    
    public void setNiftyChange(Double niftyChange) {
        this.niftyChange = niftyChange;
    }
    
    public Double getNiftyChangePercent() {
        return niftyChangePercent;
    }
    
    public void setNiftyChangePercent(Double niftyChangePercent) {
        this.niftyChangePercent = niftyChangePercent;
    }
    
    public Double getBankniftyChange() {
        return bankniftyChange;
    }
    
    public void setBankniftyChange(Double bankniftyChange) {
        this.bankniftyChange = bankniftyChange;
    }
    
    public Double getBankniftyChangePercent() {
        return bankniftyChangePercent;
    }
    
    public void setBankniftyChangePercent(Double bankniftyChangePercent) {
        this.bankniftyChangePercent = bankniftyChangePercent;
    }
}
