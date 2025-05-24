package com.tuneup.backend.controller;

import com.tuneup.backend.model.Song;
import com.tuneup.backend.payload.request.SongRequest;
import com.tuneup.backend.services.FileStoreService;
import com.tuneup.backend.services.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;


import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
@CrossOrigin
public class SongController {

    private final SongService songService;

    private final FileStoreService fileStoreService;

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

    @GetMapping
    public List<Song> geAllSongs() {
        return songService.getAllSongs();
    }

    @GetMapping("/paged")
    public Page<Song> getPagedSongs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return songService.getPagedSongs(page, size);
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSong(@PathVariable Long id) {
        songService.deleteSong(id);
        return ResponseEntity.noContent().build();
    }

    //TODO: Для тестирования. Потом можно убрать.
    @PostMapping("/generate")
    public ResponseEntity<String> generateSongs() {
        String testTabFilePath = "tabs/1f911043-fdae-40c6-ab23-117968901a22_canon(3).gp";

        String defaultTextAndChords = """
        [Intro]
        C     G       Am      F
        Починається шалений біт

        [Verse]
        Am     G          F          C
        Я прийшов на репетицію з лопато́й
        F       G           C
        А тепер я в шоубізнесі

        [Chorus]
        Am     C/G        F          C
        Просто бий в бочку — і все буде добре!
        """.trim();

        List<String[]> funnySongs = List.of(
                new String[]{"Сева Бакутов", "Me suda la Polla"},
                new String[]{"DJ Ковбаса", "Життя на мінімалках"},
                new String[]{"MC Дупа", "Шо ти робиш, вийди з хати"},
                new String[]{"DJ Нюхач", "Паті в маршрутці"},
                new String[]{"Йосип Жужик", "Магнітола з базару"},
                new String[]{"Lil Вєня", "Трабли з фізрою"},
                new String[]{"Баба Клава", "Мелодія звонка"},
                new String[]{"Гурт Сало", "Копчений реп"},
                new String[]{"DJ Пельмєнь", "Дим в під’їзді"},
                new String[]{"MC Піксель", "Wi-Fi без пароля"},
                new String[]{"Сева Бакутов", "Me suda la Polla"},
                new String[]{"DJ Ковбаса", "Життя на мінімалках"},
                new String[]{"MC Дупа", "Шо ти робиш, вийди з хати"},
                new String[]{"DJ Нюхач", "Паті в маршрутці"},
                new String[]{"Йосип Жужик", "Магнітола з базару"},
                new String[]{"Lil Вєня", "Трабли з фізрою"},
                new String[]{"Баба Клава", "Мелодія звонка"},
                new String[]{"Гурт Сало", "Копчений реп"},
                new String[]{"DJ Пельмєнь", "Дим в під’їзді"},
                new String[]{"MC Піксель", "Wi-Fi без пароля"},
                new String[]{"Сева Бакутов", "Me suda la Polla"},
                new String[]{"DJ Ковбаса", "Життя на мінімалках"},
                new String[]{"MC Дупа", "Шо ти робиш, вийди з хати"},
                new String[]{"DJ Нюхач", "Паті в маршрутці"},
                new String[]{"Йосип Жужик", "Магнітола з базару"},
                new String[]{"Lil Вєня", "Трабли з фізрою"},
                new String[]{"Баба Клава", "Мелодія звонка"},
                new String[]{"Гурт Сало", "Копчений реп"},
                new String[]{"DJ Пельмєнь", "Дим в під’їзді"},
                new String[]{"MC Піксель", "Wi-Fi без пароля"}
        );

        for (String[] entry : funnySongs) {
            String artist = entry[0];
            String title = entry[1];

            Song song = new Song(title, artist, testTabFilePath, defaultTextAndChords);
            songService.saveSong(song);
        }

        return ResponseEntity.ok(funnySongs.size() + " funny songs created.");
    }


}
