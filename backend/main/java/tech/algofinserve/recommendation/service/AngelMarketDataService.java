package tech.algofinserve.recommendation.service;

import com.angelbroking.smartapi.SmartConnect;
import tech.algofinserve.recommendation.constants.ExchSeg;
import tech.algofinserve.recommendation.model.domain.Ticker;

public interface AngelMarketDataService {
    
    Double getLTPForTicker(SmartConnect smartConnect, Ticker ticker);
    
    Double getLivePrice(String symbol, ExchSeg exchSeg);
}
