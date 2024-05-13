package tech.algofinserve.recommendation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tech.algofinserve.recommendation.model.domain.Alert;


@RestController
public class ChartinkController {

//    @Autowired
//    private ChartInkAlertProcessingEngine alertProcessing;

    @PostMapping(path = "/test/BuyAlertHourly", consumes = "application/json")
    public void alertsReceived(@RequestBody Alert alert) {
        System.out.println(alert.toString());
    //    alertProcessing.processBuyAlert(alert);

    }



}