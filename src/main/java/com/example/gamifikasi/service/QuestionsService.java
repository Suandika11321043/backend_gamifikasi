package com.example.gamifikasi.service;

import com.example.gamifikasi.dto.QuestionsDto;
import com.example.gamifikasi.entity.Questions;
import com.example.gamifikasi.entity.Topic;
import com.example.gamifikasi.repository.MatchingRelationRepository;
import com.example.gamifikasi.repository.QuestionOptionsRepository;
import com.example.gamifikasi.repository.QuestionsRepository;
import com.example.gamifikasi.repository.StudentAnswerRepository;
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
public class QuestionsService {

    @Autowired
    private QuestionsRepository questionsRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private FileStorageUtil fileStorageUtil;

    @Autowired
    private StudentAnswerRepository studentAnswerRepository;

    @Autowired
    private QuestionOptionsRepository questionOptionsRepository;

    @Autowired
    private MatchingRelationRepository matchingRelationRepository;

    private QuestionsDto convertToDto(Questions q) {
        return new QuestionsDto(
                q.getId(),
                q.getTopic() != null ? q.getTopic().getId() : null,
                q.getQuestionType(),
                q.getContentInstruction(),
                q.getContentImage(),
                q.getContentAudio(),
                q.getTimeLimitMinutes(),
                q.getScorePoint());
    }

    // Create
    public QuestionsDto createQuestion(Long topicId, String questionType, String contentInstruction,
            MultipartFile imageFile, MultipartFile audioFile,
            Integer timeLimitMinutes, Integer scorePoint) throws IOException {
        Questions q = new Questions();
        if (topicId != null) {
            Topic topic = topicRepository.findById(topicId).orElse(null);
            q.setTopic(topic);
        }
        q.setQuestionType(questionType);
        q.setContentInstruction(contentInstruction);
        q.setTimeLimitMinutes(timeLimitMinutes);
        q.setScorePoint(scorePoint);
        if (imageFile != null && !imageFile.isEmpty()) {
            q.setContentImage(fileStorageUtil.storeFile(imageFile));
        }
        if (audioFile != null && !audioFile.isEmpty()) {
            q.setContentAudio(fileStorageUtil.storeFile(audioFile));
        }
        return convertToDto(questionsRepository.save(q));
    }

    // Get All
    public List<QuestionsDto> getAllQuestions() {
        return questionsRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Get by ID
    public Optional<QuestionsDto> getQuestionById(Long id) {
        return questionsRepository.findById(id).map(this::convertToDto);
    }

    // Get by Topic ID
    public List<QuestionsDto> getQuestionsByTopicId(Long topicId) {
        return topicRepository.findById(topicId)
                .map(topic -> questionsRepository.findByTopic(topic).stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    // Update
    public Optional<QuestionsDto> updateQuestion(Long id, Long topicId, String questionType,
            String contentInstruction,
            MultipartFile imageFile, MultipartFile audioFile,
            Integer timeLimitMinutes, Integer scorePoint) throws IOException {
        Optional<Questions> existingOpt = questionsRepository.findById(id);
        if (existingOpt.isEmpty())
            return Optional.empty();

        Questions q = existingOpt.get();
        if (topicId != null) {
            Topic topic = topicRepository.findById(topicId).orElse(null);
            q.setTopic(topic);
        }
        q.setQuestionType(questionType);
        q.setContentInstruction(contentInstruction);
        q.setTimeLimitMinutes(timeLimitMinutes);
        q.setScorePoint(scorePoint);
        if (imageFile != null && !imageFile.isEmpty()) {
            fileStorageUtil.deleteFile(q.getContentImage());
            q.setContentImage(fileStorageUtil.storeFile(imageFile));
        }
        if (audioFile != null && !audioFile.isEmpty()) {
            fileStorageUtil.deleteFile(q.getContentAudio());
            q.setContentAudio(fileStorageUtil.storeFile(audioFile));
        }
        return Optional.of(convertToDto(questionsRepository.save(q)));
    }

    // Update timer only
    public Optional<QuestionsDto> updateTimerLimit(Long id, Integer timeLimitMinutes) {
        return questionsRepository.findById(id)
                .map(q -> {
                    q.setTimeLimitMinutes(timeLimitMinutes);
                    return convertToDto(questionsRepository.save(q));
                });
    }

    // Delete
    public boolean deleteQuestion(Long id) {
        return questionsRepository.findById(id)
                .map(q -> {
                    try {
                        fileStorageUtil.deleteFile(q.getContentImage());
                        fileStorageUtil.deleteFile(q.getContentAudio());
                    } catch (IOException e) {
                        // continue deletion
                    }
                    // Hapus relasi anak sebelum menghapus soal
                    matchingRelationRepository.deleteAll(matchingRelationRepository.findByQuestions(q));
                    studentAnswerRepository.deleteAll(studentAnswerRepository.findByQuestions(q));
                    questionOptionsRepository.deleteAll(questionOptionsRepository.findByQuestions(q));
                    questionsRepository.deleteById(id);
                    return true;
                })
                .orElse(false);
    }
}
