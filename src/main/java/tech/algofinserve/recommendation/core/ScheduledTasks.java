package tech.algofinserve.recommendation.core;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

  @Autowired private ChartInkAlertProcessingService alertProcessing;

  @Scheduled(cron = "0 30 19 * * ?") // Cron expression for running every minute
  public void execute() {
    alertProcessing.generateStockAlertOutputReport();
    System.out.println("Scheduler ran for report genration::" + new Date());
  }
}
