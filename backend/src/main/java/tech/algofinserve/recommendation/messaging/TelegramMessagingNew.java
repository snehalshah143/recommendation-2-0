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
import java.util.concurrent.Semaphore;
@Service
public class TelegramMessagingNew {
  private static final Logger logger = LoggerFactory.getLogger(TelegramMessagingNew.class);

  // Tokens loaded from application.properties (telegram.bot.token1 through token5)
  private static volatile List<String> TELEGRAM_TOKENS = List.of();

  @org.springframework.beans.factory.annotation.Value("${telegram.bot.token1}")
  private String token1;
  @org.springframework.beans.factory.annotation.Value("${telegram.bot.token2}")
  private String token2;
  @org.springframework.beans.factory.annotation.Value("${telegram.bot.token3}")
  private String token3;
  @org.springframework.beans.factory.annotation.Value("${telegram.bot.token4:}")
  private String token4;
  @org.springframework.beans.factory.annotation.Value("${telegram.bot.token5:}")
  private String token5;

  @javax.annotation.PostConstruct
  public void initTokens() {
    TELEGRAM_TOKENS = java.util.Arrays.stream(new String[]{token1, token2, token3, token4, token5})
        .filter(t -> t != null && !t.isEmpty())
        .collect(java.util.stream.Collectors.toList());
    logger.info("Telegram tokens loaded: {} token(s) configured", TELEGRAM_TOKENS.size());
  }
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
  // 1 permit — strictly sequential sends, prevents burst 429
  private static final Semaphore SEND_SEMAPHORE = new Semaphore(1);
  // Global pause timestamp — when 429 received, all sends wait until this time
  private static volatile long pauseUntil = 0;

  // Default constructor (uses internal thread pool)
  public TelegramMessagingNew() {
    this(Executors.newFixedThreadPool(3));
  }

  // Spring-injected constructor
  public TelegramMessagingNew(@Qualifier("taskExecutorTelegramMessaging")
                              Executor executor) {

    this.sendExecutor = Executors.newFixedThreadPool(3); // 3 threads = 1 per bot token
    this.retryScheduler = Executors.newScheduledThreadPool(2);
    startRetryWorker();
  //  logger.info("✅ TelegramMessaging initialized using executor: " + executor.getClass().getSimpleName());
  }

  private static String getTelegramToken() {
    List<String> tokens = TELEGRAM_TOKENS;
    if (tokens.isEmpty()) throw new IllegalStateException("No Telegram tokens configured");
    int current = TOKEN_INDEX.getAndUpdate(i -> (i + 1) % tokens.size());
    return tokens.get(current);
  }

  public void sendMessageAsync(String chatId, String text) {
    sendExecutor.execute(() -> sendAsyncInternal(chatId, text));
  }

  private void sendAsyncInternal(String chatId, String text) {
    // If rate limited, wait until Telegram's window clears before sending
    long wait = pauseUntil - System.currentTimeMillis();
    if (wait > 0) {
      try {
        logger.info("Rate limited — pausing {}ms before next send", wait);
        Thread.sleep(wait);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
    }

    try {
      SEND_SEMAPHORE.acquire();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return;
    }

    String token = nextToken();
    String url = "https://api.telegram.org/bot" + token + "/sendMessage";

    RequestBody body = new FormBody.Builder()
            .add("chat_id", chatId)
            .add("text", text)
            .build();

    Request request = new Request.Builder().url(url).post(body).build();

    CLIENT.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        SEND_SEMAPHORE.release();
        FAILED.incrementAndGet();
        logger.error("Failed: " + e.getMessage());
        retryLater(chatId, text, 3);
      }

      @Override
      public void onResponse(Call call, Response response) {
        SEND_SEMAPHORE.release();
        int code = 0;
        try (response) {
          code = response.code();
          logger.info(String.valueOf(code));
          if (code == 200) {
            SUCCESS.incrementAndGet();
          } else if (code == 429) {
            int retryAfter = extractRetryAfter(response.body() != null ? response.body().string() : null);
            pauseUntil = System.currentTimeMillis() + retryAfter * 1000L;
            logger.info("Rate limit — pausing all sends for {}s", retryAfter);
            retryLater(chatId, text, retryAfter);
          } else if (code == 401) {
            FAILED.incrementAndGet();
            logger.error("HTTP 401 Unauthorized — bot token is invalid or revoked. Check telegram.bot.token1/2/3 in application.properties. Message dropped.");
          } else {
            FAILED.incrementAndGet();
            String errBody = response.body() != null ? response.body().string() : "null";
            logger.error("HTTP " + code + " body: " + errBody);
          }
        } catch (IOException e) {
          FAILED.incrementAndGet();
        }
        logger.info(code + ": success count :" + SUCCESS + " :failed :" + FAILED);
      }
    });
  }


  private static int extractRetryAfter(String body) {
    if (body == null) return 1;
    Matcher m = RETRY_AFTER_PATTERN.matcher(body);
    return m.find() ? Integer.parseInt(m.group(1)) : 1;
  }

  private String nextToken() {
    List<String> tokens = TELEGRAM_TOKENS;
    if (tokens.isEmpty()) throw new IllegalStateException("No Telegram tokens configured — check telegram.bot.token1/2/3 in application.properties");
    return tokens.get(TOKEN_INDEX.getAndUpdate(i -> (i + 1) % tokens.size()));
  }

  // === Retry logic ===
  private void retryLater(String chatId, String text, int delaySeconds) {
    retryQueue.add(new MessageTask(chatId, text, System.currentTimeMillis() + delaySeconds * 1000L));
  }

  private void startRetryWorker() {
    retryScheduler.scheduleAtFixedRate(() -> {
      long now = System.currentTimeMillis();
      MessageTask task = retryQueue.peek();
      if (task != null && now >= task.scheduledTime) {
        retryQueue.poll();
        sendExecutor.execute(() -> sendAsyncInternal(task.chatId, task.text));
      }
    }, 1, 1000, TimeUnit.MILLISECONDS);
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
