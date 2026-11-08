package tech.algofinserve.recommendation.helper;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import tech.algofinserve.recommendation.cache.RedisAlertCache;
import tech.algofinserve.recommendation.model.domain.StockAlert;
import tech.algofinserve.recommendation.model.domain.StockAlertOutput;

public class StockAlertOutputHelper {

  public static List<StockAlertOutput> buildStockAlertOutputList(RedisAlertCache redisAlertCache) {
    List<StockAlertOutput> stockAlertOutputList = new LinkedList<>();

    Map<String, List<StockAlert>> buyAlerts = redisAlertCache.getAllBuyStockAlerts();

    for (Map.Entry<String, List<StockAlert>> entry : buyAlerts.entrySet()) {

      for (StockAlert stockAlert : entry.getValue()) {
        StockAlertOutput stockAlertOutput = new StockAlertOutput();
        stockAlertOutput.setAlertDate(stockAlert.getAlertDate());
        stockAlertOutput.setStockCode(stockAlert.getStockCode());
        stockAlertOutput.setPrice(stockAlert.getPrice());
        stockAlertOutput.setBuySell(stockAlert.getBuySell());
        stockAlertOutput.setScanName(stockAlert.getScanName());
        stockAlertOutputList.add(stockAlertOutput);
      }

      List<StockAlert> sellAlerts = redisAlertCache.getSellStockAlerts(entry.getKey());
      if (sellAlerts != null && !sellAlerts.isEmpty()) {
        for (StockAlert stockAlert : sellAlerts) {
          StockAlertOutput stockAlertOutput = new StockAlertOutput();
          stockAlertOutput.setAlertDate(stockAlert.getAlertDate());
          stockAlertOutput.setStockCode(stockAlert.getStockCode());
          stockAlertOutput.setPrice(stockAlert.getPrice());
          stockAlertOutput.setBuySell(stockAlert.getBuySell());
          stockAlertOutput.setScanName(stockAlert.getScanName());
          stockAlertOutputList.add(stockAlertOutput);
        }
      }
    }

    return stockAlertOutputList;
  }
}
