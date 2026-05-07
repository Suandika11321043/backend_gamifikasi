package com.example.gamifikasi.repository;

import com.example.gamifikasi.entity.Questions;
import com.example.gamifikasi.entity.Student;
import com.example.gamifikasi.entity.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {
    List<StudentAnswer> findByStudent(Student student);
    List<StudentAnswer> findByStudentAndQuestions(Student student, Questions questions);
    List<StudentAnswer> findByQuestions(Questions questions);

    @Query("SELECT COALESCE(SUM(a.earnedScore), 0) FROM StudentAnswer a WHERE a.student = :student")
    int sumEarnedScoreByStudent(@Param("student") Student student);
}
