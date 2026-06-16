package com.example.gamifikasi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * Riwayat jawaban siswa untuk satu soal, lengkap dengan info topik, soal, dan opsi.
 * Digunakan oleh endpoint GET /api/questions/students/{studentId}/history.
 */
@Getter
@Setter
@NoArgsConstructor
public class StudentAnswerHistoryDto {

    // ── Info jawaban ───────────────────────────────
    private Long answerId;
    private Boolean correct;
    private Integer earnedScore;
    private String submittedAnswer;

    // ── Info soal ──────────────────────────────────
    private Long questionId;
    private String questionType;
    private String contentInstruction;
    private String contentImage;
    private String contentAudio;
    private Integer timeLimitMinutes;
    private Integer scorePoint;
    private LocalDate learningDate;

    // ── Opsi soal (QUIZ / MATCHING / SORTING / DRAG_AND_DROP) ─────────────────
    private List<QuestionWithOptionsDto.OptionDto> options;

    // ── Info puzzle (hanya untuk tipe PUZZLE) ────────────────────────────────
    private PuzzleInfoDto puzzleInfo;

    // ── Info topik ─────────────────────────────────
    private Long topicId;
    private String topicName;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PuzzleInfoDto {
        private Long puzzleId;
        private String imageUrl;
        private Integer gridRows;
        private Integer gridCols;
        private List<PieceDto> pieces;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PieceDto {
        private Long pieceId;
        private Integer pieceIndex;
        private Integer correctPosition;
        private String pieceImageUrl;
    }
}
