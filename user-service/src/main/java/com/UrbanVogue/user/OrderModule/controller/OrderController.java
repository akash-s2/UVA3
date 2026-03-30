package com.UrbanVogue.user.OrderModule.controller;

import com.UrbanVogue.user.OrderModule.dto.MyOrderResponseDTO;
import com.UrbanVogue.user.OrderModule.dto.OrderRequestDTO;
import com.UrbanVogue.user.OrderModule.dto.OrderResponseDTO;
import com.UrbanVogue.user.OrderModule.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/place")
    public OrderResponseDTO placeOrder(
            @RequestBody OrderRequestDTO request,
            @RequestHeader("Authorization") String authHeader
    ) {
        // remove Bearer prefix
        String token = authHeader.substring(7);

        return orderService.placeOrder(request, token);
    }
    @GetMapping("/my-orders")
    public ResponseEntity<List<MyOrderResponseDTO>> getMyOrders(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }

        String token = authHeader.substring(7).trim(); //  trim to remove extra spaces
        String email = orderService.getEmail(token);   //  pass clean token

        return ResponseEntity.ok(orderService.getOrdersByEmail(email));
    }
}