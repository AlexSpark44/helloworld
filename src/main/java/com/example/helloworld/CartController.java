package com.example.helloworld;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class CartController {
  private final CartService cartService;

  public CartController(CartService cartService) {
    this.cartService = cartService;
  }

  @GetMapping("/cart")
  public CartResponse getCart(@RequestHeader("X-User-Id") String userId) {
    return CartResponse.from(cartService.getOrCreateCart(userId));
  }

  @PutMapping(path = "/cart/items/{sku}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public CartResponse updateItem(
      @RequestHeader("X-User-Id") String userId,
      @RequestHeader("If-Match-Version") long ifMatchVersion,
      @PathVariable String sku,
      @Valid @RequestBody UpdateItemRequest request
  ) {
    return CartResponse.from(cartService.updateItem(userId, sku, ifMatchVersion, request.quantity()));
  }

  @DeleteMapping("/cart/items/{sku}")
  public CartResponse deleteItem(
      @RequestHeader("X-User-Id") String userId,
      @RequestHeader("If-Match-Version") long ifMatchVersion,
      @PathVariable String sku
  ) {
    return CartResponse.from(cartService.deleteItem(userId, sku, ifMatchVersion));
  }

  public record UpdateItemRequest(@NotNull @Min(1) Integer quantity) {
  }

  public record CartItemResponse(String sku, int quantity) {
    static CartItemResponse from(CartItem item) {
      return new CartItemResponse(item.getSku(), item.getQuantity());
    }
  }

  public record CartResponse(Long cartId, String userId, long version, List<CartItemResponse> items) {
    static CartResponse from(Cart cart) {
      List<CartItemResponse> items = cart.getItems().stream()
          .map(CartItemResponse::from)
          .toList();
      return new CartResponse(cart.getId(), cart.getUserId(), cart.getVersion(), items);
    }
  }
}
