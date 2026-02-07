package com.example.helloworld;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class CartConcurrencyIT {
  @Container
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
  }

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void updateItemWithStaleVersionReturnsConflict() throws Exception {
    mockMvc.perform(get("/api/cart")
            .header("X-User-Id", "u1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.version").value(0));

    mockMvc.perform(put("/api/cart/items/ABC")
            .header("X-User-Id", "u1")
            .header("If-Match-Version", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new CartController.UpdateItemRequest(2))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.version").value(1));

    mockMvc.perform(put("/api/cart/items/ABC")
            .header("X-User-Id", "u1")
            .header("If-Match-Version", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new CartController.UpdateItemRequest(1))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error").value("CART_VERSION_CONFLICT"))
        .andExpect(jsonPath("$.message").value("Cart version conflict"))
        .andExpect(jsonPath("$.currentVersion").value(1));
  }
}
