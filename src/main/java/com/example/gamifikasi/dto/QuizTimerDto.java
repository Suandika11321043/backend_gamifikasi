package com.example.gamifikasi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** DTO untuk menyimpan dan mengambil sisa waktu timer per soal. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizTimerDto {

    private Long studentId;
    private Long topicId;
    private Long questionId;

    /** Sisa waktu dalam detik. */
    private Integer remainingSeconds;

    /** Waktu terakhir disimpan (diisi oleh server saat response). */
    private LocalDateTime updatedAt;
}
