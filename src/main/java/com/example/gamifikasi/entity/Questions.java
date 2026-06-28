package com.example.gamifikasi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Setter
@Getter
@Table(name = "Questions")
public class Questions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "topic_id")
    private Tema topic;

    @Column(name = "learning_date")
    private LocalDate learningDate;

    @Column(name = "question_type")
    private String questionType;

    @Column(name = "content_instruction")
    private String contentInstruction;

    @Column(name = "content_image")
    private String contentImage;

    @Column(name = "content_audio")
    private String contentAudio;

    @Column(name = "time_limit_minutes")
    private Integer timeLimitMinutes;

    @Column(name = "score_point")
    private Integer scorePoint;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = false;
}
