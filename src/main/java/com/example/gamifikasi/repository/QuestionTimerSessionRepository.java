package com.example.gamifikasi.repository;

import com.example.gamifikasi.entity.QuestionTimerSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionTimerSessionRepository extends JpaRepository<QuestionTimerSession, Long> {

    Optional<QuestionTimerSession> findByStudentIdAndTopicIdAndQuestionId(
            Long studentId, Long topicId, Long questionId);

    List<QuestionTimerSession> findByStudentIdAndTopicId(Long studentId, Long topicId);

    void deleteByStudentIdAndTopicId(Long studentId, Long topicId);

    void deleteByStudentIdAndTopicIdAndQuestionId(Long studentId, Long topicId, Long questionId);

    void deleteByStudentId(Long studentId);

    void deleteByTopicId(Long topicId);
}
