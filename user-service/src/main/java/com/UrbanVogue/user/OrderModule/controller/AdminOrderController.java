package com.UrbanVogue.user.OrderModule.controller;

import com.UrbanVogue.user.OrderModule.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/change/admin")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @PutMapping("/{orderId}/status")
    public String updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String value
    ) {
        orderService.updateOrderStatus(orderId, value);
        return "Order status updated successfully";
    }
}