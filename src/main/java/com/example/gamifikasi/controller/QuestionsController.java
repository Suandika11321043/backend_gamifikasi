package com.example.gamifikasi.controller;

import com.example.gamifikasi.dto.LearningDateGroupDto;
import com.example.gamifikasi.dto.QuestionsDto;
import com.example.gamifikasi.dto.StudentAnswerHistoryDto;

import com.example.gamifikasi.service.QuestionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * CRUD soal.
 *
 * POST   /api/questions                                                        → buat soal
 * GET    /api/questions                                                        → semua soal
 * GET    /api/questions/{id}                                                   → soal by ID
 * GET    /api/questions/topic/{topicId}                                        → soal by topik
 * GET    /api/questions/topic/{topicId}/date/{date}                            → soal by topik+tanggal (yyyy-MM-dd)
 * GET    /api/questions/topic/{topicId}/date-range?from=yyyy-MM-dd&to=yyyy-MM-dd → soal by rentang tanggal
 * PUT    /api/questions/{id}                                                   → update soal
 * PATCH  /api/questions/{id}/timer                                             → update timer
 * DELETE /api/questions/{id}                                                   → hapus soal
 */
@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "*")
public class QuestionsController {

    @Autowired
    private QuestionsService questionsService;

    // Create Question
    @PostMapping
    public ResponseEntity<QuestionsDto> createQuestion(
            @RequestParam("topicId") Long topicId,
            @RequestParam("learningDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate learningDate,
            @RequestParam("questionType") String questionType,
            @RequestParam(value = "contentInstruction", required = false) String contentInstruction,
            @RequestPart(value = "contentImage", required = false) MultipartFile imageFile,
            @RequestPart(value = "contentAudio", required = false) MultipartFile audioFile,
            @RequestParam(value = "timeLimitMinutes", required = false) Integer timeLimitMinutes,
            @RequestParam(value = "scorePoint", required = false) Integer scorePoint) {
        try {
            QuestionsDto created = questionsService.createQuestion(topicId, learningDate,
                    questionType, contentInstruction, imageFile, audioFile, timeLimitMinutes, scorePoint);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), e);
        }
    }

    // Get All Questions
    @GetMapping
    public ResponseEntity<List<QuestionsDto>> getAllQuestions() {
        List<QuestionsDto> list = questionsService.getAllQuestions();
        if (list.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(list);
    }

    // Get Question by ID
    @GetMapping("/{id}")
    public ResponseEntity<QuestionsDto> getQuestionById(@PathVariable Long id) {
        return questionsService.getQuestionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get by Topic
    @GetMapping("/topic/{topicId}")
    public ResponseEntity<List<QuestionsDto>> getByTopic(@PathVariable Long topicId) {
        List<QuestionsDto> list = questionsService.getQuestionsByTopicId(topicId);
        if (list.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(list);
    }

    // Get by Topic + specific date  (contoh: /topic/1/date/2025-06-13)
    @GetMapping("/topic/{topicId}/date/{date}")
    public ResponseEntity<List<QuestionsDto>> getByTopicAndDate(
            @PathVariable Long topicId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<QuestionsDto> list = questionsService.getQuestionsByTopicAndDate(topicId, date);
        if (list.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(list);
    }

    // Get by Topic + date range  (contoh: ?from=2025-06-01&to=2025-06-30)
    @GetMapping("/topic/{topicId}/date-range")
    public ResponseEntity<List<QuestionsDto>> getByTopicAndDateRange(
            @PathVariable Long topicId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<QuestionsDto> list = questionsService.getQuestionsByTopicAndDateRange(topicId, from, to);
        if (list.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(list);
    }

    // Get by Topic + student status, dikelompokkan per learningDate
    @GetMapping("/topic/{topicId}/student/{studentId}")
    public ResponseEntity<List<LearningDateGroupDto>> getByTopicWithStatus(
            @PathVariable Long topicId,
            @PathVariable Long studentId) {
        List<LearningDateGroupDto> list =
                questionsService.getQuestionsGroupedByDateWithStatus(topicId, studentId);
        if (list.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(list);
    }

    // Riwayat jawaban siswa
    // GET /api/questions/students/{studentId}/history
    // ?topicId=  (opsional)
    // ?date=yyyy-MM-dd  (opsional)
    @GetMapping("/students/{studentId}/history")
    public ResponseEntity<List<StudentAnswerHistoryDto>> getStudentAnswerHistory(
            @PathVariable Long studentId,
            @RequestParam(value = "topicId", required = false) Long topicId,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<StudentAnswerHistoryDto> list =
                questionsService.getStudentAnswerHistory(studentId, topicId, date);
        if (list.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(list);
    }

    // Update Question
    @PutMapping("/{id}")
    public ResponseEntity<QuestionsDto> updateQuestion(
            @PathVariable Long id,
            @RequestParam("topicId") Long topicId,
            @RequestParam("learningDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate learningDate,
            @RequestParam("questionType") String questionType,
            @RequestParam(value = "contentInstruction", required = false) String contentInstruction,
            @RequestPart(value = "contentImage", required = false) MultipartFile imageFile,
            @RequestPart(value = "contentAudio", required = false) MultipartFile audioFile,
            @RequestParam(value = "timeLimitMinutes", required = false) Integer timeLimitMinutes,
            @RequestParam(value = "scorePoint", required = false) Integer scorePoint) {
        try {
            Optional<QuestionsDto> updated = questionsService.updateQuestion(id, topicId, learningDate,
                    questionType, contentInstruction, imageFile, audioFile, timeLimitMinutes, scorePoint);
            return updated.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), e);
        }
    }

    // Update timer only
    @PatchMapping("/{id}/timer")
    public ResponseEntity<QuestionsDto> updateTimerLimit(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Integer> body) {
        return questionsService.updateTimerLimit(id, body.get("timeLimitMinutes"))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete Question
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        return questionsService.deleteQuestion(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // Set isAvailable = true for all questions in a topic on a given learningDate
    @PostMapping("/topic/{topicId}/set-available")
    public ResponseEntity<List<QuestionsDto>> setAvailable(
            @PathVariable Long topicId,
            @RequestBody java.util.Map<String, String> body) {
        String dateStr = body.get("learningDate");
        if (dateStr == null || dateStr.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        LocalDate learningDate = LocalDate.parse(dateStr);
        List<QuestionsDto> updated = questionsService.setAvailableByTopicAndDate(topicId, learningDate);
        if (updated.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(updated);
    }
}