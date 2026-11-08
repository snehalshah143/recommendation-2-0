package tech.algofinserve.recommendation.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import tech.algofinserve.recommendation.alerts.dto.AlertDto;
import tech.algofinserve.recommendation.model.domain.Alert;
import tech.algofinserve.recommendation.redis.RedisBlockingQueue;

@Configuration
@EnableAsync
public class AsyncConfiguration {

  @Bean(name = "dbQueue")
  public BlockingQueue<AlertDto> dbQueue(StringRedisTemplate redisTemplate,
      @Qualifier("redisObjectMapper") ObjectMapper objectMapper) {
    return new RedisBlockingQueue<>(redisTemplate, "queue:db", AlertDto.class, objectMapper);
  }

  @Bean(name = "messageQueueBuy")
  public BlockingQueue<String> messageQueueBuy(StringRedisTemplate redisTemplate,
      @Qualifier("redisObjectMapper") ObjectMapper objectMapper) {
    return new RedisBlockingQueue<>(redisTemplate, "queue:msg-buy", String.class, objectMapper);
  }

  @Bean(name = "messageQueueSell")
  public BlockingQueue<String> messageQueueSell(StringRedisTemplate redisTemplate,
      @Qualifier("redisObjectMapper") ObjectMapper objectMapper) {
    return new RedisBlockingQueue<>(redisTemplate, "queue:msg-sell", String.class, objectMapper);
  }

  @Bean(name = "messageQueueBuyEOD")
  public BlockingQueue<String> messageQueueBuyEOD(StringRedisTemplate redisTemplate,
      @Qualifier("redisObjectMapper") ObjectMapper objectMapper) {
    return new RedisBlockingQueue<>(redisTemplate, "queue:msg-buy-eod", String.class, objectMapper);
  }

  @Bean(name = "messageQueueSellEOD")
  public BlockingQueue<String> messageQueueSellEOD(StringRedisTemplate redisTemplate,
      @Qualifier("redisObjectMapper") ObjectMapper objectMapper) {
    return new RedisBlockingQueue<>(redisTemplate, "queue:msg-sell-eod", String.class, objectMapper);
  }

  @Bean("buyAlertQueue")
  public BlockingQueue<Alert> buyAlertQueue(StringRedisTemplate redisTemplate,
      @Qualifier("redisObjectMapper") ObjectMapper objectMapper) {
    return new RedisBlockingQueue<>(redisTemplate, "queue:buy-alert", Alert.class, objectMapper);
  }

  @Bean("sellAlertQueue")
  public BlockingQueue<Alert> sellAlertQueue(StringRedisTemplate redisTemplate,
      @Qualifier("redisObjectMapper") ObjectMapper objectMapper) {
    return new RedisBlockingQueue<>(redisTemplate, "queue:sell-alert", Alert.class, objectMapper);
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

  @Bean
  public TaskScheduler taskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(2);
    scheduler.setThreadNamePrefix("CustomScheduler-");
    scheduler.initialize();
    System.out.println("✅ TaskScheduler bean created manually.");
    return scheduler;
  }

}
