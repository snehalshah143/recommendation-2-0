package tech.algofinserve.recommendation.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;

public class MessagingService implements Runnable{
    BlockingQueue<String> messageQueue;
public MessagingService(BlockingQueue<String> messageQueue){this.messageQueue=messageQueue;}
//    @Async("taskExecutor")
    public void sendMessage(String message) throws InterruptedException {
    TelegramMessaging.sendMessage2(message);
    }

    @Override
    public void run() {

        try {
            while(true) {
                String message = messageQueue.take();
                sendMessage(message);
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
