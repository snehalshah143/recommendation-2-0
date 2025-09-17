package tech.algofinserve.recommendation.helper;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import tech.algofinserve.recommendation.cache.ChartInkAlertFactory;
import tech.algofinserve.recommendation.model.domain.StockAlert;
import tech.algofinserve.recommendation.model.domain.StockAlertOutput;

public class StockAlertOutputHelper {

  public static List<StockAlertOutput> buildStockAlertOutputList() {
    List<StockAlertOutput> stockAlertOutputList = new LinkedList<>();

    for (Map.Entry<String, List<StockAlert>> entry :
        ChartInkAlertFactory.buyStockAlertListForStockNameMap.entrySet()) {

      for (StockAlert stockAlert : entry.getValue()) {
        StockAlertOutput stockAlertOutput = new StockAlertOutput();
        stockAlertOutput.setAlertDate(stockAlert.getAlertDate());
        stockAlertOutput.setStockCode(stockAlert.getStockCode());
        stockAlertOutput.setPrice(stockAlert.getPrice());
        stockAlertOutput.setBuySell(stockAlert.getBuySell());
        stockAlertOutput.setScanName(stockAlert.getScanName());
        stockAlertOutputList.add(stockAlertOutput);
      }
      if (null != ChartInkAlertFactory.sellStockAlertListForStockNameMap.get(entry.getKey())) {
        for (StockAlert stockAlert :
            ChartInkAlertFactory.sellStockAlertListForStockNameMap.get(entry.getKey())) {
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
