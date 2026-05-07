package com.example.gamifikasi.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "student_rank")
public class StudentRank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rank")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_student", nullable = false, unique = true)
    private Student student;

    @Column(name = "total_bintang", columnDefinition = "INT DEFAULT 0")
    private Integer totalStars;

    @Enumerated(EnumType.STRING)
    @Column(name = "nama_rank", columnDefinition = "VARCHAR(50) DEFAULT 'BEGINNER'")
    private RankLevel rankName = RankLevel.BEGINNER;
}
