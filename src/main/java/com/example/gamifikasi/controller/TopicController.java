package com.example.gamifikasi.controller;

import com.example.gamifikasi.dto.TopicDto;
import com.example.gamifikasi.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/topics")
@CrossOrigin(origins = "*")
public class TopicController {

    @Autowired
    private TopicService topicService;

    // Create Topic
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TopicDto> createTopic(
            @RequestParam("nameTopic") String nameTopic,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "isActive", required = false) String isActiveStr,
            @RequestPart(value = "icon", required = false) MultipartFile iconFile) {
        try {
            boolean isActive = isActiveStr == null || Boolean.parseBoolean(isActiveStr.trim());
            TopicDto createDto = new TopicDto(null, nameTopic, description, null, isActive);
            TopicDto createdTopic = topicService.createTopic(createDto, iconFile);
            return new ResponseEntity<>(createdTopic, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), e);
        }
    }

    // Get all Topics
    @GetMapping
    public ResponseEntity<List<TopicDto>> getAllTopics() {
        try {
            List<TopicDto> topics = topicService.getAllTopics();
            if (topics.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return new ResponseEntity<>(topics, HttpStatus.OK);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), e);
        }
    }

    // Get Topic by ID
    @GetMapping("/{id}")
    public ResponseEntity<TopicDto> getTopicById(@PathVariable("id") Long id) {
        try {
            Optional<TopicDto> topicData = topicService.getTopicById(id);
            if (topicData.isPresent()) {
                return new ResponseEntity<>(topicData.get(), HttpStatus.OK);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), e);
        }
    }

    // Get Topic by Name
    @GetMapping("/name/{nameTopic}")
    public ResponseEntity<TopicDto> getTopicByName(@PathVariable("nameTopic") String nameTopic) {
        try {
            Optional<TopicDto> topicData = topicService.getTopicByName(nameTopic);
            if (topicData.isPresent()) {
                return new ResponseEntity<>(topicData.get(), HttpStatus.OK);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), e);
        }
    }

    // Update Topic
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TopicDto> updateTopic(
            @PathVariable("id") Long id,
            @RequestParam("nameTopic") String nameTopic,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "isActive", required = false) String isActiveStr,
            @RequestPart(value = "icon", required = false) MultipartFile iconFile) {
        try {
            Boolean isActive = isActiveStr != null ? Boolean.parseBoolean(isActiveStr.trim()) : null;
            Optional<TopicDto> updatedTopic = topicService.updateTopic(id, nameTopic, description, isActive, iconFile);
            if (updatedTopic.isPresent()) {
                return new ResponseEntity<>(updatedTopic.get(), HttpStatus.OK);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), e);
        }
    }

    // Get active Topics only
    @GetMapping("/active")
    public ResponseEntity<List<TopicDto>> getActiveTopics() {
        try {
            List<TopicDto> topics = topicService.getActiveTopics();
            if (topics.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return new ResponseEntity<>(topics, HttpStatus.OK);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), e);
        }
    }

    // Activate Topic
    @PatchMapping("/{id}/activate")
    public ResponseEntity<TopicDto> activateTopic(@PathVariable("id") Long id) {
        try {
            return topicService.setActive(id, true)
                    .map(t -> new ResponseEntity<>(t, HttpStatus.OK))
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), e);
        }
    }

    // Deactivate Topic
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<TopicDto> deactivateTopic(@PathVariable("id") Long id) {
        try {
            return topicService.setActive(id, false)
                    .map(t -> new ResponseEntity<>(t, HttpStatus.OK))
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), e);
        }
    }

    // Delete Topic
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteTopic(@PathVariable("id") Long id) {
        try {
            boolean deleted = topicService.deleteTopic(id);
            if (deleted) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), e);
        }
    }
}
