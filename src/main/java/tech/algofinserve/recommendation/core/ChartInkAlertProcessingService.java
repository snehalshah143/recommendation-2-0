package tech.algofinserve.recommendation.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import tech.algofinserve.recommendation.cache.ChartInkAlertFactory;
import tech.algofinserve.recommendation.constants.BuySell;
import tech.algofinserve.recommendation.messaging.MessagingService;
import tech.algofinserve.recommendation.model.domain.Alert;
import tech.algofinserve.recommendation.model.domain.StockAlert;


import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ChartInkAlertProcessingService {

    @Autowired
    BlockingQueue<String> messageQueue;
@EventListener(ApplicationReadyEvent.class)
public void startMessagingService(){
    new Thread(new MessagingService(messageQueue)).start();
    System.out.println("Messaging Service Started.....");
}


    @Async("taskExecutor")
    public void processBuyAlert(Alert alert) throws InterruptedException {


        String[] stocksName=alert.getStocks().split(",");
        String[] prices=alert.getTriggerPrices().split(",");

       for(int i=0;i<stocksName.length;i++){

           StockAlert stockAlert = convertAlertToStockAlert(alert, stocksName, prices,i);

           if(ChartInkAlertFactory.stockAlertListForStockNameMap.containsKey(stockAlert.getStockCode())){
               ChartInkAlertFactory.stockAlertListForStockNameMap.get(stockAlert.getStockCode()).add(stockAlert);
               String recommendation="R::"+stockAlert.toString();
               messageQueue.put(recommendation);
            //   TelegramMessaging.sendMessage2("R::"+stockAlert.toString());
           }else{
             //  TelegramMessaging.sendMessage2(stockAlert.toString());
               messageQueue.put(stockAlert.toString());
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

    private StockAlert convertAlertToStockAlert(Alert alert,String[] stocksName, String[] prices, int i) {
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
