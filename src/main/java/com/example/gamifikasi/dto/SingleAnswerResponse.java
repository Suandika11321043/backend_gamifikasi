package com.example.gamifikasi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Respons setelah siswa submit satu jawaban (mode soal satu-per-satu).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SingleAnswerResponse {

    private Long questionId;
    private boolean correct;
    private int earnedScore;
}
