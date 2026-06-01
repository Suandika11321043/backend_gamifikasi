package com.example.gamifikasi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "Topic")
public class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
    @Column(name = "NAME_TOPIC")
    private String nameTopic;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "ICON")
    private String icon;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive = true;
}
