package com.glemora.glemora.api.repository;

import com.glemora.glemora.api.model.Order;
import com.glemora.glemora.api.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder(Order order);

    void deleteByOrder(Order order);

    void deleteByOrderId(Long orderId);
}
