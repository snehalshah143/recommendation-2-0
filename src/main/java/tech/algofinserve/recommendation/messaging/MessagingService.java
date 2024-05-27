package tech.algofinserve.recommendation.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;

public class MessagingService implements Runnable{
    BlockingQueue<String> myQueue;
public MessagingService(BlockingQueue<String> myQueue){this.myQueue=myQueue;}
//    @Async("taskExecutor")
    public void sendMessage(String message) throws InterruptedException {
        Thread.sleep(1000);
    TelegramMessaging.sendMessage2(message);
    }

    @Override
    public void run() {
        try {
            String message=myQueue.take();
            sendMessage(message);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
