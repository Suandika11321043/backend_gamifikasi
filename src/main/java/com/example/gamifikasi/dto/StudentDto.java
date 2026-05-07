package com.example.gamifikasi.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StudentDto {
    private Long id;
    private String name;
    private String group;
    private String avatar;

    /** Total earned score dari semua jawaban siswa (student_answer.earned_score). */
    private Integer totalEarnedScore;

    /** Total bintang dari semua topik (student_rank.total_bintang). */
    private Integer totalStars;

    /** Nama rank siswa saat ini (student_rank.nama_rank). */
    private String rankName;
}
