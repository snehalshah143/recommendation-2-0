package tech.algofinserve.recommendation.trade.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for trade recommendation components
 */
@Configuration
public class TradeConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}





