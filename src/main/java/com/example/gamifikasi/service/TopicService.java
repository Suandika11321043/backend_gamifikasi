package com.example.gamifikasi.service;

import com.example.gamifikasi.dto.TopicDto;
import com.example.gamifikasi.dto.TopicCreateDto;
import com.example.gamifikasi.entity.Topic;
import com.example.gamifikasi.entity.Level;
import com.example.gamifikasi.repository.TopicRepository;
import com.example.gamifikasi.repository.LevelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TopicService {

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private LevelRepository levelRepository;

    // Convert Entity to DTO
    private TopicDto convertToDto(Topic topic) {
        return new TopicDto(
                topic.getId(),
                topic.getNameTopic(),
                topic.getLevelId() != null ? topic.getLevelId().getId() : null,
                topic.getIcon()
        );
    }

    // Convert DTO to Entity
    private Topic convertToEntity(TopicDto topicDto) {
        Topic topic = new Topic();
        topic.setId(topicDto.getId());
        topic.setNameTopic(topicDto.getNameTopic());

        if (topicDto.getLevelId() != null) {
            Level level = levelRepository.findById(topicDto.getLevelId()).orElse(null);
            topic.setLevelId(level);
        }

        topic.setIcon(topicDto.getIcon());
        return topic;
    }

    // Convert CreateDTO to Entity
    private Topic convertCreateDtoToEntity(TopicCreateDto createDto) {
        Topic topic = new Topic();
        topic.setNameTopic(createDto.getNameTopic());

        if (createDto.getLevelId() != null) {
            Long levelId = Long.valueOf(createDto.getLevelId());
            Level level = levelRepository.findById(levelId).orElse(null);
            topic.setLevelId(level);
        }

        topic.setIcon(createDto.getIcon());
        return topic;
    }

    // Create Topic
    public TopicDto createTopic(TopicCreateDto createDto) {
        Topic topic = convertCreateDtoToEntity(createDto);
        Topic savedTopic = topicRepository.save(topic);
        return convertToDto(savedTopic);
    }

    // Get all Topics
    public List<TopicDto> getAllTopics() {
        return topicRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Get Topic by ID
    public Optional<TopicDto> getTopicById(Long id) {
        return topicRepository.findById(id)
                .map(this::convertToDto);
    }

    // Get Topic by Name
    public Optional<TopicDto> getTopicByName(String nameTopic) {
        return topicRepository.findByNameTopic(nameTopic)
                .map(this::convertToDto);
    }

    // Get Topics by Level ID
    public List<TopicDto> getTopicsByLevelId(Long levelId) {
        Level level = levelRepository.findById(levelId).orElse(null);
        if (level != null) {
            return topicRepository.findByLevelId(level).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    // Update Topic
    public Optional<TopicDto> updateTopic(Long id, TopicDto topicDto) {
        return topicRepository.findById(id)
                .map(existingTopic -> {
                    existingTopic.setNameTopic(topicDto.getNameTopic());
                    existingTopic.setIcon(topicDto.getIcon());

                    if (topicDto.getLevelId() != null) {
                        Level level = levelRepository.findById(topicDto.getLevelId()).orElse(null);
                        existingTopic.setLevelId(level);
                    }

                    Topic updatedTopic = topicRepository.save(existingTopic);
                    return convertToDto(updatedTopic);
                });
    }

    // Delete Topic
    public boolean deleteTopic(Long id) {
        if (topicRepository.existsById(id)) {
            topicRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
