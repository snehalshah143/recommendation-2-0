package tech.algofinserve.recommendation.report;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class TelegramReportSender {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.chat.id}")
    private String chatId;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendChartinkReport(String filePath) {
        try {
            // Build file path based on today's date
            String date = new SimpleDateFormat("ddMMyyyy").format(new Date());
           // String filePath = "D:\\Report\\Chartink\\chartink_report_" + date + ".csv";

            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("File not found: " + filePath);
                return;
            }

            String url = "https://api.telegram.org/bot" + botToken + "/sendDocument";

            // Build multipart body
            FileSystemResource resource = new FileSystemResource(file);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("chat_id", chatId);
            body.add("document", resource);
            body.add("caption", "Chartink Report for " + date);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("✅ Report sent successfully!");
            } else {
                System.err.println("❌ Failed to send report: " + response.getBody());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}