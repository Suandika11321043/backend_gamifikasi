package com.example.gamifikasi.service;

import com.example.gamifikasi.dto.*;
import java.util.LinkedHashMap;
import com.example.gamifikasi.entity.*;
import com.example.gamifikasi.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private static final Logger log = LoggerFactory.getLogger(QuizService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private QuestionsRepository questionsRepository;

    @Autowired
    private QuestionOptionsRepository questionOptionsRepository;

    @Autowired
    private MatchingRelationRepository matchingRelationRepository;

    @Autowired
    private JigsawPuzzleRepository jigsawPuzzleRepository;

    @Autowired
    private StudentAnswerRepository studentAnswerRepository;

    @Autowired
    private StudentScoreRepository studentScoreRepository;

    @Autowired
    private StudentDayScoreRepository studentDayScoreRepository;

    @Autowired
    private QuestionsService questionsService;

    // ────────────────────────────────────────────────────────────
    // GET: ambil semua soal beserta opsi untuk satu topik
    // ────────────────────────────────────────────────────────────

    public List<QuestionWithOptionsDto> getQuestionsByTopic(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topik tidak ditemukan: " + topicId));
        return questionsRepository.findByTopic(topic).stream()
                .map(this::toQuestionWithOptionsDto)
                .collect(Collectors.toList());
    }

    /**
     * Ambil soal berdasarkan topik + tanggal spesifik.
     * GET /api/quiz/topics/{topicId}/date/{date}/questions
     */
    public List<QuestionWithOptionsDto> getQuestionsByTopicAndDate(
            Long topicId, java.time.LocalDate date) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topik tidak ditemukan: " + topicId));
        if (!questionsService.isLearningDateAvailable(topicId, date)) {
            throw new RuntimeException("Soal untuk tanggal ini belum tersedia.");
        }
        return questionsRepository
                .findByTopicAndLearningDate(topic, date)
                .stream()
                .map(this::toQuestionWithOptionsDto)
                .collect(Collectors.toList());
    }

    private QuestionWithOptionsDto toQuestionWithOptionsDto(Questions q) {
        List<QuestionWithOptionsDto.OptionDto> options = questionOptionsRepository.findByQuestions(q).stream()
                .map(o -> new QuestionWithOptionsDto.OptionDto(
                        o.getId(),
                        o.getTeksOpsi(),
                        o.getMediaOpsi(),
                        o.getTipeItem() != null ? o.getTipeItem().name() : null))
                .collect(Collectors.toCollection(ArrayList::new));

        if (q.getQuestionType() != null) {
            String type = q.getQuestionType().toUpperCase();
            if ("SORTING".equals(type) || "ORDERING".equals(type)) {
                Collections.shuffle(options);
            }
        }

        return new QuestionWithOptionsDto(
                q.getId(),
                q.getQuestionType(),
                q.getContentInstruction(),
                q.getContentImage(),
                q.getContentAudio(),
                q.getTimeLimitMinutes(),
                q.getScorePoint(),
                options);
    }

    // ────────────────────────────────────────────────────────────
    // SUBMIT SATU SOAL: mode soal satu-per-satu
    // ────────────────────────────────────────────────────────────

    @Transactional
    public SingleAnswerResponse submitSingleAnswer(SingleAnswerRequest request) {

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Siswa tidak ditemukan: " + request.getStudentId()));

        AnswerRequest answerReq = request.getAnswer();
        Questions question = questionsRepository.findById(answerReq.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Soal tidak ditemukan: " + answerReq.getQuestionId()));

        if (studentAnswerRepository.existsByStudentIdAndQuestionsId(student.getId(), question.getId())) {
            throw new IllegalStateException("Soal ini sudah dikerjakan. Anda hanya dapat melihat jawaban yang telah dikirim.");
        }

        boolean isCorrect = gradeAnswer(question, answerReq);
        int baseScore = question.getScorePoint() != null ? question.getScorePoint() : 100;
        int earnedScore = isCorrect ? baseScore : 0;

        StudentAnswer studentAnswer = new StudentAnswer();
        studentAnswer.setStudent(student);
        studentAnswer.setQuestions(question);
        studentAnswer.setIsCorrect(isCorrect);
        studentAnswer.setEarnedScore(earnedScore);
        try {
            studentAnswer.setStudentAnswer(MAPPER.writeValueAsString(answerReq));
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            studentAnswer.setStudentAnswer(answerReq.toString());
        }
        studentAnswerRepository.save(studentAnswer);

        return new SingleAnswerResponse(question.getId(), isCorrect, earnedScore);
    }

    // ────────────────────────────────────────────────────────────
    // FINISH KUIS: hitung bintang & update rank setelah semua soal dijawab
    // ────────────────────────────────────────────────────────────

    @Transactional
    public QuizResultResponse finishQuiz(QuizFinishRequest request) {

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Siswa tidak ditemukan: " + request.getStudentId()));

        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new RuntimeException("Topik tidak ditemukan: " + request.getTopicId()));

        int sessionCorrect = request.getCorrectCount() != null ? request.getCorrectCount() : 0;
        int totalQuestions = request.getTotalQuestions() != null ? request.getTotalQuestions() : 0;
        java.time.LocalDate learningDate = request.getLearningDate();

        int dayCorrectCount;
        int dayEarnedScore;
        if (learningDate != null) {
            dayCorrectCount = studentAnswerRepository
                    .countLatestCorrectByStudentIdAndTopicIdAndLearningDate(
                            student.getId(), topic.getId(), learningDate);
            dayEarnedScore = studentAnswerRepository
                    .sumLatestEarnedScoreByStudentIdAndTopicIdAndLearningDate(
                            student.getId(), topic.getId(), learningDate);
        } else {
            dayCorrectCount = sessionCorrect;
            dayEarnedScore = request.getTotalEarnedScore() != null ? request.getTotalEarnedScore() : 0;
        }

        boolean improved = false;
        if (learningDate != null) {
            StudentDayScore dayScore = studentDayScoreRepository
                    .findByStudentAndTopicAndLearningDate(student, topic, learningDate)
                    .orElse(new StudentDayScore());
            int previousDayStars = dayScore.getStarCount() != null ? dayScore.getStarCount() : 0;
            int previousDayScore = dayScore.getTotalEarnedScore() != null ? dayScore.getTotalEarnedScore() : 0;
            improved = dayCorrectCount > previousDayStars || dayEarnedScore > previousDayScore;

            dayScore.setStudent(student);
            dayScore.setTopic(topic);
            dayScore.setLearningDate(learningDate);
            dayScore.setCorrectCount(dayCorrectCount);
            dayScore.setStarCount(dayCorrectCount);
            dayScore.setTotalEarnedScore(dayEarnedScore);
            studentDayScoreRepository.save(dayScore);
        }

        int topicCorrectCount = studentAnswerRepository
                .countLatestCorrectByStudentIdAndTopicId(student.getId(), topic.getId());

        int topicEarnedScore = studentAnswerRepository
                .sumLatestEarnedScoreByStudentIdAndTopicId(student.getId(), topic.getId());

        StudentScore score = studentScoreRepository
                .findByStudentAndTopic(student, topic)
                .orElse(new StudentScore());

        int previousTopicStars = score.getStarCount() != null ? score.getStarCount() : 0;
        int currentBestScore = score.getTotalEarnedScore() != null ? score.getTotalEarnedScore() : 0;
        if (!improved) {
            improved = topicCorrectCount > previousTopicStars || topicEarnedScore > currentBestScore;
        }

        score.setStudent(student);
        score.setTopic(topic);
        score.setTotalEarnedScore(topicEarnedScore);
        score.setCorrectCount(topicCorrectCount);
        score.setStarCount(topicCorrectCount);
        studentScoreRepository.save(score);

        int totalStars = studentDayScoreRepository.sumStarsByStudent(student);
        if (totalStars == 0) {
            totalStars = studentScoreRepository.sumStarsByStudent(student);
        }
        RankLevel rankLevel = RankLevel.fromTotalStars(totalStars);

        int starsEarned = learningDate != null ? dayCorrectCount : sessionCorrect;
        int earnedScoreForResponse = learningDate != null ? dayEarnedScore
                : (request.getTotalEarnedScore() != null ? request.getTotalEarnedScore() : topicEarnedScore);

        return new QuizResultResponse(
                learningDate != null ? dayCorrectCount : sessionCorrect,
                totalQuestions,
                starsEarned,
                improved,
                totalStars,
                rankLevel,
                earnedScoreForResponse,
                null);
    }

    // ────────────────────────────────────────────────────────────
    // SUBMIT: siswa mengumpulkan jawaban kuis (semua sekaligus – legacy)
    // ────────────────────────────────────────────────────────────

    @Transactional
    public QuizResultResponse submitQuiz(QuizSubmitRequest request) {

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Siswa tidak ditemukan: " + request.getStudentId()));

        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new RuntimeException("Topik tidak ditemukan: " + request.getTopicId()));

        List<Questions> topicQuestions = questionsRepository.findByTopic(topic);
        int totalQuestions = topicQuestions.size();

        int correctCount = 0;
        int totalEarnedScore = 0;
        List<AnswerResultDto> details = new ArrayList<>();

        for (AnswerRequest answerReq : request.getAnswers()) {

            Questions question = questionsRepository.findById(answerReq.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Soal tidak ditemukan: " + answerReq.getQuestionId()));

            if (studentAnswerRepository.existsByStudentIdAndQuestionsId(student.getId(), question.getId())) {
                // Soal sudah dijawab – lewati, hitung dari data tersimpan
                studentAnswerRepository.findTopByStudentIdAndQuestionsIdOrderByIdDesc(student.getId(), question.getId())
                        .ifPresent(existing -> details.add(new AnswerResultDto(question.getId(),
                                Boolean.TRUE.equals(existing.getIsCorrect()),
                                existing.getEarnedScore() != null ? existing.getEarnedScore() : 0)));
                continue;
            }

            boolean isCorrect = gradeAnswer(question, answerReq);
            int baseScore = question.getScorePoint() != null ? question.getScorePoint() : 100;
            int earnedScore = isCorrect ? baseScore : 0;
            if (isCorrect)
                correctCount++;

            // Simpan jawaban siswa
            StudentAnswer studentAnswer = new StudentAnswer();
            studentAnswer.setStudent(student);
            studentAnswer.setQuestions(question);
            studentAnswer.setIsCorrect(isCorrect);
            studentAnswer.setEarnedScore(earnedScore);
            try {
                studentAnswer.setStudentAnswer(MAPPER.writeValueAsString(answerReq));
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                studentAnswer.setStudentAnswer(answerReq.toString());
            }
            studentAnswerRepository.save(studentAnswer);

            totalEarnedScore += earnedScore;
            details.add(new AnswerResultDto(answerReq.getQuestionId(), isCorrect, earnedScore));
        }

        int newStars = correctCount;

        StudentScore score = studentScoreRepository
                .findByStudentAndTopic(student, topic)
                .orElse(new StudentScore());

        int topicCorrectCount = studentAnswerRepository
                .countLatestCorrectByStudentIdAndTopicId(student.getId(), topic.getId());

        int previousStars = score.getStarCount() != null ? score.getStarCount() : 0;
        int currentBestScore = score.getTotalEarnedScore() != null ? score.getTotalEarnedScore() : 0;
        int bestEarnedScore = Math.max(currentBestScore, totalEarnedScore);
        boolean improved = topicCorrectCount > previousStars || bestEarnedScore > currentBestScore;

        score.setStudent(student);
        score.setTopic(topic);
        score.setTotalEarnedScore(bestEarnedScore);
        score.setCorrectCount(topicCorrectCount);
        score.setStarCount(topicCorrectCount);
        studentScoreRepository.save(score);

        int totalStars = studentScoreRepository.sumStarsByStudent(student);
        RankLevel rankLevel = RankLevel.fromTotalStars(totalStars);

        return new QuizResultResponse(
                correctCount,
                totalQuestions,
                newStars,
                improved,
                totalStars,
                rankLevel,
                totalEarnedScore,
                details);
    }

    // ────────────────────────────────────────────────────────────
    // DEBUG: dump isi DB untuk satu soal
    // ────────────────────────────────────────────────────────────

    public java.util.Map<String, Object> debugQuestion(Long questionId) {
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        Questions q = questionsRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Soal tidak ditemukan: " + questionId));

        result.put("questionId", q.getId());
        result.put("questionType", q.getQuestionType());

        List<com.example.gamifikasi.entity.QuestionOptions> options = questionOptionsRepository.findByQuestions(q);

        List<java.util.Map<String, Object>> optList = options.stream().map(o -> {
            java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", o.getId());
            m.put("teksOpsi", o.getTeksOpsi());
            m.put("kunciJawaban", o.getKunciJawaban());
            m.put("urutanBenar", o.getUrutanBenar());
            m.put("tipeItem", o.getTipeItem());
            return m;
        }).collect(java.util.stream.Collectors.toList());
        result.put("options", optList);

        List<MatchingRelation> relations = matchingRelationRepository.findByQuestions(q);
        List<java.util.Map<String, Object>> relList = relations.stream().map(r -> {
            java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("opsiPertanyaanId", r.getOpsiPertanyaan().getId());
            m.put("opsiJawabanId", r.getOpsiJawaban().getId());
            return m;
        }).collect(java.util.stream.Collectors.toList());
        result.put("matchingRelations", relList);

        return result;
    }

    // ────────────────────────────────────────────────────────────
    // CRUD: StudentScore
    // ────────────────────────────────────────────────────────────

    public List<StudentScore> getScoresByStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Siswa tidak ditemukan: " + studentId));
        return studentScoreRepository.findByStudent(student);
    }

    public StudentScore getScoreByStudentAndTopic(Long studentId, Long topicId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Siswa tidak ditemukan: " + studentId));
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topik tidak ditemukan: " + topicId));

        int correctCount = studentAnswerRepository
                .countLatestCorrectByStudentIdAndTopicId(studentId, topicId);
        int earnedScore = studentAnswerRepository
                .sumLatestEarnedScoreByStudentIdAndTopicId(studentId, topicId);
        int dayStars = studentDayScoreRepository.sumStarsByStudentAndTopic(student, topic);
        int starCount = Math.max(correctCount, dayStars);

        StudentScore score = studentScoreRepository
                .findByStudentAndTopic(student, topic)
                .orElse(new StudentScore());

        score.setStudent(student);
        score.setTopic(topic);
        score.setCorrectCount(correctCount);
        score.setStarCount(starCount);
        score.setTotalEarnedScore(earnedScore);
        return score;
    }

    // ────────────────────────────────────────────────────────────
    // GET: soal beserta kunci jawaban (admin / review)
    // ────────────────────────────────────────────────────────────

    /**
     * Ambil soal + kunci jawaban berdasarkan topik & tanggal.
     * GET /api/quiz/topics/{topicId}/date/{date}/questions/answer
     */
    public List<QuestionWithCorrectAnswerDto> getQuestionsWithAnswersByTopicAndDate(
            Long topicId, java.time.LocalDate date) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topik tidak ditemukan: " + topicId));
        return questionsRepository.findByTopicAndLearningDate(topic, date).stream()
                .map(this::toQuestionWithCorrectAnswerDto)
                .collect(Collectors.toList());
    }

    private QuestionWithCorrectAnswerDto toQuestionWithCorrectAnswerDto(Questions q) {
        List<com.example.gamifikasi.entity.QuestionOptions> optionEntities =
                questionOptionsRepository.findByQuestions(q);

        List<QuestionWithCorrectAnswerDto.OptionWithAnswerDto> options = optionEntities.stream()
                .map(o -> new QuestionWithCorrectAnswerDto.OptionWithAnswerDto(
                        o.getId(),
                        o.getTeksOpsi(),
                        o.getMediaOpsi(),
                        o.getTipeItem() != null ? o.getTipeItem().name() : null,
                        o.getKunciJawaban(),
                        o.getUrutanBenar()))
                .collect(Collectors.toList());

        List<QuestionWithCorrectAnswerDto.MatchingPairDto> correctPairs =
                matchingRelationRepository.findByQuestions(q).stream()
                        .map(r -> new QuestionWithCorrectAnswerDto.MatchingPairDto(
                                r.getOpsiPertanyaan().getId(),
                                r.getOpsiJawaban().getId()))
                        .collect(Collectors.toList());

        // Puzzle answer: filled only for PUZZLE type
        QuestionWithCorrectAnswerDto.PuzzleAnswerDto puzzleAnswer = null;
        if ("PUZZLE".equalsIgnoreCase(q.getQuestionType())) {
            puzzleAnswer = jigsawPuzzleRepository.findByQuestion(q).map(puzzle -> {
                List<QuestionWithCorrectAnswerDto.PiecePosDto> pieces = puzzle.getPieces().stream()
                        .map(p -> new QuestionWithCorrectAnswerDto.PiecePosDto(
                                p.getId(),
                                p.getPieceIndex(),
                                p.getCorrectPosition(),
                                p.getPieceImageUrl()))
                        .collect(Collectors.toList());
                return new QuestionWithCorrectAnswerDto.PuzzleAnswerDto(
                        puzzle.getId(),
                        puzzle.getImageUrl(),
                        puzzle.getGridRows(),
                        puzzle.getGridCols(),
                        pieces);
            }).orElse(null);
        }

        return new QuestionWithCorrectAnswerDto(
                q.getId(),
                q.getQuestionType(),
                q.getContentInstruction(),
                q.getContentImage(),
                q.getContentAudio(),
                q.getTimeLimitMinutes(),
                q.getScorePoint(),
                options,
                correctPairs,
                puzzleAnswer);
    }

    // ────────────────────────────────────────────────────────────
    // GET: jawaban siswa per topik + detail soal
    // ────────────────────────────────────────────────────────────

    /**
     * Ambil jawaban siswa untuk setiap soal dalam topik, dilengkapi detail soal.
     * Soal yang belum dikerjakan tetap muncul dengan correct/earnedScore = null.
     * GET /api/quiz/students/{studentId}/topics/{topicId}/answers
     */
    public List<StudentAnswerDetailDto> getStudentAnswersDetailForTopic(
            Long studentId, Long topicId, java.time.LocalDate learningDate) {
        if (!studentRepository.existsById(studentId))
            throw new RuntimeException("Siswa tidak ditemukan: " + studentId);
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topik tidak ditemukan: " + topicId));

        List<Questions> questions = learningDate != null
                ? questionsRepository.findByTopicAndLearningDate(topic, learningDate)
                : questionsRepository.findByTopic(topic);

        return questions.stream().map(q -> {
            List<QuestionWithOptionsDto.OptionDto> opts = questionOptionsRepository.findByQuestions(q).stream()
                    .map(o -> new QuestionWithOptionsDto.OptionDto(
                            o.getId(),
                            o.getTeksOpsi(),
                            o.getMediaOpsi(),
                            o.getTipeItem() != null ? o.getTipeItem().name() : null))
                    .collect(Collectors.toList());

            Boolean correct = null;
            Integer earnedScore = null;
            String submittedAnswer = null;

            Optional<StudentAnswer> answerOpt =
                    studentAnswerRepository.findTopByStudentIdAndQuestionsIdOrderByIdDesc(studentId, q.getId());
            if (answerOpt.isPresent()) {
                StudentAnswer sa = answerOpt.get();
                correct = sa.getIsCorrect();
                earnedScore = sa.getEarnedScore();
                submittedAnswer = sa.getStudentAnswer();
            }

            return new StudentAnswerDetailDto(
                    q.getId(),
                    q.getQuestionType(),
                    q.getContentInstruction(),
                    q.getContentImage(),
                    q.getContentAudio(),
                    q.getTimeLimitMinutes(),
                    q.getScorePoint(),
                    opts,
                    correct,
                    earnedScore,
                    submittedAnswer);
        }).collect(Collectors.toList());
    }

    // ────────────────────────────────────────────────────────────
    // VIEW: jawaban yang sudah dikerjakan siswa
    // ────────────────────────────────────────────────────────────

    /**
     * Ambil jawaban siswa untuk satu soal tertentu.
     * Mengembalikan empty jika siswa belum pernah mengerjakan soal tsb.
     */
    public Optional<StudentAnswerViewDto> getStudentAnswerForQuestion(Long studentId, Long questionId) {
        if (!studentRepository.existsById(studentId))
            throw new RuntimeException("Siswa tidak ditemukan: " + studentId);
        if (!questionsRepository.existsById(questionId))
            throw new RuntimeException("Soal tidak ditemukan: " + questionId);

        return studentAnswerRepository
                .findTopByStudentIdAndQuestionsIdOrderByIdDesc(studentId, questionId)
                .map(sa -> new StudentAnswerViewDto(
                        questionId,
                        Boolean.TRUE.equals(sa.getIsCorrect()),
                        sa.getEarnedScore() != null ? sa.getEarnedScore() : 0,
                        sa.getStudentAnswer()));
    }

    /**
     * Ambil semua jawaban siswa untuk semua soal dalam satu topik.
     * Berguna untuk menampilkan review hasil kuis setelah selesai.
     */
    public List<StudentAnswerViewDto> getStudentAnswersForTopic(Long studentId, Long topicId) {
        if (!studentRepository.existsById(studentId))
            throw new RuntimeException("Siswa tidak ditemukan: " + studentId);
        if (!topicRepository.existsById(topicId))
            throw new RuntimeException("Topik tidak ditemukan: " + topicId);

        List<StudentAnswer> answers = studentAnswerRepository
                .findLatestAnswersByStudentIdAndTopicId(studentId, topicId);

        // Deduplikasi: ambil jawaban terbaru per soal
        Map<Long, StudentAnswer> latest = new LinkedHashMap<>();
        for (StudentAnswer sa : answers) {
            latest.putIfAbsent(sa.getQuestions().getId(), sa);
        }

        return latest.values().stream()
                .map(sa -> new StudentAnswerViewDto(
                        sa.getQuestions().getId(),
                        Boolean.TRUE.equals(sa.getIsCorrect()),
                        sa.getEarnedScore() != null ? sa.getEarnedScore() : 0,
                        sa.getStudentAnswer()))
                .collect(Collectors.toList());
    }

    // ────────────────────────────────────────────────────────────
    // Helper: penilaian jawaban
    // ────────────────────────────────────────────────────────────

    private boolean gradeAnswer(Questions question, AnswerRequest answer) {
        if (question.getQuestionType() == null) {
            log.warn("[GRADE] questionId={} → questionType NULL!", question.getId());
            return false;
        }

        log.info("[GRADE] questionId={} type='{}'", question.getId(), question.getQuestionType());
        switch (question.getQuestionType().toUpperCase()) {
            case "MULTIPLE_CHOICE":
            case "QUIZ":
                return gradeMultipleChoice(question, answer);
            case "ORDERING":
            case "SORTING":
                return gradeOrdering(question, answer);
            case "MATCHING":
            case "MATCH":
            case "DRAG_AND_DROP":
                return gradeMatching(question, answer);
            default:
                log.warn("[GRADE] questionId={} → tipe '{}' tidak dikenali!", question.getId(),
                        question.getQuestionType());
                return false;
        }
    }

    /**
     * MULTIPLE_CHOICE: jawaban benar bila set opsi yang dipilih sama persis
     * dengan set opsi yang bertanda kunciJawaban = true.
     */
    private boolean gradeMultipleChoice(Questions question, AnswerRequest answer) {
        if (answer.getSelectedOptionIds() == null || answer.getSelectedOptionIds().isEmpty()) {
            log.warn("[MC] questionId={} → selectedOptionIds kosong", question.getId());
            return false;
        }

        List<QuestionOptions> options = questionOptionsRepository.findByQuestions(question);
        Set<Long> correctIds = options.stream()
                .filter(o -> Boolean.TRUE.equals(o.getKunciJawaban()))
                .map(QuestionOptions::getId)
                .collect(Collectors.toSet());

        Set<Long> selected = new HashSet<>(answer.getSelectedOptionIds());
        log.info("[MC] questionId={} correctIds={} selected={}", question.getId(), correctIds, selected);
        return selected.equals(correctIds);
    }

    /**
     * ORDERING: jawaban benar bila setiap opsi berada pada posisi yang sesuai
     * dengan nilai urutanBenar-nya (1-based).
     */
    private boolean gradeOrdering(Questions question, AnswerRequest answer) {
        if (answer.getOrderedOptionIds() == null || answer.getOrderedOptionIds().isEmpty()) {
            log.warn("[ORDERING] questionId={} → orderedOptionIds kosong", question.getId());
            return false;
        }

        List<QuestionOptions> options = questionOptionsRepository.findByQuestions(question);
        Map<Long, Integer> correctOrder = options.stream()
                .filter(o -> o.getUrutanBenar() != null)
                .collect(Collectors.toMap(QuestionOptions::getId, QuestionOptions::getUrutanBenar));

        List<Long> submitted = answer.getOrderedOptionIds();
        log.info("[ORDERING] questionId={} correctOrder={} submitted={}", question.getId(), correctOrder, submitted);
        for (int i = 0; i < submitted.size(); i++) {
            Long optId = submitted.get(i);
            Integer expectedPos = correctOrder.get(optId);
            if (expectedPos == null || expectedPos != i + 1) {
                return false;
            }
        }
        return !submitted.isEmpty();
    }

    /**
     * MATCHING / DRAG_AND_DROP: jawaban benar bila setiap pasangan
     * (pertanyaan → set jawaban) yang dipilih siswa sama persis dengan
     * relasi yang ada di MatchingRelation (satu pertanyaan bisa punya
     * lebih dari satu jawaban benar).
     */
    private boolean gradeMatching(Questions question, AnswerRequest answer) {
        if (answer.getMatchingPairs() == null || answer.getMatchingPairs().isEmpty()) {
            log.warn("[MATCHING] questionId={} → matchingPairs kosong", question.getId());
            return false;
        }

        List<MatchingRelation> relations = matchingRelationRepository.findByQuestions(question);

        // Bangun correctPairs: Map<id_pertanyaan, Set<id_jawaban>>
        Map<Long, Set<Long>> correctPairs = new HashMap<>();
        for (MatchingRelation r : relations) {
            correctPairs
                    .computeIfAbsent(r.getOpsiPertanyaan().getId(), k -> new HashSet<>())
                    .add(r.getOpsiJawaban().getId());
        }

        // Konversi jawaban siswa dari Map<Long, List<Long>> ke Map<Long, Set<Long>>
        Map<Long, Set<Long>> submittedPairs = new HashMap<>();
        for (Map.Entry<Long, List<Long>> entry : answer.getMatchingPairs().entrySet()) {
            submittedPairs.put(entry.getKey(),
                    entry.getValue() == null ? new HashSet<>() : new HashSet<>(entry.getValue()));
        }

        log.info("[MATCHING] questionId={} correctPairs={} submitted={}", question.getId(), correctPairs,
                submittedPairs);
        return submittedPairs.equals(correctPairs);
    }
}
