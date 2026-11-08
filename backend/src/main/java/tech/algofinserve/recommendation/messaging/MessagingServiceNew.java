package tech.algofinserve.recommendation.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;


public class MessagingServiceNew implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(MessagingServiceNew.class);
    private final BlockingQueue<String> messageQueue;
    private final String chatId;
    private final TelegramMessagingNew telegramMessaging;

    private static final int SLEEP_BETWEEN_SENDS = 50; // minimal breathing room

    public MessagingServiceNew(
            BlockingQueue<String> messageQueue,
            String chatId,
            TelegramMessagingNew telegramMessaging
    ) {
        this.messageQueue = messageQueue;
        this.chatId = chatId;
        this.telegramMessaging = telegramMessaging;
        logger.info("MessagingService initialized for chat: " + chatId);
    }

    public void start() {
        Thread thread = new Thread(this, "MessagingService-" + chatId);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        try {
            while (true) {
                String msg = messageQueue.take();
                telegramMessaging.sendMessageAsync(chatId, msg);
                Thread.sleep(SLEEP_BETWEEN_SENDS);
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
