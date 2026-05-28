package com.example.gamifikasi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Menyimpan sisa waktu pengerjaan per soal agar frontend
 * dapat melanjutkan timer setelah halaman di-refresh.
 */
@Entity
@Getter
@Setter
@Table(name = "quiz_timer_session", uniqueConstraints = @UniqueConstraint(columnNames = { "student_id", "topic_id",
        "question_id" }))
public class QuizTimerSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    /** Sisa waktu dalam detik. */
    @Column(name = "remaining_seconds", nullable = false)
    private Integer remainingSeconds;

    /** Waktu terakhir data ini disimpan (UTC). */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
