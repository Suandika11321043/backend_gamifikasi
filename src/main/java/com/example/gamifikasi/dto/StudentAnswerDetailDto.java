package com.example.gamifikasi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Jawaban siswa untuk satu soal, dilengkapi detail soal dan opsinya.
 * Digunakan oleh endpoint GET /api/quiz/students/{studentId}/topics/{topicId}/answers.
 *
 * Jika siswa belum mengerjakan soal ini: correct = null, earnedScore = null, submittedAnswer = null.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentAnswerDetailDto {

    // ── Info soal ──────────────────────────────────
    private Long questionId;
    private String questionType;
    private String contentInstruction;
    private String contentImage;
    private String contentAudio;
    private Integer timeLimitMinutes;
    private Integer scorePoint;

    /** Opsi-opsi soal (tanpa kunci jawaban). */
    private List<QuestionWithOptionsDto.OptionDto> options;

    // ── Jawaban siswa ──────────────────────────────
    /** true = benar, false = salah, null = belum dikerjakan. */
    private Boolean correct;

    /** Poin yang diperoleh siswa. null jika belum dikerjakan. */
    private Integer earnedScore;

    /**
     * JSON jawaban yang dikirim siswa, sesuai tipe soal:
     *  - QUIZ/MULTIPLE_CHOICE : {"selectedOptionIds":[...]}
     *  - SORTING              : {"orderedOptionIds":[...]}
     *  - MATCHING/DRAG_AND_DROP : {"matchingPairs":{...}}
     * null jika belum dikerjakan.
     */
    private String submittedAnswer;
}
