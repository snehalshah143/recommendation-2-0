package tech.algofinserve.recommendation.alerts.service;
import org.springframework.context.event.EventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AlertService {

    @Autowired private AlertRepository repo;
    @Autowired  private SseEmitters sseEmitters;

    @Autowired @Qualifier("dbQueue") private BlockingQueue<AlertDto> dbQueue;

    @EventListener(ApplicationReadyEvent.class)
    public void startDbQueueConsumer() {
        System.out.println("Starting DB Queue Consumer for Alerts...");
        new Thread(this::consumeDbQueue).start();
    }

    @Async("taskExecutorDB") // optional: can use a TaskExecutor bean
    public void consumeDbQueue() {
        while (true) {
            try {
             //   AlertDto dto = dbQueue.take(); // Blocks until available
             //   processIncomingAlert(dto);
                List<AlertDto> dtoList= takeBatch(dbQueue,10);
                processIncomingAlertInBatch(dtoList);
            } catch (Exception e) {
                System.err.println("Error processing dbQueue alert: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Transactional
    public void processIncomingAlert(AlertDto dto) {
        AlertEntity e = new AlertEntity();
        e.setStockCode(dto.getStockCode());
        e.setPrice(dto.getPrice());
        e.setAlertDate(dto.getAlertDate());
        e.setScanName(dto.getScanName());
        e.setBuySell(dto.getBuySell());
        e.setSinceDays(0); // Will be calculated later
        
        // Save the alert first
        AlertEntity savedAlert = repo.save(e);
        
        // Now calculate since days including this alert and update
        int sinceDays = calculateSinceDaysIncludingCurrent(dto.getStockCode(), dto.getBuySell(), dto.getAlertDate());
        savedAlert.setSinceDays(sinceDays);
        repo.save(savedAlert);

        // send telegram
      //  telegram.send(formatMessage(dto));

        // broadcast via SSE
        sseEmitters.broadcast(dto);
    }

    @Transactional
    public void processIncomingAlertInBatch(List<AlertDto> dtoList) {
        List<AlertEntity> alertEntityList=new ArrayList<>();
        for(AlertDto dto:dtoList){
            AlertEntity e = new AlertEntity();
            e.setStockCode(dto.getStockCode());
            e.setPrice(dto.getPrice());
            e.setAlertDate(dto.getAlertDate());
            e.setScanName(dto.getScanName());
            e.setBuySell(dto.getBuySell());
            e.setSinceDays(0); // Will be calculated later
            alertEntityList.add(e);
        }

        // Save the alert first
        List<AlertEntity> savedAlertList = repo.saveAll(alertEntityList);

        // Now calculate since days including this alert and update
   //     int sinceDays = calculateSinceDaysIncludingCurrent(dto.getStockCode(), dto.getBuySell(), dto.getAlertDate());
   //     savedAlert.setSinceDays(sinceDays);
   //     repo.save(savedAlert);

        // send telegram
        //  telegram.send(formatMessage(dto));

        // broadcast via SSE
        dtoList.forEach(p->sseEmitters.broadcast(p));
    }


    public static List<AlertDto>  takeBatch(BlockingQueue<AlertDto> queue, int maxAlerts) throws InterruptedException {
        StringBuilder sb = new StringBuilder();
List<AlertDto> alerts =new ArrayList<>();
        // Take at least one message (blocking)
        AlertDto first = queue.take();
        alerts.add(first);

        // Now try to take the remaining messages without blocking
        for (int i = 1; i < maxAlerts; i++) {
            AlertDto dto = queue.poll(10, TimeUnit.MILLISECONDS); // small timeout to avoid busy wait
            if (dto == null) break;
            alerts.add(dto); // append empty line between messages

        }

        return alerts;
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
        
        // Start from today and check consecutive calendar days backwards
        LocalDate today = LocalDate.now();
        int consecutiveDays = 0;
        LocalDate checkDate = today;
        
        // Check consecutive days starting from today
        while (alertsByDate.containsKey(checkDate)) {
            List<AlertEntity> dayAlerts = alertsByDate.get(checkDate);
            boolean hasCurrentAction = dayAlerts.stream().anyMatch(alert -> alert.getBuySell() == currentAction);
            boolean hasDifferentAction = dayAlerts.stream().anyMatch(alert -> alert.getBuySell() != currentAction);
            
            if (hasCurrentAction && !hasDifferentAction) {
                // Day has only the current action (no mixed actions)
                consecutiveDays++;
                checkDate = checkDate.minusDays(1); // Check previous day
            } else if (hasDifferentAction) {
                // Day has different action - this breaks the consecutive streak
                break;
            } else {
                // Day doesn't have the current action - this breaks the consecutive streak
                break;
            }
        }
        
        // Return consecutive days - 1 (so 1 day = since 0, 2 days = since 1, etc.)
        return Math.max(0, consecutiveDays - 1);
    }

    /**
     * Calculate since days including the current alert being processed
     */
    private int calculateSinceDaysIncludingCurrent(String stockCode, tech.algofinserve.recommendation.constants.BuySell currentAction, Instant currentAlertDate) {
        // Get all alerts for this stock, sorted by alert_date (newest first)
        List<AlertEntity> stockAlerts = repo.findByStockCodeOrderByAlertDateDesc(stockCode);
        
        // Convert current alert date to LocalDate
        LocalDate currentDate = currentAlertDate.atZone(ZoneId.systemDefault()).toLocalDate();
        
        // Group alerts by date (YYYY-MM-DD format)
        Map<LocalDate, List<AlertEntity>> alertsByDate = new HashMap<>();
        for (AlertEntity alert : stockAlerts) {
            LocalDate alertDate = alert.getAlertDate().atZone(ZoneId.systemDefault()).toLocalDate();
            alertsByDate.computeIfAbsent(alertDate, k -> new ArrayList<>()).add(alert);
        }
        
        // Count consecutive days starting from current date backwards
        int consecutiveDays = 0;
        LocalDate checkDate = currentDate;
        
        // Check if current date has the action (it should, since we just added it)
        List<AlertEntity> todayAlerts = alertsByDate.get(checkDate);
        if (todayAlerts != null) {
            boolean hasTodayAction = todayAlerts.stream().anyMatch(alert -> alert.getBuySell() == currentAction);
            boolean hasTodayDifferentAction = todayAlerts.stream().anyMatch(alert -> alert.getBuySell() != currentAction);
            
            if (hasTodayAction && !hasTodayDifferentAction) {
                consecutiveDays = 1; // Today counts as 1 day
            } else if (hasTodayAction && hasTodayDifferentAction) {
                return 0; // Mixed actions today, so since 0 days
            }
        }
        
        // Check previous days
        checkDate = checkDate.minusDays(1);
        while (alertsByDate.containsKey(checkDate)) {
            List<AlertEntity> dayAlerts = alertsByDate.get(checkDate);
            boolean hasCurrentAction = dayAlerts.stream().anyMatch(alert -> alert.getBuySell() == currentAction);
            boolean hasDifferentAction = dayAlerts.stream().anyMatch(alert -> alert.getBuySell() != currentAction);
            
            if (hasCurrentAction && !hasDifferentAction) {
                consecutiveDays++;
                checkDate = checkDate.minusDays(1);
            } else {
                break; // Different action or no action breaks the streak
            }
        }
        
        // Return consecutive days - 1 (since 1 day = since 0 days)
        return Math.max(0, consecutiveDays - 1);
    }
    
    public String debugSinceDaysCalculation(String stockCode) {
        List<AlertEntity> stockAlerts = repo.findByStockCodeOrderByAlertDateDesc(stockCode);
        StringBuilder debug = new StringBuilder();
        
        debug.append("=== DEBUG FOR ").append(stockCode).append(" ===\n");
        debug.append("Total alerts: ").append(stockAlerts.size()).append("\n\n");
        
        if (stockAlerts.isEmpty()) {
            debug.append("No alerts found for this stock\n");
            return debug.toString();
        }
        
        // Show all alerts with their dates and actions
        debug.append("All alerts (newest first):\n");
        for (int i = 0; i < stockAlerts.size(); i++) {
            AlertEntity alert = stockAlerts.get(i);
            LocalDate alertDate = alert.getAlertDate().atZone(ZoneId.systemDefault()).toLocalDate();
            debug.append(String.format("%d. %s - %s - %s (sinceDays: %d)\n", 
                i+1, alertDate, alert.getBuySell(), alert.getScanName(), alert.getSinceDays()));
        }
        
        // Group by date and show the logic
        Map<LocalDate, List<AlertEntity>> alertsByDate = new HashMap<>();
        for (AlertEntity alert : stockAlerts) {
            LocalDate alertDate = alert.getAlertDate().atZone(ZoneId.systemDefault()).toLocalDate();
            alertsByDate.computeIfAbsent(alertDate, k -> new ArrayList<>()).add(alert);
        }
        
        debug.append("\nGrouped by date:\n");
        List<LocalDate> sortedDates = alertsByDate.keySet().stream()
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toList());
                
        for (LocalDate date : sortedDates) {
            List<AlertEntity> dayAlerts = alertsByDate.get(date);
            debug.append(String.format("%s: ", date));
            for (AlertEntity alert : dayAlerts) {
                debug.append(alert.getBuySell()).append(" ");
            }
            debug.append("\n");
        }
        
        // Calculate for BUY action as example
        if (!stockAlerts.isEmpty()) {
            debug.append("\nCalculating for BUY action:\n");
            int buyResult = calculateSinceDays(stockCode, tech.algofinserve.recommendation.constants.BuySell.BUY);
            debug.append("BUY sinceDays result: ").append(buyResult).append("\n");
            
            debug.append("\nCalculating for SELL action:\n");
            int sellResult = calculateSinceDays(stockCode, tech.algofinserve.recommendation.constants.BuySell.SELL);
            debug.append("SELL sinceDays result: ").append(sellResult).append("\n");
        }
        
        return debug.toString();
    }
}
