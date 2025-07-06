package tech.algofinserve.recommendation.messaging;

import java.util.concurrent.BlockingQueue;
import java.util.function.Function;

public class MessagingService implements Runnable {
  BlockingQueue<String> messageQueue;
  TelegramMessaging telegramMessaging = new TelegramMessaging();
  Function sendMessage;

  public MessagingService(BlockingQueue<String> messageQueue, Function sendMessage)
      throws Exception {
    if (messageQueue == null) {
      throw new Exception("Queue is null");
    }
    this.messageQueue = messageQueue;
    this.sendMessage = sendMessage;
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
        sendMessage.apply(message);
        //  sendMessage(message);
      }

    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
