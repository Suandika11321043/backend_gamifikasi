package com.example.gamifikasi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(
        name = "student_day_score",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_student", "id_topic", "learning_date"})
)
@Getter
@Setter
public class StudentScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_student", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_topic", nullable = false)
    private Tema topic;

    @Column(name = "learning_date", nullable = false)
    private LocalDate learningDate;

    @Column(name = "correct_count", columnDefinition = "INT DEFAULT 0")
    private Integer correctCount;

    @Column(name = "total_earned_score", columnDefinition = "INT DEFAULT 0")
    private Integer totalEarnedScore;
}
