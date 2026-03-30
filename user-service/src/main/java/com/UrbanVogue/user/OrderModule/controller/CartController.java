package com.UrbanVogue.user.OrderModule.controller;

import com.UrbanVogue.user.OrderModule.dto.CartRequestDTO;
import com.UrbanVogue.user.OrderModule.dto.CartResponseDTO;
import com.UrbanVogue.user.OrderModule.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/orders")
public class CartController {

    @Autowired
    private CartService cartService;


    @PostMapping("/cart")
    public CartResponseDTO placeCartOrder(
            @RequestBody CartRequestDTO cartRequest,
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.substring(7);

        return cartService.placeCartOrder(cartRequest, token);
    }
}