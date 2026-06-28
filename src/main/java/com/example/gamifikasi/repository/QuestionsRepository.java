package com.example.gamifikasi.repository;

import com.example.gamifikasi.entity.Questions;
import com.example.gamifikasi.entity.Tema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionsRepository extends JpaRepository<Questions, Long> {
    List<Questions> findByTopic(Tema topic);
    List<Questions> findByQuestionType(String questionType);
    List<Questions> findByTopicAndLearningDate(Tema topic, java.time.LocalDate learningDate);
    List<Questions> findByTopicAndLearningDateBetween(Tema topic, java.time.LocalDate from, java.time.LocalDate to);
}
