package com.glemora.glemora.api.repository;

import com.glemora.glemora.api.model.Cart;
import com.glemora.glemora.api.model.CartStatus;
import com.glemora.glemora.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserAndStatus(User user, CartStatus status);
    Optional<Cart> findByUser(User user);
    void deleteByUserId(Long userId);
}
