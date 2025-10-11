package tech.algofinserve.recommendation.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;


public class MessagingServiceNew implements Runnable {

    private final BlockingQueue<String> messageQueue;
    private final String chatId;
 //   private TelegramMessagingNew telegramMessaging;

    private TelegramSenderPool telegramSenderPool;
    private static final int BATCH_SIZE = 5;          // Max messages per Telegram send
    private static final int BATCH_WAIT_MS = 200;     // Wait up to 200ms to fill batch
    private static final int SLEEP_BETWEEN_BATCHES = 50; // 50ms pause between sends

    public MessagingServiceNew(
            BlockingQueue<String> messageQueue,
            String chatId,
            //TelegramMessagingNew telegramMessaging
            TelegramSenderPool telegramSenderPool
           // @Qualifier("taskExecutorTelegramMessaging") Executor taskExecutor
    ) {
        this.messageQueue = messageQueue;
        this.chatId=chatId;
        this.telegramSenderPool=telegramSenderPool;
     //   this.telegramMessaging=telegramMessaging;
        System.out.println("💬 MessagingService initialized for chat: " + chatId);

    }

    @PostConstruct
    public void start() {
        Thread thread = new Thread(this, "MessagingService-" + chatId);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        try {
            while (true) {
              //  String msg = takeBatch(messageQueue, 10);
                String msg = messageQueue.take();
    ;
             //   telegramMessaging.sendMessageAsync(chatId,msg);
                telegramSenderPool.sendAsync(chatId,msg);
                Thread.sleep(SLEEP_BETWEEN_BATCHES);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static String takeBatch(BlockingQueue<String> queue, int maxMessages) throws InterruptedException {
     //   StringBuilder sb = new StringBuilder();
        List<String> batch = new ArrayList<>();
        String first = queue.take();
        batch.add(first);
      //  sb.append(first);

        for (int i = 1; i < maxMessages; i++) {
            String msg = queue.poll(20, TimeUnit.MILLISECONDS);
            if (msg == null) break;
            batch.add(msg);
           // sb.append("\n\n").append(msg);
        }
        String combinedMsg = String.join("\n\n", batch);
        return combinedMsg;
    }
}
