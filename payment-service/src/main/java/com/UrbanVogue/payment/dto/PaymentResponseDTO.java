package com.UrbanVogue.payment.dto;

public class PaymentResponseDTO {

    private String status;

    public PaymentResponseDTO() {}

    public PaymentResponseDTO(String status) {
        this.status = status;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}