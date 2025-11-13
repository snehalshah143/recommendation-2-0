package tech.algofinserve.recommendation.core;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {
  private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

  @Autowired private ChartInkAlertProcessingService alertProcessing;

//  @Scheduled(cron = "0 59 15 * * ?") // Cron expression for running every minute
  @Scheduled(cron = "0 00 23 * * ?") // Cron expression for running every minute
  public void execute() {
    alertProcessing.generateStockAlertOutputReport();
    logger.info("Scheduler ran for report genration::" + new Date());
    System.out.println("Scheduler ran for report genration::" + new Date());
  }
}
