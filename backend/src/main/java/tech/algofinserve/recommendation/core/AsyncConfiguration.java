package tech.algofinserve.recommendation.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import tech.algofinserve.recommendation.alerts.dto.AlertDto;
import tech.algofinserve.recommendation.model.domain.Alert;

@Configuration
@EnableAsync
public class AsyncConfiguration {

  @Bean(name = "dbQueue")
  public BlockingQueue<AlertDto> dbQueue() {
    return new LinkedBlockingQueue<>(1000);
  }

  @Bean(name = "messageQueueBuy")
  public BlockingQueue<String> messageQueueBuy() {
    return new LinkedBlockingQueue<>(1000);
  }

  @Bean(name = "messageQueueSell")
  public BlockingQueue<String> messageQueueSell() {
    return new LinkedBlockingQueue<>(1000);
  }

  @Bean(name = "messageQueueBuyEOD")
  public BlockingQueue<String> messageQueueBuyEOD() {
    return new LinkedBlockingQueue<>(200);
  }

  @Bean(name = "messageQueueSellEOD")
  public BlockingQueue<String> messageQueueSellEOD() {
    return new LinkedBlockingQueue<>(200);
  }


  @Bean("buyAlertQueue")
  public BlockingQueue<Alert> buyAlertQueue() {
    return new LinkedBlockingQueue<>(1000);
  }

  @Bean("sellAlertQueue")
  public BlockingQueue<Alert> sellAlertQueue() {
    return new LinkedBlockingQueue<>(1000);
  }

  @Bean(name = "taskExecutorTelegramMessaging")
  public Executor getAsyncExecutorTelegramMessaging() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(1000);
    executor.setThreadNamePrefix("ThreadPool-TelegramMessaging-");
    executor.initialize();
    return executor;
  }

  @Bean(name = "taskExecutorBuy")
  public Executor getAsyncExecutorBuy() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(1000);
    executor.setThreadNamePrefix("ThreadPool-BUY-");
    executor.initialize();
    return executor;
  }

  @Bean(name = "taskExecutorBuyEOD")
  public Executor getAsyncExecutorBuyEOD() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(2);
    executor.setQueueCapacity(200);
    executor.setThreadNamePrefix("ThreadPool-BUY-EOD-");
    executor.initialize();
    return executor;
  }

  @Bean(name = "taskExecutorSell")
  public Executor getAsyncExecutorSell() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(1000);
    executor.setThreadNamePrefix("ThreadPool-SELL-");
    executor.initialize();
    return executor;
  }

  @Bean(name = "taskExecutorSellEOD")
  public Executor getAsyncExecutorSellEOD() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(2);
    executor.setQueueCapacity(200);
    executor.setThreadNamePrefix("ThreadPool-SELL-EOD-");
    executor.initialize();
    return executor;
  }

  @Bean(name = "taskExecutorDB")
  public Executor taskExecutorDB() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(4);
    executor.setQueueCapacity(1000);
    executor.setThreadNamePrefix("DBExecutor-");
    executor.initialize();
    return executor;
  }


}
