package com.tuneup.backend.controller;

import com.tuneup.backend.model.Song;
import com.tuneup.backend.payload.request.SongRequest;
import com.tuneup.backend.services.FileStoreService;
import com.tuneup.backend.services.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;


import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
@CrossOrigin
public class SongController {

    private final SongService songService;

    private final FileStoreService fileStoreService;

    @GetMapping
    public List<Song> geAllSongs() {
        return songService.getAllSongs();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Song> getSongById(@PathVariable Long id) {
        Optional<Song> song = songService.getSongById(id);
        return song.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/tab")
    public ResponseEntity<Resource> serveTab(@PathVariable Long id) throws MalformedURLException {
        Optional<Song> songOptional = songService.findById(id);

        if (songOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Song song = songOptional.get();
        String pathString = song.getTabFilePath();
        Path filePath = Paths.get(pathString);
        Resource file = new UrlResource(filePath.toUri());

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }


    @PostMapping
    public ResponseEntity<String> receiveSong(@ModelAttribute SongRequest songRequest, @RequestParam("file") MultipartFile file) {
        //TODO: Написать класс songResponse(если нужен)
        try {
            String publicPath = fileStoreService.storeFile(file);

            Song song = songRequest.toEntity(publicPath);

            songService.saveSong(song);

            return ResponseEntity.ok("Данные получены и выведены в лог");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при сохранении песни");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSong(@PathVariable Long id) {
        songService.deleteSong(id);
        return ResponseEntity.noContent().build();
    }
}
