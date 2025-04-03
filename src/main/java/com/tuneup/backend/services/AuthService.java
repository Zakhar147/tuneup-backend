package com.tuneup.backend.services;

import com.tuneup.backend.model.Users;
import com.tuneup.backend.model.VerificationEmailToken;
import com.tuneup.backend.payload.request.LoginRequest;
import com.tuneup.backend.repo.UserRepo;
import com.tuneup.backend.repo.VerificationEmailTokenRepo;
import com.tuneup.backend.secutiry.services.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    //TODO: метод verifyEmailToken(String token)

    private final AuthenticationManager authenticationManager;
    private final UserRepo userRepo;
    private final VerificationEmailTokenRepo verificationEmailTokenRepo;

    public Users registerUser (Users user) {
        String verificationToken = generateNumericCode();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(10);

        VerificationEmailToken verificationEmailToken = new VerificationEmailToken(user, verificationToken, expiryDate);
        verificationEmailTokenRepo.save(verificationEmailToken);

        //TODO: отправить письмо
        //

        return userRepo.save(user);
    }

    public UserDetailsImpl verify(LoginRequest loginRequest) {
        String login = loginRequest.getLogin();
        String password = loginRequest.getPassword();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(login,password));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return authentication.getPrincipal() instanceof UserDetailsImpl ? (UserDetailsImpl) authentication.getPrincipal() : null;
    }

    public boolean existsByEmail(String email) {
        return userRepo.existsByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userRepo.existsByUsername(username);
    }

    private String generateNumericCode() {
        int code = (int)(Math.random() * 90000000) + 10000000;
        return String.valueOf(code);
    }

}
