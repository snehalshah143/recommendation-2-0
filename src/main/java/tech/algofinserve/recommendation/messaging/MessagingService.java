package tech.algofinserve.recommendation.messaging;

import java.util.concurrent.BlockingQueue;

public class MessagingService implements Runnable {
  BlockingQueue<String> messageQueue;
  TelegramMessaging telegramMessaging = new TelegramMessaging();

  public MessagingService(BlockingQueue<String> messageQueue) throws Exception {
    if (messageQueue == null) {
      throw new Exception("Queue is null");
    }
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
