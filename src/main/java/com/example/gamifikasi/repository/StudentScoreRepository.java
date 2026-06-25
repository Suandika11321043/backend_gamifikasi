package com.example.gamifikasi.repository;

import com.example.gamifikasi.entity.Student;
import com.example.gamifikasi.entity.StudentScore;
import com.example.gamifikasi.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentScoreRepository extends JpaRepository<StudentScore, Long> {
    Optional<StudentScore> findByStudentAndTopic(Student student, Topic topic);
    List<StudentScore> findByStudent(Student student);
    List<StudentScore> findByTopic(Topic topic);

    @Query("SELECT COALESCE(SUM(s.starCount), 0) FROM StudentScore s WHERE s.student = :student")
    int sumStarsByStudent(@Param("student") Student student);

    void deleteByStudent(Student student);
}
