package tech.algofinserve.recommendation;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableScheduling
// @ComponentScan({"tech.algofinserve"})
public class RecommendationApplication {
  private static ConfigurableApplicationContext context;

  public static void main(String[] args) {
    context = SpringApplication.run(RecommendationApplication.class, args);
  }

  public static void restart() {
    ApplicationArguments args = context.getBean(ApplicationArguments.class);
    System.out.println("Existing Server getting stopped.");

    Thread thread =
        new Thread(
            () -> {
              context.close();
              context =
                  SpringApplication.run(RecommendationApplication.class, args.getSourceArgs());
            });

    thread.setDaemon(false);
    thread.start();
    System.out.println("Server Restarted...");
  }
}
