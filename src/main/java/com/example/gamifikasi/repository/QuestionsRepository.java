package com.example.gamifikasi.repository;

import com.example.gamifikasi.entity.Questions;
import com.example.gamifikasi.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionsRepository extends JpaRepository<Questions, Long> {
    List<Questions> findByTopic(Topic topic);
    List<Questions> findByQuestionType(String questionType);
    List<Questions> findByTopicAndLearningDate(Topic topic, java.time.LocalDate learningDate);
    List<Questions> findByTopicAndLearningDateBetween(Topic topic, java.time.LocalDate from, java.time.LocalDate to);
}
