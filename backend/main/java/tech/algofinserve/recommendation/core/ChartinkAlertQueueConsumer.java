package tech.algofinserve.recommendation.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import tech.algofinserve.recommendation.model.domain.Alert;

import java.util.concurrent.BlockingQueue;

@Service
public class ChartinkAlertQueueConsumer {
    @Autowired
    private ChartInkAlertProcessingService alertProcessingService;

    @Autowired
    @Qualifier("buyAlertQueue")
    private BlockingQueue<Alert> buyAlertQueue;

    @Autowired
    @Qualifier("sellAlertQueue")
    private BlockingQueue<Alert> sellAlertQueue;

    @EventListener(ApplicationReadyEvent.class)
    public void startConsumers() {
        // Start background consumers
        new Thread(() -> consumeBuyAlerts()).start();
        new Thread(() -> consumeSellAlerts()).start();
        System.out.println("Chartink Alert Queue Consumers started...");
    }

    private void consumeBuyAlerts() {
        while (true) {
            try {
                Alert alert = buyAlertQueue.take();
                alertProcessingService.processBuyAlert(alert);  // delegate actual processing
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Error processing Buy Alert: " + e.getMessage());
            }
        }
    }

    private void consumeSellAlerts() {
        while (true) {
            try {
                Alert alert = sellAlertQueue.take();
                alertProcessingService.processSellAlert(alert);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Error processing Sell Alert: " + e.getMessage());
            }
        }
    }
}
