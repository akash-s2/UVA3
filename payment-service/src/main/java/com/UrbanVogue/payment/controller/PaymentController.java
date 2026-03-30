package com.UrbanVogue.payment.controller;

import com.UrbanVogue.payment.dto.PaymentRequestDTO;
import com.UrbanVogue.payment.dto.PaymentResponseDTO;
import com.UrbanVogue.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/process")
    public PaymentResponseDTO processPayment(@RequestBody PaymentRequestDTO request) {
        return paymentService.processPayment(request);
    }
}