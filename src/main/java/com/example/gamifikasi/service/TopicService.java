package com.example.gamifikasi.service;

import com.example.gamifikasi.dto.TopicDto;
import com.example.gamifikasi.entity.Tema;
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
    private FileStorageUtil fileStorageUtil;

    private TopicDto convertToDto(Tema topic) {
        return new TopicDto(
                topic.getId(),
                topic.getNameTopic(),
                topic.getDescription(),
                topic.getIcon(),
                topic.getIsActive());
    }

    @Transactional
    public TopicDto createTopic(TopicDto createDto, MultipartFile iconFile) throws IOException {
        Tema topic = new Tema();
        topic.setNameTopic(createDto.getNameTopic());
        topic.setDescription(createDto.getDescription());
        topic.setIsActive(createDto.getIsActive() != null ? createDto.getIsActive() : true);
        if (iconFile != null && !iconFile.isEmpty()) {
            String iconUrl = fileStorageUtil.storeFile(iconFile);
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

    @Transactional
    public Optional<TopicDto> updateTopic(Long id, String nameTopic, String description, Boolean isActive,
            MultipartFile iconFile)
            throws IOException {
        Optional<Tema> existingOpt = temaRepository.findById(id);
        if (existingOpt.isEmpty())
            return Optional.empty();

        Tema topic = existingOpt.get();
        topic.setNameTopic(nameTopic);
        topic.setDescription(description);
        if (isActive != null) {
            topic.setIsActive(isActive);
        }

        if (iconFile != null && !iconFile.isEmpty()) {
            fileStorageUtil.deleteFile(topic.getIcon());
            topic.setIcon(fileStorageUtil.storeFile(iconFile));
        }

        return Optional.of(convertToDto(temaRepository.save(topic)));
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

    @Transactional
    public boolean deleteTopic(Long id) {
        return temaRepository.findById(id)
                .map(topic -> {
                    try {
                        fileStorageUtil.deleteFile(topic.getIcon());
                    } catch (IOException e) {
                        // continue deletion even if icon delete fails
                    }
                    temaRepository.deleteById(id);
                    return true;
                })
                .orElse(false);
    }
}
