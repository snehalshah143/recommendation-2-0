package tech.algofinserve.recommendation.service;

import org.springframework.stereotype.Service;
import tech.algofinserve.recommendation.constants.ExchSeg;
import tech.algofinserve.recommendation.constants.InstrumentType;
import tech.algofinserve.recommendation.model.domain.Ticker;

import java.util.HashMap;
import java.util.Map;

@Service
public class MetaDataServiceImpl implements MetaDataService {
    
    // Mock token mapping - in real implementation, this would come from database
    private static final Map<String, String> SYMBOL_TO_TOKEN_MAP = new HashMap<>();
    
    static {
        // Individual stocks
        SYMBOL_TO_TOKEN_MAP.put("RELIANCE", "2881");
        SYMBOL_TO_TOKEN_MAP.put("TCS", "2951");
        SYMBOL_TO_TOKEN_MAP.put("HDFC", "1333");
        SYMBOL_TO_TOKEN_MAP.put("INFY", "4081");
        SYMBOL_TO_TOKEN_MAP.put("HDFCBANK", "1333");
        SYMBOL_TO_TOKEN_MAP.put("ITC", "424");
        SYMBOL_TO_TOKEN_MAP.put("SBIN", "3045");
        SYMBOL_TO_TOKEN_MAP.put("BHARTIARTL", "2713");
        SYMBOL_TO_TOKEN_MAP.put("KOTAKBANK", "1922");
        SYMBOL_TO_TOKEN_MAP.put("LT", "11536");
        
        // Index tokens (from Angel Broking instrument master)
        SYMBOL_TO_TOKEN_MAP.put("NIFTY", "26000");      // Nifty 50 index token
        SYMBOL_TO_TOKEN_MAP.put("BANKNIFTY", "26009");  // Bank Nifty index token
    }
    
    @Override
    public Ticker getInstrumentTickerForStockName(String stockName, ExchSeg exchSeg) {
        Ticker ticker = new Ticker();
        ticker.setStockSymbol(stockName);
        ticker.setExchSeg(exchSeg);
        
        // Set instrument type based on symbol
        if ("NIFTY".equals(stockName) || "BANKNIFTY".equals(stockName)) {
            ticker.setInstrumentType(InstrumentType.INDEX); // Index type for NIFTY and BANKNIFTY
        } else {
            ticker.setInstrumentType(InstrumentType.EQ); // Equity type for individual stocks
        }
        
        ticker.setToken(getTokenForSymbol(stockName, exchSeg));
        return ticker;
    }
    
    @Override
    public String getTokenForSymbol(String symbol, ExchSeg exchSeg) {
        return SYMBOL_TO_TOKEN_MAP.getOrDefault(symbol.toUpperCase(), "1");
    }
}
