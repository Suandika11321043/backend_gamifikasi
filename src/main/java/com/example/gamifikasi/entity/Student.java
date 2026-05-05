package com.example.gamifikasi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Setter
@Getter
@Table(name = "Student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "student_group")
    private String group;

    @Column(name = "AVATAR")
    private String avatar;

    @Column(name = "TOTAL_POINTS")
    private String totalPoints;

    @Column(name = "LEVEL")
    private String level;
}
