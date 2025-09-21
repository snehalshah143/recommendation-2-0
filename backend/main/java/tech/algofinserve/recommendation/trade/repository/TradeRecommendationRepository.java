package tech.algofinserve.recommendation.trade.repository;

import tech.algofinserve.recommendation.trade.entity.TradeRecommendation;
import tech.algofinserve.recommendation.trade.entity.Direction;
import tech.algofinserve.recommendation.trade.entity.TradeDuration;
import tech.algofinserve.recommendation.trade.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for TradeRecommendation entity
 */
@Repository
public interface TradeRecommendationRepository extends JpaRepository<TradeRecommendation, Long> {

    /**
     * Find active recommendation for specific symbol, direction, and trade duration
     */
    Optional<TradeRecommendation> findBySymbolAndDirectionAndTradeDurationAndStatus(
        String symbol, Direction direction, TradeDuration tradeDuration, Status status
    );

    /**
     * Find all recommendations for a symbol with specific status
     */
    List<TradeRecommendation> findBySymbolAndStatus(String symbol, Status status);

    /**
     * Find all active recommendations
     */
    List<TradeRecommendation> findByStatus(Status status);

    /**
     * Find active recommendations for a specific symbol
     */
    List<TradeRecommendation> findBySymbolAndDirectionAndStatus(
        String symbol, Direction direction, Status status
    );

    /**
     * Find recommendations created after a specific time
     */
    List<TradeRecommendation> findByCreatedAtAfter(LocalDateTime createdAt);

    /**
     * Find recommendations by symbol and trade duration
     */
    List<TradeRecommendation> findBySymbolAndTradeDuration(String symbol, TradeDuration tradeDuration);

    /**
     * Count active recommendations for a symbol
     */
    long countBySymbolAndStatus(String symbol, Status status);

    /**
     * Find recommendations that need stoploss monitoring (active with stoploss values)
     */
    @Query("SELECT tr FROM TradeRecommendation tr WHERE tr.status = 'ACTIVE' " +
           "AND tr.stoploss1 IS NOT NULL AND tr.symbol = :symbol")
    List<TradeRecommendation> findActiveRecommendationsForMonitoring(@Param("symbol") String symbol);

    /**
     * Find all active recommendations for monitoring
     */
    @Query("SELECT tr FROM TradeRecommendation tr WHERE tr.status = 'ACTIVE' " +
           "AND tr.stoploss1 IS NOT NULL")
    List<TradeRecommendation> findAllActiveRecommendationsForMonitoring();
}
