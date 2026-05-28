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

    @Query(value = "SELECT COALESCE(SUM(max_score), 0) FROM (SELECT MAX(sa.earned_score) AS max_score FROM student_answer sa JOIN questions q ON sa.id_question = q.ID WHERE sa.id_student = :studentId GROUP BY q.topic_id, sa.id_question) AS question_scores", nativeQuery = true)
    int sumMaxEarnedScorePerTopicByStudentId(@Param("studentId") Long studentId);

    /**
     * Hitung total earned_score dari jawaban terbaru (ID tertinggi) per soal
     * untuk siswa dan topik tertentu. Dipakai oleh finishQuiz untuk kalkulasi
     * server-side tanpa bergantung pada data frontend.
     */
    @Query(value = "SELECT COALESCE(SUM(sa.earned_score), 0) FROM student_answer sa " +
            "JOIN questions q ON sa.id_question = q.ID " +
            "WHERE sa.id_student = :studentId AND q.topic_id = :topicId " +
            "AND sa.id IN (" +
            "  SELECT MAX(sa2.id) FROM student_answer sa2 " +
            "  JOIN questions q2 ON sa2.id_question = q2.ID " +
            "  WHERE sa2.id_student = :studentId AND q2.topic_id = :topicId " +
            "  GROUP BY sa2.id_question" +
            ")", nativeQuery = true)
    int sumLatestEarnedScoreByStudentIdAndTopicId(@Param("studentId") Long studentId, @Param("topicId") Long topicId);
}
