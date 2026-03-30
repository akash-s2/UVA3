package com.UrbanVogue.gateway.filter;

import com.UrbanVogue.gateway.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;

import java.util.Collections;

@Component
public class AuthFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    System.out.println(" FILTER HIT ");
    String path = exchange.getRequest().getURI().getPath();

    // PUBLIC API  will be skip
    if (path.startsWith("/auth") ||
            path.startsWith("/user/getProducts") ||
            path.startsWith("/catalog")) {
        return chain.filter(exchange);
    }

    String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

    //  DEBUG PRINT
    System.out.println("PATH: " + path);
    System.out.println("AUTH HEADER: " + authHeader);

    //  no token
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        System.out.println(" TOKEN MISSING OR INVALID FORMAT"); // added
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    String token = authHeader.substring(7);

    try {
        //  debug
        System.out.println("TOKEN: " + token); // added

        boolean isValid = jwtUtil.validateToken(token);

        //  debug validation
        System.out.println("IS TOKEN VALID: " + isValid); // added

        if (!isValid) {
            throw new RuntimeException("Invalid Token");
        }

        //  Extract email and role
        String email = jwtUtil.extractEmail(token);
        String role = jwtUtil.extractRole(token);

        //  Debug user data
        System.out.println("EMAIL: " + email);
        System.out.println("ROLE: " + role);

        //  create authentication
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_" + role)
                        )
                );

        //  Setint the  security ccontext
        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));

    } catch (Exception e) {
        System.out.println(" TOKEN VALIDATION FAILED: " + e.getMessage());
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
    @Override
    public int getOrder() {
        return -1; // high priority
    }
}









//
//package com.UrbanVogue.gateway.filter;
//
//import com.UrbanVogue.gateway.security.JwtUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.core.Ordered;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.ReactiveSecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import reactor.core.publisher.Mono;
//
//import java.util.Collections;
//import java.util.List;
//
//import static java.security.KeyRep.Type.SECRET;
//
//@Component
//public class AuthFilter implements GlobalFilter, Ordered {
//
//    private final JwtUtil jwtUtil;
//
//    // injecting the jwtUtil
//    public AuthFilter(JwtUtil jwtUtil) {
//        this.jwtUtil = jwtUtil;
//    }
//
//
//    // Public endpoints that should bypass authentication
//    private static final List<String> PUBLIC_APIS = List.of(
//            "/auth",
//            "/user/getProducts",
//            "/catalog"
//    );
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        System.out.println(" FILTER HIT ");
//        System.out.println("FULL URL: " + exchange.getRequest().getURI());
//        String path = exchange.getRequest().getURI().getPath();
//        System.out.println("Path: "+path);
//        // Skip authentication for public endpoints
//        if (isPublicEndpoint(path)) {
//            return chain.filter(exchange);
//        }
//
//        // Extract Authorization header
//        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
//        System.out.println("AUTH HEADER: " + authHeader);
//        // Reject request if token is missing
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//            return exchange.getResponse().setComplete();
//        }
//
//        String token = authHeader.substring(7);
//        System.out.println("Token "+token);
//        try {
//            // Validate JWT
//            if (!jwtUtil.validateToken(token)) {
//                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//                return exchange.getResponse().setComplete();
//            }
//
//
//            // Extract user details from token
//            String email = jwtUtil.extractEmail(token);
//
//            String role = jwtUtil.extractRole(token);
//
//            System.out.println("EMAIL: " + email);
//            System.out.println("ROLE: " + role);
//            // Forward user details to downstream services via headers
//            ServerWebExchange mutatedExchange = exchange.mutate()
//                    .request(builder -> builder.headers(headers -> {
//                        headers.set("X-User-Email", email);
//                        headers.set("X-User-Role", role);
//                    }))
//                    .build();
//
//            System.out.println("EMAIL: " + email);
//            System.out.println("ROLE: " + role);
//
//            // Create authentication object for Spring Security context
//            UsernamePasswordAuthenticationToken authentication =
//                    new UsernamePasswordAuthenticationToken(
//                            email,
//                            null,
//                            Collections.singletonList(
//                                    new SimpleGrantedAuthority("ROLE_" + role)
//                            )
//                    );
//
//            return chain.filter(mutatedExchange)
//                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
//
//        } catch (Exception ex) {
//            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//            return exchange.getResponse().setComplete();
//        }
//    }
//
//    // Utility method to check if path is public
//    private boolean isPublicEndpoint(String path) {
//        return PUBLIC_APIS.stream().anyMatch(path::startsWith);
//    }
//
//    @Override
//    public int getOrder() {
//        // High precedence to run before other filters
//        return -1;
//    }
//}