package tech.algofinserve.recommendation.alerts.dto;

import tech.algofinserve.recommendation.constants.BuySell;

import java.time.Instant;

public class AlertDto {
    private String stockCode;
    private String price;
    private Instant alertDate;
    private String scanName;
    private BuySell buySell;

    public AlertDto() {}

    public AlertDto(String stockCode, String price, Instant alertDate, String scanName, BuySell buySell) {
        this.stockCode = stockCode;
        this.price = price;
        this.alertDate = alertDate;
        this.scanName = scanName;
        this.buySell = buySell;
    }

    public String getStockCode() { return stockCode; }
    public void setStockCode(String stockCode) { this.stockCode = stockCode; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public Instant getAlertDate() { return alertDate; }
    public void setAlertDate(Instant alertDate) { this.alertDate = alertDate; }
    public String getScanName() { return scanName; }
    public void setScanName(String scanName) { this.scanName = scanName; }
    public BuySell getBuySell() { return buySell; }
    public void setBuySell(BuySell buySell) { this.buySell = buySell; }
}
