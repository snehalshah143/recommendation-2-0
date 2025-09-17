package tech.algofinserve.recommendation;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

//@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@SpringBootApplication
@EnableScheduling
//@EntityScan(basePackages = "tech.algofinserve.recommendation.alerts.persistance")
//@EnableJpaRepositories(basePackages = "tech.algofinserve.recommendation.alerts.repository")

//@ComponentScan({"tech.algofinserve"})
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
