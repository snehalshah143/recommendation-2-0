package tech.algofinserve.recommendation.trade.worker;

import tech.algofinserve.recommendation.trade.entity.TradeRecommendation;
import tech.algofinserve.recommendation.trade.entity.CloseReason;
import tech.algofinserve.recommendation.trade.entity.Direction;
import tech.algofinserve.recommendation.trade.service.TradeRecommendationService;
import tech.algofinserve.recommendation.trade.adapter.MarketDataAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Background worker to monitor stoploss levels and close recommendations when breached
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StoplossMonitor {

    private final TradeRecommendationService tradeRecommendationService;
    private final MarketDataAdapter marketDataAdapter;
    private final boolean start=false;
    /**
     * Monitor stoploss levels every 30 seconds during market hours
     * This runs every 30 seconds but will only process during market hours
     */
   // @Scheduled(fixedRate = 30000) // 30 seconds
    @Transactional
    public void monitorStoplossLevels() {
        if (!isMarketHours() && !start) {
            log.debug("Market is closed, skipping stoploss monitoring");
            return;
        }

        try {
            List<TradeRecommendation> activeRecommendations = tradeRecommendationService.getActiveRecommendations();
            log.debug("Monitoring {} active recommendations", activeRecommendations.size());

            for (TradeRecommendation recommendation : activeRecommendations) {
                checkStoplossBreach(recommendation);
            }

        } catch (Exception e) {
            log.error("Error during stoploss monitoring: {}", e.getMessage(), e);
        }
    }

    /**
     * Check if a specific recommendation's stoploss has been breached
     */
    private void checkStoplossBreach(TradeRecommendation recommendation) {
        try {
            String symbol = recommendation.getSymbol();
            Double currentLTP = marketDataAdapter.getCurrentLTP(symbol);

            if (currentLTP == null) {
                log.warn("Could not fetch LTP for {} during stoploss monitoring", symbol);
                return;
            }

            boolean isBreached = false;
            String breachReason = "";

            if (recommendation.getDirection() == Direction.BUY) {
                // For BUY: stoploss is breached if current price goes below stoploss
                if (currentLTP <= recommendation.getStoploss1()) {
                    isBreached = true;
                    breachReason = String.format("BUY stoploss breached: LTP %.2f <= stoploss %.2f", 
                            currentLTP, recommendation.getStoploss1());
                }
            } else {
                // For SELL: stoploss is breached if current price goes above stoploss
                if (currentLTP >= recommendation.getStoploss1()) {
                    isBreached = true;
                    breachReason = String.format("SELL stoploss breached: LTP %.2f >= stoploss %.2f", 
                            currentLTP, recommendation.getStoploss1());
                }
            }

            if (isBreached) {
                log.info("Stoploss breached for recommendation {} ({}): {}", 
                        recommendation.getId(), symbol, breachReason);
                
                tradeRecommendationService.closeRecommendation(
                        recommendation.getId(), CloseReason.STOPLOSS_HIT);
            } else {
                log.debug("No stoploss breach for {} {}: LTP={}, Stoploss={}", 
                        symbol, recommendation.getDirection(), currentLTP, recommendation.getStoploss1());
            }

        } catch (Exception e) {
            log.error("Error checking stoploss for recommendation {}: {}", 
                    recommendation.getId(), e.getMessage(), e);
        }
    }

    /**
     * Check if current time is during market hours (9:15 AM to 3:30 PM IST)
     */
    private boolean isMarketHours() {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        int minute = now.getMinute();
        
        // Market hours: 9:15 AM to 3:30 PM IST
        int marketStartHour = 9;
        int marketStartMinute = 15;
        int marketEndHour = 15;
        int marketEndMinute = 30;
        
        int currentTimeInMinutes = hour * 60 + minute;
        int marketStartInMinutes = marketStartHour * 60 + marketStartMinute;
        int marketEndInMinutes = marketEndHour * 60 + marketEndMinute;
        
        return currentTimeInMinutes >= marketStartInMinutes && currentTimeInMinutes <= marketEndInMinutes;
    }

    /**
     * Manual stoploss check for a specific symbol
     * This can be called from REST endpoints for immediate checking
     */
    @Transactional
    public void checkStoplossForSymbol(String symbol) {
        try {
            List<TradeRecommendation> activeRecommendations = 
                    tradeRecommendationService.getActiveRecommendations(symbol);
            
            log.info("Manual stoploss check for {}: {} active recommendations", 
                    symbol, activeRecommendations.size());
            
            for (TradeRecommendation recommendation : activeRecommendations) {
                checkStoplossBreach(recommendation);
            }
            
        } catch (Exception e) {
            log.error("Error during manual stoploss check for {}: {}", symbol, e.getMessage(), e);
        }
    }

    /**
     * Get monitoring statistics
     */
    public MonitoringStats getMonitoringStats() {
        List<TradeRecommendation> activeRecommendations = tradeRecommendationService.getActiveRecommendations();
        
        return MonitoringStats.builder()
                .activeRecommendations(activeRecommendations.size())
                .marketDataAvailable(marketDataAdapter.isMarketDataAvailable())
                .isMarketHours(isMarketHours())
                .lastCheck(LocalDateTime.now())
                .build();
    }

    /**
     * Inner class for monitoring statistics
     */
    @lombok.Data
    @lombok.Builder
    public static class MonitoringStats {
        private int activeRecommendations;
        private boolean marketDataAvailable;
        private boolean isMarketHours;
        private LocalDateTime lastCheck;
    }
}
