package tech.algofinserve.recommendation.core;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tech.algofinserve.recommendation.cache.ChartInkAlertFactory;
import tech.algofinserve.recommendation.constants.BuySell;
import tech.algofinserve.recommendation.helper.StockAlertOutputHelper;
import tech.algofinserve.recommendation.messaging.MessagingService;
import tech.algofinserve.recommendation.model.domain.Alert;
import tech.algofinserve.recommendation.model.domain.StockAlert;
import tech.algofinserve.recommendation.model.domain.StockAlertOutput;
import tech.algofinserve.recommendation.report.ReportGenerator;

@Service
public class ChartInkAlertProcessingService {
  public static String stockAlertReportFileName =
      "D:\\Report\\Chartink\\chartink_report_DDMMYYYY.csv";
  Format formatter_DDMMYYYY = new SimpleDateFormat("yyyy-MM-dd");
  @Autowired BlockingQueue<String> messageQueue;

  @EventListener(ApplicationReadyEvent.class)
  public void startMessagingService() {
    new Thread(new MessagingService(messageQueue)).start();
    System.out.println("Messaging Service Started.....");
  }

  @Async("taskExecutor")
  public void processBuyAlert(Alert alert) throws InterruptedException {

    String[] stocksName = alert.getStocks().split(",");
    String[] prices = alert.getTriggerPrices().split(",");

    for (int i = 0; i < stocksName.length; i++) {

      StockAlert stockAlert = convertAlertToStockAlert(alert, stocksName, prices, i);

      if (ChartInkAlertFactory.buyStockAlertListForStockNameMap.containsKey(
          stockAlert.getStockCode())) {
        ChartInkAlertFactory.buyStockAlertListForStockNameMap
            .get(stockAlert.getStockCode())
            .add(stockAlert);
        String recommendation = "R::" + stockAlert.toString();
        messageQueue.put(recommendation);
        //   TelegramMessaging.sendMessage2("R::"+stockAlert.toString());
      } else {
        //  TelegramMessaging.sendMessage2(stockAlert.toString());
        messageQueue.put(stockAlert.toString());
        List<StockAlert> stockAlertList = new CopyOnWriteArrayList<>();
        stockAlertList.add(stockAlert);
        ChartInkAlertFactory.buyStockAlertListForStockNameMap.put(
            stockAlert.getStockCode(), stockAlertList);
      }

      if (ChartInkAlertFactory.buyStockAlertListForScanNameMap.containsKey(
          stockAlert.getScanName())) {
        ChartInkAlertFactory.buyStockAlertListForScanNameMap
            .get(stockAlert.getScanName())
            .add(stockAlert);
      } else {

        List<StockAlert> stockAlertList = new CopyOnWriteArrayList<>();
        stockAlertList.add(stockAlert);
        ChartInkAlertFactory.buyStockAlertListForScanNameMap.put(
            stockAlert.getScanName(), stockAlertList);
      }
    }
  }

  @Async("taskExecutor")
  public void processSellAlert(Alert alert) throws InterruptedException {

    String[] stocksName = alert.getStocks().split(",");
    String[] prices = alert.getTriggerPrices().split(",");

    for (int i = 0; i < stocksName.length; i++) {

      StockAlert stockAlert = convertAlertToStockAlert(alert, stocksName, prices, i);

      if (ChartInkAlertFactory.sellStockAlertListForStockNameMap.containsKey(
          stockAlert.getStockCode())) {
        ChartInkAlertFactory.sellStockAlertListForStockNameMap
            .get(stockAlert.getStockCode())
            .add(stockAlert);
        String recommendation = "R::" + stockAlert.toString();
        messageQueue.put(recommendation);
        //   TelegramMessaging.sendMessage2("R::"+stockAlert.toString());
      } else {
        //  TelegramMessaging.sendMessage2(stockAlert.toString());
        messageQueue.put(stockAlert.toString());
        List<StockAlert> stockAlertList = new CopyOnWriteArrayList<>();
        stockAlertList.add(stockAlert);
        ChartInkAlertFactory.sellStockAlertListForStockNameMap.put(
            stockAlert.getStockCode(), stockAlertList);
      }

      if (ChartInkAlertFactory.sellStockAlertListForScanNameMap.containsKey(
          stockAlert.getScanName())) {
        ChartInkAlertFactory.sellStockAlertListForScanNameMap
            .get(stockAlert.getScanName())
            .add(stockAlert);
      } else {

        List<StockAlert> stockAlertList = new CopyOnWriteArrayList<>();
        stockAlertList.add(stockAlert);
        ChartInkAlertFactory.sellStockAlertListForScanNameMap.put(
            stockAlert.getScanName(), stockAlertList);
      }
    }
  }

  private StockAlert convertAlertToStockAlert(
      Alert alert, String[] stocksName, String[] prices, int i) {
    String scanName = alert.getScanName();
    String[] triggeredAt = alert.getTriggerdAt().split(":");
    String hour = triggeredAt[0];
    String[] minutes = triggeredAt[1].split(" ");

    Date triggeredDate = new Date();
    triggeredDate.setMinutes(Integer.parseInt(minutes[0]));

    if (minutes[1].equals("am")) {
      triggeredDate.setHours(Integer.parseInt(hour));
    } else if (minutes[1].equals("pm")) {

      triggeredDate.setHours(
          hour.equals("12") ? Integer.parseInt("12") : Integer.parseInt(hour) + 12);
    }

    StockAlert stockAlert = new StockAlert();
    if (scanName.contains("SELL")) {
      stockAlert.setBuySell(BuySell.SELL);
    } else {
      stockAlert.setBuySell(BuySell.BUY);
    }

    stockAlert.setAlertDate(triggeredDate);
    stockAlert.setPrice(prices[i]);
    stockAlert.setStockCode(stocksName[i]);
    stockAlert.setScanName(scanName);
    return stockAlert;
  }

  public void clearPreviousDayData() {
    ChartInkAlertFactory.buyStockAlertListForStockNameMap.clear();
    ChartInkAlertFactory.sellStockAlertListForStockNameMap.clear();
  }

  public boolean generateStockAlertOutputReport() {

    try {
      Date date = new Date();

      String fileDate = formatter_DDMMYYYY.format(date);
      stockAlertReportFileName = stockAlertReportFileName.replace("DDMMYYYY", fileDate);
      ReportGenerator reportGenerator = new ReportGenerator();
      List<StockAlertOutput> stockRankOutputList =
          StockAlertOutputHelper.buildStockAlertOutputList();
      reportGenerator.generateStockRankReport(stockAlertReportFileName, stockRankOutputList);
      System.out.println("Report Generated ::" + stockAlertReportFileName);
      clearPreviousDayData();
      System.out.println("Data Cleared For ::" + fileDate);

      System.out.println("Email Sent For Date ::" + fileDate);

      return true;
    } catch (Exception e) {
      System.out.println("Issue while Generating Report.");
      return false;
    }
  }
}
