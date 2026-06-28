package com.example.gamifikasi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Respons setelah siswa submit kuis, berisi rekap nilai.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultResponse {

    private int correctCount;
    private int totalQuestions;

    /** Bintang sesi ini = jumlah soal benar pada sesi kuis yang baru selesai. */
    private int starsEarned;

    /** true jika skor/bintang kali ini lebih baik dari percobaan sebelumnya. */
    private boolean improved;

    /** Total bintang siswa setelah submit ini. */
    private int totalStars;

    /** Total poin yang diperoleh pada sesi kuis ini. */
    private int totalEarnedScore;

    /** Detail benar/salah per soal. */
    private List<AnswerResultDto> answerDetails;
}
