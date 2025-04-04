package com.tuneup.backend.services;

import com.tuneup.backend.model.Users;
import com.tuneup.backend.model.VerificationEmailToken;
import com.tuneup.backend.payload.request.LoginRequest;
import com.tuneup.backend.payload.request.SignupRequest;
import com.tuneup.backend.repo.UserRepo;
import com.tuneup.backend.repo.VerificationEmailTokenRepo;
import com.tuneup.backend.secutiry.services.UserDetailsImpl;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    //TODO: метод verifyEmailToken(String token)
    //TODO: переименовать verificationToken HA verificationCode

    private final AuthenticationManager authenticationManager;

    private final UserRepo userRepo;
    private final VerificationEmailTokenRepo verificationEmailTokenRepo;
    private final EmailService emailService;

    private final PasswordEncoder encoder;

    public String createUnverifiedUser(SignupRequest signupRequest) {
        Users existingUser = userRepo.findByEmail(signupRequest.getEmail()).orElse(null);

        if (existingUser != null) {
            verificationEmailTokenRepo.deleteByUser(existingUser);
            VerificationEmailToken verificationEmailCode = createVerificationEmailCodeForUser(existingUser);

            return sendVerificationEmailCode(existingUser.getEmail(), verificationEmailCode.getToken());

        } else {
            Users userEntity = signupRequest.toEntity(encoder.encode(signupRequest.getPassword()));
            Users user = createUserInDatabase(userEntity);
            VerificationEmailToken verificationEmailCode = createVerificationEmailCodeForUser(user);

            return sendVerificationEmailCode(user.getEmail(), verificationEmailCode.getToken());
        }

    }

    public Users createUserInDatabase(Users user) {
        return userRepo.save(user);
    }

    public VerificationEmailToken createVerificationEmailCodeForUser (Users user) {
        String verificationToken = generateNumericCode();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(1);

        VerificationEmailToken verificationEmailToken = new VerificationEmailToken(user, verificationToken, expiryDate);
        return verificationEmailTokenRepo.save(verificationEmailToken);
    }

    public String resendVerificationEmailCode(String email) {
        Optional<Users> optionalUser = userRepo.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return "User not found";
        }

        Users user = optionalUser.get();
        if (user.isEnabled()) {
            return "User is already verified";
        }

        verificationEmailTokenRepo.deleteByUser(user);
        VerificationEmailToken verificationEmailCode = createVerificationEmailCodeForUser(user);


        return sendVerificationEmailCode(user.getEmail(), verificationEmailCode.getToken());
    }

    public String sendVerificationEmailCode(String email, String verificationCode) {
        String subject = "Account Verification";
        String verificationCodeString = "VERIFICATION CODE " + verificationCode;
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCodeString + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendVerificationEmail(email, subject, htmlMessage);
            return "Code sent successfully";
        } catch (MessagingException e) {
            return "Failed to send verification code";
        }
    }

    public boolean existsByEmail(String email) {
        return userRepo.existsByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userRepo.existsByUsername(username);
    }


    public UserDetailsImpl verify(LoginRequest loginRequest) {
        String login = loginRequest.getLogin();
        String password = loginRequest.getPassword();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(login,password));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return authentication.getPrincipal() instanceof UserDetailsImpl ? (UserDetailsImpl) authentication.getPrincipal() : null;
    }

    private String generateNumericCode() {
        int code = (int)(Math.random() * 90000000) + 10000000;
        return String.valueOf(code);
    }


}
