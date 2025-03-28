package com.tuneup.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "songs")
@NoArgsConstructor
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String artist;

    @Column(name = "tab_file_path")
    private String tabFilePath;

    public Song(String title, String artist, String tabFilePath) {
        this.title = title;
        this.artist = artist;
        this.tabFilePath = tabFilePath;
    }

    public String getTabFilePath() {
        return tabFilePath;
    }
}

