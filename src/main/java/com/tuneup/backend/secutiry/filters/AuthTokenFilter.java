package com.tuneup.backend.secutiry.filters;

import com.tuneup.backend.secutiry.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class AuthTokenFilter extends OncePerRequestFilter {

    private  static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = this.parseJwt(request);

            if(jwt != null &&  jwtService.validateJwtToken(jwt)) {
                String username = jwtService.getUsernameFromJwt(jwt);

                logger.info("Authenticated user: {}", username);
            }

        }catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if(StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}
