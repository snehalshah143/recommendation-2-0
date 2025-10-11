package tech.algofinserve.recommendation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.algofinserve.recommendation.constants.ExchSeg;
import tech.algofinserve.recommendation.service.AngelMarketDataService;
import tech.algofinserve.recommendation.service.MetaDataService;
import tech.algofinserve.recommendation.model.domain.Ticker;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @Autowired
    private AngelMarketDataService angelMarketDataService;
    
    @Autowired
    private MetaDataService metaDataService;
    
    @GetMapping("/prices")
    public ResponseEntity<Map<String, Object>> testAngelSmartAPIPrices() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Test Nifty price using Angel SmartAPI
            Ticker niftyTicker = metaDataService.getInstrumentTickerForStockName("NIFTY", ExchSeg.NSE);
            Double niftyPrice = angelMarketDataService.getLivePrice("NIFTY", ExchSeg.NSE);
            
            response.put("nifty", niftyPrice);
            response.put("nifty_source", niftyPrice != null ? "Angel SmartAPI" : "fallback");
            response.put("nifty_ticker", niftyTicker.getToken());
            
            // Test Bank Nifty price using Angel SmartAPI
            Ticker bankNiftyTicker = metaDataService.getInstrumentTickerForStockName("BANKNIFTY", ExchSeg.NSE);
            Double bankNiftyPrice = angelMarketDataService.getLivePrice("BANKNIFTY", ExchSeg.NSE);
            
            response.put("banknifty", bankNiftyPrice);
            response.put("banknifty_source", bankNiftyPrice != null ? "Angel SmartAPI" : "fallback");
            response.put("banknifty_ticker", bankNiftyTicker.getToken());
            
            response.put("timestamp", System.currentTimeMillis());
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("status", "error");
            return ResponseEntity.ok(response);
        }
    }
}
