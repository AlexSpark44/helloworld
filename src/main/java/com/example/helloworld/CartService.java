package com.example.helloworld;

import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CartService {
  private final CartRepository cartRepository;

  public CartService(CartRepository cartRepository) {
    this.cartRepository = cartRepository;
  }

  @Transactional
  public Cart getOrCreateCart(String userId) {
    Cart cart = cartRepository.findByUserId(userId)
        .orElseGet(() -> {
          Cart created = new Cart();
          created.setUserId(userId);
          return cartRepository.save(created);
        });
    cart.getItems().size();
    return cart;
  }

  @Transactional
  public Cart updateItem(String userId, String sku, long ifMatchVersion, int quantity) {
    Cart cart = findExistingCart(userId);
    verifyVersion(cart, ifMatchVersion);

    Optional<CartItem> existing = cart.getItems().stream()
        .filter(item -> item.getSku().equals(sku))
        .findFirst();
    if (existing.isPresent()) {
      existing.get().setQuantity(quantity);
    } else {
      CartItem item = new CartItem();
      item.setSku(sku);
      item.setQuantity(quantity);
      cart.addItem(item);
    }
    cart.getItems().size();
    return cart;
  }

  @Transactional
  public Cart deleteItem(String userId, String sku, long ifMatchVersion) {
    Cart cart = findExistingCart(userId);
    verifyVersion(cart, ifMatchVersion);

    cart.getItems().stream()
        .filter(item -> item.getSku().equals(sku))
        .findFirst()
        .ifPresent(cart::removeItem);

    cart.getItems().size();
    return cart;
  }

  private Cart findExistingCart(String userId) {
    return cartRepository.findByUserId(userId)
        .orElseThrow(() -> new CartNotFoundException("Cart not found"));
  }

  private void verifyVersion(Cart cart, long ifMatchVersion) {
    if (cart.getVersion() != ifMatchVersion) {
      throw new VersionConflictException(cart.getVersion());
    }
  }
}

class VersionConflictException extends RuntimeException {
  private final long currentVersion;

  VersionConflictException(long currentVersion) {
    super("Cart version conflict");
    this.currentVersion = currentVersion;
  }

  public long getCurrentVersion() {
    return currentVersion;
  }
}

class CartNotFoundException extends RuntimeException {
  CartNotFoundException(String message) {
    super(message);
  }
}
