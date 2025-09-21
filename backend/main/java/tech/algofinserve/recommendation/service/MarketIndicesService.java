package tech.algofinserve.recommendation.service;

import tech.algofinserve.recommendation.model.dto.MarketIndicesDto;

public interface MarketIndicesService {
    
    /**
     * Get current market indices data including Nifty and Bank Nifty
     * @return MarketIndicesDto containing current prices and market status
     */
    MarketIndicesDto getMarketIndices();
    
    /**
     * Check if the market is currently open
     * @return true if market is open, false otherwise
     */
    boolean isMarketOpen();
    
    /**
     * Get the current Nifty 50 price
     * @return current Nifty price or null if unavailable
     */
    Double getNiftyPrice();
    
    /**
     * Get the current Bank Nifty price
     * @return current Bank Nifty price or null if unavailable
     */
    Double getBankNiftyPrice();
}
