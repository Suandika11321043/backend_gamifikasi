package com.example.gamifikasi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO untuk menampilkan jawaban yang sudah dikerjakan siswa pada satu soal.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentAnswerViewDto {
    private Long questionId;
    private boolean correct;
    private int earnedScore;
    /** JSON string jawaban yang dikirim siswa (selectedOptionIds / orderedOptionIds / matchingPairs). */
    private String submittedAnswer;
}
