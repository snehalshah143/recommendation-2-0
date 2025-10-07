package tech.algofinserve.recommendation.core;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tech.algofinserve.recommendation.alerts.dto.AlertDto;
import tech.algofinserve.recommendation.alerts.service.AlertService;
import tech.algofinserve.recommendation.cache.ChartInkAlertFactory;
import tech.algofinserve.recommendation.constants.BuySell;
import tech.algofinserve.recommendation.helper.StockAlertOutputHelper;
import tech.algofinserve.recommendation.messaging.MessagingService;
import tech.algofinserve.recommendation.messaging.TelegramMessaging;
import tech.algofinserve.recommendation.messaging.TelegramSenderPool;
import tech.algofinserve.recommendation.model.domain.Alert;
import tech.algofinserve.recommendation.model.domain.StockAlert;
import tech.algofinserve.recommendation.model.domain.StockAlertOutput;
import tech.algofinserve.recommendation.report.ReportGenerator;

@Service
public class ChartInkAlertProcessingService {

  Format formatter_DDMMYYYY = new SimpleDateFormat("yyyy-MM-dd");
  @Autowired private AlertService alertService;

  @Autowired
  @Qualifier("dbQueue")
  BlockingQueue<AlertDto> dbQueue;

  @Autowired
  @Qualifier("messageQueueBuy")
  BlockingQueue<String> messageQueueBuy;

  @Autowired
  @Qualifier("messageQueueSell")
  BlockingQueue<String> messageQueueSell;

  @Autowired
  @Qualifier("messageQueueBuyEOD")
  BlockingQueue<String> messageQueueBuyEOD;

  @Autowired
  @Qualifier("messageQueueSellEOD")
  BlockingQueue<String> messageQueueSellEOD;

  @Autowired
  TelegramSenderPool telegramSenderPool;
//  TelegramMessaging telegramMessagingNormal;
//  TelegramMessaging telegramMessagingEOD;

  Function<String, Boolean> sendMessageNormal =
      p -> {
     //   return telegramMessagingNormal.sendMessage2(p);
        return telegramSenderPool.sendAndWait("@shreejitrades",p,5000);
      };

  Function<String, Boolean> sendMessageEOD =
      p -> {
   //     return telegramMessagingEOD.sendMessageEOD(p);
        return telegramSenderPool.sendAndWait("@ideastoinvest",p,5000);
      };

  @EventListener(ApplicationReadyEvent.class)
  public void startMessagingService() throws Exception {
//    telegramMessagingNormal=new TelegramMessaging();
 //   telegramMessagingEOD=new TelegramMessaging();
    new Thread(new MessagingService(messageQueueBuy, sendMessageNormal)).start();
    new Thread(new MessagingService(messageQueueSell, sendMessageNormal)).start();
    new Thread(new MessagingService(messageQueueBuyEOD, sendMessageEOD)).start();
    new Thread(new MessagingService(messageQueueSellEOD, sendMessageEOD)).start();
    //    new Thread(new MessagingService(messageQueueBuy)).start();
    //    new Thread(new MessagingService(messageQueueSell)).start();
    System.out.println("Messaging Service Started.....");
  }

