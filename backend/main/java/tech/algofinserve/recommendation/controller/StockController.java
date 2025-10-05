package tech.algofinserve.recommendation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.algofinserve.recommendation.model.dto.StockFundamentalsDto;
import tech.algofinserve.recommendation.service.StockService;

@RestController
@RequestMapping("/api/stocks")
public class StockController {
    
    @Autowired
    private StockService stockService;
    
    @GetMapping("/fundamentals/{symbol}")
    public ResponseEntity<StockFundamentalsDto> getStockFundamentals(@PathVariable String symbol) {
        try {
            StockFundamentalsDto fundamentals = stockService.getStockFundamentals(symbol.toUpperCase());
            return ResponseEntity.ok(fundamentals);
        } catch (Exception e) {
            // Return mock data if there's an error
            StockFundamentalsDto mockFundamentals = stockService.getStockFundamentals(symbol.toUpperCase());
            return ResponseEntity.ok(mockFundamentals);
        }
    }
}




