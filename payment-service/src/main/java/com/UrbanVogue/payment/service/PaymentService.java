//package com.UrbanVogue.payment.service;
//
//import com.UrbanVogue.payment.dto.PaymentRequestDTO;
//import com.UrbanVogue.payment.dto.PaymentResponseDTO;
//import org.springframework.stereotype.Service;
//
//import java.util.Random;
//
//@Service
//public class PaymentService {
//
//    public PaymentResponseDTO processPayment(PaymentRequestDTO request) {
//
//        Random random = new Random();
//        int chance = random.nextInt(100);
//
//        if (chance < 70) {
//            return new PaymentResponseDTO("SUCCESS");
//        } else {
//            return new PaymentResponseDTO("FAILED");
//        }
//    }
//}


package com.UrbanVogue.payment.service;

import com.UrbanVogue.payment.dto.PaymentRequestDTO;
import com.UrbanVogue.payment.dto.PaymentResponseDTO;
import com.UrbanVogue.payment.entity.Payment;
import com.UrbanVogue.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Transactional
    public PaymentResponseDTO processPayment(PaymentRequestDTO request) {
          System.out.println("request_id:: "+request.getOrderId());
          System.out.println("idempotncy ::"+request.getIdempotencyKey());
        // 1. Check idempotency
        if (request.getIdempotencyKey() != null) {
            Optional<Payment> existingPayment = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existingPayment.isPresent()) {
                return new PaymentResponseDTO(existingPayment.get().getStatus());
            }
        }

        // 2. Process mock payment
        boolean isSuccess = Math.random() > 0.3; // 70% success rate
        String status = isSuccess ? "SUCCESS" : "FAILED";
        String transactionId = UUID.randomUUID().toString();

        // 3. Persist payment
        Payment payment = new Payment();
        payment.setRequestID(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setStatus(status);
        payment.setIdempotencyKey(request.getIdempotencyKey());
        payment.setTransactionId(transactionId);
        payment.setPaymentMode("UPI");  // by default as of now

        paymentRepository.save(payment);

        return new PaymentResponseDTO(status);
    }
}