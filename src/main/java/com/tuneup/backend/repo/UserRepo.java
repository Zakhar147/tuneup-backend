package com.tuneup.backend.repo;

import com.tuneup.backend.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<Users, Long> {
   Optional<Users> findByUsername(String username);
   Optional<Users> findByEmail(String email);
   boolean existsByUsername(String username);
   boolean existsByEmail(String email);
}
