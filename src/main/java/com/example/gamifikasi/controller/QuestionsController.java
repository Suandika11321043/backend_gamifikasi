package com.example.gamifikasi.controller;

import com.example.gamifikasi.dto.QuestionsDto;
import com.example.gamifikasi.service.QuestionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "*")
public class QuestionsController {

    @Autowired
    private QuestionsService questionsService;

    // Create Question
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<QuestionsDto> createQuestion(
            @RequestParam("topicId") Long topicId,
            @RequestParam("questionType") String questionType,
            @RequestParam(value = "contentInstruction", required = false) String contentInstruction,
            @RequestPart(value = "contentImage", required = false) MultipartFile imageFile,
            @RequestPart(value = "contentAudio", required = false) MultipartFile audioFile,
            @RequestParam(value = "timeLimitMinutes", required = false) Integer timeLimitMinutes,
            @RequestParam(value = "scorePoint", required = false) Integer scorePoint) {
        try {
            QuestionsDto created = questionsService.createQuestion(topicId, questionType, contentInstruction, imageFile,
                    audioFile, timeLimitMinutes, scorePoint);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get All Questions
    @GetMapping
    public ResponseEntity<List<QuestionsDto>> getAllQuestions() {
        try {
            List<QuestionsDto> list = questionsService.getAllQuestions();
            if (list.isEmpty())
                return ResponseEntity.noContent().build();
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get Question by ID
    @GetMapping("/{id}")
    public ResponseEntity<QuestionsDto> getQuestionById(@PathVariable("id") Long id) {
        try {
            Optional<QuestionsDto> data = questionsService.getQuestionById(id);
            return data.map(q -> new ResponseEntity<>(q, HttpStatus.OK))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get Questions by Topic ID
    @GetMapping("/topic/{topicId}")
    public ResponseEntity<List<QuestionsDto>> getQuestionsByTopicId(@PathVariable("topicId") Long topicId) {
        try {
            List<QuestionsDto> list = questionsService.getQuestionsByTopicId(topicId);
            if (list.isEmpty())
                return ResponseEntity.noContent().build();
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Update Question
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<QuestionsDto> updateQuestion(
            @PathVariable("id") Long id,
            @RequestParam("topicId") Long topicId,
            @RequestParam("questionType") String questionType,
            @RequestParam(value = "contentInstruction", required = false) String contentInstruction,
            @RequestPart(value = "contentImage", required = false) MultipartFile imageFile,
            @RequestPart(value = "contentAudio", required = false) MultipartFile audioFile,
            @RequestParam(value = "timeLimitMinutes", required = false) Integer timeLimitMinutes,
            @RequestParam(value = "scorePoint", required = false) Integer scorePoint) {
        try {
            Optional<QuestionsDto> updated = questionsService.updateQuestion(id, topicId, questionType,
                    contentInstruction, imageFile, audioFile, timeLimitMinutes, scorePoint);
            return updated.map(q -> new ResponseEntity<>(q, HttpStatus.OK))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Update timer only (PATCH /api/questions/{id}/timer)
    // Kirim { "timeLimitMinutes": 5 } untuk set timer, atau { "timeLimitMinutes":
    // null } untuk hapus timer
    @PatchMapping("/{id}/timer")
    public ResponseEntity<QuestionsDto> updateTimerLimit(
            @PathVariable("id") Long id,
            @RequestBody java.util.Map<String, Integer> body) {
        try {
            Integer timeLimitMinutes = body.get("timeLimitMinutes");
            Optional<QuestionsDto> updated = questionsService.updateTimerLimit(id, timeLimitMinutes);
            return updated.map(q -> new ResponseEntity<>(q, HttpStatus.OK))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Delete Question
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteQuestion(@PathVariable("id") Long id) {
        try {
            boolean deleted = questionsService.deleteQuestion(id);
            return deleted
                    ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                    : new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
