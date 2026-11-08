package tech.algofinserve.recommendation.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tech.algofinserve.recommendation.alerts.dto.AlertDto;
import tech.algofinserve.recommendation.alerts.service.AlertService;
import tech.algofinserve.recommendation.cache.RedisAlertCache;
import tech.algofinserve.recommendation.constants.BuySell;
import tech.algofinserve.recommendation.helper.StockAlertOutputHelper;
import tech.algofinserve.recommendation.messaging.MessagingServiceNew;
import tech.algofinserve.recommendation.messaging.TelegramMessagingNew;
import tech.algofinserve.recommendation.model.domain.Alert;
import tech.algofinserve.recommendation.model.domain.StockAlert;
import tech.algofinserve.recommendation.model.domain.StockAlertOutput;
import tech.algofinserve.recommendation.report.ReportGenerator;
import tech.algofinserve.recommendation.report.TelegramReportSender;

@Service
public class ChartInkAlertProcessingService {

  private static final Logger logger = LoggerFactory.getLogger(ChartInkAlertProcessingService.class);

  Format formatter_DDMMYYYY = new SimpleDateFormat("yyyy-MM-dd");
  @Autowired private AlertService alertService;
  @Autowired private RedisAlertCache redisAlertCache;
  private static final AtomicInteger alertCount = new AtomicInteger(0);
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
  TelegramMessagingNew telegramMessagingNew;

  @Autowired
  TelegramReportSender telegramReportSender;
//  TelegramMessaging telegramMessagingNormal;
//  TelegramMessaging telegramMessagingEOD;

  String normalChatId="@shreejitrades";
  String eodChatId= "@ideastoinvest";
/*  Function<String, Boolean> sendMessageNormal =
      p -> {
     //   return telegramMessagingNormal.sendMessage2(p);
        return telegramSenderPool.sendAndWait("@shreejitrades",p,10000);
      };

  Function<String, Boolean> sendMessageEOD =
      p -> {
   //     return telegramMessagingEOD.sendMessageEOD(p);
        return telegramSenderPool.sendAndWait("@ideastoinvest",p,10000);
      };*/

  @EventListener(ApplicationReadyEvent.class)
  public void startMessagingService() throws Exception {
//    telegramMessagingNormal=new TelegramMessaging();
 //   telegramMessagingEOD=new TelegramMessaging();
 //   new Thread(new MessagingService(messageQueueBuy, sendMessageNormal)).start();
 //   new Thread(new MessagingService(messageQueueSell, sendMessageNormal)).start();
 //   new Thread(new MessagingService(messageQueueBuyEOD, sendMessageEOD)).start();
 //   new Thread(new MessagingService(messageQueueSellEOD, sendMessageEOD)).start();
    new Thread(new MessagingServiceNew(messageQueueBuy, normalChatId, telegramMessagingNew)).start();
    new Thread(new MessagingServiceNew(messageQueueSell, normalChatId, telegramMessagingNew)).start();
    new Thread(new MessagingServiceNew(messageQueueBuyEOD, eodChatId, telegramMessagingNew)).start();
    new Thread(new MessagingServiceNew(messageQueueSellEOD, eodChatId, telegramMessagingNew)).start();
    //    new Thread(new MessagingService(messageQueueBuy)).start();
    //    new Thread(new MessagingService(messageQueueSell)).start();
   // System.out.println("Messaging Service Started.....");
    logger.info("Messaging Service Started.....");
  }

  @Async("taskExecutorBuy")
  public void processBuyAlert(Alert alert) throws InterruptedException {

    String[] stocksName = alert.getStocks().split(",");
    String[] prices = alert.getTriggerPrices().split(",");
    alertCount.getAndUpdate(i -> (i + stocksName.length ));

    for (int i = 0; i < stocksName.length; i++) {

      StockAlert stockAlert = convertAlertToStockAlert(alert, stocksName, prices, i, BuySell.BUY);


      if (redisAlertCache.buyStockExists(stockAlert.getStockCode())) {
        redisAlertCache.addToBuyStockAlerts(stockAlert.getStockCode(), stockAlert);
        messageQueueBuy.put("R::" + stockAlert.toString());
      } else {
        messageQueueBuy.put(stockAlert.toString());
        redisAlertCache.addToBuyStockAlerts(stockAlert.getStockCode(), stockAlert);
      }
      AlertDto alertDto = convertAlertToAlertDto(alert, stocksName, prices, i, BuySell.BUY);
      dbQueue.put(alertDto);
      redisAlertCache.addToBuyScanAlerts(stockAlert.getScanName(), stockAlert);
    }
  }

