package com.example.gamifikasi.controller;

import com.example.gamifikasi.dto.QuestionOptionsDto;
import com.example.gamifikasi.entity.QuestionOptions;
import com.example.gamifikasi.service.QuestionOptionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/question-options")
@CrossOrigin(origins = "*")
public class QuestionOptionsController {

    @Autowired
    private QuestionOptionsService questionOptionsService;

    // Create Option
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<QuestionOptionsDto> createOption(
            @RequestParam("questionId") Long questionId,
            @RequestParam(value = "teksOpsi", required = false) String teksOpsi,
            @RequestPart(value = "mediaOpsi", required = false) MultipartFile mediaOpsiFile,
            @RequestParam(value = "kunciJawaban", required = false) Boolean kunciJawaban,
            @RequestParam(value = "urutanBenar", required = false) Integer urutanBenar,
            @RequestParam(value = "tipeItem", required = false) QuestionOptions.TipeItem tipeItem) {
        try {
            QuestionOptionsDto created = questionOptionsService.createOption(
                    questionId, teksOpsi, mediaOpsiFile, kunciJawaban, urutanBenar, tipeItem);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get All Options
    @GetMapping
    public ResponseEntity<List<QuestionOptionsDto>> getAllOptions() {
        try {
            List<QuestionOptionsDto> list = questionOptionsService.getAllOptions();
            if (list.isEmpty()) return ResponseEntity.noContent().build();
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get Option by ID
    @GetMapping("/{id}")
    public ResponseEntity<QuestionOptionsDto> getOptionById(@PathVariable("id") Long id) {
        try {
            Optional<QuestionOptionsDto> data = questionOptionsService.getOptionById(id);
            return data.map(opt -> new ResponseEntity<>(opt, HttpStatus.OK))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get Options by Question ID
    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<QuestionOptionsDto>> getOptionsByQuestionId(@PathVariable("questionId") Long questionId) {
        try {
            List<QuestionOptionsDto> list = questionOptionsService.getOptionsByQuestionId(questionId);
            if (list.isEmpty()) return ResponseEntity.noContent().build();
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Update Option
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<QuestionOptionsDto> updateOption(
            @PathVariable("id") Long id,
            @RequestParam("questionId") Long questionId,
            @RequestParam(value = "teksOpsi", required = false) String teksOpsi,
            @RequestPart(value = "mediaOpsi", required = false) MultipartFile mediaOpsiFile,
            @RequestParam(value = "kunciJawaban", required = false) Boolean kunciJawaban,
            @RequestParam(value = "urutanBenar", required = false) Integer urutanBenar,
            @RequestParam(value = "tipeItem", required = false) QuestionOptions.TipeItem tipeItem) {
        try {
            Optional<QuestionOptionsDto> updated = questionOptionsService.updateOption(
                    id, questionId, teksOpsi, mediaOpsiFile, kunciJawaban, urutanBenar, tipeItem);
            return updated.map(opt -> new ResponseEntity<>(opt, HttpStatus.OK))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Delete Option
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteOption(@PathVariable("id") Long id) {
        try {
            boolean deleted = questionOptionsService.deleteOption(id);
            return deleted
                    ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                    : new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
