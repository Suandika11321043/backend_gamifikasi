package com.example.gamifikasi.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO untuk membuat atau menampilkan data JigsawPuzzle.
 * Field soal (contentInstruction, dll.) diisi saat menampilkan ke
 * student/admin.
 * Saat create: cukup isi questionId, gridRows, gridCols (+ image upload).
 */
@Getter
@Setter
@NoArgsConstructor
public class JigsawPuzzleDto {
    // ─── Data puzzle ───────────────────────────────────────────────────────
    private Long id;
    private Long questionId;
    private String imageUrl;
    private Integer gridRows;
    private Integer gridCols;
    private List<JigsawPieceDto> pieces;

    // ─── Detail soal (diisi saat GET, null saat POST/PUT) ─────────────────
    private String contentInstruction;
    private String contentImage;
    private String contentAudio;
    private Integer timeLimitMinutes;
    private Integer scorePoint;
}
