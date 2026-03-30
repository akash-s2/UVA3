package com.UrbanVogue.user.OrderModule.dto;

public class PaymentRequestDTO {

    private String orderId;
    private Double amount;
    private String idempotencyKey;
    public PaymentRequestDTO(String orderId, Double amount, String idempotencyKey) {
        this.orderId = orderId;
        this.amount = amount;
        this.idempotencyKey=idempotencyKey;
    }

    public String getOrderId() {
        return orderId;
    }

    public Double getAmount() {
        return amount;
    }

    public String getIdempotencyKey()
    {
        return idempotencyKey;
    }
}