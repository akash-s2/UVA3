package com.UrbanVogue.user.OrderModule.service;

import com.UrbanVogue.user.OrderModule.client.PaymentClient;
import com.UrbanVogue.user.OrderModule.client.ProductClient;
import com.UrbanVogue.user.OrderModule.dto.*;
import com.UrbanVogue.user.OrderModule.entity.Order;
import com.UrbanVogue.user.OrderModule.repository.OrderRepository;
import com.UrbanVogue.user.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import com.UrbanVogue.user.OrderModule.dto.MyOrderResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class OrderService {

    @Autowired
    private com.UrbanVogue.user.OrderModule.repository.OrderRepository orderRepository;

    @Autowired
    private ProductClient productClient;

    @Autowired
    private PaymentClient paymentClient;

    @Autowired
    //@Qualifier("orderJwtUtil")
    private JwtUtil jwtUtil;

    public String getEmail(String token)
    {
        return jwtUtil.extractEmail(token);
    }
    public OrderResponseDTO placeOrder(OrderRequestDTO request, String token) {

        // extract email from JWT
        String email = jwtUtil.extractEmail(token);


        // fetch product from admin service
        ProductResponseDTO product = productClient.getProduct(request.getProductId(), request.getQuantity());

        if (product == null) {
            return new OrderResponseDTO("Product not found", "FAILED", "FAILED");
        }

        // check stock availability
        //if (request.getQuantity() > product.getNumberOfPieces())
        if (product.getNumberOfPieces() == null) {
            return new OrderResponseDTO("Out of stock", "FAILED", "FAILED");
        }

        // calculate total amount
        Double totalAmount = product.getPrice() * request.getQuantity();

        // create order object
        Order order = new Order();
        order.setProductId(request.getProductId());
        order.setCustomerEmail(email);
        order.setQuantity(request.getQuantity());
        order.setPrice(product.getPrice());
        order.setProductName(product.getName());
        order.setTotalAmount(totalAmount);
        order.setAddress(request.getAddress());
        order.setCartId(null);   // because it is the single order only
        orderRepository.save(order);

        // generating the idempotency key
        String idempotencyKey = UUID.randomUUID().toString().replace("-", "").substring(0, 5);
        // call payment service
        PaymentResponseDTO paymentResponse =
                paymentClient.processPayment(order.getId(), totalAmount,idempotencyKey);

        if ("SUCCESS".equals(paymentResponse.getStatus())) {

//            // update stock
            productClient.reduceStock(request.getProductId(), request.getQuantity());

            order.setPaymentStatus("SUCCESS");
            order.setOrderStatus("BOOKED");

            orderRepository.save(order);

            return new OrderResponseDTO(
                    "Order placed successfully",
                    "SUCCESS",
                    "BOOKED"
            );

        } else {

            order.setPaymentStatus("FAILED");
            order.setOrderStatus("FAILED");

            orderRepository.save(order);

            return new OrderResponseDTO(
                    "Payment failed, order not booked",
                    "FAILED",
                    "FAILED"
            );
        }
    }



    // only admin acess role
    public void updateOrderStatus(Long orderId, String status) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));


        if (!"SUCCESS".equalsIgnoreCase(order.getPaymentStatus())) {
            throw new RuntimeException("Cannot update status for failed orders");
        }



        if ("DELIVERED".equalsIgnoreCase(order.getOrderStatus())) {
            throw new RuntimeException("Order already delivered, cannot update");
        }


        if (!status.equalsIgnoreCase("SHIPPED") &&
                !status.equalsIgnoreCase("OUT_FOR_DELIVERY") &&
                !status.equalsIgnoreCase("DELIVERED")) {

            throw new RuntimeException("Invalid order status");
        }

        order.setOrderStatus(status.toUpperCase());

        orderRepository.save(order);
    }
   // My orders for user tracking
    public List<MyOrderResponseDTO> getOrdersByEmail(String email) {
        List<Order> orders = orderRepository.findByCustomerEmail(email);

        return orders.stream()
                .map(order -> new MyOrderResponseDTO(
                        order.getProductId(),            // new
                        order.getProductName(),          // new
                        order.getPrice(),                // new
                        order.getQuantity(),             // new
                        order.getTotalAmount(),          // new
                        order.getOrderStatus(),
                        order.getPaymentStatus()
                ))
                .toList();
    }


}