package com.example.gamifikasi.repository;

import com.example.gamifikasi.entity.Student;
import com.example.gamifikasi.entity.StudentDayScore;
import com.example.gamifikasi.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentDayScoreRepository extends JpaRepository<StudentDayScore, Long> {

    Optional<StudentDayScore> findByStudentAndTopicAndLearningDate(
            Student student, Topic topic, LocalDate learningDate);

    List<StudentDayScore> findByStudentAndTopic(Student student, Topic topic);

    @Query("SELECT COALESCE(SUM(s.starCount), 0) FROM StudentDayScore s WHERE s.student = :student")
    int sumStarsByStudent(@Param("student") Student student);

    @Query("SELECT COALESCE(SUM(s.starCount), 0) FROM StudentDayScore s WHERE s.student = :student AND s.topic = :topic")
    int sumStarsByStudentAndTopic(@Param("student") Student student, @Param("topic") Topic topic);

    void deleteByStudent(Student student);
}
