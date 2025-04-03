package com.tuneup.backend.controller;

import com.tuneup.backend.exception.TokenRefreshException;
import com.tuneup.backend.model.RefreshToken;
import com.tuneup.backend.model.Users;
import com.tuneup.backend.payload.request.LoginRequest;
import com.tuneup.backend.payload.request.SignupRequest;
import com.tuneup.backend.payload.request.TokenRefreshRequest;
import com.tuneup.backend.payload.response.JwtResponse;
import com.tuneup.backend.payload.response.MessageResponse;
import com.tuneup.backend.payload.response.TokenRefreshResponse;
import com.tuneup.backend.secutiry.services.JwtService;
import com.tuneup.backend.secutiry.services.RefreshTokenService;

import com.tuneup.backend.secutiry.services.UserDetailsImpl;
import com.tuneup.backend.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {
    //TODO: создать endpoint /verify — POST, принимает код, ищет токен в базе.

    private final AuthService authService;

    private final JwtService jwtService;

    private final PasswordEncoder encoder;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    public AuthController(AuthService authService, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.authService = authService;
        this.encoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        //TODO: При логине пропускать только тех, кто enabled == true
        UserDetailsImpl verifiedResponse = authService.verify(loginRequest);

        if (verifiedResponse == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else {
            String accessToken = jwtService.generateJwtFromUsername(verifiedResponse.getUsername());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(verifiedResponse.getId());

            return ResponseEntity.ok(new JwtResponse(
                    accessToken,
                    refreshToken.getToken(),
                    verifiedResponse.getId(),
                    verifiedResponse.getUsername(),
                    verifiedResponse.getEmail()));
        }
    }

    @PostMapping("/registration")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signupRequest) {
        //TODO: Настроить подтверждение почты

        if(authService.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Email already in use!"));
        }

        if(authService.existsByUsername(signupRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Username is already taken!"));
        }

        Users user = signupRequest.toEntity(encoder.encode(signupRequest.getPassword()));
        authService.registerUser(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtService.generateJwtFromUsername(user.getUsername());
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();
        refreshTokenService.deleteByUserId(userId);
        return ResponseEntity.ok(new MessageResponse("Log out successful!"));
    }
}
