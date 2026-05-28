package com.example.gamifikasi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Entitas untuk soal tipe Jigsaw Puzzle.
 * Menyimpan konfigurasi puzzle (gambar asli + ukuran grid) yang terhubung ke
 * satu Questions.
 */
@Entity
@Getter
@Setter
@Table(name = "jigsaw_puzzle")
public class JigsawPuzzle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Soal induk bertipe JIGSAW. */
    @OneToOne
    @JoinColumn(name = "question_id", unique = true, nullable = false)
    private Questions question;

    /** URL gambar asli sebelum dipotong menjadi keping. */
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    /** Jumlah baris grid (contoh: 2, 3, 4). */
    @Column(name = "grid_rows", nullable = false)
    private Integer gridRows;

    /** Jumlah kolom grid (contoh: 2, 3, 4). */
    @Column(name = "grid_cols", nullable = false)
    private Integer gridCols;

    /** Daftar keping puzzle (total = gridRows * gridCols). */
    @OneToMany(mappedBy = "puzzle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JigsawPiece> pieces = new ArrayList<>();
}
