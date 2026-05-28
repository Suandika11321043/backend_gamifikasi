package com.example.gamifikasi.repository;

import com.example.gamifikasi.entity.QuizTimerSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizTimerSessionRepository extends JpaRepository<QuizTimerSession, Long> {

    Optional<QuizTimerSession> findByStudentIdAndTopicIdAndQuestionId(
            Long studentId, Long topicId, Long questionId);

    List<QuizTimerSession> findByStudentIdAndTopicId(Long studentId, Long topicId);

    void deleteByStudentIdAndTopicId(Long studentId, Long topicId);
}
