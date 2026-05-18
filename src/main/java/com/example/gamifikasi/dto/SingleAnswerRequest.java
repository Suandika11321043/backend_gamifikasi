package com.example.gamifikasi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request untuk submit satu jawaban sekaligus (mode soal satu-per-satu).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SingleAnswerRequest {

    private Long studentId;
    private Long topicId;

    /** Jawaban untuk satu soal. */
    private AnswerRequest answer;
}
