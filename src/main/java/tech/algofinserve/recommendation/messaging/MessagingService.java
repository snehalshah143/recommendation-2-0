package tech.algofinserve.recommendation.messaging;


import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class MessagingService implements Runnable {
  BlockingQueue<String> messageQueue;
  TelegramMessaging telegramMessaging = new TelegramMessaging();

  public MessagingService(BlockingQueue<String> messageQueue) throws IOException {
    this.messageQueue = messageQueue;
  }
  //    @Async("taskExecutor")
  public void sendMessage(String message) throws InterruptedException {
    telegramMessaging.sendMessage2(message);
  }

  @Override
  public void run() {

    try {
      while (true) {
        String message = messageQueue.take();
        sendMessage(message);
      }

    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
