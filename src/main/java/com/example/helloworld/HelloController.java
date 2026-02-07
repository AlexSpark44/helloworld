package com.example.helloworld;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class HelloController {
  @GetMapping("/hello")
  public HelloResponse hello() {
    return new HelloResponse("Hello World", "shopping-cart", "UP");
  }

  public record HelloResponse(String message, String service, String status) {
  }
}
