package com.tuneup.backend.services;

import com.tuneup.backend.models.Users;
import com.tuneup.backend.payload.request.LoginRequest;
import com.tuneup.backend.repos.UserRepo;
import com.tuneup.backend.secutiry.services.JwtService;
import com.tuneup.backend.secutiry.services.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service

public class UserService {

    private UserRepo userRepo;

    private AuthenticationManager authenticationManager;

    private JwtService jwtService;

    @Autowired
    public UserService(UserRepo userRepo, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepo = userRepo;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }


    public Optional<Users> findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public List<Users> getAllUsers() {
        return userRepo.findAll();
    }

    public void createUser(Users user) {
        userRepo.save(user);
    }

    public UserDetailsImpl verify(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return authentication.getPrincipal() instanceof UserDetailsImpl ? (UserDetailsImpl) authentication.getPrincipal() : null;
    }
}
