package com.tuneup.backend.repos;

import com.tuneup.backend.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<Users, Long> {
   Optional<Users> findByUsername(String username);
   Optional<Users> findByEmail(String email);
}
