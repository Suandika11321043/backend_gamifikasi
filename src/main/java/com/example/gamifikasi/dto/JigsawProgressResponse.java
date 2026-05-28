package com.example.gamifikasi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Respons progress puzzle untuk siswa.
 * correctPosition pada setiap pieceResult sengaja disembunyikan (null)
 * agar kunci jawaban tidak bocor ke frontend.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JigsawProgressResponse {
    /** Jumlah keping yang sudah diletakkan di posisi benar. */
    private Integer correctPiecesCount;
    /** Total keping puzzle. */
    private Integer totalPieces;
    /**
     * Persentase keping yang sudah benar (0–100).
     * progressPercent = correctPiecesCount / totalPieces * 100
     */
    private Double progressPercent;
    /**
     * Detail per-keping: apakah posisi saat ini sudah benar? correctPosition =
     * null.
     */
    private List<JigsawPieceResultDto> pieceResults;
}
