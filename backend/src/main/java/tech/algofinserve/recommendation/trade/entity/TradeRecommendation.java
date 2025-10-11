package tech.algofinserve.recommendation.trade.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a trade recommendation with targets and stoploss
 */
@Entity
@Table(
    name = "trade_recommendations",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"symbol", "direction", "trade_duration", "status"}
        )
    }
)
public class TradeRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;

    @Column(name = "exchange", nullable = false, length = 10)
    private String exchange;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false)
    private Direction direction;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_duration", nullable = false)
    private TradeDuration tradeDuration;

    @Column(name = "timeframe", nullable = false, length = 10)
    private String timeframe;

    @Column(name = "entry_price", nullable = false, precision = 10, scale = 2)
    private Double entryPrice;

    @Column(name = "target1", precision = 10, scale = 2)
    private Double target1;

    @Column(name = "target2", precision = 10, scale = 2)
    private Double target2;

    @Column(name = "target3", precision = 10, scale = 2)
    private Double target3;

    @Column(name = "stoploss1", nullable = false, precision = 10, scale = 2)
    private Double stoploss1;

    @Column(name = "stoploss2", nullable = false, precision = 10, scale = 2)
    private Double stoploss2;

    @Column(name = "hard_stoploss", nullable = false, precision = 10, scale = 2)
    private Double hardStoploss;

    @Column(name = "trailing_method", length = 50)
    private String trailingMethod;

    @Column(name = "trailing_value", precision = 10, scale = 2)
    private Double trailingValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "close_reason", length = 50)
    private CloseReason closeReason;

    @Column(name = "rule_version", nullable = false, length = 50)
    private String ruleVersion = "v1-supertrend-75low";

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional data

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Constructors
    public TradeRecommendation() {
        this.status = Status.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.ruleVersion = "v1-supertrend-75low";
    }

    public TradeRecommendation(Long id, String symbol, String exchange, Direction direction, 
                             TradeDuration tradeDuration, String timeframe, Double entryPrice,
                             Double target1, Double target2, Double target3, Double stoploss1,
                             Double stoploss2, Double hardStoploss, String trailingMethod,
                             Double trailingValue, Status status, LocalDateTime createdAt,
                             LocalDateTime updatedAt, LocalDateTime closedAt, CloseReason closeReason,
                             String ruleVersion, String metadata) {
        this.id = id;
        this.symbol = symbol;
        this.exchange = exchange;
        this.direction = direction;
        this.tradeDuration = tradeDuration;
        this.timeframe = timeframe;
        this.entryPrice = entryPrice;
        this.target1 = target1;
        this.target2 = target2;
        this.target3 = target3;
        this.stoploss1 = stoploss1;
        this.stoploss2 = stoploss2;
        this.hardStoploss = hardStoploss;
        this.trailingMethod = trailingMethod;
        this.trailingValue = trailingValue;
        this.status = status != null ? status : Status.ACTIVE;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
        this.closedAt = closedAt;
        this.closeReason = closeReason;
        this.ruleVersion = ruleVersion != null ? ruleVersion : "v1-supertrend-75low";
        this.metadata = metadata;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }

    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }

    public TradeDuration getTradeDuration() { return tradeDuration; }
    public void setTradeDuration(TradeDuration tradeDuration) { this.tradeDuration = tradeDuration; }

    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }

    public Double getEntryPrice() { return entryPrice; }
    public void setEntryPrice(Double entryPrice) { this.entryPrice = entryPrice; }

    public Double getTarget1() { return target1; }
    public void setTarget1(Double target1) { this.target1 = target1; }

    public Double getTarget2() { return target2; }
    public void setTarget2(Double target2) { this.target2 = target2; }

    public Double getTarget3() { return target3; }
    public void setTarget3(Double target3) { this.target3 = target3; }

    public Double getStoploss1() { return stoploss1; }
    public void setStoploss1(Double stoploss1) { this.stoploss1 = stoploss1; }

    public Double getStoploss2() { return stoploss2; }
    public void setStoploss2(Double stoploss2) { this.stoploss2 = stoploss2; }

    public Double getHardStoploss() { return hardStoploss; }
    public void setHardStoploss(Double hardStoploss) { this.hardStoploss = hardStoploss; }

    public String getTrailingMethod() { return trailingMethod; }
    public void setTrailingMethod(String trailingMethod) { this.trailingMethod = trailingMethod; }

    public Double getTrailingValue() { return trailingValue; }
    public void setTrailingValue(Double trailingValue) { this.trailingValue = trailingValue; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }

    public CloseReason getCloseReason() { return closeReason; }
    public void setCloseReason(CloseReason closeReason) { this.closeReason = closeReason; }

    public String getRuleVersion() { return ruleVersion; }
    public void setRuleVersion(String ruleVersion) { this.ruleVersion = ruleVersion; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String symbol;
        private String exchange;
        private Direction direction;
        private TradeDuration tradeDuration;
        private String timeframe;
        private Double entryPrice;
        private Double target1;
        private Double target2;
        private Double target3;
        private Double stoploss1;
        private Double stoploss2;
        private Double hardStoploss;
        private String trailingMethod;
        private Double trailingValue;
        private Status status = Status.ACTIVE;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();
        private LocalDateTime closedAt;
        private CloseReason closeReason;
        private String ruleVersion = "v1-supertrend-75low";
        private String metadata;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder symbol(String symbol) { this.symbol = symbol; return this; }
        public Builder exchange(String exchange) { this.exchange = exchange; return this; }
        public Builder direction(Direction direction) { this.direction = direction; return this; }
        public Builder tradeDuration(TradeDuration tradeDuration) { this.tradeDuration = tradeDuration; return this; }
        public Builder timeframe(String timeframe) { this.timeframe = timeframe; return this; }
        public Builder entryPrice(Double entryPrice) { this.entryPrice = entryPrice; return this; }
        public Builder target1(Double target1) { this.target1 = target1; return this; }
        public Builder target2(Double target2) { this.target2 = target2; return this; }
        public Builder target3(Double target3) { this.target3 = target3; return this; }
        public Builder stoploss1(Double stoploss1) { this.stoploss1 = stoploss1; return this; }
        public Builder stoploss2(Double stoploss2) { this.stoploss2 = stoploss2; return this; }
        public Builder hardStoploss(Double hardStoploss) { this.hardStoploss = hardStoploss; return this; }
        public Builder trailingMethod(String trailingMethod) { this.trailingMethod = trailingMethod; return this; }
        public Builder trailingValue(Double trailingValue) { this.trailingValue = trailingValue; return this; }
        public Builder status(Status status) { this.status = status; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder closedAt(LocalDateTime closedAt) { this.closedAt = closedAt; return this; }
        public Builder closeReason(CloseReason closeReason) { this.closeReason = closeReason; return this; }
        public Builder ruleVersion(String ruleVersion) { this.ruleVersion = ruleVersion; return this; }
        public Builder metadata(String metadata) { this.metadata = metadata; return this; }

        public TradeRecommendation build() {
            return new TradeRecommendation(id, symbol, exchange, direction, tradeDuration, timeframe,
                    entryPrice, target1, target2, target3, stoploss1, stoploss2, hardStoploss,
                    trailingMethod, trailingValue, status, createdAt, updatedAt, closedAt,
                    closeReason, ruleVersion, metadata);
        }
    }
}
