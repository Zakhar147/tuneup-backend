package com.tuneup.backend.secutiry.services;

import com.tuneup.backend.model.Users;
import com.tuneup.backend.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        if (login == null || login.isBlank()) {
            throw new UsernameNotFoundException("Login is null or empty");
        }

        Users user = userRepo.findByUsername(login)
                .orElseGet(() -> userRepo.findByEmail(login)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found")));

        return new UserDetailsImpl(user);
    }
}
