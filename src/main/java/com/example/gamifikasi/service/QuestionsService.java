package com.example.gamifikasi.service;

import com.example.gamifikasi.dto.LearningDateAvailabilityDto;
import com.example.gamifikasi.dto.LearningDateGroupDto;
import com.example.gamifikasi.dto.QuestionsDto;
import com.example.gamifikasi.dto.QuestionWithOptionsDto;
import com.example.gamifikasi.dto.StudentAnswerHistoryDto;
import com.example.gamifikasi.entity.JigsawPiece;
import com.example.gamifikasi.entity.JigsawPuzzle;
import com.example.gamifikasi.entity.MatchingRelation;
import com.example.gamifikasi.entity.QuestionOptions;
import com.example.gamifikasi.entity.Questions;
import com.example.gamifikasi.entity.Tema;
import com.example.gamifikasi.repository.MatchingRelationRepository;
import com.example.gamifikasi.repository.QuestionOptionsRepository;
import com.example.gamifikasi.repository.QuestionsRepository;
import com.example.gamifikasi.repository.JigsawPieceRepository;
import com.example.gamifikasi.repository.JigsawPuzzleRepository;
import com.example.gamifikasi.repository.StudentAnswerRepository;
import com.example.gamifikasi.repository.TemaRepository;
import com.example.gamifikasi.util.FileStorageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
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
    private TemaRepository temaRepository;

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

    @Autowired
    private JigsawPieceRepository jigsawPieceRepository;

    public boolean isLearningDateAvailable(Long topicId, LocalDate learningDate) {
        return temaRepository.findById(topicId)
                .map(topic -> questionsRepository.findByTopicAndLearningDate(topic, learningDate))
                .filter(list -> !list.isEmpty())
                .map(list -> list.stream().anyMatch(q -> Boolean.TRUE.equals(q.getIsAvailable())))
                .orElse(false);
    }

    private boolean resolveAvailabilityForNewQuestion(Tema topic, LocalDate learningDate) {
        return questionsRepository.findByTopicAndLearningDate(topic, learningDate).stream()
                .anyMatch(q -> Boolean.TRUE.equals(q.getIsAvailable()));
    }

    private QuestionsDto convertToDto(Questions q) {
        Long topicId = q.getTopic() != null ? q.getTopic().getId() : null;
        return new QuestionsDto(
                q.getId(),
                topicId,
                q.getLearningDate(),
                q.getQuestionType(),
                q.getContentInstruction(),
                q.getContentImage(),
                q.getContentAudio(),
                q.getTimeLimitMinutes(),
                q.getScorePoint(),
                Boolean.TRUE.equals(q.getIsAvailable()),
                isQuestionOptionsConfigured(q));
    }

    /** Soal sudah punya opsi, pasangan, atau puzzle — tipe tidak boleh diganti. */
    public boolean isQuestionOptionsConfigured(Questions q) {
        if (q.getId() != null && jigsawPuzzleRepository.existsByQuestionId(q.getId())) {
            return true;
        }
        if (questionOptionsRepository.existsByQuestions(q)) {
            return true;
        }
        return matchingRelationRepository.existsByQuestions(q);
    }

    private void validateQuestionTypeChange(Questions q, String newType) {
        String currentType = q.getQuestionType();
        if (currentType == null || newType == null || currentType.equalsIgnoreCase(newType)) {
            return;
        }
        if (isQuestionOptionsConfigured(q)) {
            throw new IllegalStateException(
                    "Tipe soal tidak dapat diubah karena opsi atau konfigurasi sudah diatur. "
                            + "Hapus semua opsi atau konfigurasi puzzle terlebih dahulu.");
        }
    }

    // Create
    @Transactional
    public QuestionsDto createQuestion(Long topicId, LocalDate learningDate,
            String questionType, String contentInstruction,
            MultipartFile imageFile, MultipartFile audioFile,
            Integer timeLimitMinutes, Integer scorePoint) throws IOException {
        validateScorePoint(scorePoint);
        Tema topic = temaRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topik tidak ditemukan: " + topicId));
        Questions q = new Questions();
        q.setTopic(topic);
        q.setLearningDate(learningDate);
        q.setQuestionType(questionType);
        q.setContentInstruction(contentInstruction);
        q.setTimeLimitMinutes(timeLimitMinutes);
        q.setScorePoint(scorePoint);
        if (imageFile != null && !imageFile.isEmpty()) {
            q.setContentImage(fileStorageUtil.storeFile(imageFile, "questions"));
        }
        if (audioFile != null && !audioFile.isEmpty()) {
            q.setContentAudio(fileStorageUtil.storeFile(audioFile, "questions/audio"));
        }
        q.setIsAvailable(resolveAvailabilityForNewQuestion(topic, learningDate));
        return convertToDto(questionsRepository.save(q));
    }

    /**
     * Duplikat soal beserta opsi, pasangan (MATCH/DRAG_AND_DROP), urutan (SORTING), dan puzzle.
     */
    @Transactional
    public QuestionsDto duplicateQuestion(Long sourceId, Long topicId, LocalDate learningDate) {
        Questions source = questionsRepository.findById(sourceId)
                .orElseThrow(() -> new RuntimeException("Soal tidak ditemukan: " + sourceId));
        validateScorePoint(source.getScorePoint());

        Tema topic = temaRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topik tidak ditemukan: " + topicId));

        Questions copy = new Questions();
        copy.setTopic(topic);
        copy.setLearningDate(learningDate);
        copy.setQuestionType(source.getQuestionType());
        copy.setContentInstruction(source.getContentInstruction());
        copy.setContentImage(source.getContentImage());
        copy.setContentAudio(source.getContentAudio());
        copy.setTimeLimitMinutes(source.getTimeLimitMinutes());
        copy.setScorePoint(source.getScorePoint());
        copy.setIsAvailable(resolveAvailabilityForNewQuestion(topic, learningDate));

        Questions saved = questionsRepository.save(copy);
        copyQuestionAttachments(source, saved);
        return convertToDto(saved);
    }

    private void copyQuestionAttachments(Questions source, Questions target) {
        String qType = source.getQuestionType() != null ? source.getQuestionType() : "QUIZ";
        if ("PUZZLE".equalsIgnoreCase(qType)) {
            copyPuzzleAttachment(source, target);
            return;
        }

        Map<Long, Long> optionIdMap = copyQuestionOptions(source, target);
        if (needsMatchingRelations(qType)) {
            copyMatchingRelations(source, target, optionIdMap);
        }
    }

    private boolean needsMatchingRelations(String questionType) {
        return "MATCH".equalsIgnoreCase(questionType)
                || "MATCHING".equalsIgnoreCase(questionType)
                || "DRAG_AND_DROP".equalsIgnoreCase(questionType);
    }

    private Map<Long, Long> copyQuestionOptions(Questions source, Questions target) {
        Map<Long, Long> idMap = new HashMap<>();
        for (QuestionOptions src : questionOptionsRepository.findByQuestions(source)) {
            QuestionOptions opt = new QuestionOptions();
            opt.setQuestions(target);
            opt.setTeksOpsi(src.getTeksOpsi());
            opt.setMediaOpsi(src.getMediaOpsi());
            opt.setKunciJawaban(src.getKunciJawaban() != null ? src.getKunciJawaban() : false);
            opt.setUrutanBenar(src.getUrutanBenar());
            opt.setTipeItem(src.getTipeItem() != null ? src.getTipeItem() : QuestionOptions.TipeItem.JAWABAN);
            QuestionOptions saved = questionOptionsRepository.save(opt);
            idMap.put(src.getId(), saved.getId());
        }
        return idMap;
    }

    private void copyMatchingRelations(Questions source, Questions target, Map<Long, Long> optionIdMap) {
        for (MatchingRelation rel : matchingRelationRepository.findByQuestions(source)) {
            if (rel.getOpsiPertanyaan() == null || rel.getOpsiJawaban() == null) {
                continue;
            }
            Long newLeftId = optionIdMap.get(rel.getOpsiPertanyaan().getId());
            Long newRightId = optionIdMap.get(rel.getOpsiJawaban().getId());
            if (newLeftId == null || newRightId == null) {
                continue;
            }

            MatchingRelation copy = new MatchingRelation();
            copy.setQuestions(target);
            copy.setOpsiPertanyaan(questionOptionsRepository.findById(newLeftId).orElse(null));
            copy.setOpsiJawaban(questionOptionsRepository.findById(newRightId).orElse(null));
            matchingRelationRepository.save(copy);
        }
    }

    private void copyPuzzleAttachment(Questions source, Questions target) {
        jigsawPuzzleRepository.findByQuestion(source).ifPresent(puzzle -> {
            JigsawPuzzle copy = new JigsawPuzzle();
            copy.setQuestion(target);
            copy.setImageUrl(puzzle.getImageUrl());
            copy.setGridRows(puzzle.getGridRows());
            copy.setGridCols(puzzle.getGridCols());
            JigsawPuzzle savedPuzzle = jigsawPuzzleRepository.save(copy);

            for (JigsawPiece piece : jigsawPieceRepository.findByPuzzle(puzzle)) {
                JigsawPiece pieceCopy = new JigsawPiece();
                pieceCopy.setPuzzle(savedPuzzle);
                pieceCopy.setPieceIndex(piece.getPieceIndex());
                pieceCopy.setCorrectPosition(piece.getCorrectPosition());
                pieceCopy.setPieceImageUrl(piece.getPieceImageUrl());
                jigsawPieceRepository.save(pieceCopy);
            }
        });
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
        return temaRepository.findById(topicId)
                .map(topic -> questionsRepository.findByTopic(topic).stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    // Get by Topic + specific date
    public List<QuestionsDto> getQuestionsByTopicAndDate(Long topicId, LocalDate learningDate) {
        return temaRepository.findById(topicId)
                .map(topic -> questionsRepository.findByTopicAndLearningDate(topic, learningDate).stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    // Get by Topic + date range (e.g. seluruh bulan: from=2025-06-01, to=2025-06-30)
    public List<QuestionsDto> getQuestionsByTopicAndDateRange(Long topicId, LocalDate from, LocalDate to) {
        return temaRepository.findById(topicId)
                .map(topic -> questionsRepository.findByTopicAndLearningDateBetween(topic, from, to).stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    // Update
    @Transactional
    public Optional<QuestionsDto> updateQuestion(Long id, Long topicId, LocalDate learningDate,
            String questionType, String contentInstruction,
            MultipartFile imageFile, MultipartFile audioFile,
            Integer timeLimitMinutes, Integer scorePoint) throws IOException {
        validateScorePoint(scorePoint);
        Optional<Questions> existingOpt = questionsRepository.findById(id);
        if (existingOpt.isEmpty()) return Optional.empty();

        Tema topic = temaRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topik tidak ditemukan: " + topicId));
        Questions q = existingOpt.get();
        validateQuestionTypeChange(q, questionType);
        LocalDate previousDate = q.getLearningDate();
        q.setTopic(topic);
        q.setLearningDate(learningDate);
        q.setQuestionType(questionType);
        q.setContentInstruction(contentInstruction);
        q.setTimeLimitMinutes(timeLimitMinutes);
        q.setScorePoint(scorePoint);
        if (imageFile != null && !imageFile.isEmpty()) {
            fileStorageUtil.deleteFile(q.getContentImage());
            q.setContentImage(fileStorageUtil.storeFile(imageFile, "questions"));
        }
        if (audioFile != null && !audioFile.isEmpty()) {
            fileStorageUtil.deleteFile(q.getContentAudio());
            q.setContentAudio(fileStorageUtil.storeFile(audioFile, "questions/audio"));
        }
        if (previousDate == null || !previousDate.equals(learningDate)) {
            q.setIsAvailable(resolveAvailabilityForNewQuestion(topic, learningDate));
        }
        return Optional.of(convertToDto(questionsRepository.save(q)));
    }

    // Update timer only
    @Transactional
    public Optional<QuestionsDto> updateTimerLimit(Long id, Integer timeLimitMinutes) {
        return questionsRepository.findById(id)
                .map(q -> {
                    q.setTimeLimitMinutes(timeLimitMinutes);
                    return convertToDto(questionsRepository.save(q));
                });
    }

    // Get by Topic + student status, grouped by learningDate
    public List<LearningDateGroupDto> getQuestionsGroupedByDateWithStatus(Long topicId, Long studentId) {
        List<Questions> questions = temaRepository.findById(topicId)
                .map(topic -> questionsRepository.findByTopic(topic))
                .orElse(List.of());

        // Group by learningDate, preserving insertion order
        Map<LocalDate, List<LearningDateGroupDto.QuestionWithStatusDto>> grouped = new LinkedHashMap<>();
        for (Questions q : questions) {
            Long qTopicId = q.getTopic() != null ? q.getTopic().getId() : topicId;
            boolean dateAvailable = Boolean.TRUE.equals(q.getIsAvailable());
            boolean done = studentAnswerRepository.existsByStudentIdAndQuestionsId(studentId, q.getId());
            Boolean correct = null;
            if (done) {
                correct = studentAnswerRepository.findTopByStudentIdAndQuestionsIdOrderByIdDesc(studentId, q.getId())
                        .map(sa -> Boolean.TRUE.equals(sa.getIsCorrect()))
                        .orElse(false);
            }
            LearningDateGroupDto.QuestionWithStatusDto dto = new LearningDateGroupDto.QuestionWithStatusDto(
                    q.getId(),
                    qTopicId,
                    q.getLearningDate(),
                    q.getQuestionType(),
                    q.getContentInstruction(),
                    q.getContentImage(),
                    q.getContentAudio(),
                    q.getTimeLimitMinutes(),
                    q.getScorePoint(),
                    dateAvailable,
                    correct,
                    done ? "SELESAI" : "BELUM");
            grouped.computeIfAbsent(q.getLearningDate(), k -> new java.util.ArrayList<>()).add(dto);
        }

        return grouped.entrySet().stream()
                .map(e -> {
                    LocalDate date = e.getKey();
                    int starCount = studentAnswerRepository
                            .countLatestCorrectByStudentIdAndTopicIdAndLearningDate(studentId, topicId, date);
                    return new LearningDateGroupDto(
                            date,
                            isLearningDateAvailable(topicId, date),
                            starCount,
                            e.getValue());
                })
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
        return answers.stream()
                .collect(Collectors.toMap(
                        sa -> sa.getQuestions().getId(),
                        sa -> sa,
                        (first, ignored) -> first,
                        LinkedHashMap::new))
                .values().stream()
                .map(sa -> {
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

    // Atur ketersediaan per learning date (topik + tanggal) — update semua soal di tanggal tersebut
    @Transactional
    public LearningDateAvailabilityDto setAvailabilityByTopicAndDate(
            Long topicId, LocalDate learningDate, boolean available) {
        Tema topic = temaRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topik tidak ditemukan: " + topicId));
        List<Questions> questions = questionsRepository.findByTopicAndLearningDate(topic, learningDate);
        for (Questions q : questions) {
            q.setIsAvailable(available);
        }
        questionsRepository.saveAll(questions);

        return new LearningDateAvailabilityDto(learningDate, available, questions.size());
    }

    public Optional<LearningDateAvailabilityDto> getAvailabilityByTopicAndDate(
            Long topicId, LocalDate learningDate) {
        Tema topic = temaRepository.findById(topicId).orElse(null);
        if (topic == null) return Optional.empty();
        int count = questionsRepository.findByTopicAndLearningDate(topic, learningDate).size();
        if (count == 0) return Optional.empty();
        return Optional.of(new LearningDateAvailabilityDto(
                learningDate,
                isLearningDateAvailable(topicId, learningDate),
                count));
    }

    public List<LearningDateAvailabilityDto> getAvailabilityByTopic(Long topicId) {
        Tema topic = temaRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topik tidak ditemukan: " + topicId));
        Map<LocalDate, Integer> counts = new LinkedHashMap<>();
        for (Questions q : questionsRepository.findByTopic(topic)) {
            if (q.getLearningDate() != null) {
                counts.merge(q.getLearningDate(), 1, Integer::sum);
            }
        }
        return counts.entrySet().stream()
                .sorted(Map.Entry.<LocalDate, Integer>comparingByKey().reversed())
                .map(e -> new LearningDateAvailabilityDto(
                        e.getKey(),
                        isLearningDateAvailable(topicId, e.getKey()),
                        e.getValue()))
                .collect(Collectors.toList());
    }

    // Delete
    @Transactional
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

    /** Hapus semua soal (dan data terkait) dalam satu tema — dipanggil saat hapus tema. */
    @Transactional
    public void deleteAllQuestionsByTopicId(Long topicId) {
        Tema topic = temaRepository.findById(topicId).orElse(null);
        if (topic == null) {
            return;
        }
        List<Long> questionIds = questionsRepository.findByTopic(topic).stream()
                .map(Questions::getId)
                .toList();
        for (Long questionId : questionIds) {
            deleteQuestion(questionId);
        }
    }

    private void validateScorePoint(Integer scorePoint) {
        if (scorePoint == null || scorePoint <= 0) {
            throw new IllegalArgumentException("Poin soal wajib diisi dan harus lebih dari 0.");
        }
    }
}
