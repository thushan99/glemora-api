package com.glemora.glemora.api.repository;

import com.glemora.glemora.api.model.Order;
import com.glemora.glemora.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByOrderDateDesc(User user);
    void deleteByUserId(Long userId);
    List<Order> findByUserId(Long userId);
    List<Order> findAllByOrderByOrderDateDesc();

}
