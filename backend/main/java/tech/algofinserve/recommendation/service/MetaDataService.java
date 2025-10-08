package tech.algofinserve.recommendation.service;

import tech.algofinserve.recommendation.constants.ExchSeg;
import tech.algofinserve.recommendation.model.domain.Ticker;

public interface MetaDataService {
    
    Ticker getInstrumentTickerForStockName(String stockName, ExchSeg exchSeg);
    
    String getTokenForSymbol(String symbol, ExchSeg exchSeg);
}





