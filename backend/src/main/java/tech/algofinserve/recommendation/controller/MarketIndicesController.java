package tech.algofinserve.recommendation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.algofinserve.recommendation.model.dto.MarketIndicesDto;
import tech.algofinserve.recommendation.service.MarketIndicesService;

@RestController
@RequestMapping("/api")
public class MarketIndicesController {
    
    @Autowired
    private MarketIndicesService marketIndicesService;
    
    @GetMapping("/indices")
    public ResponseEntity<MarketIndicesDto> getMarketIndices() {
        try {
            MarketIndicesDto indices = marketIndicesService.getMarketIndices();
            return ResponseEntity.ok(indices);
        } catch (Exception e) {
            // Return fallback data on error
            MarketIndicesDto fallbackIndices = marketIndicesService.getMarketIndices();
            return ResponseEntity.ok(fallbackIndices);
        }
    }
    
    @GetMapping("/indices/market-status")
    public ResponseEntity<Boolean> getMarketStatus() {
        try {
            boolean isOpen = marketIndicesService.isMarketOpen();
            return ResponseEntity.ok(isOpen);
        } catch (Exception e) {
            return ResponseEntity.ok(false); // Default to closed on error
        }
    }
}
