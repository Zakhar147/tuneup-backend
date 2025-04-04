package com.tuneup.backend.repo;

import com.tuneup.backend.model.Users;
import com.tuneup.backend.model.VerificationEmailToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationEmailTokenRepo extends JpaRepository<VerificationEmailToken, Long> {
    Optional<VerificationEmailToken> findByToken(String token);

    @Transactional
    void deleteByUser(Users existingUser);
}

