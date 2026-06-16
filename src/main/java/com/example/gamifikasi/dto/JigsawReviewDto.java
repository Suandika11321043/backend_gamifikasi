package com.example.gamifikasi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Review hasil pengerjaan puzzle siswa – membandingkan jawaban siswa
 * dengan posisi yang sebenarnya (correctPosition).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JigsawReviewDto {

    private Long puzzleId;
    private Long questionId;
    private String imageUrl;
    private Integer gridRows;
    private Integer gridCols;
    private String contentInstruction;
    private Integer scorePoint;

    /** Apakah semua keping sudah di posisi yang benar? */
    private Boolean allCorrect;
    /** Jumlah keping yang benar. */
    private Integer correctPiecesCount;
    /** Total keping. */
    private Integer totalPieces;
    /** Skor yang diperoleh siswa. */
    private Integer earnedScore;

    /**
     * Detail per keping: posisi yang diletakkan siswa vs posisi yang benar.
     * Jika siswa belum mengerjakan puzzle ini, list ini akan kosong.
     */
    private List<PieceComparisonDto> pieces;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PieceComparisonDto {
        private Long pieceId;
        private Integer pieceIndex;
        private String pieceImageUrl;
        /** Posisi benar (0-based, row-major). */
        private Integer correctPosition;
        /**
         * Posisi yang diletakkan siswa. null jika siswa belum menempatkan keping ini.
         */
        private Integer studentPlacedPosition;
        /** true = siswa meletakkan keping di posisi yang benar. */
        private Boolean correct;
    }
}
