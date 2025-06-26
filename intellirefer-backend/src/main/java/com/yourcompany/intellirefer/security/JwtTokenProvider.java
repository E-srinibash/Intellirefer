package com.yourcompany.intellirefer.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority; // <-- Import this
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.stream.Collectors; // <-- Import this

@Service
public class JwtTokenProvider {

    @Value("${jwt.algorithm.key}")
    private String algorithmKey;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.expiry.duration}")
    private long expiryDuration;

    private Algorithm algorithm;
    private JWTVerifier verifier;

    @PostConstruct
    public void init() throws UnsupportedEncodingException {
        this.algorithm = Algorithm.HMAC256(algorithmKey);
        this.verifier = JWT.require(algorithm)
                .withIssuer(issuer)
                .build();
    }

    public String generateToken(Authentication authentication) {
        String email = authentication.getName();

        // === THIS IS THE FIX (PART 1) ===
        // Extract the user's roles from the Authentication object.
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")); // e.g., "ROLE_EMPLOYEE,ROLE_USER"

        return JWT.create()
                .withSubject(email)
                .withIssuer(issuer)
                .withClaim("roles", roles) // <-- Embed the roles into the token
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expiryDuration))
                .sign(algorithm);
    }

    public DecodedJWT verifyToken(String token) throws JWTVerificationException {
        return verifier.verify(token);
    }
}