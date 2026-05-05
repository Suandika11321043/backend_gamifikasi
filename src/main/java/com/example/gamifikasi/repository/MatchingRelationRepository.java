package com.example.gamifikasi.repository;

import com.example.gamifikasi.entity.MatchingRelation;
import com.example.gamifikasi.entity.Questions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchingRelationRepository extends JpaRepository<MatchingRelation, Long> {
    List<MatchingRelation> findByQuestions(Questions questions);
}
