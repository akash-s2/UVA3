//
//
//package com.UrbanVogue.user.OrderModule.client;
//
//import com.UrbanVogue.user.OrderModule.dto.ProductResponseDTO;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.*;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//@Component
//public class ProductClient {
//
//    @Autowired
//    private RestTemplate restTemplate;
//
//    //  COMMON METHODE for extracting the tokens
//    private String getToken() {
//        Object credentials = SecurityContextHolder.getContext()
//                .getAuthentication()
//                .getCredentials();
//        System.out.println(" FORWARDING TOKEN: " + credentials);
//        if (credentials != null) {
//            return credentials.toString();
//        }
//        return null;
//    }
//
//    //  COMMON HEADER BUILDERs
//    private HttpHeaders getHeaders() {
//        HttpHeaders headers = new HttpHeaders();
//
//        String token = getToken();
//
//        if (token != null) {
//            headers.set("Authorization", "Bearer " + token);
//        }
//
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        return headers;
//    }
//
//    //  FETCHing the  PRODUCT
//    public ProductResponseDTO getProduct(Long productId, Integer quantity) {
//
//        String url = "http://localhost:8092/internal/products/" + productId + "?qty=" + quantity;
//        ;
//
//        HttpEntity<Void> entity = new HttpEntity<>(getHeaders());
//
//        ResponseEntity<ProductResponseDTO> response =
//                restTemplate.exchange(
//                        url,
//                        HttpMethod.GET,
//                        entity,
//                        ProductResponseDTO.class
//                );
//
//        return response.getBody();
//    }
//
//    //Reduce stock
//
//    public void reduceStock(Long productId, Integer quantity) {
//
//        String url = "http://localhost:8092/internal/products/reduce/"
//                + productId + "?qty=" + quantity;
//
//        HttpEntity<Void> entity = new HttpEntity<>(getHeaders());
//
//        restTemplate.exchange(
//                url,
//                HttpMethod.PUT,
//                entity,
//                Void.class
//        );
//    }
//}


package com.UrbanVogue.user.OrderModule.client;

import com.UrbanVogue.user.OrderModule.dto.ProductResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ProductClient {

    @Autowired
    private RestTemplate restTemplate;

    // Extract user details from SecurityContext
    private Authentication getAuth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    // Build headers for inter-service communication
    private HttpHeaders getHeaders() {

        HttpHeaders headers = new HttpHeaders();

        Authentication auth = getAuth();

        if (auth != null && auth.getPrincipal() != null) {

            // Extract email (principal)
            String email = auth.getPrincipal().toString();

            // Extract role
            String role = auth.getAuthorities()
                    .stream()
                    .findFirst()
                    .map(a -> a.getAuthority().replace("ROLE_", ""))
                    .orElse(null);

            // Forward headers to admin service
            headers.set("X-User-Email", email);
            headers.set("X-User-Role", role);
        }

        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // Fetch product details
    public ProductResponseDTO getProduct(Long productId, Integer quantity) {

        String url = "http://localhost:8092/internal/products/" + productId + "?qty=" + quantity;

        HttpEntity<Void> entity = new HttpEntity<>(getHeaders());

        ResponseEntity<ProductResponseDTO> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        ProductResponseDTO.class
                );

        return response.getBody();
    }

    // Reduce stock
    public void reduceStock(Long productId, Integer quantity) {

        String url = "http://localhost:8092/internal/products/reduce/"
                + productId + "?qty=" + quantity;

        HttpEntity<Void> entity = new HttpEntity<>(getHeaders());

        restTemplate.exchange(
                url,
                HttpMethod.PUT,
                entity,
                Void.class
        );
    }
}