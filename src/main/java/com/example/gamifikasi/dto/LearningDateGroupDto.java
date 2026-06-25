package com.example.gamifikasi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * Kumpulan soal dalam satu tanggal belajar, dilengkapi status pengerjaan siswa.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LearningDateGroupDto {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate learningDate;

    /** Ketersediaan soal untuk tanggal ini (diatur per learning date, bukan per soal). */
    private Boolean isAvailable;

    /** Jumlah soal yang dijawab benar pada tanggal ini. */
    private Integer starCount;

    private List<QuestionWithStatusDto> questions;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionWithStatusDto {
        private Long id;
        private Long topicId;
        private LocalDate learningDate;
        private String questionType;
        private String contentInstruction;
        private String contentImage;
        private String contentAudio;
        private Integer timeLimitMinutes;
        private Integer scorePoint;
        private Boolean isAvailable;
        /** null jika belum dijawab; true/false jika sudah selesai. */
        private Boolean correct;
        /**
         * "SELESAI"  → siswa sudah mengerjakan soal ini
         * "BELUM"    → siswa belum mengerjakan soal ini
         */
        private String status;
    }
}
