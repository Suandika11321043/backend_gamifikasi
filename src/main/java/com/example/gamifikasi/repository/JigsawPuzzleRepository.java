package com.example.gamifikasi.repository;

import com.example.gamifikasi.entity.JigsawPuzzle;
import com.example.gamifikasi.entity.Questions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JigsawPuzzleRepository extends JpaRepository<JigsawPuzzle, Long> {
    Optional<JigsawPuzzle> findByQuestion(Questions question);

    Optional<JigsawPuzzle> findByQuestionId(Long questionId);

    boolean existsByQuestionId(Long questionId);
}
