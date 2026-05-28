package com.example.gamifikasi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO untuk satu keping (piece) puzzle.
 * Saat dikirim ke siswa, correctPosition di-set null agar kunci jawaban tidak
 * bocor.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JigsawPieceDto {
    private Long id;
    private Long puzzleId;
    private Integer pieceIndex;
    /** Null ketika dikirim ke siswa (disembunyikan). */
    private Integer correctPosition;
    private String pieceImageUrl;
}
