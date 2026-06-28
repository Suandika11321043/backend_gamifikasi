package com.example.gamifikasi.service;

import com.example.gamifikasi.dto.QuizTimerDto;
import com.example.gamifikasi.entity.QuestionTimerSession;
import com.example.gamifikasi.repository.QuestionTimerSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class QuizTimerService {

    @Autowired
    private QuestionTimerSessionRepository repository;

    /** Simpan (atau perbarui) sisa waktu untuk satu soal. */
    @Transactional
    public QuizTimerDto saveTimer(QuizTimerDto dto) {
        QuestionTimerSession session = repository
                .findByStudentIdAndTopicIdAndQuestionId(dto.getStudentId(), dto.getTopicId(), dto.getQuestionId())
                .orElse(new QuestionTimerSession());

        session.setStudentId(dto.getStudentId());
        session.setTopicId(dto.getTopicId());
        session.setQuestionId(dto.getQuestionId());
        session.setRemainingSeconds(dto.getRemainingSeconds());
        session.setUpdatedAt(LocalDateTime.now());

        QuestionTimerSession saved = repository.save(session);
        return toDto(saved);
    }

    /** Ambil sisa waktu untuk satu soal tertentu. */
    public Optional<QuizTimerDto> getTimer(Long studentId, Long topicId, Long questionId) {
        return repository
                .findByStudentIdAndTopicIdAndQuestionId(studentId, topicId, questionId)
                .map(this::toDto);
    }

    /**
     * Hapus semua timer session untuk satu topik (dipanggil setelah quiz selesai).
     */
    @Transactional
    public void clearTimers(Long studentId, Long topicId) {
        repository.deleteByStudentIdAndTopicId(studentId, topicId);
    }

    @Transactional
    public void clearTimer(Long studentId, Long topicId, Long questionId) {
        repository.deleteByStudentIdAndTopicIdAndQuestionId(studentId, topicId, questionId);
    }

    private QuizTimerDto toDto(QuestionTimerSession s) {
        return new QuizTimerDto(
                s.getStudentId(),
                s.getTopicId(),
                s.getQuestionId(),
                s.getRemainingSeconds(),
                s.getUpdatedAt());
    }
}
