package tech.algofinserve.recommendation.trade.service;

import tech.algofinserve.recommendation.alerts.dto.AlertDto;
import tech.algofinserve.recommendation.trade.entity.*;
import tech.algofinserve.recommendation.trade.repository.TradeRecommendationRepository;
import tech.algofinserve.recommendation.trade.adapter.MarketDataAdapter;
import tech.algofinserve.recommendation.trade.util.SupertrendUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing trade recommendations with targets and stoploss calculations
 */
@Service
public class TradeRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(TradeRecommendationService.class);
    
    private final TradeRecommendationRepository repository;
    private final MarketDataAdapter marketDataAdapter;

    public TradeRecommendationService(TradeRecommendationRepository repository, MarketDataAdapter marketDataAdapter) {
        this.repository = repository;
        this.marketDataAdapter = marketDataAdapter;
    }

    /**
     * Handle incoming alert and create/update trade recommendations
     */
    @Transactional
    public TradeRecommendation handleAlert(AlertDto alertDto) {
        String symbol = alertDto.getStockCode();
        Direction direction = mapBuySellToDirection(alertDto.getBuySell());
        TradeDuration tradeDuration = determineTradeDuration(alertDto.getScanName());
        Double alertPrice = Double.valueOf(alertDto.getPrice());

        log.info("Processing alert for {} {} at price {} for duration {}", 
                symbol, direction, alertPrice, tradeDuration);

        // 1. Check if ACTIVE recommendation exists for same symbol + direction + tradeDuration
        Optional<TradeRecommendation> existing = repository
                .findBySymbolAndDirectionAndTradeDurationAndStatus(
                        symbol, direction, tradeDuration, Status.ACTIVE);

        if (existing.isPresent()) {
            log.info("Active recommendation already exists for {} {} {}, reusing existing", 
                    symbol, direction, tradeDuration);
            return existing.get();
        }

        // 2. Close opposite direction recommendations if they exist
        closeOppositeRecommendations(symbol, direction, tradeDuration);

        // 3. Compute stoploss and targets using Rule v1
        TradeRecommendationCalculation calculation = calculateTargetsAndStoploss(
                symbol, alertPrice, direction, tradeDuration);

        // 4. Create and persist new recommendation
        TradeRecommendation recommendation = createRecommendation(
                alertDto, tradeDuration, calculation);

        TradeRecommendation saved = repository.save(recommendation);
        log.info("Created new trade recommendation: {}", saved.getId());

        return saved;
    }

    /**
     * Close opposite direction recommendations
     */
    @Transactional
    public void closeOppositeRecommendations(String symbol, Direction direction, TradeDuration tradeDuration) {
        Direction opposite = (direction == Direction.BUY) ? Direction.SELL : Direction.BUY;
        
        List<TradeRecommendation> oppositeRecommendations = repository
                .findBySymbolAndDirectionAndStatus(symbol, opposite, Status.ACTIVE)
                .stream()
                .filter(rec -> rec.getTradeDuration() == tradeDuration)
                .collect(java.util.stream.Collectors.toList());

        for (TradeRecommendation rec : oppositeRecommendations) {
            rec.setStatus(Status.CLOSED);
            rec.setClosedAt(LocalDateTime.now());
            rec.setCloseReason(CloseReason.OPPOSITE_SIGNAL);
            repository.save(rec);
            log.info("Closed opposite recommendation {} for {} {} {}", 
                    rec.getId(), symbol, opposite, tradeDuration);
        }
    }

    /**
     * Calculate targets and stoploss using Rule v1
     */
    private TradeRecommendationCalculation calculateTargetsAndStoploss(
            String symbol, Double alertPrice, Direction direction, TradeDuration tradeDuration) {

        // Get timeframe based on trade duration
        String timeframe = getTimeframeForDuration(tradeDuration);
        
        // Fetch market data
        Double prev75mLow = marketDataAdapter.getPrevCandleLow(symbol, "75m");
        Double prev75mHigh = marketDataAdapter.getPrevCandleHigh(symbol, "75m");
        Double supertrend15 = marketDataAdapter.getSupertrend(symbol, "15m", 11, 20);

        // Calculate stoploss using Rule v1
        Double stoploss;
        if (direction == Direction.BUY) {
            // BUY: stoploss = min(previous 75-min candle low, Supertrend(11,20) on 15-min)
            if (prev75mLow != null && supertrend15 != null) {
                stoploss = Math.min(prev75mLow, supertrend15);
            } else if (prev75mLow != null) {
                stoploss = prev75mLow;
            } else if (supertrend15 != null) {
                stoploss = supertrend15;
            } else {
                // Fallback: 2% below alert price
                stoploss = alertPrice * 0.98;
                log.warn("Using fallback stoploss for BUY {}: {}", symbol, stoploss);
            }
        } else {
            // SELL: stoploss = max(previous 75-min candle high, Supertrend(11,20) on 15-min)
            if (prev75mHigh != null && supertrend15 != null) {
                stoploss = Math.max(prev75mHigh, supertrend15);
            } else if (prev75mHigh != null) {
                stoploss = prev75mHigh;
            } else if (supertrend15 != null) {
                stoploss = supertrend15;
            } else {
                // Fallback: 2% above alert price
                stoploss = alertPrice * 1.02;
                log.warn("Using fallback stoploss for SELL {}: {}", symbol, stoploss);
            }
        }

        // Calculate targets using Risk-Reward ratios
        Double risk = Math.abs(alertPrice - stoploss);
        Double target1, target2, target3;

        if (direction == Direction.BUY) {
            target1 = alertPrice + (risk * 1.25);
            target2 = alertPrice + (risk * 1.5);
            target3 = alertPrice + (risk * 2.0);
        } else {
            target1 = alertPrice - (risk * 1.25);
            target2 = alertPrice - (risk * 1.5);
            target3 = alertPrice - (risk * 2.0);
        }

        return TradeRecommendationCalculation.builder()
                .stoploss1(stoploss)
                .stoploss2(stoploss) // Same for now, future-proofing
                .hardStoploss(stoploss) // Same for now, future-proofing
                .target1(target1)
                .target2(target2)
                .target3(target3)
                .risk(risk)
                .prev75mLow(prev75mLow)
                .prev75mHigh(prev75mHigh)
                .supertrend15(supertrend15)
                .build();
    }

    /**
     * Create TradeRecommendation entity
     */
    private TradeRecommendation createRecommendation(
            AlertDto alertDto, TradeDuration tradeDuration, TradeRecommendationCalculation calculation) {

        return TradeRecommendation.builder()
                .symbol(alertDto.getStockCode())
                .exchange("NSE") // Default exchange
                .direction(mapBuySellToDirection(alertDto.getBuySell()))
                .tradeDuration(tradeDuration)
                .timeframe(getTimeframeForDuration(tradeDuration))
                .entryPrice(Double.parseDouble(alertDto.getPrice()))
                .stoploss1(calculation.getStoploss1())
                .stoploss2(calculation.getStoploss2())
                .hardStoploss(calculation.getHardStoploss())
                .target1(calculation.getTarget1())
                .target2(calculation.getTarget2())
                .target3(calculation.getTarget3())
                .status(Status.ACTIVE)
                .ruleVersion("v1-supertrend-75low")
                .metadata(createMetadata(calculation))
                .build();
    }

    /**
     * Determine trade duration from scan name
     */
    private TradeDuration determineTradeDuration(String scanName) {
        if (scanName == null) {
            return TradeDuration.INTRADAY;
        }

        String upperScanName = scanName.toUpperCase();
        if (upperScanName.contains("INTRADAY")) {
            return TradeDuration.INTRADAY;
        } else if (upperScanName.contains("POSITIONAL")) {
            return TradeDuration.POSITIONAL;
        } else if (upperScanName.contains("SHORT") || upperScanName.contains("SHORTTERM")) {
            return TradeDuration.SHORTTERM;
        } else if (upperScanName.contains("LONG") || upperScanName.contains("LONGTERM")) {
            return TradeDuration.LONGTERM;
        }

        return TradeDuration.INTRADAY; // Default
    }

    /**
     * Get timeframe string for trade duration
     */
    private String getTimeframeForDuration(TradeDuration tradeDuration) {
        switch (tradeDuration) {
            case INTRADAY:
                return "75m";
            case POSITIONAL:
                return "1d";
            case SHORTTERM:
                return "1w";
            case LONGTERM:
                return "1M";
            default:
                return "75m";
        }
    }

    /**
     * Map BuySell enum to Direction enum
     */
    private Direction mapBuySellToDirection(tech.algofinserve.recommendation.constants.BuySell buySell) {
        switch (buySell) {
            case BUY:
            case LONG_BUY:
                return Direction.BUY;
            case SELL:
            case SHORT_SELL:
            case EXIT:
            case SQUAREOFF_LONG:
            case COVER_SHORT:
                return Direction.SELL;
            default:
                return Direction.BUY; // Default to BUY
        }
    }

    /**
     * Create metadata JSON string
     */
    private String createMetadata(TradeRecommendationCalculation calculation) {
        return String.format(
                "{\"prev75mLow\":%s,\"prev75mHigh\":%s,\"supertrend15\":%s,\"risk\":%s}",
                calculation.getPrev75mLow(),
                calculation.getPrev75mHigh(),
                calculation.getSupertrend15(),
                calculation.getRisk()
        );
    }

    /**
     * Get all active recommendations for monitoring
     */
    public List<TradeRecommendation> getActiveRecommendations() {
        return repository.findByStatus(Status.ACTIVE);
    }

    /**
     * Get active recommendations for a specific symbol
     */
    public List<TradeRecommendation> getActiveRecommendations(String symbol) {
        return repository.findBySymbolAndStatus(symbol, Status.ACTIVE);
    }

    /**
     * Close a recommendation
     */
    @Transactional
    public void closeRecommendation(Long recommendationId, CloseReason reason) {
        Optional<TradeRecommendation> optional = repository.findById(recommendationId);
        if (optional.isPresent()) {
            TradeRecommendation rec = optional.get();
            rec.setStatus(Status.CLOSED);
            rec.setClosedAt(LocalDateTime.now());
            rec.setCloseReason(reason);
            repository.save(rec);
            log.info("Closed recommendation {} with reason {}", recommendationId, reason);
        }
    }

    /**
     * Inner class for calculation results
     */
    private static class TradeRecommendationCalculation {
        private Double stoploss1;
        private Double stoploss2;
        private Double hardStoploss;
        private Double target1;
        private Double target2;
        private Double target3;
        private Double risk;
        private Double prev75mLow;
        private Double prev75mHigh;
        private Double supertrend15;

        // Getters and Setters
        public Double getStoploss1() { return stoploss1; }
        public void setStoploss1(Double stoploss1) { this.stoploss1 = stoploss1; }
        public Double getStoploss2() { return stoploss2; }
        public void setStoploss2(Double stoploss2) { this.stoploss2 = stoploss2; }
        public Double getHardStoploss() { return hardStoploss; }
        public void setHardStoploss(Double hardStoploss) { this.hardStoploss = hardStoploss; }
        public Double getTarget1() { return target1; }
        public void setTarget1(Double target1) { this.target1 = target1; }
        public Double getTarget2() { return target2; }
        public void setTarget2(Double target2) { this.target2 = target2; }
        public Double getTarget3() { return target3; }
        public void setTarget3(Double target3) { this.target3 = target3; }
        public Double getRisk() { return risk; }
        public void setRisk(Double risk) { this.risk = risk; }
        public Double getPrev75mLow() { return prev75mLow; }
        public void setPrev75mLow(Double prev75mLow) { this.prev75mLow = prev75mLow; }
        public Double getPrev75mHigh() { return prev75mHigh; }
        public void setPrev75mHigh(Double prev75mHigh) { this.prev75mHigh = prev75mHigh; }
        public Double getSupertrend15() { return supertrend15; }
        public void setSupertrend15(Double supertrend15) { this.supertrend15 = supertrend15; }

        // Builder pattern
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Double stoploss1;
            private Double stoploss2;
            private Double hardStoploss;
            private Double target1;
            private Double target2;
            private Double target3;
            private Double risk;
            private Double prev75mLow;
            private Double prev75mHigh;
            private Double supertrend15;

            public Builder stoploss1(Double stoploss1) { this.stoploss1 = stoploss1; return this; }
            public Builder stoploss2(Double stoploss2) { this.stoploss2 = stoploss2; return this; }
            public Builder hardStoploss(Double hardStoploss) { this.hardStoploss = hardStoploss; return this; }
            public Builder target1(Double target1) { this.target1 = target1; return this; }
            public Builder target2(Double target2) { this.target2 = target2; return this; }
            public Builder target3(Double target3) { this.target3 = target3; return this; }
            public Builder risk(Double risk) { this.risk = risk; return this; }
            public Builder prev75mLow(Double prev75mLow) { this.prev75mLow = prev75mLow; return this; }
            public Builder prev75mHigh(Double prev75mHigh) { this.prev75mHigh = prev75mHigh; return this; }
            public Builder supertrend15(Double supertrend15) { this.supertrend15 = supertrend15; return this; }

            public TradeRecommendationCalculation build() {
                TradeRecommendationCalculation calc = new TradeRecommendationCalculation();
                calc.stoploss1 = this.stoploss1;
                calc.stoploss2 = this.stoploss2;
                calc.hardStoploss = this.hardStoploss;
                calc.target1 = this.target1;
                calc.target2 = this.target2;
                calc.target3 = this.target3;
                calc.risk = this.risk;
                calc.prev75mLow = this.prev75mLow;
                calc.prev75mHigh = this.prev75mHigh;
                calc.supertrend15 = this.supertrend15;
                return calc;
            }
        }
    }
}
