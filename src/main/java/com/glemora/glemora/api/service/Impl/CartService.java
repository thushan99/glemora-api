package com.glemora.glemora.api.service.Impl;

import com.glemora.glemora.api.controller.request.CartDTO;
import com.glemora.glemora.api.controller.request.CartItemDTO;
import com.glemora.glemora.api.exception.ActiveCartNotFoundException;
import com.glemora.glemora.api.exception.CartItemNotFoundException;
import com.glemora.glemora.api.exception.ProductNotFoundException;
import com.glemora.glemora.api.exception.UserNotFoundException;
import com.glemora.glemora.api.model.*;
import com.glemora.glemora.api.repository.CartItemRepository;
import com.glemora.glemora.api.repository.CartRepository;
import com.glemora.glemora.api.repository.ProductRepository;
import com.glemora.glemora.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CartDTO getActiveCart(String username) throws UserNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("User not found with username: " + username);
        }

        // First check if the user has any cart, regardless of status
        Optional<Cart> existingCart = cartRepository.findByUser(user);

        Cart cart;
        if (existingCart.isPresent()) {
            cart = existingCart.get();
            // Update status to ACTIVE if it's not already
            if (cart.getStatus() != CartStatus.ACTIVE) {
                cart.setStatus(CartStatus.ACTIVE);
                cart = cartRepository.save(cart);
            }
        } else {
            // Create new cart only if user doesn't have any cart
            Cart newCart = new Cart();
            newCart.setUser(user);
            newCart.setStatus(CartStatus.ACTIVE);
            cart = cartRepository.save(newCart);
        }

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        return convertToDTO(cart, cartItems);
    }

    @Transactional
    public CartDTO addToCart(String username, Long productId, Integer quantity, String size) throws UserNotFoundException, ProductNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("User not found with username: " + username);
        }

        Product product = productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        // Check if product is in stock
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Not enough stock available for product: " + product.getName());
        }

        // First check if the user has any cart, regardless of status
        Optional<Cart> existingCart = cartRepository.findByUser(user);

        Cart cart;
        if (existingCart.isPresent()) {
            cart = existingCart.get();
            // Update status to ACTIVE if it's not already
            if (cart.getStatus() != CartStatus.ACTIVE) {
                cart.setStatus(CartStatus.ACTIVE);
                cart = cartRepository.save(cart);
            }
        } else {
            // Create new cart only if user doesn't have any cart
            Cart newCart = new Cart();
            newCart.setUser(user);
            newCart.setStatus(CartStatus.ACTIVE);
            cart = cartRepository.save(newCart);
        }

        // Check if item already exists in cart
        CartItem existingItem = cartItemRepository.findByCartAndProductAndSize(cart, product, size);

        if (existingItem != null) {
            // Update quantity if item exists
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            cartItemRepository.save(existingItem);
        } else {
            // Create new cart item
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setSize(size);
            cartItemRepository.save(cartItem);
        }

        // Get updated cart items for DTO conversion
        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        return convertToDTO(cart, cartItems);
    }

    @Transactional
    public CartDTO updateCartItem(String username, Long itemId, Integer quantity) throws UserNotFoundException, CartItemNotFoundException, ActiveCartNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("User not found with username: " + username);
        }

        Optional<Cart> existingCart = cartRepository.findByUser(user);
        if (existingCart.isEmpty() || existingCart.get().getStatus() != CartStatus.ACTIVE) {
            throw new ActiveCartNotFoundException("No active cart found for user: " + username);
        }

        Cart cart = existingCart.get();

        CartItem cartItem = cartItemRepository.findById(itemId).orElseThrow(() -> new CartItemNotFoundException("Cart item not found with id: " + itemId));

        // Verify the item belongs to the user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new CartItemNotFoundException("Cart item does not belong to user's cart");
        }

        // Check if product is in stock
        if (cartItem.getProduct().getStockQuantity() < quantity) {
            throw new RuntimeException("Not enough stock available for product: " + cartItem.getProduct().getName());
        }

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        return convertToDTO(cart, cartItems);
    }

    @Transactional
    public void removeCartItem(String username, Long itemId) throws UserNotFoundException, CartItemNotFoundException, ActiveCartNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("User not found with username: " + username);
        }

        Optional<Cart> existingCart = cartRepository.findByUser(user);
        if (existingCart.isEmpty() || existingCart.get().getStatus() != CartStatus.ACTIVE) {
            throw new ActiveCartNotFoundException("No active cart found for user: " + username);
        }

        Cart cart = existingCart.get();

        CartItem cartItem = cartItemRepository.findById(itemId).orElseThrow(() -> new CartItemNotFoundException("Cart item not found with id: " + itemId));

        // Verify the item belongs to the user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new CartItemNotFoundException("Cart item does not belong to user's cart");
        }

        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void clearCart(String username) throws UserNotFoundException, ActiveCartNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("User not found with username: " + username);
        }

        Optional<Cart> existingCart = cartRepository.findByUser(user);
        if (existingCart.isEmpty() || existingCart.get().getStatus() != CartStatus.ACTIVE) {
            throw new ActiveCartNotFoundException("No active cart found for user: " + username);
        }

        Cart cart = existingCart.get();

        // Remove all items from cart
        cartItemRepository.deleteByCart(cart);
        cartRepository.save(cart);
    }

    private CartDTO convertToDTO(Cart cart, List<CartItem> cartItems) {
        CartDTO cartDTO = new CartDTO();
        cartDTO.setId(cart.getId());
        cartDTO.setStatus(cart.getStatus().toString());
        cartDTO.setCreatedAt(cart.getCreatedAt());
        cartDTO.setUpdatedAt(cart.getUpdatedAt());

        List<CartItemDTO> itemDTOs = cartItems.stream().map(this::convertToDTO).collect(Collectors.toList());

        cartDTO.setItems(itemDTOs);

        // Calculate total price
        double totalPrice = cartItems.stream().mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity()).sum();

        cartDTO.setTotalPrice(totalPrice);

        return cartDTO;
    }

    private CartItemDTO convertToDTO(CartItem cartItem) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(cartItem.getId());
        dto.setProductId(cartItem.getProduct().getId());
        dto.setProductName(cartItem.getProduct().getName());
        dto.setProductImage(cartItem.getProduct().getImage());
        dto.setPrice(cartItem.getProduct().getPrice());
        dto.setQuantity(cartItem.getQuantity());
        dto.setSize(cartItem.getSize());
        dto.setAddedAt(cartItem.getAddedAt());
        return dto;
    }
}