package com.tuneup.backend.controller;

import com.tuneup.backend.exception.TokenRefreshException;
import com.tuneup.backend.model.RefreshToken;
import com.tuneup.backend.payload.request.*;
import com.tuneup.backend.payload.response.JwtResponse;
import com.tuneup.backend.payload.response.MessageResponse;
import com.tuneup.backend.payload.response.TokenRefreshResponse;
import com.tuneup.backend.secutiry.services.JwtService;
import com.tuneup.backend.secutiry.services.RefreshTokenService;

import com.tuneup.backend.secutiry.services.UserDetailsImpl;
import com.tuneup.backend.services.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
@Slf4j
public class AuthController {

    private final AuthService authService;

    private final JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/registration")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signupRequest) {
        String result = authService.createUnverifiedUser(signupRequest);

        return ResponseEntity.ok(new MessageResponse(result));
    }

    @PostMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestBody CheckUsernameRequest checkUsernameRequest) {
        if(authService.existsByUsername(checkUsernameRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Username is already taken!"));
        }

        return ResponseEntity.ok(new MessageResponse("OK"));
    }

    @PostMapping("/check-email")
    public ResponseEntity<?> checkUser(@RequestBody CheckEmailRequest checkUserRequest) {
        if(authService.existsByEmail(checkUserRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Email already in use!"));
        }

        return ResponseEntity.ok(new MessageResponse("OK"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
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

    @PostMapping("/refresh-access-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
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

    @PostMapping("/registration/resendCode")
    public ResponseEntity<?> resendVerificationCode(@RequestBody EmailRequest emailRequest) {
        String result = authService.resendVerificationCode(emailRequest.getEmail());

        return ResponseEntity.ok(new MessageResponse(result));
    }

    @PostMapping("/registration/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerificationEmailRequest verificationRequest) {
        String result = authService.verifyEmailCode(verificationRequest.getEmail(), verificationRequest.getVerificationCode());
        if(result.equals("Incorrect verification code") || result.equals("Verification code expired or not found")) {
            return ResponseEntity.badRequest().body(new MessageResponse(result));
        }else {
            return ResponseEntity.ok(new MessageResponse(result));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();
        refreshTokenService.deleteByUserId(userId);
        return ResponseEntity.ok(new MessageResponse("Log out successful!"));
    }
}