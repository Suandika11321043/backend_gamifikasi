package com.example.gamifikasi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request payload untuk pengumpulan (submit) jawaban kuis satu topik.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmitRequest {

    private Long studentId;
    private Long topicId;

    /** Daftar jawaban siswa, satu AnswerRequest per soal. */
    private List<AnswerRequest> answers;
}
