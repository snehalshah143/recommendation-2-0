package tech.algofinserve.recommendation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableScheduling
// @ComponentScan({"tech.algofinserve"})
public class RecommendationApplication {

  public static void main(String[] args) {
    SpringApplication.run(RecommendationApplication.class, args);
  }
}
