package com.example.gamifikasi.service;

import com.example.gamifikasi.dto.TopicDto;
import com.example.gamifikasi.dto.TopicDto;
import com.example.gamifikasi.entity.Topic;
import com.example.gamifikasi.repository.TopicRepository;
import com.example.gamifikasi.util.FileStorageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TopicService {

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private FileStorageUtil fileStorageUtil;

    private TopicDto convertToDto(Topic topic) {
        return new TopicDto(
                topic.getId(),
                topic.getNameTopic(),
                topic.getDescription(),
                topic.getIcon()
        );
    }

    public TopicDto createTopic(TopicDto createDto, MultipartFile iconFile) throws IOException {
        Topic topic = new Topic();
        topic.setNameTopic(createDto.getNameTopic());
        topic.setDescription(createDto.getDescription());
        if (iconFile != null && !iconFile.isEmpty()) {
            String iconUrl = fileStorageUtil.storeFile(iconFile);
            topic.setIcon(iconUrl);
        }
        return convertToDto(topicRepository.save(topic));
    }

    public List<TopicDto> getAllTopics() {
        return topicRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<TopicDto> getTopicById(Long id) {
        return topicRepository.findById(id).map(this::convertToDto);
    }

    public Optional<TopicDto> getTopicByName(String nameTopic) {
        return topicRepository.findByNameTopic(nameTopic).map(this::convertToDto);
    }

    public Optional<TopicDto> updateTopic(Long id, String nameTopic, String description, MultipartFile iconFile) throws IOException {
        Optional<Topic> existingOpt = topicRepository.findById(id);
        if (existingOpt.isEmpty()) return Optional.empty();

        Topic topic = existingOpt.get();
        topic.setNameTopic(nameTopic);
        topic.setDescription(description);

        if (iconFile != null && !iconFile.isEmpty()) {
            fileStorageUtil.deleteFile(topic.getIcon());
            topic.setIcon(fileStorageUtil.storeFile(iconFile));
        }

        return Optional.of(convertToDto(topicRepository.save(topic)));
    }

    public boolean deleteTopic(Long id) {
        return topicRepository.findById(id)
                .map(topic -> {
                    try {
                        fileStorageUtil.deleteFile(topic.getIcon());
                    } catch (IOException e) {
                        // continue deletion even if icon delete fails
                    }
                    topicRepository.deleteById(id);
                    return true;
                })
                .orElse(false);
    }
}
