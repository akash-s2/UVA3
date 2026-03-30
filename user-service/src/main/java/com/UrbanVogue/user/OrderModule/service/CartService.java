package com.UrbanVogue.user.OrderModule.service;

import com.UrbanVogue.user.OrderModule.client.PaymentClient;
import com.UrbanVogue.user.OrderModule.client.ProductClient;
import com.UrbanVogue.user.OrderModule.dto.CartRequestDTO;
import com.UrbanVogue.user.OrderModule.dto.CartResponseDTO;
import com.UrbanVogue.user.OrderModule.dto.PaymentResponseDTO;
import com.UrbanVogue.user.OrderModule.dto.ProductResponseDTO;
import com.UrbanVogue.user.OrderModule.entity.Order;
import com.UrbanVogue.user.OrderModule.repository.OrderRepository;
import com.UrbanVogue.user.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CartService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductClient productClient;

    @Autowired
    private PaymentClient paymentClient;

    @Autowired
    private JwtUtil jwtUtil;

    public CartResponseDTO placeCartOrder(CartRequestDTO cartRequest, String token) {

        String email = jwtUtil.extractEmail(token);


        String cartId = UUID.randomUUID().toString().substring(0,4); // genrating the cart_id
        List<CartResponseDTO.ProductResponse> responseList = new ArrayList<>();
        List<Order> inStockOrders = new ArrayList<>();
        double totalAmount = 0.0;

        // Loop through products and check stock
        for (CartRequestDTO.ProductItem item : cartRequest.getItems()) {
            ProductResponseDTO product = productClient.getProduct(item.getProductId(), item.getQuantity());

            CartResponseDTO.ProductResponse productResponse = new CartResponseDTO.ProductResponse();
            productResponse.setProductId(item.getProductId());
            productResponse.setQuantity(item.getQuantity());

            if (product == null || product.getNumberOfPieces() == null || product.getNumberOfPieces() < item.getQuantity()) {
                // Out of stock
                productResponse.setPrice(0.0);
                productResponse.setOrderStatus("FAILED (OUT OF STOCK)");
                productResponse.setPaymentStatus("FAILED");

            } else {
                // In stock, prepare order object
                Order order = new Order();
                order.setProductId(item.getProductId());
                order.setProductName(product.getName());
                order.setPrice(product.getPrice());
                order.setQuantity(item.getQuantity());
                order.setCustomerEmail(email);
                order.setAddress(cartRequest.getAddress());
                order.setCartId(cartId);

                // Total amount calculation
                double amount = product.getPrice() * item.getQuantity();
                totalAmount += amount;
                order.setTotalAmount(amount);

                inStockOrders.add(order);

                // Response placeholder (status updated after payment)
                productResponse.setProductName(product.getName());
                productResponse.setPrice(product.getPrice());
            }

            responseList.add(productResponse);
        }

        //  Call payment only if at least one product in stock
        if (!inStockOrders.isEmpty()) {
            // Using the cart_id for the for tracing the cart orders
            String cartReferenceOrderId = cartId ;//inStockOrders.get(0).getId();

            // creating the idempotency key here
            String idempotencyKey = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
            PaymentResponseDTO paymentResponse = paymentClient.processPayment(cartReferenceOrderId, totalAmount,idempotencyKey);

            boolean success = "SUCCESS".equals(paymentResponse.getStatus());

            // Save orders and update stock
            for (int i = 0; i < inStockOrders.size(); i++) {
                Order order = inStockOrders.get(i);
                CartResponseDTO.ProductResponse productResp = responseList.stream()
                        .filter(p -> p.getProductId().equals(order.getProductId()))
                        .findFirst()
                        .orElse(null);

                if (success) {
                    // Reduce stock in admin service
                    productClient.reduceStock(order.getProductId(), order.getQuantity());

                    order.setOrderStatus("BOOKED");
                    order.setPaymentStatus("SUCCESS");

                    if (productResp != null) {
                        productResp.setOrderStatus("BOOKED");
                        productResp.setPaymentStatus("SUCCESS");
                    }
                } else {
                    order.setOrderStatus("FAILED");
                    order.setPaymentStatus("FAILED");

                    if (productResp != null) {
                        productResp.setOrderStatus("FAILED");
                        productResp.setPaymentStatus("FAILED");
                    }
                }

                orderRepository.save(order);
            }
        }

        //  Calculate final totalAmount for response (only successful AND  in-stock)
        double finalTotal = inStockOrders.stream().mapToDouble(Order::getTotalAmount).sum();

        CartResponseDTO cartResponse = new CartResponseDTO();
        cartResponse.setProducts(responseList);
        cartResponse.setTotalAmount(finalTotal);

        return cartResponse;
    }
}