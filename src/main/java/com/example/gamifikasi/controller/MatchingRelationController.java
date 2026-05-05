package com.example.gamifikasi.controller;

import com.example.gamifikasi.dto.MatchingRelationDto;
import com.example.gamifikasi.service.MatchingRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/matching-relations")
@CrossOrigin(origins = "*")
public class MatchingRelationController {

    @Autowired
    private MatchingRelationService matchingRelationService;

    // Create Relation
    @PostMapping
    public ResponseEntity<MatchingRelationDto> createRelation(
            @RequestParam("questionId") Long questionId,
            @RequestParam("opsiPertanyaanId") Long opsiPertanyaanId,
            @RequestParam("opsiJawabanId") Long opsiJawabanId) {
        try {
            MatchingRelationDto created = matchingRelationService.createRelation(
                    questionId, opsiPertanyaanId, opsiJawabanId);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get All Relations
    @GetMapping
    public ResponseEntity<List<MatchingRelationDto>> getAllRelations() {
        try {
            List<MatchingRelationDto> list = matchingRelationService.getAllRelations();
            if (list.isEmpty()) return ResponseEntity.noContent().build();
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get Relation by ID
    @GetMapping("/{id}")
    public ResponseEntity<MatchingRelationDto> getRelationById(@PathVariable("id") Long id) {
        try {
            Optional<MatchingRelationDto> data = matchingRelationService.getRelationById(id);
            return data.map(r -> new ResponseEntity<>(r, HttpStatus.OK))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get Relations by Question ID
    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<MatchingRelationDto>> getRelationsByQuestionId(@PathVariable("questionId") Long questionId) {
        try {
            List<MatchingRelationDto> list = matchingRelationService.getRelationsByQuestionId(questionId);
            if (list.isEmpty()) return ResponseEntity.noContent().build();
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Update Relation
    @PutMapping("/{id}")
    public ResponseEntity<MatchingRelationDto> updateRelation(
            @PathVariable("id") Long id,
            @RequestParam("questionId") Long questionId,
            @RequestParam("opsiPertanyaanId") Long opsiPertanyaanId,
            @RequestParam("opsiJawabanId") Long opsiJawabanId) {
        try {
            Optional<MatchingRelationDto> updated = matchingRelationService.updateRelation(
                    id, questionId, opsiPertanyaanId, opsiJawabanId);
            return updated.map(r -> new ResponseEntity<>(r, HttpStatus.OK))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Delete Relation
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteRelation(@PathVariable("id") Long id) {
        try {
            boolean deleted = matchingRelationService.deleteRelation(id);
            return deleted
                    ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                    : new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
