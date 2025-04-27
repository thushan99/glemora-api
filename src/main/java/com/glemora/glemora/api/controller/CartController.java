package com.glemora.glemora.api.controller;

import com.glemora.glemora.api.controller.request.CartDTO;
import com.glemora.glemora.api.exception.ActiveCartNotFoundException;
import com.glemora.glemora.api.exception.CartItemNotFoundException;
import com.glemora.glemora.api.exception.ProductNotFoundException;
import com.glemora.glemora.api.exception.UserNotFoundException;
import com.glemora.glemora.api.service.Impl.CartService;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping(headers = "X-Api-Version=v1")
    @RolesAllowed({"USER", "ADMIN"})
    public ResponseEntity<CartDTO> getCart(Authentication authentication) throws UserNotFoundException {
        CartDTO cart = cartService.getActiveCart(authentication.getName());
        return ResponseEntity.ok(cart);
    }

    @PostMapping(headers = "X-Api-Version=v1")
    @RolesAllowed({"USER", "ADMIN"})
    public ResponseEntity<CartDTO> addToCart(Authentication authentication, @RequestParam Long productId, @RequestParam Integer quantity, @RequestParam(required = false, defaultValue = "M") String size) throws UserNotFoundException, ProductNotFoundException {
        CartDTO cart = cartService.addToCart(authentication.getName(), productId, quantity, size);
        return ResponseEntity.ok(cart);
    }

    @PutMapping(value = "/items/{itemId}", headers = "X-Api-Version=v1")
    @RolesAllowed({"USER", "ADMIN"})
    public ResponseEntity<CartDTO> updateCartItem(Authentication authentication, @PathVariable Long itemId, @RequestParam Integer quantity) throws UserNotFoundException, CartItemNotFoundException, ActiveCartNotFoundException {
        CartDTO cart = cartService.updateCartItem(authentication.getName(), itemId, quantity);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping(value = "/items/{itemId}", headers = "X-Api-Version=v1")
    @RolesAllowed({"USER", "ADMIN"})
    public ResponseEntity<Void> removeCartItem(Authentication authentication, @PathVariable Long itemId) throws UserNotFoundException, CartItemNotFoundException, ActiveCartNotFoundException {
        cartService.removeCartItem(authentication.getName(), itemId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(headers = "X-Api-Version=v1")
    @RolesAllowed({"USER", "ADMIN"})
    public ResponseEntity<Void> clearCart(Authentication authentication) throws UserNotFoundException, ActiveCartNotFoundException {
        cartService.clearCart(authentication.getName());
        return ResponseEntity.ok().build();
    }
}
