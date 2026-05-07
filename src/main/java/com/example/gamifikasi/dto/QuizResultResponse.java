package com.example.gamifikasi.dto;

import com.example.gamifikasi.entity.RankLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Respons setelah siswa submit kuis, berisi rekap nilai dan info rank terkini.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultResponse {

    private int correctCount;
    private int totalQuestions;

    /** Bintang yang diperoleh untuk topik ini (0–3). */
    private int starsEarned;

    /** true jika skor/bintang kali ini lebih baik dari percobaan sebelumnya. */
    private boolean improved;

    /** Total bintang siswa setelah submit ini. */
    private int totalStars;

    /** Rank siswa setelah submit ini. */
    private RankLevel rankName;

    /** Detail benar/salah per soal. */
    private List<AnswerResultDto> answerDetails;
}
