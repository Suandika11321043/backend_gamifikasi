package com.example.gamifikasi.controller;

import com.example.gamifikasi.dto.*;
import com.example.gamifikasi.entity.StudentRank;
import com.example.gamifikasi.entity.StudentScore;
import com.example.gamifikasi.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller untuk mekanisme pengerjaan kuis (quiz).
 *
 * Endpoints:
 *  GET  /api/quiz/topics/{topicId}/questions          → ambil soal-soal satu topik
 *  POST /api/quiz/submit/answer                       → submit satu jawaban (mode satu-per-satu)
 *  POST /api/quiz/finish                              → selesaikan kuis & hitung bintang
 *  POST /api/quiz/submit                              → (legacy) submit semua jawaban sekaligus
 *  GET  /api/quiz/scores/students/{studentId}         → semua skor siswa
 *  GET  /api/quiz/scores/students/{studentId}/topics/{topicId} → skor per topik
 *  GET  /api/quiz/ranks                               → leaderboard semua rank
 *  GET  /api/quiz/ranks/students/{studentId}          → rank satu siswa
 */
@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    @Autowired
    private QuizService quizService;

    // ─── Ambil soal ─────────────────────────────────────────────

    @GetMapping("/topics/{topicId}/questions")
    public ResponseEntity<List<QuestionWithOptionsDto>> getQuestions(
            @PathVariable Long topicId) {
        return ResponseEntity.ok(quizService.getQuestionsByTopic(topicId));
    }

    // ─── Submit satu jawaban (mode soal satu-per-satu) ──────────

    /**
     * Submit jawaban untuk satu soal. Kembalikan hasil langsung (benar/salah).
     * Panggil untuk setiap soal secara berurutan.
     */
    @PostMapping("/submit/answer")
    public ResponseEntity<SingleAnswerResponse> submitSingleAnswer(
            @RequestBody SingleAnswerRequest request) {
        return ResponseEntity.ok(quizService.submitSingleAnswer(request));
    }

    /**
     * Dipanggil setelah semua soal selesai dijawab satu-per-satu.
     * Hitung bintang, update StudentScore & StudentRank, kembalikan hasil akhir.
     */
    @PostMapping("/finish")
    public ResponseEntity<QuizResultResponse> finishQuiz(
            @RequestBody QuizFinishRequest request) {
        return ResponseEntity.ok(quizService.finishQuiz(request));
    }

    // ─── Submit semua jawaban sekaligus (legacy) ─────────────────

    @PostMapping("/submit")
    public ResponseEntity<QuizResultResponse> submitQuiz(
            @RequestBody QuizSubmitRequest request) {
        return ResponseEntity.ok(quizService.submitQuiz(request));
    }

    // ─── DEBUG: cek detail soal dari DB ──────────────────────────
    @GetMapping("/debug/question/{questionId}")
    public ResponseEntity<java.util.Map<String, Object>> debugQuestion(
            @PathVariable Long questionId) {
        return ResponseEntity.ok(quizService.debugQuestion(questionId));
    }

    // ─── Skor siswa ──────────────────────────────────────────────

    @GetMapping("/scores/students/{studentId}")
    public ResponseEntity<List<StudentScore>> getScoresByStudent(
            @PathVariable Long studentId) {
        return ResponseEntity.ok(quizService.getScoresByStudent(studentId));
    }

    @GetMapping("/scores/students/{studentId}/topics/{topicId}")
    public ResponseEntity<StudentScore> getScoreByStudentAndTopic(
            @PathVariable Long studentId,
            @PathVariable Long topicId) {
        return quizService.getScoreByStudentAndTopic(studentId, topicId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── Rank ─────────────────────────────────────────────────────

    @GetMapping("/ranks")
    public ResponseEntity<List<StudentRank>> getAllRanks() {
        return ResponseEntity.ok(quizService.getAllRanks());
    }

    @GetMapping("/ranks/students/{studentId}")
    public ResponseEntity<StudentRank> getRankByStudent(
            @PathVariable Long studentId) {
        return quizService.getRankByStudent(studentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
