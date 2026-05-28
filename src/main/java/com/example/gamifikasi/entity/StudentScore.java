package com.example.gamifikasi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "student_score", uniqueConstraints = @UniqueConstraint(columnNames = { "id_user", "id_topik" }))
@Getter
@Setter
public class StudentScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_skor")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_student", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_topic", nullable = false)
    private Topic topic;

    @Column(name = "correct_count", columnDefinition = "INT DEFAULT 0")
    private Integer correctCount;

    @Column(name = "star_count", columnDefinition = "INT DEFAULT 0")
    private Integer starCount;

    @Column(name = "total_earned_score", columnDefinition = "INT DEFAULT 0")
    private Integer totalEarnedScore;
}