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
            @RequestBody MatchingRelationDto dto) {
        try {
            MatchingRelationDto created = matchingRelationService.createRelation(
                    dto.getQuestionId(), dto.getOpsiPertanyaanId(), dto.getOpsiJawabanId());
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), e);
        }
    }

    // Get All Relations
    @GetMapping
    public ResponseEntity<List<MatchingRelationDto>> getAllRelations() {
        try {
            List<MatchingRelationDto> list = matchingRelationService.getAllRelations();
            if (list.isEmpty())
                return ResponseEntity.noContent().build();
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), e);
        }
    }

    // Get Relation by ID
    @GetMapping("/{id}")
    public ResponseEntity<MatchingRelationDto> getRelationById(@PathVariable("id") Long id) {
        try {
            Optional<MatchingRelationDto> data = matchingRelationService.getRelationById(id);
            return data.map(r -> new ResponseEntity<>(r, HttpStatus.OK))
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), e);
        }
    }

    // Get Relations by Question ID
    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<MatchingRelationDto>> getRelationsByQuestionId(
            @PathVariable("questionId") Long questionId) {
        try {
            List<MatchingRelationDto> list = matchingRelationService.getRelationsByQuestionId(questionId);
            if (list.isEmpty())
                return ResponseEntity.noContent().build();
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), e);
        }
    }

    // Update Relation
    @PutMapping("/{id}")
    public ResponseEntity<MatchingRelationDto> updateRelation(
            @PathVariable("id") Long id,
            @RequestBody MatchingRelationDto dto) {
        try {
            Optional<MatchingRelationDto> updated = matchingRelationService.updateRelation(
                    id, dto.getQuestionId(), dto.getOpsiPertanyaanId(), dto.getOpsiJawabanId());
            return updated.map(r -> new ResponseEntity<>(r, HttpStatus.OK))
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), e);
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
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), e);
        }
    }
}
