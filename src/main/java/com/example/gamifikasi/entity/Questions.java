package com.example.gamifikasi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    private Topic topic;

    @Column(name = "question_type")
    private String questionType;

    @Column(name = "content_instruction")
    private String contentInstruction;

    @Column(name = "content_image")
    private String contentImage;

    @Column(name = "content_audio")
    private String contentAudio;
}
