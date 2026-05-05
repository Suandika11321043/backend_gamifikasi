package com.example.gamifikasi.repository;

import com.example.gamifikasi.entity.QuestionOptions;
import com.example.gamifikasi.entity.Questions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionOptionsRepository extends JpaRepository<QuestionOptions, Long> {
    List<QuestionOptions> findByQuestions(Questions questions);
    List<QuestionOptions> findByQuestionsAndTipeItem(Questions questions, QuestionOptions.TipeItem tipeItem);
}
