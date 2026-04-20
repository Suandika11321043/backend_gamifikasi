package com.example.gamifikasi.controller;

import com.example.gamifikasi.dto.LevelDto;
import com.example.gamifikasi.service.LevelService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/levels")
@CrossOrigin(origins = "*")
public class LevelController {

    @Autowired
    private LevelService levelService;

    // Create Level
    @PostMapping
    public ResponseEntity<LevelDto> createLevel(@Valid @RequestBody LevelDto levelDto) {
        try {
            LevelDto createdLevel = levelService.createLevel(levelDto);
            return new ResponseEntity<>(createdLevel, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get all Levels
    @GetMapping
    public ResponseEntity<List<LevelDto>> getAllLevels() {
        try {
            List<LevelDto> levels = levelService.getAllLevels();
            if (levels.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return new ResponseEntity<>(levels, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get Level by ID
    @GetMapping("/{id}")
    public ResponseEntity<LevelDto> getLevelById(@PathVariable("id") Long id) {
        try {
            Optional<LevelDto> levelData = levelService.getLevelById(id);
            if (levelData.isPresent()) {
                return new ResponseEntity<>(levelData.get(), HttpStatus.OK);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get Level by Name
    @GetMapping("/name/{nameLevel}")
    public ResponseEntity<LevelDto> getLevelByName(@PathVariable("nameLevel") String nameLevel) {
        try {
            Optional<LevelDto> levelData = levelService.getLevelByName(nameLevel);
            if (levelData.isPresent()) {
                return new ResponseEntity<>(levelData.get(), HttpStatus.OK);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Update Level
    @PutMapping("/{id}")
    public ResponseEntity<LevelDto> updateLevel(@PathVariable("id") Long id, @RequestBody LevelDto levelDto) {
        try {
            Optional<LevelDto> updatedLevel = levelService.updateLevel(id, levelDto);
            if (updatedLevel.isPresent()) {
                return new ResponseEntity<>(updatedLevel.get(), HttpStatus.OK);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Delete Level
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLevel(@PathVariable("id") Long id) {
        try {
            boolean deleted = levelService.deleteLevel(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
