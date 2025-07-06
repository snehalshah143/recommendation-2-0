package tech.algofinserve.recommendation.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfiguration {
  @Bean(name = "messageQueueBuy")
  public BlockingQueue<String> messageQueueBuy() {
    return new PriorityBlockingQueue<>(200);
  }

  @Bean(name = "messageQueueSell")
  public BlockingQueue<String> messageQueueSell() {
    return new PriorityBlockingQueue<>(200);
  }

  @Bean(name = "messageQueueBuyEOD")
  public BlockingQueue<String> messageQueueBuyEOD() {
    return new PriorityBlockingQueue<>(200);
  }

  @Bean(name = "messageQueueSellEOD")
  public BlockingQueue<String> messageQueueSellEOD() {
    return new PriorityBlockingQueue<>(200);
  }

  @Bean(name = "taskExecutorBuy")
  public Executor getAsyncExecutorBuy() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(2);
    executor.setQueueCapacity(200);
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
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(2);
    executor.setQueueCapacity(200);
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
}
