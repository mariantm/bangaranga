package com.bangaranga.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    @GetMapping("/payment")
    public String hello(@RequestParam(defaultValue = "money flow") String name) {
        return "Increase " + name + "!";
    }
}