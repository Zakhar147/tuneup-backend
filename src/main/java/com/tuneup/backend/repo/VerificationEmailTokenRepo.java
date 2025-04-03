package com.tuneup.backend.repo;

import com.tuneup.backend.model.VerificationEmailToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationEmailTokenRepo extends JpaRepository<VerificationEmailToken, Long> {
    Optional<VerificationEmailToken> findByToken(String token);
}

