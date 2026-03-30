package com.UrbanVogue.user.OrderModule.dto;

import java.util.List;

public class CartRequestDTO {

    private String address;  // single delivery address
    private List<ProductItem> items;  // list of products in cart

    public static class ProductItem {
        private Long productId;
        private Integer quantity;

        // getters and setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public List<ProductItem> getItems() { return items; }
    public void setItems(List<ProductItem> items) { this.items = items; }
}