package com.glemora.glemora.api.repository;

import com.glemora.glemora.api.model.Cart;
import com.glemora.glemora.api.model.CartItem;
import com.glemora.glemora.api.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    CartItem findByCartAndProductAndSize(Cart cart, Product product, String size);
    List<CartItem> findByCart(Cart cart);
    void deleteByCart(Cart cart);
}
