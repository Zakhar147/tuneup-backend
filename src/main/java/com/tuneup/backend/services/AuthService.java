package com.tuneup.backend.services;

import com.tuneup.backend.model.Users;
import com.tuneup.backend.model.VerificationEmailToken;
import com.tuneup.backend.payload.request.LoginRequest;
import com.tuneup.backend.payload.request.SignupRequest;
import com.tuneup.backend.repo.UserRepo;
import com.tuneup.backend.repo.VerificationEmailTokenRepo;
import com.tuneup.backend.secutiry.services.UserDetailsImpl;
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

    private final AuthenticationManager authenticationManager;

    private final UserRepo userRepo;
    private final VerificationEmailTokenRepo verificationEmailTokenRepo;

    private final PasswordEncoder encoder;

    public void createUnverifiedUser(SignupRequest signupRequest) {
        Users existingUser = userRepo.findByEmail(signupRequest.getEmail()).orElse(null);

        if (existingUser != null) {
            verificationEmailTokenRepo.deleteByUser(existingUser);
            createVerificationEmailTokenForUser(existingUser);

        } else {
            Users userEntity = signupRequest.toEntity(encoder.encode(signupRequest.getPassword()));
            Users user = createUserInDatabase(userEntity);
            createVerificationEmailTokenForUser(user);
        }


    }

    public Users createUserInDatabase(Users user) {
        return userRepo.save(user);
    }

    public void createVerificationEmailTokenForUser(Users user) {
        String verificationToken = generateNumericCode();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(10);

        VerificationEmailToken verificationEmailToken = new VerificationEmailToken(user, verificationToken, expiryDate);
        verificationEmailTokenRepo.save(verificationEmailToken);

        log.info("📬 Verification code entity: {}", verificationEmailToken.toString());
    }

    public boolean existsByEmail(String email) {
        Optional<Users> userOptional = userRepo.findByEmail(email);

        return userOptional.isPresent() && userOptional.get().isEnabled();
    }

    public boolean existsByUsername(String username) {
        Optional<Users> userOptional = userRepo.findByUsername(username);

        return userOptional.isPresent() && userOptional.get().isEnabled();
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
