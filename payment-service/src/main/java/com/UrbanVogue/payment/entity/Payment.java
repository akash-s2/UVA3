package com.UrbanVogue.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String requestId;
    
    private Double amount;

    private String status;

    @Column(unique = true)
    private String idempotencyKey;

    private String transactionId;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Payment() {}

    public Payment(Long id, String requestId, Double amount, String status, String idempotencyKey, String transactionId, LocalDateTime createdAt) {
        this.id = id;
        this.requestId = requestId;
        this.amount = amount;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
        this.transactionId = transactionId;
        this.createdAt = createdAt;
    }


    public Long getId() {
        return id;
    }

    public String getRequestId() {
        return requestId;
    }

    public Double getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}
