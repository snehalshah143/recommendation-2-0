package tech.algofinserve.recommendation.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.algofinserve.recommendation.RecommendationApplication;

@RestController
public class RestartController {

  @PostMapping("/Restart")
  public void restart() {
    RecommendationApplication.restart();
  }
}
