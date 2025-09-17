package tech.algofinserve.recommendation.model.domain;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import tech.algofinserve.recommendation.constants.BuySell;

@JsonPropertyOrder({"symbol", "buySell", "alertDate", "price", "scanName"})
public class StockAlertOutput { // implements Comparable<StockAlertOutput>{

  Format formatter = new SimpleDateFormat("EEE, d MMM HH:mm:ss");

  @CsvBindByName(column = "#####", required = true)
  @CsvBindByPosition(position = 0)
  private String dummy;

  @CsvBindByName(column = "symbol", required = true)
  @CsvBindByPosition(position = 1)
  String stockCode;

  @CsvBindByName(column = "buySell", required = true)
  @CsvBindByPosition(position = 2)
  BuySell buySell;

  @CsvBindByName(column = "alertDate", required = true)
  @CsvBindByPosition(position = 3)
  Date alertDate;

  @CsvBindByName(column = "price", required = true)
  @CsvBindByPosition(position = 4)
  String price;

  @CsvBindByName(column = "scanName", required = true)
  @CsvBindByPosition(position = 5)
  String scanName;

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
    String dateFormat = formatter.format(alertDate);
    return ""
        + buySell
        + " :: "
        + stockCode
        + " @ "
        + price
        + " ON "
        + dateFormat
        + " :: FOR :: "
        + scanName
        + "";
  }

  /*  @Override
  public int compareTo(StockAlertOutput o) {
    return this.alertDate < o.alertDate ? 1 : -1;
  }*/

}
