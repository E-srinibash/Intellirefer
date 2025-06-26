package com.yourcompany.intellirefer.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // <-- Import this
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JWTAuthenticationFilter.class);

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String tokenHeader = request.getHeader("Authorization");

        if (StringUtils.hasText(tokenHeader) && tokenHeader.startsWith("Bearer ")) {
            String jwtToken = tokenHeader.substring(7);

            try {
                DecodedJWT decodedJWT = jwtTokenProvider.verifyToken(jwtToken);
                String userEmail = decodedJWT.getSubject();

                if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // === THIS IS THE FIX (PART 2) ===
                    // Extract the roles claim from the decoded token.
                    String rolesClaim = decodedJWT.getClaim("roles").asString();

                    // Create a list of authorities from the roles string.
                    var authorities = Arrays.stream(rolesClaim.split(","))
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    // Create the authentication token WITH the correct authorities.
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userEmail, // The principal can be the email string
                            null,
                            authorities // Pass the authorities list here
                    );

                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            } catch (Exception ex) {
                logger.error("JWT Authentication failed: {}", ex.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}