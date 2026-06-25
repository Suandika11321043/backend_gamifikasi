package com.example.gamifikasi.controller;

import com.example.gamifikasi.dto.QuizTimerDto;
import com.example.gamifikasi.service.QuizTimerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints untuk persistensi timer di sisi frontend.
 *
 * POST /api/quiz/timer → simpan / perbarui sisa waktu
 * GET /api/quiz/timer/{studentId}/{topicId}/{questionId} → ambil sisa waktu
 * DELETE /api/quiz/timer/{studentId}/{topicId} → hapus semua timer topik
 * (setelah selesai)
 */
@RestController
@RequestMapping("/api/quiz/timer")
@CrossOrigin(origins = "*")
public class QuizTimerController {

    @Autowired
    private QuizTimerService quizTimerService;

    /** Simpan sisa waktu (dipanggil frontend setiap beberapa detik). */
    @PostMapping
    public ResponseEntity<QuizTimerDto> saveTimer(@RequestBody QuizTimerDto dto) {
        return ResponseEntity.ok(quizTimerService.saveTimer(dto));
    }

    /** Ambil sisa waktu saat halaman dimuat ulang. */
    @GetMapping("/{studentId}/{topicId}/{questionId}")
    public ResponseEntity<QuizTimerDto> getTimer(
            @PathVariable Long studentId,
            @PathVariable Long topicId,
            @PathVariable Long questionId) {
        return quizTimerService.getTimer(studentId, topicId, questionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Hapus semua sesi timer setelah quiz selesai. */
    @DeleteMapping("/{studentId}/{topicId}")
    public ResponseEntity<Void> clearTimers(
            @PathVariable Long studentId,
            @PathVariable Long topicId) {
        quizTimerService.clearTimers(studentId, topicId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /** Hapus timer satu soal setelah jawaban dikirim. */
    @DeleteMapping("/{studentId}/{topicId}/{questionId}")
    public ResponseEntity<Void> clearTimer(
            @PathVariable Long studentId,
            @PathVariable Long topicId,
            @PathVariable Long questionId) {
        quizTimerService.clearTimer(studentId, topicId, questionId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
