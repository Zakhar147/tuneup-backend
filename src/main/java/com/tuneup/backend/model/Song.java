package com.tuneup.backend.model;

import jakarta.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "songs")
@NoArgsConstructor
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String artist;

    @Column(name = "tab_file_path")
    private String tabFilePath;

    @Lob
    @Column(name = "text_and_chords", columnDefinition = "TEXT")
    private String textAndChords;

    public Song(String title, String artist, String tabFilePath, String textAndChords) {
        this.title = title;
        this.artist = artist;
        this.tabFilePath = tabFilePath;
        this.textAndChords = textAndChords;
    }

    public String getTabFilePath() {
        return tabFilePath;
    }
}