  @Async("taskExecutorBuy")
  public void processBuyAlert(Alert alert) throws InterruptedException {

    String[] stocksName = alert.getStocks().split(",");
    String[] prices = alert.getTriggerPrices().split(",");

    for (int i = 0; i < stocksName.length; i++) {

      StockAlert stockAlert = convertAlertToStockAlert(alert, stocksName, prices, i, BuySell.BUY);


      if (ChartInkAlertFactory.buyStockAlertListForStockNameMap.containsKey(
          stockAlert.getStockCode())) {
        ChartInkAlertFactory.buyStockAlertListForStockNameMap
            .get(stockAlert.getStockCode())
            .add(stockAlert);
        String recommendation = "R::" + stockAlert.toString();
        messageQueueBuy.put(recommendation);
        //   TelegramMessaging.sendMessage2("R::"+stockAlert.toString());
      } else {
        //  TelegramMessaging.sendMessage2(stockAlert.toString());
        messageQueueBuy.put(stockAlert.toString());
        List<StockAlert> stockAlertList = new CopyOnWriteArrayList<>();
        stockAlertList.add(stockAlert);
        ChartInkAlertFactory.buyStockAlertListForStockNameMap.put(
            stockAlert.getStockCode(), stockAlertList);
      }
      AlertDto alertDto = convertAlertToAlertDto(alert, stocksName, prices, i, BuySell.BUY);
    //  alertService.processIncomingAlert(alertDto);
      dbQueue.put(alertDto);

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

  @Async("taskExecutorBuyEOD")
  public void processBuyAlertEOD(Alert alert) throws InterruptedException {

    String[] stocksName = alert.getStocks().split(",");
    String[] prices = alert.getTriggerPrices().split(",");

    for (int i = 0; i < stocksName.length; i++) {

      StockAlert stockAlert = convertAlertToStockAlert(alert, stocksName, prices, i, BuySell.BUY);

      if (ChartInkAlertFactory.buyStockAlertListForStockNameMap.containsKey(
          stockAlert.getStockCode())) {
        ChartInkAlertFactory.buyStockAlertListForStockNameMap
            .get(stockAlert.getStockCode())
            .add(stockAlert);
        String recommendation = "R::" + stockAlert.toString();
        messageQueueBuyEOD.put(recommendation);
        //   TelegramMessaging.sendMessage2("R::"+stockAlert.toString());
      } else {
        //  TelegramMessaging.sendMessage2(stockAlert.toString());
        messageQueueBuyEOD.put(stockAlert.toString());
        List<StockAlert> stockAlertList = new CopyOnWriteArrayList<>();
        stockAlertList.add(stockAlert);
        ChartInkAlertFactory.buyStockAlertListForStockNameMap.put(
            stockAlert.getStockCode(), stockAlertList);
      }

      AlertDto alertDto = convertAlertToAlertDto(alert, stocksName, prices, i, BuySell.BUY);
    //  alertService.processIncomingAlert(alertDto);
      dbQueue.put(alertDto);
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

  @Async("taskExecutorSell")
  public void processSellAlert(Alert alert) throws InterruptedException {

    String[] stocksName = alert.getStocks().split(",");
    String[] prices = alert.getTriggerPrices().split(",");

    for (int i = 0; i < stocksName.length; i++) {

      StockAlert stockAlert = convertAlertToStockAlert(alert, stocksName, prices, i, BuySell.SELL);

      if (ChartInkAlertFactory.sellStockAlertListForStockNameMap.containsKey(
          stockAlert.getStockCode())) {
        ChartInkAlertFactory.sellStockAlertListForStockNameMap
            .get(stockAlert.getStockCode())
            .add(stockAlert);
        String recommendation = "R::" + stockAlert.toString();
        messageQueueSell.put(recommendation);
        //   TelegramMessaging.sendMessage2("R::"+stockAlert.toString());
      } else {
        //  TelegramMessaging.sendMessage2(stockAlert.toString());
        messageQueueSell.put(stockAlert.toString());
        List<StockAlert> stockAlertList = new CopyOnWriteArrayList<>();
        stockAlertList.add(stockAlert);
        ChartInkAlertFactory.sellStockAlertListForStockNameMap.put(
            stockAlert.getStockCode(), stockAlertList);
      }

      AlertDto alertDto = convertAlertToAlertDto(alert, stocksName, prices, i, BuySell.SELL);
     // alertService.processIncomingAlert(alertDto);
      dbQueue.put(alertDto);
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

  @Async("taskExecutorSellEOD")
  public void processSellAlertEOD(Alert alert) throws InterruptedException {

    String[] stocksName = alert.getStocks().split(",");
    String[] prices = alert.getTriggerPrices().split(",");

    for (int i = 0; i < stocksName.length; i++) {

      StockAlert stockAlert = convertAlertToStockAlert(alert, stocksName, prices, i, BuySell.SELL);

      if (ChartInkAlertFactory.sellStockAlertListForStockNameMap.containsKey(
          stockAlert.getStockCode())) {
        ChartInkAlertFactory.sellStockAlertListForStockNameMap
            .get(stockAlert.getStockCode())
            .add(stockAlert);
        String recommendation = "R::" + stockAlert.toString();
        messageQueueSellEOD.put(recommendation);
        //   TelegramMessaging.sendMessage2("R::"+stockAlert.toString());
      } else {
        //  TelegramMessaging.sendMessage2(stockAlert.toString());
        messageQueueSellEOD.put(stockAlert.toString());
        List<StockAlert> stockAlertList = new CopyOnWriteArrayList<>();
        stockAlertList.add(stockAlert);
        ChartInkAlertFactory.sellStockAlertListForStockNameMap.put(
            stockAlert.getStockCode(), stockAlertList);
      }

      AlertDto alertDto = convertAlertToAlertDto(alert, stocksName, prices, i, BuySell.SELL);
   //   alertService.processIncomingAlert(alertDto);
      dbQueue.put(alertDto);
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
      Alert alert, String[] stocksName, String[] prices, int i, BuySell buySell) {
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
    stockAlert.setBuySell(buySell);
    stockAlert.setAlertDate(triggeredDate);
    stockAlert.setPrice(prices[i]);
    stockAlert.setStockCode(stocksName[i]);
    stockAlert.setScanName(scanName);
    return stockAlert;
  }

  private AlertDto convertAlertToAlertDto(
          Alert alert, String[] stocksName, String[] prices, int i, BuySell buySell) {
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

    AlertDto alertDto = new AlertDto();
    alertDto.setBuySell(buySell);
    alertDto.setAlertDate(triggeredDate.toInstant());
    alertDto.setPrice(prices[i]);
    alertDto.setStockCode(stocksName[i]);
    alertDto.setScanName(scanName);
    return alertDto;
  }

  public void clearPreviousDayData() {
    ChartInkAlertFactory.buyStockAlertListForStockNameMap.clear();
    ChartInkAlertFactory.sellStockAlertListForStockNameMap.clear();
  }

  public boolean generateStockAlertOutputReport() {
    String stockAlertReportFileName = "D:\\Report\\Chartink\\chartink_report_DDMMYYYY.csv";
    try {
      Date date = new Date();

      String fileDate = formatter_DDMMYYYY.format(date);
      stockAlertReportFileName = stockAlertReportFileName.replace("DDMMYYYY", fileDate);
      System.out.println("stockAlertReportFileName ::" + stockAlertReportFileName);
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
