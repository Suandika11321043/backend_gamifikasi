package com.example.gamifikasi.service;

import com.example.gamifikasi.dto.TopicDto;
import com.example.gamifikasi.entity.Tema;
import com.example.gamifikasi.repository.QuestionTimerSessionRepository;
import com.example.gamifikasi.repository.StudentScoreRepository;
import com.example.gamifikasi.repository.TemaRepository;
import com.example.gamifikasi.util.FileStorageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TopicService {

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private QuestionsService questionsService;

    @Autowired
    private StudentScoreRepository studentScoreRepository;

    @Autowired
    private QuestionTimerSessionRepository questionTimerSessionRepository;

    @Autowired
    private FileStorageUtil fileStorageUtil;

    private TopicDto convertToDto(Tema topic) {
        return new TopicDto(
                topic.getId(),
                topic.getNameTopic(),
                topic.getDescription(),
                topic.getIcon(),
                topic.getIsActive());
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nama tema wajib diisi.");
        }
        return name.trim();
    }

    private String normalizeDescription(String description) {
        return description != null ? description.trim() : null;
    }

    public TopicDto createTopic(TopicDto createDto, MultipartFile iconFile) throws IOException {
        String iconUrl = null;
        if (iconFile != null && !iconFile.isEmpty()) {
            iconUrl = fileStorageUtil.storeFile(iconFile);
        }
        return persistNewTopic(createDto, iconUrl);
    }

    @Transactional
    protected TopicDto persistNewTopic(TopicDto createDto, String iconUrl) {
        Tema topic = new Tema();
        topic.setNameTopic(normalizeName(createDto.getNameTopic()));
        topic.setDescription(normalizeDescription(createDto.getDescription()));
        topic.setIsActive(createDto.getIsActive() != null ? createDto.getIsActive() : true);
        if (iconUrl != null) {
            topic.setIcon(iconUrl);
        }
        return convertToDto(temaRepository.save(topic));
    }

    public List<TopicDto> getAllTopics() {
        return temaRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<TopicDto> getTopicById(Long id) {
        return temaRepository.findById(id).map(this::convertToDto);
    }

    public Optional<TopicDto> getTopicByName(String nameTopic) {
        return temaRepository.findByNameTopic(nameTopic).map(this::convertToDto);
    }

    public Optional<TopicDto> updateTopic(Long id, String nameTopic, String description, Boolean isActive,
            MultipartFile iconFile) throws IOException {
        Optional<Tema> existingOpt = temaRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }

        String newIconUrl = null;
        if (iconFile != null && !iconFile.isEmpty()) {
            String oldIcon = existingOpt.get().getIcon();
            fileStorageUtil.deleteFile(oldIcon);
            newIconUrl = fileStorageUtil.storeFile(iconFile);
        }

        return applyTopicUpdate(id, normalizeName(nameTopic), normalizeDescription(description), isActive, newIconUrl);
    }

    @Transactional
    protected Optional<TopicDto> applyTopicUpdate(Long id, String nameTopic, String description, Boolean isActive,
            String iconUrl) {
        return temaRepository.findById(id).map(topic -> {
            topic.setNameTopic(nameTopic);
            topic.setDescription(description);
            if (isActive != null) {
                topic.setIsActive(isActive);
            }
            if (iconUrl != null) {
                topic.setIcon(iconUrl);
            }
            return convertToDto(temaRepository.save(topic));
        });
    }

    public List<TopicDto> getActiveTopics() {
        return temaRepository.findAll().stream()
                .filter(t -> Boolean.TRUE.equals(t.getIsActive()))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<TopicDto> setActive(Long id, boolean active) {
        return temaRepository.findById(id).map(topic -> {
            topic.setIsActive(active);
            return convertToDto(temaRepository.save(topic));
        });
    }

    public boolean deleteTopic(Long id) {
        Optional<Tema> topicOpt = temaRepository.findById(id);
        if (topicOpt.isEmpty()) {
            return false;
        }
        String icon = topicOpt.get().getIcon();
        deleteTopicRecord(id);
        try {
            fileStorageUtil.deleteFile(icon);
        } catch (IOException e) {
            // continue even if icon delete fails
        }
        return true;
    }

    @Transactional
    protected void deleteTopicRecord(Long id) {
        Tema topic = temaRepository.findById(id).orElse(null);
        if (topic == null) {
            return;
        }
        questionsService.deleteAllQuestionsByTopicId(id);
        studentScoreRepository.deleteByTopic(topic);
        questionTimerSessionRepository.deleteByTopicId(id);
        temaRepository.deleteById(id);
    }
}
