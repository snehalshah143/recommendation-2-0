package tech.algofinserve.recommendation.alerts.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tech.algofinserve.recommendation.alerts.dto.AlertDto;
import tech.algofinserve.recommendation.alerts.service.AlertService;
import tech.algofinserve.recommendation.alerts.sse.SseEmitters;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    @Autowired
    private AlertService service;
    @Autowired private SseEmitters sseEmitters;


    // existing Chartink webhook -> POST to this endpoint
    @PostMapping("/webhook")
    public ResponseEntity<Void> receiveWebhook(@RequestBody AlertDto dto) {
        service.processIncomingAlert(dto);
        return ResponseEntity.accepted().build();
    }

    // client subscribes to real-time alerts
    @GetMapping("/stream")
    public SseEmitter stream() {
        return sseEmitters.createEmitter();
    }

    // recent alerts with pagination
    @GetMapping
    public List<AlertDto> recent(@RequestParam(defaultValue = "50") int limit, 
                                 @RequestParam(defaultValue = "0") int offset) {
        return service.getRecentAlerts(limit, offset);
    }

    // history for a stock (days default 7)
    @GetMapping("/stock/{code}")
    public List<AlertDto> history(@PathVariable String code, @RequestParam(defaultValue = "7") int days) {
        return service.getStockHistory(code, days);
    }

    // recalculate since days for all existing alerts
    @PostMapping("/recalculate-since-days")
    public ResponseEntity<String> recalculateSinceDays() {
        service.recalculateAllSinceDays();
        return ResponseEntity.ok("Since days recalculated for all alerts");
    }
}
