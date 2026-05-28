package com.example.gamifikasi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Satu keping (piece) dari JigsawPuzzle.
 *
 * pieceIndex      – label keping ini (0-based, dikirim ke siswa dalam urutan acak)
 * correctPosition – posisi grid yang benar untuk keping ini (0-based, row-major)
 *                   contoh grid 3×3: posisi 0=kiri-atas, 1=tengah-atas, ..., 8=kanan-bawah
 * pieceImageUrl   – URL gambar keping yang sudah dipotong (opsional, bisa di-generate di frontend)
 */
@Entity
@Getter
@Setter
@Table(name = "jigsaw_piece")
public class JigsawPiece {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "puzzle_id", nullable = false)
    private JigsawPuzzle puzzle;

    /** Label unik keping dalam puzzle ini (0 hingga rows*cols - 1). */
    @Column(name = "piece_index", nullable = false)
    private Integer pieceIndex;

    /** Posisi benar di grid (0-based, row-major order). */
    @Column(name = "correct_position", nullable = false)
    private Integer correctPosition;

    /** URL gambar keping yang sudah di-crop (opsional). */
    @Column(name = "piece_image_url")
    private String pieceImageUrl;
}