  @Async("taskExecutorBuyEOD")
  public void processBuyAlertEOD(Alert alert) throws InterruptedException {

    String[] stocksName = alert.getStocks().split(",");
    String[] prices = alert.getTriggerPrices().split(",");

    for (int i = 0; i < stocksName.length; i++) {

      StockAlert stockAlert = convertAlertToStockAlert(alert, stocksName, prices, i, BuySell.BUY);

      if (redisAlertCache.buyStockExists(stockAlert.getStockCode())) {
        redisAlertCache.addToBuyStockAlerts(stockAlert.getStockCode(), stockAlert);
        messageQueueBuyEOD.put("R::" + stockAlert.toString());
      } else {
        messageQueueBuyEOD.put(stockAlert.toString());
        redisAlertCache.addToBuyStockAlerts(stockAlert.getStockCode(), stockAlert);
      }

      AlertDto alertDto = convertAlertToAlertDto(alert, stocksName, prices, i, BuySell.BUY);
      dbQueue.put(alertDto);
      redisAlertCache.addToBuyScanAlerts(stockAlert.getScanName(), stockAlert);
    }
  }

  @Async("taskExecutorSell")
  public void processSellAlert(Alert alert) throws InterruptedException {

    String[] stocksName = alert.getStocks().split(",");
    String[] prices = alert.getTriggerPrices().split(",");
    alertCount.getAndUpdate(i -> (i + stocksName.length ));
    for (int i = 0; i < stocksName.length; i++) {

      StockAlert stockAlert = convertAlertToStockAlert(alert, stocksName, prices, i, BuySell.SELL);

      if (redisAlertCache.sellStockExists(stockAlert.getStockCode())) {
        redisAlertCache.addToSellStockAlerts(stockAlert.getStockCode(), stockAlert);
        messageQueueSell.put("R::" + stockAlert.toString());
      } else {
        messageQueueSell.put(stockAlert.toString());
        redisAlertCache.addToSellStockAlerts(stockAlert.getStockCode(), stockAlert);
      }

      AlertDto alertDto = convertAlertToAlertDto(alert, stocksName, prices, i, BuySell.SELL);
      dbQueue.put(alertDto);
      redisAlertCache.addToSellScanAlerts(stockAlert.getScanName(), stockAlert);
    }
  }

  @Async("taskExecutorSellEOD")
  public void processSellAlertEOD(Alert alert) throws InterruptedException {

    String[] stocksName = alert.getStocks().split(",");
    String[] prices = alert.getTriggerPrices().split(",");

    for (int i = 0; i < stocksName.length; i++) {

      StockAlert stockAlert = convertAlertToStockAlert(alert, stocksName, prices, i, BuySell.SELL);

      if (redisAlertCache.sellStockExists(stockAlert.getStockCode())) {
        redisAlertCache.addToSellStockAlerts(stockAlert.getStockCode(), stockAlert);
        messageQueueSellEOD.put("R::" + stockAlert.toString());
      } else {
        messageQueueSellEOD.put(stockAlert.toString());
        redisAlertCache.addToSellStockAlerts(stockAlert.getStockCode(), stockAlert);
      }

      AlertDto alertDto = convertAlertToAlertDto(alert, stocksName, prices, i, BuySell.SELL);
      dbQueue.put(alertDto);
      redisAlertCache.addToSellScanAlerts(stockAlert.getScanName(), stockAlert);
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
    redisAlertCache.clearAll();
  }

  public boolean generateStockAlertOutputReport() {

    //String reportDir = "/opt/render/project/src/reports";
    String reportDir = "\\app\\reports";
    try {
      //Files.createDirectories(Paths.get(reportDir));
      String stockAlertReportFileName = reportDir + "\\chartink_report_DDMMYYYY.csv";
      //String stockAlertReportFileName = "D:\\Report\\Chartink\\chartink_report_DDMMYYYY.csv";
      Date date = new Date();

      String fileDate = formatter_DDMMYYYY.format(date);
      stockAlertReportFileName = stockAlertReportFileName.replace("DDMMYYYY", fileDate);
     // System.out.println("stockAlertReportFileName ::" + stockAlertReportFileName);
      logger.info("stockAlertReportFileName ::" + stockAlertReportFileName);
      ReportGenerator reportGenerator = new ReportGenerator();
      List<StockAlertOutput> stockRankOutputList =
          StockAlertOutputHelper.buildStockAlertOutputList(redisAlertCache);
      reportGenerator.generateStockRankReport(stockAlertReportFileName, stockRankOutputList);
    //  System.out.println("Report Generated ::" + stockAlertReportFileName);
      logger.info("Report Generated ::" + stockAlertReportFileName);
      clearPreviousDayData();
     // System.out.println("Data Cleared For ::" + fileDate);
      logger.info("Data Cleared For ::" + fileDate);
      telegramReportSender.sendChartinkReport(stockAlertReportFileName);
     // System.out.println("Email Sent For Date ::" + fileDate);
     // System.out.println("Total Alerts Today ::" + alertCount);
      logger.info("Email Sent For Date ::" + fileDate);
      logger.info("Total Alerts Today ::" + alertCount);
      return true;
    } catch (Exception e) {
    //  System.out.println("Issue while Generating Report.");
      logger.info("Issue while Generating Report.");
      return false;
    }
  }
}
