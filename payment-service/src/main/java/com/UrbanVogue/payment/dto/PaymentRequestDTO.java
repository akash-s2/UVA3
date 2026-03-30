package com.UrbanVogue.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PaymentRequestDTO {

    @NotNull(message = "order ID cannot be null")
    private String orderId;

    @NotNull(message = "Amount cannot be null")
    @Min(value = 1, message = "Amount must be greater than zero")
    private Double amount;

    @NotBlank(message = "Idempotency key cannot be blank")
    private String idempotencyKey;

    public PaymentRequestDTO() {}

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getIdempotencyKey(){
        return idempotencyKey;
    }

    public void setIdempotencyKey(){
        this.idempotencyKey=idempotencyKey;
    }
}