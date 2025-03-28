package com.tuneup.backend.services;

import com.tuneup.backend.model.Song;
import com.tuneup.backend.repo.SongRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SongService {
    private final SongRepo songRepository;

    public List<Song> getAllSongs() {
        return songRepository.findAll();
    }

    public Optional<Song> getSongById(long id) {
        return songRepository.findById(id);
    }

    public Song saveSong(Song song) {
        return songRepository.save(song);
    }

    public void deleteSong(long id) {
        songRepository.deleteById(id);
    }

    public Optional<Song> findById(Long id) {
        return songRepository.findById(id);
    }
}
