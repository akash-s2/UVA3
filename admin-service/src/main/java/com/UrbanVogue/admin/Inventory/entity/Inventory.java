package com.UrbanVogue.admin.Inventory.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    private Long productId;  // same as product_id

    private Integer numberOfPieces;

    // getters & setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getNumberOfPieces() { return numberOfPieces; }
    public void setNumberOfPieces(Integer numberOfPieces) { this.numberOfPieces = numberOfPieces; }
}