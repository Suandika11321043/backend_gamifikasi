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
    @Column( name = "ID")
    private Long id;
    @Column( name = "NAME_TOPIC")
    private String nameTopic;
    @ManyToOne
    @JoinColumn(name = "LEVEL_ID", referencedColumnName = "ID")
    private Level levelId;
    @Column(name="ICON")
    private String icon;
}
