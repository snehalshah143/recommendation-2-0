package tech.algofinserve.recommendation.messaging;

import java.io.*;
import java.net.*;
import okhttp3.*;

public class TelegramMessaging {
  // static String telegramToken = "6552278371:AAHhYOrBcC1ccls6BVTwF9UoOjFjc8Zj9p8";
  // static String CHAT_ID = "873305334";
  String telegramToken = "6552278371:AAHhYOrBcC1ccls6BVTwF9UoOjFjc8Zj9p8";
  // String chatId = "@shreejitrades";

  /*  public static void main(String[] args) {

     //  TelegramMessaging.getUpdates();
  //   TelegramMessaging.sendMessage2("1234");
   }*/

  /*public static void sendMessage() {
      String urlString = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s";

      String text = "Hello world!";

      urlString = String.format(urlString, telegramToken, CHAT_ID, text);

      try {
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        InputStream is = new BufferedInputStream(conn.getInputStream());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  */
  public void getUpdates() {
    OkHttpClient client = new OkHttpClient();

    Request request =
        new Request.Builder()
            .url("https://api.telegram.org/bot" + telegramToken + "/getUpdates")
            .build();

    try {
      Response response = client.newCall(request).execute();
      String responseBody = response.body().string();
      System.out.println(responseBody);

      // Parse the response to get chat IDs
      // Example: Extract chat ID from responseBody and use it
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /*  public URL url;
   public HttpURLConnection conn;
   public OutputStream outputStream;

   public TelegramMessaging() throws IOException {
     url = new URL("https://api.telegram.org/bot" + telegramToken + "/sendMessage");
     conn = (HttpURLConnection) url.openConnection();

     conn.setRequestMethod("POST");
     conn.setDoOutput(true);
  //   conn.setRequestProperty("Connection", "Keep-Alive");
     outputStream = conn.getOutputStream();
   }*/

  public boolean sendMessageEOD(String text) {
    // String telegramToken = "6552278371:AAHhYOrBcC1ccls6BVTwF9UoOjFjc8Zj9p8";
    // String chatId = "-1001565809937";
    // String chatId = "@AlGoStationBySnehal";
    // String chatId = "@shreejitrades";

    //   String text = "Hello world!";

    String chatId = "@ideastoinvest";
    // String chatId = "@shreejitrades";
    try {

      URL url = new URL("https://api.telegram.org/bot" + telegramToken + "/sendMessage");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);

      //    conn.setRequestProperty("Content-Type", "application/json");
      //    String jsonInputString = "{\"chat_id\": \"" + chatId + "\", \"text\": \"" + text +
      // "\"}";

      StringBuilder sb = new StringBuilder();
      sb.append("chat_id=").append(URLEncoder.encode(chatId, "UTF-8"));
      sb.append("&text=").append(URLEncoder.encode(text, "UTF-8"));

      try (OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream())) {
        //   try (OutputStreamWriter wr = new OutputStreamWriter(outputStream)) {
        wr.write(sb.toString());
        wr.flush();
        wr.close();
      }

      System.out.println(conn.getResponseCode());

      /*      System.out.println(
      "Thread ::" + Thread.currentThread().getName() + "::" + conn.getResponseCode());*/
      conn.disconnect();
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  public boolean sendMessage2(String text) {
    // String telegramToken = "6552278371:AAHhYOrBcC1ccls6BVTwF9UoOjFjc8Zj9p8";
    // String chatId = "-1001565809937";
    // String chatId = "@AlGoStationBySnehal";
    // String chatId = "@shreejitrades";

    //   String text = "Hello world!";

    // String chatId = "@ideastoinvest";
    String chatId = "@shreejitrades";
    try {

      URL url = new URL("https://api.telegram.org/bot" + telegramToken + "/sendMessage");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);

      //    conn.setRequestProperty("Content-Type", "application/json");
      //    String jsonInputString = "{\"chat_id\": \"" + chatId + "\", \"text\": \"" + text +
      // "\"}";

      StringBuilder sb = new StringBuilder();
      sb.append("chat_id=").append(URLEncoder.encode(chatId, "UTF-8"));
      sb.append("&text=").append(URLEncoder.encode(text, "UTF-8"));

      try (OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream())) {
        //   try (OutputStreamWriter wr = new OutputStreamWriter(outputStream)) {
        wr.write(sb.toString());
        wr.flush();
        wr.close();
      }

      System.out.println(conn.getResponseCode());

      /*      System.out.println(
      "Thread ::" + Thread.currentThread().getName() + "::" + conn.getResponseCode());*/
      conn.disconnect();
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  /*  protected void finalize() throws Throwable {
    System.out.println("Finalized Method Executed.");
    conn.disconnect();
  }*/
  /*
  public void sendDocument(String filePath) {
    // String telegramToken = "6552278371:AAHhYOrBcC1ccls6BVTwF9UoOjFjc8Zj9p8";
    // String chatId = "-1001565809937";
    String chatId = "@AlGoStationBySnehal";
    // String chatId = "@shreejitrades";
    //  String chatId = "@ideastoinvest";
    //   String text = "Hello world!";
    String filePath1 = "D:\\Report\\Chartink\\chartink_report_DDMMYYYY.csv";
    try {
      URL url = new URL("https://api.telegram.org/bot" + telegramToken + "/sendDocument");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);
      //    conn.setRequestProperty("Content-Type", "application/json");

      //    String jsonInputString = "{\"chat_id\": \"" + chatId + "\", \"text\": \"" + text +
      // "\"}";
      StringBuilder sb = new StringBuilder();
      sb.append("chat_id=").append(URLEncoder.encode(chatId, "UTF-8"));
      // sb.append("&text=").append(URLEncoder.encode(text, "UTF-8"));
      sb.append("&document=").append(URLEncoder.encode(filePath1, "UTF-8"));
      try (OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream())) {
        wr.write(sb.toString());
        wr.flush();
        wr.close();
      }

      */
  /*            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
  String output;
  while ((output = br.readLine()) != null) {
      System.out.println(output);
  }*/
  /*
      System.out.println(conn.getResponseCode());

      conn.disconnect();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }*/
}
