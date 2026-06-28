package com.example.gamifikasi.repository;

import com.example.gamifikasi.entity.Student;
import com.example.gamifikasi.entity.StudentScore;
import com.example.gamifikasi.entity.Tema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentScoreRepository extends JpaRepository<StudentScore, Long> {

    Optional<StudentScore> findByStudentAndTopicAndLearningDate(
            Student student, Tema topic, LocalDate learningDate);

    List<StudentScore> findByStudent(Student student);

    List<StudentScore> findByStudentAndTopic(Student student, Tema topic);

    @Query("SELECT COALESCE(SUM(s.correctCount), 0) FROM StudentScore s WHERE s.student = :student")
    int sumStarsByStudent(@Param("student") Student student);

    @Query("SELECT COALESCE(SUM(s.correctCount), 0) FROM StudentScore s WHERE s.student = :student AND s.topic = :topic")
    int sumStarsByStudentAndTopic(@Param("student") Student student, @Param("topic") Tema topic);

    @Query("SELECT COALESCE(SUM(s.correctCount), 0) FROM StudentScore s WHERE s.student = :student AND s.topic = :topic")
    int sumCorrectCountByStudentAndTopic(@Param("student") Student student, @Param("topic") Tema topic);

    @Query("SELECT COALESCE(SUM(s.totalEarnedScore), 0) FROM StudentScore s WHERE s.student = :student AND s.topic = :topic")
    int sumTotalEarnedScoreByStudentAndTopic(@Param("student") Student student, @Param("topic") Tema topic);

    void deleteByStudent(Student student);
}
