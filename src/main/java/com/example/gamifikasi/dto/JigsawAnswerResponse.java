package com.example.gamifikasi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Respons setelah siswa mengumpulkan jawaban Jigsaw Puzzle.
 * isCorrect = true hanya jika semua keping berada di posisi yang tepat.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JigsawAnswerResponse {
    private Boolean isCorrect;
    private Integer earnedScore;
    private Integer correctPiecesCount;
    private Integer totalPieces;
    /** Berapa pieceId dari request yang benar-benar ditemukan di puzzle ini. */
    private Integer foundPiecesCount;
    /** ID puzzle yang dievaluasi — cocokkan dengan GET /questions/{id}/puzzle. */
    private Long puzzleId;
    /** Detail per keping: apakah posisi masing-masing sudah benar? */
    private List<JigsawPieceResultDto> pieceResults;
}
