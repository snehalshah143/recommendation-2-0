package tech.algofinserve.recommendation.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;

@Service
public class MessagingService {
    @Autowired
    BlockingQueue<String> myQueue;

    @Async("taskExecutor")
    public void sendMessage() throws InterruptedException {
    TelegramMessaging.sendMessage2(myQueue.take());
    }

}
