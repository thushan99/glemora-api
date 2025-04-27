package com.glemora.glemora.api.service.Impl;

import com.glemora.glemora.api.controller.request.OrderDTO;
import com.glemora.glemora.api.controller.request.OrderItemDTO;
import com.glemora.glemora.api.controller.request.OrderRequest;
import com.glemora.glemora.api.exception.ActiveCartNotFoundException;
import com.glemora.glemora.api.exception.OrderNotFoundException;
import com.glemora.glemora.api.exception.UserNotFoundException;
import com.glemora.glemora.api.model.*;
import com.glemora.glemora.api.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrderDTO createOrder(String username, OrderRequest orderRequest) throws UserNotFoundException, ActiveCartNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("User not found with username: " + username);
        }

        // Get active cart
        Cart cart = cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE).orElseThrow(() -> new ActiveCartNotFoundException("No active cart found for user: " + username));

        // Get cart items
        List<CartItem> cartItems = cartItemRepository.findByCart(cart);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cannot create order with empty cart");
        }

        // Create or get shipping address
        UserAddress shippingAddress = createOrUpdateShippingAddress(user, orderRequest);

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setShippingMethod(orderRequest.getShippingMethod());
        order.setPaymentMethod(orderRequest.getPaymentMethod());
        order.setNotes(orderRequest.getNotes());

        // Calculate totals
        double subtotal = cartItems.stream().mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity()).sum();

        double tax = subtotal * 0.1; // 10% tax rate
        double total = subtotal + tax + orderRequest.getShippingCost();

        order.setSubtotal(subtotal);
        order.setTax(tax);
        order.setTotal(total);

        // Generate order tracking number
        String orderTrackingNumber = UUID.randomUUID().toString();
        cart.setOrderTrackingNumber(orderTrackingNumber);

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Create order items from cart items
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());

            // Update product stock
            Product product = cartItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            orderItemRepository.save(orderItem);
        }

        // Mark cart as ordered
        cart.setStatus(CartStatus.ORDERED);
        cartRepository.save(cart);

        return convertToDTO(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getUserOrders(String username) throws UserNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("User not found with username: " + username);
        }

        List<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(user);
        return orders.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(String username, Long orderId) throws UserNotFoundException, OrderNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("User not found with username: " + username);
        }

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        // Verify the order belongs to the user (unless admin)
        if (!order.getUser().getId().equals(user.getId()) && !user.getRoles().stream().anyMatch(role -> "ADMIN".equals(role.getName()))) {
            throw new OrderNotFoundException("Order not found for user");
        }

        return convertToDTO(order);
    }

    @Transactional
    public void updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
        order.setStatus(status);
        orderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(Long orderId) throws OrderNotFoundException {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        orderItemRepository.deleteByOrder(order);
        orderRepository.delete(order);
    }

    private UserAddress createOrUpdateShippingAddress(User user, OrderRequest orderRequest) {
        UserAddress address = new UserAddress();
        address.setUser(user);
        address.setAddressLine1(orderRequest.getAddressLine1());
        address.setAddressLine2(orderRequest.getAddressLine2());
        address.setCity(orderRequest.getCity());
        address.setState(orderRequest.getState());
        address.setPostalCode(orderRequest.getPostalCode());
        address.setCountry(orderRequest.getCountry());
        address.setIsDefault(orderRequest.getIsDefaultAddress());

        return userAddressRepository.save(address);
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setShippingMethod(order.getShippingMethod());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setSubtotal(order.getSubtotal());
        dto.setTax(order.getTax());
        dto.setTotal(order.getTotal());
        dto.setNotes(order.getNotes());

        // Add customer info
        dto.setCustomerName(order.getUser().getName());
        dto.setCustomerEmail(order.getUser().getEmail());

        // Add shipping address
        UserAddress address = order.getShippingAddress();
        dto.setShippingAddress(address.getAddressLine1() + (address.getAddressLine2() != null ? ", " + address.getAddressLine2() : "") + ", " + address.getCity() + ", " + address.getState() + ", " + address.getPostalCode() + ", " + address.getCountry());

        // Add order items
        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
        List<OrderItemDTO> orderItemDTOs = orderItems.stream().map(this::convertToDTO).collect(Collectors.toList());

        dto.setItems(orderItemDTOs);

        return dto;
    }

    private OrderItemDTO convertToDTO(OrderItem orderItem) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(orderItem.getId());
        dto.setProductId(orderItem.getProduct().getId());
        dto.setProductName(orderItem.getProduct().getName());
        dto.setProductImage(orderItem.getProduct().getImage());
        dto.setPrice(orderItem.getPrice());
        dto.setQuantity(orderItem.getQuantity());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getOrders() {
        List<Order> orders = orderRepository.findAllByOrderByOrderDateDesc();
        List<OrderDTO> orderDTOs = new ArrayList<>();

        for (Order order : orders) {
            OrderDTO dto = convertToDTO(order); // Your existing method for all fields
            dto.setStatus(order.getStatus());   // Manually override/set only the status field
            orderDTOs.add(dto);
        }

        return orderDTOs;
    }


}