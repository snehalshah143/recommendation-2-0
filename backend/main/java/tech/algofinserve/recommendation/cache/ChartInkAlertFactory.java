package tech.algofinserve.recommendation.cache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import tech.algofinserve.recommendation.model.domain.StockAlert;

@Component
public class ChartInkAlertFactory {

  private void ChartInkAlertFactory() {}

  // scanName vrs stockAlert List
  public static Map<String, List<StockAlert>> buyStockAlertListForScanNameMap =
      new ConcurrentHashMap<>();

  // StockName NSECDOE vrs stockAlert
  public static Map<String, List<StockAlert>> buyStockAlertListForStockNameMap =
      new ConcurrentHashMap<>();

  // scanName vrs stockAlert List
  public static Map<String, List<StockAlert>> sellStockAlertListForScanNameMap =
      new ConcurrentHashMap<>();

  // StockName NSECDOE vrs stockAlert
  public static Map<String, List<StockAlert>> sellStockAlertListForStockNameMap =
      new ConcurrentHashMap<>();
}
