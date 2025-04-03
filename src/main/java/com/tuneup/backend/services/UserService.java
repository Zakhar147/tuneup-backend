package com.tuneup.backend.services;

import com.tuneup.backend.model.Users;
import com.tuneup.backend.payload.request.LoginRequest;
import com.tuneup.backend.repo.UserRepo;
import com.tuneup.backend.secutiry.services.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;

    private final AuthenticationManager authenticationManager;

    public List<Users> getAllUsers() {
        return userRepo.findAll();
    }

}
