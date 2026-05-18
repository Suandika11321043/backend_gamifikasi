package com.example.gamifikasi.service;

import com.example.gamifikasi.dto.*;
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
    private StudentAnswerRepository studentAnswerRepository;

    @Autowired
    private StudentScoreRepository studentScoreRepository;

    @Autowired
    private StudentRankRepository studentRankRepository;

    // ────────────────────────────────────────────────────────────
    //  GET: ambil semua soal beserta opsi untuk satu topik
    // ────────────────────────────────────────────────────────────

    public List<QuestionWithOptionsDto> getQuestionsByTopic(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topik tidak ditemukan: " + topicId));

        return questionsRepository.findByTopic(topic).stream()
                .map(this::toQuestionWithOptionsDto)
                .collect(Collectors.toList());
    }

    private QuestionWithOptionsDto toQuestionWithOptionsDto(Questions q) {
        List<QuestionWithOptionsDto.OptionDto> options =
                questionOptionsRepository.findByQuestions(q).stream()
                        .map(o -> new QuestionWithOptionsDto.OptionDto(
                                o.getId(),
                                o.getTeksOpsi(),
                                o.getMediaOpsi(),
                                o.getTipeItem() != null ? o.getTipeItem().name() : null))
                        .collect(Collectors.toList());

        return new QuestionWithOptionsDto(
                q.getId(),
                q.getQuestionType(),
                q.getContentInstruction(),
                q.getContentImage(),
                q.getContentAudio(),
                options);
    }

    // ────────────────────────────────────────────────────────────
    //  SUBMIT SATU SOAL: mode soal satu-per-satu
    // ────────────────────────────────────────────────────────────

    @Transactional
    public SingleAnswerResponse submitSingleAnswer(SingleAnswerRequest request) {

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Siswa tidak ditemukan: " + request.getStudentId()));

        AnswerRequest answerReq = request.getAnswer();
        Questions question = questionsRepository.findById(answerReq.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Soal tidak ditemukan: " + answerReq.getQuestionId()));

        boolean isCorrect = gradeAnswer(question, answerReq);
        int earnedScore = isCorrect ? 100 : 0;

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
    //  FINISH KUIS: hitung bintang & update rank setelah semua soal dijawab
    // ────────────────────────────────────────────────────────────

    @Transactional
    public QuizResultResponse finishQuiz(QuizFinishRequest request) {

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Siswa tidak ditemukan: " + request.getStudentId()));

        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new RuntimeException("Topik tidak ditemukan: " + request.getTopicId()));

        int correctCount = request.getCorrectCount();
        int totalQuestions = request.getTotalQuestions();
        int newStars = calculateStars(correctCount, totalQuestions);

        StudentScore score = studentScoreRepository
                .findByStudentAndTopic(student, topic)
                .orElse(new StudentScore());

        boolean improved = score.getStarCount() == null || newStars > score.getStarCount();
        if (improved) {
            score.setStudent(student);
            score.setTopic(topic);
            score.setCorrectCount(correctCount);
            score.setStarCount(newStars);
            studentScoreRepository.save(score);
        }

        StudentRank rank = updateStudentRank(student);

        return new QuizResultResponse(
                correctCount,
                totalQuestions,
                newStars,
                improved,
                rank.getTotalStars(),
                rank.getRankName(),
                null);
    }

    // ────────────────────────────────────────────────────────────
    //  SUBMIT: siswa mengumpulkan jawaban kuis (semua sekaligus – legacy)
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
        List<AnswerResultDto> details = new ArrayList<>();

        for (AnswerRequest answerReq : request.getAnswers()) {

            Questions question = questionsRepository.findById(answerReq.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Soal tidak ditemukan: " + answerReq.getQuestionId()));

            boolean isCorrect = gradeAnswer(question, answerReq);
            int earnedScore = isCorrect ? 100 : 0;
            if (isCorrect) correctCount++;

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

            details.add(new AnswerResultDto(answerReq.getQuestionId(), isCorrect, earnedScore));
        }

        int newStars = calculateStars(correctCount, totalQuestions);

        // Update StudentScore – hanya simpan jika lebih baik dari skor sebelumnya
        StudentScore score = studentScoreRepository
                .findByStudentAndTopic(student, topic)
                .orElse(new StudentScore());

        boolean improved = score.getStarCount() == null || newStars > score.getStarCount();
        if (improved) {
            score.setStudent(student);
            score.setTopic(topic);
            score.setCorrectCount(correctCount);
            score.setStarCount(newStars);
            studentScoreRepository.save(score);
        }

        // Update StudentRank
        StudentRank rank = updateStudentRank(student);

        return new QuizResultResponse(
                correctCount,
                totalQuestions,
                newStars,
                improved,
                rank.getTotalStars(),
                rank.getRankName(),
                details);
    }

    // ────────────────────────────────────────────────────────────
    //  DEBUG: dump isi DB untuk satu soal
    // ────────────────────────────────────────────────────────────

    public java.util.Map<String, Object> debugQuestion(Long questionId) {
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        Questions q = questionsRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Soal tidak ditemukan: " + questionId));

        result.put("questionId", q.getId());
        result.put("questionType", q.getQuestionType());

        List<com.example.gamifikasi.entity.QuestionOptions> options =
                questionOptionsRepository.findByQuestions(q);

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
    //  CRUD: StudentScore
    // ────────────────────────────────────────────────────────────

    public List<StudentScore> getScoresByStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Siswa tidak ditemukan: " + studentId));
        return studentScoreRepository.findByStudent(student);
    }

    public Optional<StudentScore> getScoreByStudentAndTopic(Long studentId, Long topicId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Siswa tidak ditemukan: " + studentId));
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topik tidak ditemukan: " + topicId));
        return studentScoreRepository.findByStudentAndTopic(student, topic);
    }

    // ────────────────────────────────────────────────────────────
    //  CRUD: StudentRank
    // ────────────────────────────────────────────────────────────

    public Optional<StudentRank> getRankByStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Siswa tidak ditemukan: " + studentId));
        return studentRankRepository.findByStudent(student);
    }

    public List<StudentRank> getAllRanks() {
        return studentRankRepository.findAll();
    }

    // ────────────────────────────────────────────────────────────
    //  Helper: penilaian jawaban
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
                log.warn("[GRADE] questionId={} → tipe '{}' tidak dikenali!", question.getId(), question.getQuestionType());
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

        log.info("[MATCHING] questionId={} correctPairs={} submitted={}", question.getId(), correctPairs, submittedPairs);
        return submittedPairs.equals(correctPairs);
    }

    // ────────────────────────────────────────────────────────────
    //  Helper: kalkulasi bintang dan update rank
    // ────────────────────────────────────────────────────────────

    /**
     * Hitung bintang berdasarkan persentase jawaban benar:
     *  >= 80% → 3 bintang
     *  >= 60% → 2 bintang
     *  >= 40% → 1 bintang
     *   < 40% → 0 bintang
     */
    private int calculateStars(int correct, int total) {
        if (total == 0) return 0;
        double pct = (double) correct / total * 100.0;
        if (pct >= 80) return 3;
        if (pct >= 60) return 2;
        if (pct >= 40) return 1;
        return 0;
    }

    /**
     * Hitung ulang total bintang dari semua topik lalu perbarui StudentRank.
     */
    private StudentRank updateStudentRank(Student student) {
        int totalStars = studentScoreRepository.sumStarsByStudent(student);

        StudentRank rank = studentRankRepository.findByStudent(student)
                .orElse(new StudentRank());
        rank.setStudent(student);
        rank.setTotalStars(totalStars);
        rank.setRankName(RankLevel.fromTotalStars(totalStars));
        return studentRankRepository.save(rank);
    }
}
