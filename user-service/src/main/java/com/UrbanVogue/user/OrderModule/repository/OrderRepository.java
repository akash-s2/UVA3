package com.UrbanVogue.user.OrderModule.repository;

import com.UrbanVogue.user.AuthModule.entity.User;
import com.UrbanVogue.user.OrderModule.entity.Order;
import jakarta.validation.OverridesAttribute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    //  MyOrders feature
    //OverridesAttribute.List<Order> findByCustomerEmail(String customerEmail);
    List<Order> findByCustomerEmail(String email);
}