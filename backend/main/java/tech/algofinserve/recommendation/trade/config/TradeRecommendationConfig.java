package tech.algofinserve.recommendation.trade.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * Configuration properties for trade recommendations
 */
@Configuration
@ConfigurationProperties(prefix = "trade.recommendation")
@Data
public class TradeRecommendationConfig {

    private String ruleVersion = "v1-supertrend-75low";
    private Map<String, RuleConfig> rules;
    private MarketDataConfig marketData;
    private MonitoringConfig monitoring;

    @Data
    public static class RuleConfig {
        private String stoprule;
        private List<Double> rewardMultipliers;
        private String timeframe;
        private Integer atrPeriod;
        private Integer atrMultiplier;
    }

    @Data
    public static class MarketDataConfig {
        private String baseUrl = "http://localhost:8081";
        private Integer timeout = 5000;
        private Integer retryAttempts = 3;
    }

    @Data
    public static class MonitoringConfig {
        private Long checkInterval = 30000L; // 30 seconds
        private Integer marketStartHour = 9;
        private Integer marketStartMinute = 15;
        private Integer marketEndHour = 15;
        private Integer marketEndMinute = 30;
    }
}




