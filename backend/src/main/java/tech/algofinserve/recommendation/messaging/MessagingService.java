package tech.algofinserve.recommendation.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class MessagingService implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(MessagingService.class);
  BlockingQueue<String> messageQueue;
//  TelegramMessaging telegramMessaging = new TelegramMessaging();
  Function sendMessage;

  public MessagingService(BlockingQueue<String> messageQueue, Function sendMessage)
      throws Exception {
    if (messageQueue == null) {
      throw new Exception("Queue is null");
    }
    this.messageQueue = messageQueue;
    this.sendMessage = sendMessage;
    logger.info("MessagingService Initialised ...");
  }
/*  //    @Async("taskExecutor")
  public void sendMessage(String message) throws InterruptedException {
    telegramMessaging.sendMessage2(message);
  }*/

  public static String takeBatch(BlockingQueue<String> queue, int maxMessages) throws InterruptedException {
    StringBuilder sb = new StringBuilder();

    // Take at least one message (blocking)
    String first = queue.take();
    sb.append(first);

    // Now try to take the remaining messages without blocking
    for (int i = 1; i < maxMessages; i++) {
      String msg = queue.poll(10, TimeUnit.MILLISECONDS); // small timeout to avoid busy wait
      if (msg == null) break;
      sb.append("\n\n").append(msg); // append empty line between messages

    }

    return sb.toString();
  }

  @Override
  public void run() {

    try {
      while (true) {
        String message = messageQueue.take();
     //   String message = takeBatch(messageQueue, 10);
        sendMessage.apply(message);
        //  sendMessage(message);
      }

    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
