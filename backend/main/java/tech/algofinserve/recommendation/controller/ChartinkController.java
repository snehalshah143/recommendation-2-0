package tech.algofinserve.recommendation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tech.algofinserve.recommendation.core.ChartInkAlertProcessingService;
import tech.algofinserve.recommendation.model.domain.Alert;

import java.util.concurrent.BlockingQueue;

@RestController
public class ChartinkController {

  @Autowired
  @Qualifier("buyAlertQueue")
  private BlockingQueue<Alert> buyAlertQueue;

  @Autowired
  @Qualifier("sellAlertQueue")
  private BlockingQueue<Alert> sellAlertQueue;

  @Autowired private ChartInkAlertProcessingService alertProcessing;

  @PostMapping(path = "/BuyAlert", consumes = "application/json")
  public void alertsReceivedBuy(@RequestBody Alert alert) {
    System.out.println(alert.toString());
    try {
    //  alertProcessing.processBuyAlert(alert);
      buyAlertQueue.put(alert);  // push to queue
      //  return ResponseEntity.ok("Buy Alert Queued Successfully");

    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    //   TelegramMessaging.sendMessage2(alert.toString());
  }

  @PostMapping(path = "/BuyAlertEOD", consumes = "application/json")
  public void alertsReceivedBuyEOD(@RequestBody Alert alert) {
    System.out.println(alert.toString());
    try {
      alertProcessing.processBuyAlertEOD(alert);

    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    //   TelegramMessaging.sendMessage2(alert.toString());
  }

  @PostMapping(path = "/SellAlert", consumes = "application/json")
  public void alertsReceivedSell(@RequestBody Alert alert) {
    // Adding Log
    System.out.println(alert.toString());
    try {
   //   alertProcessing.processSellAlert(alert);
      sellAlertQueue.put(alert);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    //   TelegramMessaging.sendMessage2(alert.toString());
  }

  @PostMapping(path = "/SellAlertEOD", consumes = "application/json")
  public void alertsReceivedSellEOD(@RequestBody Alert alert) {
    // Adding Log
    System.out.println(alert.toString());
    try {
      alertProcessing.processSellAlertEOD(alert);

    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    //   TelegramMessaging.sendMessage2(alert.toString());
  }

  @PostMapping(path = "/Clear")
  public ResponseEntity clearPreviousDayData() {

    alertProcessing.clearPreviousDayData();

    return ResponseEntity.ok("All Data Cleared.");
  }

  @PostMapping(path = "/GenerateChartinkReport")
  public ResponseEntity generateChartinkReport() {

    boolean status = alertProcessing.generateStockAlertOutputReport();

    return status
        ? ResponseEntity.ok("Report Generated.")
        : ResponseEntity.ok("Report Generation Failed.");
  }
}
