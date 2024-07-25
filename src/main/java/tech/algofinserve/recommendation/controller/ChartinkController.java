package tech.algofinserve.recommendation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tech.algofinserve.recommendation.core.ChartInkAlertProcessingService;
import tech.algofinserve.recommendation.model.domain.Alert;

@RestController
public class ChartinkController {

  @Autowired private ChartInkAlertProcessingService alertProcessing;

  @PostMapping(path = "/BuyAlert", consumes = "application/json")
  public void alertsReceived(@RequestBody Alert alert) {
    System.out.println(alert.toString());
    try {
      alertProcessing.processBuyAlert(alert);

    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    //   TelegramMessaging.sendMessage2(alert.toString());
  }

  @PostMapping(path = "/clear")
  public ResponseEntity clearPreviousDayData() {

    alertProcessing.clearPreviousDayData();

    return ResponseEntity.ok("All Data Cleared.");
  }
}
