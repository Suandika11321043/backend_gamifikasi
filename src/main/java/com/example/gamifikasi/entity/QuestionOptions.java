package com.example.gamifikasi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "opsi_soal")
public class QuestionOptions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_opsi")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_soal", nullable = false)
    private Questions questions;

    @Column(name = "teks_opsi", columnDefinition = "TEXT")
    private String teksOpsi;

    @Column(name = "media_opsi")
    private String mediaOpsi;

    @Column(name = "kunci_jawaban")
    private Boolean kunciJawaban = false;

    @Column(name = "urutan_benar")
    private Integer urutanBenar;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipe_item")
    private TipeItem tipeItem = TipeItem.JAWABAN;

    public enum TipeItem {
        PERTANYAAN, JAWABAN
    }
}
