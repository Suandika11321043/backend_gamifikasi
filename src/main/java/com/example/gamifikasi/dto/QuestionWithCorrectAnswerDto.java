package com.example.gamifikasi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Soal beserta opsi dan kunci jawaban yang benar.
 * Digunakan oleh endpoint admin/review – JANGAN kirim ke siswa saat kuis berlangsung.
 *
 * - QUIZ / MULTIPLE_CHOICE : cek field kunciJawaban = true pada setiap opsi
 * - SORTING / ORDERING     : cek field urutanBenar pada setiap opsi
 * - MATCHING / DRAG_AND_DROP : lihat field correctPairs
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionWithCorrectAnswerDto {

    private Long questionId;
    private String questionType;
    private String contentInstruction;
    private String contentImage;
    private String contentAudio;
    private Integer timeLimitMinutes;
    private Integer scorePoint;

    /** Semua opsi beserta informasi kunci jawaban. */
    private List<OptionWithAnswerDto> options;

    /**
     * Pasangan benar untuk tipe MATCHING / DRAG_AND_DROP.
     * Kosong ([]) untuk tipe soal lain.
     */
    private List<MatchingPairDto> correctPairs;

    /**
     * Jawaban benar untuk tipe PUZZLE: berisi keping dengan correctPosition terisi.
     * null untuk tipe soal lain.
     */
    private PuzzleAnswerDto puzzleAnswer;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionWithAnswerDto {
        private Long optionId;
        private String teksOpsi;
        private String mediaOpsi;
        /** PERTANYAAN atau JAWABAN */
        private String tipeItem;
        /** true = ini jawaban benar (untuk QUIZ / MULTIPLE_CHOICE) */
        private Boolean kunciJawaban;
        /** Nomor urut benar 1-based (untuk SORTING / ORDERING). null jika bukan tipe urutan. */
        private Integer urutanBenar;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchingPairDto {
        private Long opsiPertanyaanId;
        private Long opsiJawabanId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PuzzleAnswerDto {
        private Long puzzleId;
        private String imageUrl;
        private Integer gridRows;
        private Integer gridCols;
        /** Keping dengan correctPosition terisi (kunci jawaban posisi yang benar). */
        private List<PiecePosDto> pieces;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PiecePosDto {
        private Long pieceId;
        private Integer pieceIndex;
        private Integer correctPosition;
        private String pieceImageUrl;
    }
}
