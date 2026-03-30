package com.UrbanVogue.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

    http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})
            .authorizeExchange(exchange -> exchange
                    .pathMatchers("/auth/**").permitAll()
                    .pathMatchers("/user/getProducts/**").permitAll()
                    .pathMatchers("/change/admin/**").permitAll()
                    .pathMatchers(HttpMethod.OPTIONS).permitAll()

                    // .pathMatchers("/user/orders/**").authenticated()

                    //   fix
                    .anyExchange().permitAll()
            );

    return http.build();
}
}






//
//package com.UrbanVogue.gateway.security;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.web.server.ServerHttpSecurity;
//import org.springframework.security.web.server.SecurityWebFilterChain;
//
//@Configuration
//public class SecurityConfig {
//
//    @Bean
//    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
//
//        http
//                .csrf(csrf -> csrf.disable())
//                .cors(cors -> {})
//                .authorizeExchange(exchange -> exchange
//                        // Public endpoints
//                        .pathMatchers("/auth/**").permitAll()
//                        .pathMatchers("/user/**").permitAll()
//                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
//
//                        // All other endpoints require authentication
//                        .anyExchange().authenticated()
//                );
//
//        return http.build();
//    }
//}


