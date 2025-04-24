package com.glemora.glemora.api.controller;

import com.glemora.glemora.api.controller.request.OrderDTO;
import com.glemora.glemora.api.controller.request.OrderRequest;
import com.glemora.glemora.api.exception.ActiveCartNotFoundException;
import com.glemora.glemora.api.exception.OrderNotFoundException;
import com.glemora.glemora.api.exception.UserNotFoundException;
import com.glemora.glemora.api.service.Impl.OrderService;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping(headers = "X-Api-Version=v1")
    @RolesAllowed({"USER", "ADMIN"})
    public ResponseEntity<OrderDTO> createOrder(
            Authentication authentication,
            @RequestBody OrderRequest orderRequest
    ) throws UserNotFoundException, ActiveCartNotFoundException {
        OrderDTO order = orderService.createOrder(authentication.getName(), orderRequest);
        return ResponseEntity.ok(order);
    }

    @GetMapping(headers = "X-Api-Version=v1")
    @RolesAllowed({"USER", "ADMIN"})
    public ResponseEntity<List<OrderDTO>> getUserOrders(Authentication authentication) throws UserNotFoundException {
        List<OrderDTO> orders = orderService.getUserOrders(authentication.getName());
        return ResponseEntity.ok(orders);
    }

    @GetMapping(value = "/{orderId}", headers = "X-Api-Version=v1")
    @RolesAllowed({"USER", "ADMIN"})
    public ResponseEntity<OrderDTO> getOrderById(
            Authentication authentication,
            @PathVariable Long orderId
    ) throws UserNotFoundException, OrderNotFoundException {
        OrderDTO order = orderService.getOrderById(authentication.getName(), orderId);
        return ResponseEntity.ok(order);
    }

    @PutMapping(value = "/{orderId}/status", headers = "X-Api-Version=v1")
    @RolesAllowed({"ADMIN"})
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status
    ) throws OrderNotFoundException {
        orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/{orderId}", headers = "X-Api-Version=v1")
    @RolesAllowed({"ADMIN"})
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId) throws OrderNotFoundException {
        orderService.deleteOrder(orderId);
        return ResponseEntity.ok().build();
    }
}
