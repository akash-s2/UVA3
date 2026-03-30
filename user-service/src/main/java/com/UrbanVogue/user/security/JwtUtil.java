package com.UrbanVogue.user.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET;

    @Value("${jwt.expiration}")
    private long EXPIRATION;

    //  COMMON KEY METHOD
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    //  GENERATE TOKEN used by the auth-module only
    public String generateToken(String email, String role) {

        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    //  EXTRACTING the  EMAIL
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    //  EXTRACTING  ROLE
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // VALIDATE TOKEN
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            System.out.println("JWT VALIDATION FAILED: " + e.getMessage());
            return false;
        }
    }

    //  COMMON CLAIM PARSER
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}









//
//package com.UrbanVogue.user.security;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.security.Keys;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import javax.crypto.SecretKey;
//import java.nio.charset.StandardCharsets;
//import java.util.Date;
//
//@Component
//public class JwtUtil {
//
//    @Value("${jwt.secret}")
//    private String SECRET;
//
//    @Value("${jwt.expiration}")
//    private long EXPIRATION;
//
//    // Generate signing key from secret
//    private SecretKey getSigningKey() {
//        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
//    }
//
//    // Generate JWT token (used by auth module / login API)
//    public String generateToken(String email, String role) {
//
//        return Jwts.builder()
//                .setSubject(email)                     // user identity
//                .claim("role", role)                  // custom claim
//                .setIssuedAt(new Date())              // token creation time
//                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION)) // expiry
//                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // sign token
//                .compact();
//    }
//
//    // Extract email (subject)
//    public String extractEmail(String token) {
//        return extractAllClaims(token).getSubject();
//    }
//
//    // Extract role from claims
//    public String extractRole(String token) {
//        return extractAllClaims(token).get("role", String.class);
//    }
//
//    // Validate token (signature + expiry)
//    public boolean validateToken(String token) {
//        try {
//
//            extractAllClaims(token);
//            return true;
//        } catch (Exception ex) {
//            // In production, replace with logger
//            System.out.println("JWT validation failed: " + ex.getMessage());
//            return false;
//        }
//    }
//
//    // Parse token and return claims
//    private Claims extractAllClaims(String token) {
//        System.out.println("SECRET: " + SECRET);
//        return Jwts.parserBuilder()
//                .setSigningKey(getSigningKey())
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }
//}