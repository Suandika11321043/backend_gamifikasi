package com.example.gamifikasi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request untuk mengakhiri sesi kuis setelah semua soal dijawab satu-per-satu.
 * Client mengirimkan jumlah jawaban benar yang telah dikumpulkan dari tiap SingleAnswerResponse.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizFinishRequest {

    private Long studentId;
    private Long topicId;
    private int correctCount;
    private int totalQuestions;
}
