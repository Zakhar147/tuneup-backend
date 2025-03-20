package com.tuneup.backend.repos;

import com.tuneup.backend.models.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface SongRepo extends JpaRepository<Song, Long> {
}
