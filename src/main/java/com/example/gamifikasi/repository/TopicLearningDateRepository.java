package com.example.gamifikasi.repository;

import com.example.gamifikasi.entity.TopicLearningDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TopicLearningDateRepository extends JpaRepository<TopicLearningDate, Long> {

    Optional<TopicLearningDate> findByTopicIdAndLearningDate(Long topicId, LocalDate learningDate);

    List<TopicLearningDate> findByTopicId(Long topicId);
}
