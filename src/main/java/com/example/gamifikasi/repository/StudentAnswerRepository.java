package com.example.gamifikasi.repository;

import com.example.gamifikasi.entity.Questions;
import com.example.gamifikasi.entity.Student;
import com.example.gamifikasi.entity.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {
    List<StudentAnswer> findByStudent(Student student);

    List<StudentAnswer> findByStudentAndQuestions(Student student, Questions questions);

    /** Cek apakah siswa sudah pernah menjawab soal tertentu. */
    boolean existsByStudentIdAndQuestionsId(Long studentId, Long questionsId);

    /** Ambil jawaban terbaru siswa untuk soal tertentu. */
    Optional<StudentAnswer> findTopByStudentIdAndQuestionsIdOrderByIdDesc(Long studentId, Long questionsId);

    /** Semua jawaban siswa untuk semua soal dalam satu topik. */
    @Query("SELECT sa FROM StudentAnswer sa WHERE sa.student.id = :studentId AND sa.questions.topic.id = :topicId ORDER BY sa.id DESC")
    List<StudentAnswer> findLatestAnswersByStudentIdAndTopicId(@Param("studentId") Long studentId, @Param("topicId") Long topicId);

    /** Semua riwayat jawaban siswa (seluruh topik), diurutkan terbaru dulu. */
    @Query("SELECT sa FROM StudentAnswer sa WHERE sa.student.id = :studentId ORDER BY sa.id DESC")
    List<StudentAnswer> findAllByStudentId(@Param("studentId") Long studentId);

    /** Riwayat jawaban siswa difilter per topik. */
    @Query("SELECT sa FROM StudentAnswer sa WHERE sa.student.id = :studentId AND sa.questions.topic.id = :topicId ORDER BY sa.id DESC")
    List<StudentAnswer> findAllByStudentIdAndTopicId(@Param("studentId") Long studentId, @Param("topicId") Long topicId);

    /** Riwayat jawaban siswa difilter per learningDate. */
    @Query("SELECT sa FROM StudentAnswer sa WHERE sa.student.id = :studentId AND sa.questions.learningDate = :learningDate ORDER BY sa.id DESC")
    List<StudentAnswer> findAllByStudentIdAndLearningDate(@Param("studentId") Long studentId, @Param("learningDate") java.time.LocalDate learningDate);

    /** Riwayat jawaban siswa difilter per topik + learningDate. */
    @Query("SELECT sa FROM StudentAnswer sa WHERE sa.student.id = :studentId AND sa.questions.topic.id = :topicId AND sa.questions.learningDate = :learningDate ORDER BY sa.id DESC")
    List<StudentAnswer> findAllByStudentIdAndTopicIdAndLearningDate(@Param("studentId") Long studentId, @Param("topicId") Long topicId, @Param("learningDate") java.time.LocalDate learningDate);

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

    /** Jumlah soal dijawab benar (jawaban terbaru per soal) dalam satu topik. */
    @Query(value = "SELECT COALESCE(SUM(CASE WHEN sa.is_correct = 1 THEN 1 ELSE 0 END), 0) FROM student_answer sa " +
            "JOIN questions q ON sa.id_question = q.ID " +
            "WHERE sa.id_student = :studentId AND q.topic_id = :topicId " +
            "AND sa.id IN (" +
            "  SELECT MAX(sa2.id) FROM student_answer sa2 " +
            "  JOIN questions q2 ON sa2.id_question = q2.ID " +
            "  WHERE sa2.id_student = :studentId AND q2.topic_id = :topicId " +
            "  GROUP BY sa2.id_question" +
            ")", nativeQuery = true)
    int countLatestCorrectByStudentIdAndTopicId(@Param("studentId") Long studentId, @Param("topicId") Long topicId);

    /** Jumlah soal benar (jawaban terbaru per soal) pada satu tanggal belajar dalam topik. */
    @Query(value = "SELECT COALESCE(SUM(CASE WHEN sa.is_correct = 1 THEN 1 ELSE 0 END), 0) FROM student_answer sa " +
            "JOIN questions q ON sa.id_question = q.ID " +
            "WHERE sa.id_student = :studentId AND q.topic_id = :topicId AND q.learning_date = :learningDate " +
            "AND sa.id IN (" +
            "  SELECT MAX(sa2.id) FROM student_answer sa2 " +
            "  JOIN questions q2 ON sa2.id_question = q2.ID " +
            "  WHERE sa2.id_student = :studentId AND q2.topic_id = :topicId AND q2.learning_date = :learningDate " +
            "  GROUP BY sa2.id_question" +
            ")", nativeQuery = true)
    int countLatestCorrectByStudentIdAndTopicIdAndLearningDate(
            @Param("studentId") Long studentId,
            @Param("topicId") Long topicId,
            @Param("learningDate") java.time.LocalDate learningDate);

    /** Total poin dari jawaban terbaru per soal pada satu tanggal belajar dalam topik. */
    @Query(value = "SELECT COALESCE(SUM(sa.earned_score), 0) FROM student_answer sa " +
            "JOIN questions q ON sa.id_question = q.ID " +
            "WHERE sa.id_student = :studentId AND q.topic_id = :topicId AND q.learning_date = :learningDate " +
            "AND sa.id IN (" +
            "  SELECT MAX(sa2.id) FROM student_answer sa2 " +
            "  JOIN questions q2 ON sa2.id_question = q2.ID " +
            "  WHERE sa2.id_student = :studentId AND q2.topic_id = :topicId AND q2.learning_date = :learningDate " +
            "  GROUP BY sa2.id_question" +
            ")", nativeQuery = true)
    int sumLatestEarnedScoreByStudentIdAndTopicIdAndLearningDate(
            @Param("studentId") Long studentId,
            @Param("topicId") Long topicId,
            @Param("learningDate") java.time.LocalDate learningDate);

    void deleteByStudentId(Long studentId);
}
