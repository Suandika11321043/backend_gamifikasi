package com.example.gamifikasi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(
        name = "topic_learning_date",
        uniqueConstraints = @UniqueConstraint(columnNames = {"topic_id", "learning_date"})
)
public class TopicLearningDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(name = "learning_date", nullable = false)
    private LocalDate learningDate;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = false;
}
