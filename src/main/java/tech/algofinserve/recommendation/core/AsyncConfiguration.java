package tech.algofinserve.recommendation.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;

@Configuration
@EnableAsync
public class AsyncConfiguration {
    @Bean
    public BlockingQueue<String> messageQueue() {
        return new PriorityBlockingQueue<>(100);
    }
    @Bean(name="taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ThreadPool-");
        executor.initialize();
        return executor;
    }
}
