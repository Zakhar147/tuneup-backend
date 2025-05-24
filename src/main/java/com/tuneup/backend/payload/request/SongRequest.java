package com.tuneup.backend.payload.request;

import com.tuneup.backend.model.Song;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SongRequest {
    private String title;
    private String artist;
    private String textAndChords;

    public Song toEntity(String filePath) {
        return new Song(title, artist, filePath, textAndChords);
    }
}
