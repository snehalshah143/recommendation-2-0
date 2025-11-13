package tech.algofinserve.recommendation.core;

import java.time.ZoneId;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ScheduledTasks {
  private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

  @Autowired private ChartInkAlertProcessingService alertProcessing;
  @PostConstruct
  public void init() {
    logger.info("✅ Scheduler bean loaded successfully");
    logger.info("Container time now: " + new Date());
    logger.info("System default timezone: " + ZoneId.systemDefault());
  }
//  @Scheduled(cron = "0 58 15 * * ?") // Cron expression for running every minute
//  @Scheduled(cron = "0 00 23 * * ?") // Cron expression for running every minute
  @Scheduled(cron = "0 05 23 * * ?", zone = "Asia/Kolkata")
  public void execute() {
    alertProcessing.generateStockAlertOutputReport();
    logger.info("Scheduler ran for report genration::" + new Date());
    System.out.println("Scheduler ran for report genration::" + new Date());
  }
}
