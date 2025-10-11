package tech.algofinserve.recommendation.model.domain;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import tech.algofinserve.recommendation.constants.BuySell;

public class StockAlert {

  Format formatter = new SimpleDateFormat("EEE, d MMM HH:mm:ss");

  String stockCode;
  String price;
  Date alertDate;
  String scanName;
  Integer daysSince;

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
    this.daysSince = calculateDaysSince(alertDate);
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

  public Integer getDaysSince() {
    return daysSince;
  }

  public void setDaysSince(Integer daysSince) {
    this.daysSince = daysSince;
  }

  /**
   * Calculate days since the alert date
   */
  private Integer calculateDaysSince(Date alertDate) {
    if (alertDate == null) {
      return 0;
    }
    long diffInMillies = Math.abs(new Date().getTime() - alertDate.getTime());
    long diffInDays = diffInMillies / (1000 * 60 * 60 * 24);
    return (int) diffInDays;
  }

  @Override
  public String toString() {
    String dateFormat = formatter.format(alertDate);
    String daysSinceText = daysSince != null ? 
        (daysSince == 0 ? " (today)" : " (since " + daysSince + " days)") : "";
    return ""
        + buySell
        + " :: "
        + stockCode
        + " @ "
        + price
        + " ON "
        + dateFormat
        + daysSinceText
        + " :: FOR :: "
        + scanName
        + "";
  }
}
