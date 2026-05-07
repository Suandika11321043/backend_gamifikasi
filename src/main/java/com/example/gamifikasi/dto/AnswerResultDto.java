package com.example.gamifikasi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Hasil penilaian untuk satu soal.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResultDto {

    private Long questionId;
    private boolean correct;
    private int earnedScore;
}
