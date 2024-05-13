package tech.algofinserve.recommendation.model.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;


public class Alert implements Serializable {

//    private @Id @GeneratedValue(strategy = GenerationType.TABLE) Long alertId;
    private Long alertId;

    @JsonProperty("stocks")
    private String stocks;

    @JsonProperty("trigger_prices")
    private String triggerPrices;

    @JsonProperty("triggered_at")
    private String triggerdAt;

    @JsonProperty("scan_name")
    private String scanName;

    @JsonProperty("webhook_url")
    private String scanUrl;

    @JsonProperty("alert_name")
    private String alertName;

    public Alert() {}

    public Long getAlertId() {
        return alertId;
    }

    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }

    public String getStocks() {
        return stocks;
    }

    public void setStocks(String stocks) {
        this.stocks = stocks;
    }

    public String getTriggerPrices() {
        return triggerPrices;
    }

    public void setTriggerPrices(String triggerPrices) {
        this.triggerPrices = triggerPrices;
    }

    public Alert(
            String stocks,
            String triggerPrices,
            String triggerdAt,
            String scanName,
            String scanUrl,
            String alertName) {
        super();
        this.stocks = stocks;
        this.triggerPrices = triggerPrices;
        this.triggerdAt = triggerdAt;
        this.scanName = scanName;
        this.scanUrl = scanUrl;
        this.alertName = alertName;
    }

    public String getTriggerdAt() {
        return triggerdAt;
    }

    public void setTriggerdAt(String triggerdAt) {
        this.triggerdAt = triggerdAt;
    }

    public String getScanName() {
        return scanName;
    }

    public void setScanName(String scanName) {
        this.scanName = scanName;
    }

    public String getScanUrl() {
        return scanUrl;
    }

    public void setScanUrl(String scanUrl) {
        this.scanUrl = scanUrl;
    }

    public String getAlertName() {
        return alertName;
    }

    public void setAlertName(String alertName) {
        this.alertName = alertName;
    }

    @Override
    public String toString() {
        return "Alert [stocks="
                + stocks
                + ", triggerPrices="
                + triggerPrices
                + ", triggerdAt="
                + triggerdAt
                + ", scanName="
                + scanName
                + ", scanUrl="
                + scanUrl
                + ", alertName="
                + alertName
                + "]";
    }
}