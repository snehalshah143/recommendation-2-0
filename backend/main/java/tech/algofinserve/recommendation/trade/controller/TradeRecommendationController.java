package tech.algofinserve.recommendation.trade.controller;

import tech.algofinserve.recommendation.trade.entity.TradeRecommendation;
import tech.algofinserve.recommendation.trade.service.TradeRecommendationService;
import tech.algofinserve.recommendation.trade.worker.StoplossMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for trade recommendations
 * This will be used by the UI to display targets and stoploss data
 */
@RestController
@RequestMapping("/api/trade-recommendations")
@RequiredArgsConstructor
@Slf4j
public class TradeRecommendationController {

    private final TradeRecommendationService tradeRecommendationService;
    private final StoplossMonitor stoplossMonitor;

    /**
     * Get all active trade recommendations
     */
    @GetMapping
    public ResponseEntity<List<TradeRecommendation>> getAllActiveRecommendations() {
        try {
            List<TradeRecommendation> recommendations = tradeRecommendationService.getActiveRecommendations();
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            log.error("Error fetching active recommendations: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get active trade recommendations for a specific symbol
     */
    @GetMapping("/{symbol}")
    public ResponseEntity<List<TradeRecommendation>> getRecommendationsBySymbol(@PathVariable String symbol) {
        try {
            List<TradeRecommendation> recommendations = tradeRecommendationService.getActiveRecommendations(symbol);
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            log.error("Error fetching recommendations for symbol {}: {}", symbol, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Manually trigger stoploss monitoring for a specific symbol
     */
    @PostMapping("/{symbol}/check-stoploss")
    public ResponseEntity<String> checkStoplossForSymbol(@PathVariable String symbol) {
        try {
            stoplossMonitor.checkStoplossForSymbol(symbol);
            return ResponseEntity.ok("Stoploss check completed for " + symbol);
        } catch (Exception e) {
            log.error("Error checking stoploss for symbol {}: {}", symbol, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error checking stoploss: " + e.getMessage());
        }
    }

    /**
     * Get monitoring statistics
     */
    @GetMapping("/monitoring/stats")
    public ResponseEntity<StoplossMonitor.MonitoringStats> getMonitoringStats() {
        try {
            StoplossMonitor.MonitoringStats stats = stoplossMonitor.getMonitoringStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching monitoring stats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Close a specific recommendation manually
     */
    @PostMapping("/{id}/close")
    public ResponseEntity<String> closeRecommendation(@PathVariable Long id) {
        try {
            tradeRecommendationService.closeRecommendation(id, tech.algofinserve.recommendation.trade.entity.CloseReason.MANUAL);
            return ResponseEntity.ok("Recommendation " + id + " closed successfully");
        } catch (Exception e) {
            log.error("Error closing recommendation {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error closing recommendation: " + e.getMessage());
        }
    }
}




