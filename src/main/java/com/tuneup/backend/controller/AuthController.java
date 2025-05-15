package com.tuneup.backend.controller;

import com.tuneup.backend.exception.TokenRefreshException;
import com.tuneup.backend.model.RefreshToken;
import com.tuneup.backend.model.Users;
import com.tuneup.backend.payload.request.*;
import com.tuneup.backend.payload.response.JwtResponse;
import com.tuneup.backend.payload.response.MessageResponse;
import com.tuneup.backend.payload.response.TokenRefreshResponse;
import com.tuneup.backend.secutiry.services.JwtService;
import com.tuneup.backend.secutiry.services.RefreshTokenService;

import com.tuneup.backend.secutiry.services.UserDetailsImpl;
import com.tuneup.backend.services.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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


    //TODO: добавить в энд поинт /registration
    @PostMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestBody CheckUsernameRequest checkUsernameRequest) {
        if(authService.existsByUsername(checkUsernameRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Username is already taken!"));
        }

        return ResponseEntity.ok(new MessageResponse("OK"));
    }

    //TODO: добавить в энд поинт /registration
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
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        log.info("login request {}", loginRequest.toString());
        UserDetailsImpl verifiedResponse = authService.verify(loginRequest);

        if (verifiedResponse == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Invalid login or password"));
        } else {
            String accessToken = jwtService.generateJwtFromUsername(verifiedResponse.getUsername());
            refreshTokenService.deleteByUserId(verifiedResponse.getId());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(verifiedResponse.getId());

            ResponseCookie refreshCookie = refreshTokenService.createRefreshTokenCookie(refreshToken.getToken());
            response.addHeader("Set-Cookie", refreshCookie.toString());

            return ResponseEntity.ok(new JwtResponse(
                    accessToken,
                    verifiedResponse.getUsername(),
                    verifiedResponse.getEmail()));
        }
    }

    //TODO: добавить в энд поинт /login
    @PostMapping("/check-login")
    public ResponseEntity<?> checkLoginExists(@RequestBody CheckLoginRequest  loginRequest) {
        String login = loginRequest.getLogin();

        boolean exists = authService.existsByUsername(login) || authService.existsByEmail(login);

        if(!exists) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Username or email does not exist"));
        }

        return ResponseEntity.ok(new MessageResponse("OK"));
    }

    @PostMapping("/refresh-access-token")
    public ResponseEntity<?> refreshToken(@CookieValue(name = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Refresh token is missing"));
        }
        RefreshToken token = refreshTokenService.findByToken(refreshToken)
                .map(refreshTokenService::verifyExpiration)
                .orElseThrow(() -> new TokenRefreshException(refreshToken, "Refresh token is not in database or expired!"));

        log.info("Refresh token: " + token.toString());

        Users user = token.getUser();

        refreshTokenService.deleteByUserId(user.getId());
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

        ResponseCookie refreshCookie = refreshTokenService.createRefreshTokenCookie(newRefreshToken.getToken());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        String accessToken = jwtService.generateJwtFromUsername(user.getUsername());

        return ResponseEntity.ok(new TokenRefreshResponse(accessToken));
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
    public ResponseEntity<?> logoutUser(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                                        HttpServletResponse response) {
        if (refreshToken != null) {
            refreshTokenService.findByToken(refreshToken).ifPresent(token -> {
                refreshTokenService.deleteByUserId(token.getUser().getId());
            });

            // Удаляем куку с токеном
            ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                    .path("/api/auth")
                    .httpOnly(true)
                    .secure(false) // поставь true в проде
                    .maxAge(0)
                    .sameSite("Lax")
                    .build();

            response.addHeader("Set-Cookie", deleteCookie.toString());

            return ResponseEntity.ok(new MessageResponse("Logout successful"));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Refresh token is missing"));
        }
    }
}