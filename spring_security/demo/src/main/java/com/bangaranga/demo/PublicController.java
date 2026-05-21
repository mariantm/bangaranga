package com.bangaranga.demo;

  import org.springframework.web.bind.annotation.GetMapping;
  import org.springframework.web.bind.annotation.RequestParam;
  import org.springframework.web.bind.annotation.RestController;

  @RestController
  public class PublicController {

      @GetMapping("/")
      public String hello(@RequestParam(defaultValue = "Bangaranga") String name) {
          return "Hello, " + name + "!!";
      }
  }