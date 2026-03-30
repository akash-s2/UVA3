package com.UrbanVogue.user.OrderModule.client;

import com.UrbanVogue.user.OrderModule.dto.PaymentRequestDTO;
import com.UrbanVogue.user.OrderModule.dto.PaymentResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PaymentClient {

    @Autowired
    private RestTemplate restTemplate;

    public PaymentResponseDTO processPayment(String orderId, Double amount,String idempotencyKey) {

        PaymentRequestDTO request = new PaymentRequestDTO(orderId, amount, idempotencyKey);

        return restTemplate.postForObject(
                "http://localhost:8093/payment/process",
                request,
                PaymentResponseDTO.class
        );
    }
}