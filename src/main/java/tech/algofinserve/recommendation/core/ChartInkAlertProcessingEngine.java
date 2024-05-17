package tech.algofinserve.recommendation.core;

import org.springframework.stereotype.Component;
import tech.algofinserve.recommendation.cache.ChartInkAlertFactory;
import tech.algofinserve.recommendation.constants.BuySell;
import tech.algofinserve.recommendation.messaging.TelegramMessaging;
import tech.algofinserve.recommendation.model.domain.Alert;
import tech.algofinserve.recommendation.model.domain.StockAlert;


import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ChartInkAlertProcessingEngine {

    //Need to develop this class as multithreadin

    public void processBuyAlert(Alert alert){


        String[] stocksName=alert.getStocks().split(",");
        String[] prices=alert.getTriggerPrices().split(",");

       for(int i=0;i<stocksName.length;i++){

           StockAlert stockAlert = convertAlertToStockAlert(alert, stocksName, prices,i);

           if(ChartInkAlertFactory.stockAlertListForStockNameMap.containsKey(stockAlert.getStockCode())){
               ChartInkAlertFactory.stockAlertListForStockNameMap.get(stockAlert.getStockCode()).add(stockAlert);
               TelegramMessaging.sendMessage2("R::"+stockAlert.toString());
           }else{
               TelegramMessaging.sendMessage2(stockAlert.toString());
               List<StockAlert> stockAlertList=new CopyOnWriteArrayList<>();
               stockAlertList.add(stockAlert);
               ChartInkAlertFactory.stockAlertListForStockNameMap.put(stockAlert.getStockCode(),stockAlertList);
           }

           if(ChartInkAlertFactory.stockAlertListForScanNameMap.containsKey(stockAlert.getScanName())){
               ChartInkAlertFactory.stockAlertListForScanNameMap.get(stockAlert.getScanName()).add(stockAlert);
           }else{

               List<StockAlert> stockAlertList=new CopyOnWriteArrayList<>();
               stockAlertList.add(stockAlert);
               ChartInkAlertFactory.stockAlertListForScanNameMap.put(stockAlert.getScanName(),stockAlertList);
           }

       }

    }

    private static StockAlert convertAlertToStockAlert(Alert alert,String[] stocksName, String[] prices, int i) {
        String scanName = alert.getScanName();
        String[] triggeredAt=alert.getTriggerdAt().split(":");
        String hour=triggeredAt[0];
        String[] minutes=triggeredAt[1].split(" ");

        Date triggeredDate=new Date();
        triggeredDate.setMinutes(Integer.parseInt(minutes[0]));

        if(minutes[1].equals("am")){
            triggeredDate.setHours(Integer.parseInt(hour));
        }else if(minutes[1].equals("pm")){
            triggeredDate.setHours(Integer.parseInt(hour + 12));
        }

        StockAlert stockAlert=new StockAlert();
        stockAlert.setBuySell(BuySell.BUY);
        stockAlert.setAlertDate(triggeredDate);
        stockAlert.setPrice(prices[i]);
        stockAlert.setStockCode(stocksName[i]);
        stockAlert.setScanName(scanName);
        return stockAlert;
    }

}
