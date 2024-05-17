package tech.algofinserve.recommendation.model.domain;


import tech.algofinserve.recommendation.constants.BuySell;

import java.util.Date;

public class StockAlert {


    String stockCode;
    String price;
    Date alertDate;
    String scanName;

    BuySell buySell;

    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public Date getAlertDate() {
        return alertDate;
    }

    public void setAlertDate(Date alertDate) {
        this.alertDate = alertDate;
    }

    public String getScanName() {
        return scanName;
    }

    public void setScanName(String scanName) {
        this.scanName = scanName;
    }

    public BuySell getBuySell() {
        return buySell;
    }

    public void setBuySell(BuySell buySell) {
        this.buySell = buySell;
    }

    @Override
    public String toString() {

        return "StockAlert[" +buySell+ " :: "+stockCode+ "@"+price +" ON "+alertDate+
                " :: FOR :: " +scanName+" ]";

    }
}
