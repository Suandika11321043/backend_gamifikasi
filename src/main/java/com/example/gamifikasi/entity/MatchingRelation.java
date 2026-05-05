package com.example.gamifikasi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "relasi_matching")
public class MatchingRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_relasi")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_soal", nullable = false)
    private Questions questions;

    @ManyToOne
    @JoinColumn(name = "id_opsi_pertanyaan", nullable = false)
    private QuestionOptions opsiPertanyaan;

    @ManyToOne
    @JoinColumn(name = "id_opsi_jawaban", nullable = false)
    private QuestionOptions opsiJawaban;
}
