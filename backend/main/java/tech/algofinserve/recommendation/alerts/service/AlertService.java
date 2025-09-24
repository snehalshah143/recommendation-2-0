package tech.algofinserve.recommendation.alerts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.algofinserve.recommendation.alerts.dto.AlertDto;
import tech.algofinserve.recommendation.alerts.persistance.AlertEntity;
import tech.algofinserve.recommendation.alerts.repository.AlertRepository;
import tech.algofinserve.recommendation.alerts.sse.SseEmitters;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AlertService {

    @Autowired private AlertRepository repo;
    @Autowired  private SseEmitters sseEmitters;


    @Transactional
    public void processIncomingAlert(AlertDto dto) {
        AlertEntity e = new AlertEntity();
        e.setStockCode(dto.getStockCode());
        e.setPrice(dto.getPrice());
        e.setAlertDate(dto.getAlertDate());
        e.setScanName(dto.getScanName());
        e.setBuySell(dto.getBuySell());
        
        // Calculate since days before saving
        int sinceDays = calculateSinceDays(dto.getStockCode(), dto.getBuySell());
        e.setSinceDays(sinceDays);
        
        repo.save(e);

        // send telegram
      //  telegram.send(formatMessage(dto));

        // broadcast via SSE
        sseEmitters.broadcast(dto);
    }

    public List<AlertDto> getRecentAlerts(int limit, int offset) {
        var page = PageRequest.of(offset / limit, Math.max(1, limit));
        return repo.findAllByOrderByAlertDateDesc(page)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<AlertDto> getStockHistory(String code, int days, int limit) {
        List<AlertEntity> alerts;
        
        // If days is 0 or negative, return all alerts for the stock
        if (days <= 0) {
            alerts = repo.findByStockCodeOrderByAlertDateDesc(code);
        } else {
            // Otherwise, filter by days
            Instant after = Instant.now().minusSeconds(days * 24L * 3600L);
            alerts = repo.findByStockCodeAndAlertDateAfterOrderByAlertDateDesc(code, after);
        }
        
        // Apply limit and convert to DTOs
        return alerts.stream()
                .limit(limit)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private AlertDto toDto(AlertEntity e) {
        return new AlertDto(e.getStockCode(), e.getPrice(), e.getAlertDate(), e.getScanName(), e.getBuySell(), e.getSinceDays());
    }

    private String formatMessage(AlertDto dto) {
        var f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());
        return dto.getBuySell() + " :: " + dto.getStockCode() + " @ " + dto.getPrice()
                + " ON " + f.format(dto.getAlertDate()) + " :: FOR :: " + dto.getScanName();
    }

    /**
     * Recalculate since days for all existing alerts
     * This method should be called once to update existing data
     */
    @Transactional
    public void recalculateAllSinceDays() {
        List<AlertEntity> allAlerts = repo.findAll();
        for (AlertEntity alert : allAlerts) {
            int sinceDays = calculateSinceDays(alert.getStockCode(), alert.getBuySell());
            alert.setSinceDays(sinceDays);
            repo.save(alert);
        }
    }

    /**
     * Calculate "Since X days" for a stock based on consecutive days of same action
     * Logic: Count consecutive days with the same action, return (consecutiveDays - 1)
     * Example: 1 day = since 0 days, 2 days = since 1 day, 3 days = since 2 days
     */
    private int calculateSinceDays(String stockCode, tech.algofinserve.recommendation.constants.BuySell currentAction) {
        // Get all alerts for this stock, sorted by alert_date (newest first)
        List<AlertEntity> stockAlerts = repo.findByStockCodeOrderByAlertDateDesc(stockCode);
        
        if (stockAlerts.isEmpty()) {
            return 0;
        }
        
        // Group alerts by date (YYYY-MM-DD format)
        Map<LocalDate, List<AlertEntity>> alertsByDate = new HashMap<>();
        for (AlertEntity alert : stockAlerts) {
            LocalDate alertDate = alert.getAlertDate().atZone(ZoneId.systemDefault()).toLocalDate();
            alertsByDate.computeIfAbsent(alertDate, k -> new ArrayList<>()).add(alert);
        }
        
        // Find consecutive days with the current action (from most recent backwards)
        List<LocalDate> sortedDates = alertsByDate.keySet().stream()
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toList());
        
        int consecutiveDays = 0;
        
        for (LocalDate date : sortedDates) {
            List<AlertEntity> dayAlerts = alertsByDate.get(date);
            boolean hasCurrentAction = dayAlerts.stream().anyMatch(alert -> alert.getBuySell() == currentAction);
            boolean hasDifferentAction = dayAlerts.stream().anyMatch(alert -> alert.getBuySell() != currentAction);
            
            if (hasCurrentAction && !hasDifferentAction) {
                // Day has only the current action (no mixed actions)
                consecutiveDays++;
            } else if (hasDifferentAction) {
                // Day has different action - this breaks the consecutive streak
                break;
            } else {
                // Day doesn't have the current action - this breaks the consecutive streak
                break;
            }
        }
        
        // Return consecutive days - 1
        return Math.max(0, consecutiveDays - 1);
    }
}
