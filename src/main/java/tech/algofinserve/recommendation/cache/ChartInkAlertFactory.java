package tech.algofinserve.recommendation.cache;

import org.springframework.stereotype.Component;
import tech.algofinserve.recommendation.model.domain.StockAlert;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChartInkAlertFactory {


    private void ChartInkAlertDataFactory(){

    }
    //scanName vrs stockAlert List
 public static  Map<String, List<StockAlert>> stockAlertListForScanNameMap=new ConcurrentHashMap<>();

    //StockName NSECDOE vrs stockAlert
 public static  Map<String, List<StockAlert>> stockAlertListForStockNameMap=new ConcurrentHashMap<>();


}
