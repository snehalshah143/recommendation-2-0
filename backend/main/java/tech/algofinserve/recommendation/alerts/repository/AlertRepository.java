package tech.algofinserve.recommendation.alerts.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.algofinserve.recommendation.alerts.persistance.AlertEntity;

import java.time.Instant;
import java.util.List;
@Repository
public interface AlertRepository extends JpaRepository<AlertEntity, Long> {
    List<AlertEntity> findAllByOrderByAlertDateDesc(Pageable pageable);
    List<AlertEntity> findByStockCodeAndAlertDateAfterOrderByAlertDateDesc(String stockCode, Instant after);
}
