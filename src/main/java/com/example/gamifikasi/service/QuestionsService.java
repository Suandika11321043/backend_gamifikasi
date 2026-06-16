package com.example.gamifikasi.service;

import com.example.gamifikasi.dto.LearningDateGroupDto;
import com.example.gamifikasi.dto.QuestionsDto;
import com.example.gamifikasi.dto.QuestionWithOptionsDto;
import com.example.gamifikasi.dto.StudentAnswerHistoryDto;
import com.example.gamifikasi.entity.Questions;
import com.example.gamifikasi.entity.Topic;
import com.example.gamifikasi.repository.MatchingRelationRepository;
import com.example.gamifikasi.repository.QuestionOptionsRepository;
import com.example.gamifikasi.repository.QuestionsRepository;
import com.example.gamifikasi.repository.JigsawPuzzleRepository;
import com.example.gamifikasi.repository.StudentAnswerRepository;
import com.example.gamifikasi.repository.TopicRepository;
import com.example.gamifikasi.util.FileStorageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private JigsawPuzzleRepository jigsawPuzzleRepository;

    private QuestionsDto convertToDto(Questions q) {
        return new QuestionsDto(
                q.getId(),
                q.getTopic() != null ? q.getTopic().getId() : null,
                q.getLearningDate(),
                q.getQuestionType(),
                q.getContentInstruction(),
                q.getContentImage(),
                q.getContentAudio(),
                q.getTimeLimitMinutes(),
                q.getScorePoint(),
                Boolean.TRUE.equals(q.getIsAvailable()));
    }

    // Create
    public QuestionsDto createQuestion(Long topicId, LocalDate learningDate,
            String questionType, String contentInstruction,
            MultipartFile imageFile, MultipartFile audioFile,
            Integer timeLimitMinutes, Integer scorePoint) throws IOException {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topik tidak ditemukan: " + topicId));
        Questions q = new Questions();
        q.setTopic(topic);
        q.setLearningDate(learningDate);
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

    // Get by Topic + specific date
    public List<QuestionsDto> getQuestionsByTopicAndDate(Long topicId, LocalDate learningDate) {
        return topicRepository.findById(topicId)
                .map(topic -> questionsRepository.findByTopicAndLearningDate(topic, learningDate).stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    // Get by Topic + date range (e.g. seluruh bulan: from=2025-06-01, to=2025-06-30)
    public List<QuestionsDto> getQuestionsByTopicAndDateRange(Long topicId, LocalDate from, LocalDate to) {
        return topicRepository.findById(topicId)
                .map(topic -> questionsRepository.findByTopicAndLearningDateBetween(topic, from, to).stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    // Update
    public Optional<QuestionsDto> updateQuestion(Long id, Long topicId, LocalDate learningDate,
            String questionType, String contentInstruction,
            MultipartFile imageFile, MultipartFile audioFile,
            Integer timeLimitMinutes, Integer scorePoint) throws IOException {
        Optional<Questions> existingOpt = questionsRepository.findById(id);
        if (existingOpt.isEmpty()) return Optional.empty();

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topik tidak ditemukan: " + topicId));
        Questions q = existingOpt.get();
        q.setTopic(topic);
        q.setLearningDate(learningDate);
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

    // Get by Topic + student status, grouped by learningDate
    public List<LearningDateGroupDto> getQuestionsGroupedByDateWithStatus(Long topicId, Long studentId) {
        List<Questions> questions = topicRepository.findById(topicId)
                .map(topic -> questionsRepository.findByTopic(topic))
                .orElse(List.of());

        // Group by learningDate, preserving insertion order
        Map<LocalDate, List<LearningDateGroupDto.QuestionWithStatusDto>> grouped = new LinkedHashMap<>();
        for (Questions q : questions) {
            boolean done = studentAnswerRepository.existsByStudentIdAndQuestionsId(studentId, q.getId());
            LearningDateGroupDto.QuestionWithStatusDto dto = new LearningDateGroupDto.QuestionWithStatusDto(
                    q.getId(),
                    q.getTopic() != null ? q.getTopic().getId() : null,
                    q.getLearningDate(),
                    q.getQuestionType(),
                    q.getContentInstruction(),
                    q.getContentImage(),
                    q.getContentAudio(),
                    q.getTimeLimitMinutes(),
                    q.getScorePoint(),
                    Boolean.TRUE.equals(q.getIsAvailable()),
                    done ? "SELESAI" : "BELUM");
            grouped.computeIfAbsent(q.getLearningDate(), k -> new java.util.ArrayList<>()).add(dto);
        }

        return grouped.entrySet().stream()
                .map(e -> new LearningDateGroupDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    // Riwayat jawaban siswa (opsional filter topicId dan/atau learningDate)
    public List<StudentAnswerHistoryDto> getStudentAnswerHistory(Long studentId, Long topicId, LocalDate learningDate) {
        List<com.example.gamifikasi.entity.StudentAnswer> answers;
        if (topicId != null && learningDate != null) {
            answers = studentAnswerRepository.findAllByStudentIdAndTopicIdAndLearningDate(studentId, topicId, learningDate);
        } else if (topicId != null) {
            answers = studentAnswerRepository.findAllByStudentIdAndTopicId(studentId, topicId);
        } else if (learningDate != null) {
            answers = studentAnswerRepository.findAllByStudentIdAndLearningDate(studentId, learningDate);
        } else {
            answers = studentAnswerRepository.findAllByStudentId(studentId);
        }
        return answers.stream().map(sa -> {
            Questions q = sa.getQuestions();
            List<QuestionWithOptionsDto.OptionDto> options = questionOptionsRepository.findByQuestions(q)
                    .stream()
                    .map(opt -> new QuestionWithOptionsDto.OptionDto(
                            opt.getId(),
                            opt.getTeksOpsi(),
                            opt.getMediaOpsi(),
                            opt.getTipeItem() != null ? opt.getTipeItem().name() : null))
                    .collect(Collectors.toList());
            StudentAnswerHistoryDto dto = new StudentAnswerHistoryDto();
            dto.setAnswerId(sa.getId());
            dto.setCorrect(sa.getIsCorrect());
            dto.setEarnedScore(sa.getEarnedScore());
            dto.setSubmittedAnswer(sa.getStudentAnswer());
            dto.setQuestionId(q.getId());
            dto.setQuestionType(q.getQuestionType());
            dto.setContentInstruction(q.getContentInstruction());
            dto.setContentImage(q.getContentImage());
            dto.setContentAudio(q.getContentAudio());
            dto.setTimeLimitMinutes(q.getTimeLimitMinutes());
            dto.setScorePoint(q.getScorePoint());
            dto.setLearningDate(q.getLearningDate());
            dto.setOptions(options);
            dto.setTopicId(q.getTopic() != null ? q.getTopic().getId() : null);
            dto.setTopicName(q.getTopic() != null ? q.getTopic().getNameTopic() : null);
            if ("PUZZLE".equals(q.getQuestionType())) {
                jigsawPuzzleRepository.findByQuestion(q).ifPresent(puzzle -> {
                    List<StudentAnswerHistoryDto.PieceDto> pieces = puzzle.getPieces().stream()
                            .sorted(java.util.Comparator.comparingInt(
                                    com.example.gamifikasi.entity.JigsawPiece::getCorrectPosition))
                            .map(p -> new StudentAnswerHistoryDto.PieceDto(
                                    p.getId(), p.getPieceIndex(),
                                    p.getCorrectPosition(), p.getPieceImageUrl()))
                            .collect(Collectors.toList());
                    dto.setPuzzleInfo(new StudentAnswerHistoryDto.PuzzleInfoDto(
                            puzzle.getId(), puzzle.getImageUrl(),
                            puzzle.getGridRows(), puzzle.getGridCols(), pieces));
                });
            }
            return dto;
        }).collect(Collectors.toList());
    }

    // Set isAvailable = true for all questions in a topic on a given learningDate
    public List<QuestionsDto> setAvailableByTopicAndDate(Long topicId, LocalDate learningDate) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topik tidak ditemukan: " + topicId));
        List<Questions> questions = questionsRepository.findByTopicAndLearningDate(topic, learningDate);
        questions.forEach(q -> q.setIsAvailable(true));
        return questionsRepository.saveAll(questions).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
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
                    jigsawPuzzleRepository.findByQuestion(q).ifPresent(jigsawPuzzleRepository::delete);
                    matchingRelationRepository.deleteAll(matchingRelationRepository.findByQuestions(q));
                    studentAnswerRepository.deleteAll(studentAnswerRepository.findByQuestions(q));
                    questionOptionsRepository.deleteAll(questionOptionsRepository.findByQuestions(q));
                    questionsRepository.deleteById(id);
                    return true;
                })
                .orElse(false);
    }
}
