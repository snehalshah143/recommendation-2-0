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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

    public List<AlertDto> getStockHistory(String code, int days) {
        Instant after = Instant.now().minusSeconds(days * 24L * 3600L);
        return repo.findByStockCodeAndAlertDateAfterOrderByAlertDateDesc(code, after)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private AlertDto toDto(AlertEntity e) {
        return new AlertDto(e.getStockCode(), e.getPrice(), e.getAlertDate(), e.getScanName(), e.getBuySell());
    }

    private String formatMessage(AlertDto dto) {
        var f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());
        return dto.getBuySell() + " :: " + dto.getStockCode() + " @ " + dto.getPrice()
                + " ON " + f.format(dto.getAlertDate()) + " :: FOR :: " + dto.getScanName();
    }
}
