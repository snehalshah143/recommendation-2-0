package tech.algofinserve.recommendation.messaging;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Service
public class TelegramMessagingNew {
  private static final Logger logger = LoggerFactory.getLogger(TelegramMessagingNew.class);
  static String ideas2InvestBotTelegramToken = "6552278371:AAHhYOrBcC1ccls6BVTwF9UoOjFjc8Zj9p8";
  static String ideas2Invest2BotTelegramToken = "8334294677:AAEyr8gzPNEN9h2Y8jQfWWuU2d0AGInnxKU";
  static String ideas2Invest3BotTelegramToken = "8323993449:AAHGPMyou9JAKpgkh__c-UttS-pzOIbSCCE";
  private static final List<String> TELEGRAM_TOKENS = List.of(
          ideas2InvestBotTelegramToken,
          ideas2Invest2BotTelegramToken,
          ideas2Invest3BotTelegramToken
  );
  private static final int TOKEN_COUNT = TELEGRAM_TOKENS.size();

  private static final AtomicInteger TOKEN_INDEX = new AtomicInteger(0);

  private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
          .connectionPool(new ConnectionPool(50, 5, TimeUnit.MINUTES))
          .writeTimeout(Duration.ofSeconds(5))
          .readTimeout(Duration.ofSeconds(5))
          .callTimeout(Duration.ofSeconds(10))
          .build();

  // === Queues & Executors ===
  private final ExecutorService sendExecutor;
  private final ScheduledExecutorService retryScheduler;

  private final Queue<MessageTask> retryQueue = new ConcurrentLinkedQueue<>();


  private final String defaultChatId = "@ideastoinvest";
  private static final Pattern RETRY_AFTER_PATTERN = Pattern.compile("\"retry_after\":(\\d+)");

  private static final AtomicInteger SUCCESS = new AtomicInteger(0);
  private static final AtomicInteger FAILED = new AtomicInteger(0);
  // Default constructor (uses internal thread pool)
  public TelegramMessagingNew() {
    this(Executors.newFixedThreadPool(6));
  }

  // Spring-injected constructor
  public TelegramMessagingNew(@Qualifier("taskExecutorTelegramMessaging")
                              Executor executor) {

    this.sendExecutor = Executors.newFixedThreadPool(8);
    this.retryScheduler = Executors.newScheduledThreadPool(2);
    startRetryWorker();
  //  logger.info("✅ TelegramMessaging initialized using executor: " + executor.getClass().getSimpleName());
  }

  private static String getTelegramToken() {
    int current = TOKEN_INDEX.getAndUpdate(i -> (i + 1) % TELEGRAM_TOKENS.size());
    return TELEGRAM_TOKENS.get(current);
  }

  public void sendMessageAsync(String chatId, String text) {
    sendExecutor.execute(() -> sendAsyncInternal(chatId, text));
  }

  private void sendAsyncInternal(String chatId, String text) {
    String token = nextToken();
 //   logger.info("Telegram Token Used::"+token);
    String url = "https://api.telegram.org/bot" + token + "/sendMessage";

    RequestBody body = new FormBody.Builder()
            .add("chat_id", chatId)
            .add("text", text)
            .build();

    Request request = new Request.Builder().url(url).post(body).build();

    CLIENT.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        FAILED.incrementAndGet();
        logger.error("🚨 Failed: " + e.getMessage());
        retryLater(chatId, text, 3);
      }

      @Override
      public void onResponse(Call call, Response response) {
        int code=0;
        try (response) {
          code = response.code();
          logger.info(String.valueOf(code));
          if (code == 200) {
            SUCCESS.incrementAndGet();
          } else if (code == 429) {
            int retryAfter = extractRetryAfter(response.body() != null ? response.body().string() : null);
            logger.info("⏳ Rate limit, retry in " + retryAfter + "s");
            retryLater(chatId, text, retryAfter);
          } else {
            FAILED.incrementAndGet();
            logger.error("❌ HTTP " + code);
          }
        } catch (IOException e) {
          FAILED.incrementAndGet();
        }
        logger.info(code+": success count :"+SUCCESS +" :failed :"+FAILED);
      }
    });
  }


  private static int extractRetryAfter(String body) {
    if (body == null) return 1;
    Matcher m = RETRY_AFTER_PATTERN.matcher(body);
    return m.find() ? Integer.parseInt(m.group(1)) : 1;
  }

  private String nextToken() {
    return TELEGRAM_TOKENS.get(TOKEN_INDEX.getAndUpdate(i -> (i + 1) % TOKEN_COUNT));
  }

  // === Retry logic ===
  private void retryLater(String chatId, String text, int delaySeconds) {
    retryQueue.add(new MessageTask(chatId, text, System.currentTimeMillis() + delaySeconds * 1000L));
  }

  private void startRetryWorker() {
    retryScheduler.scheduleAtFixedRate(() -> {
      long now = System.currentTimeMillis();
      while (!retryQueue.isEmpty()) {
        MessageTask task = retryQueue.peek();
        if (task != null && now >= task.scheduledTime) {
          retryQueue.poll();
          sendExecutor.execute(() -> sendAsyncInternal(task.chatId, task.text));
        } else break;
      }
    }, 1, 500, TimeUnit.MILLISECONDS);
  }

  private static class MessageTask {
    final String chatId;
    final String text;
    final long scheduledTime;
    MessageTask(String chatId, String text, long scheduledTime) {
      this.chatId = chatId;
      this.text = text;
      this.scheduledTime = scheduledTime;
    }
  }

}
