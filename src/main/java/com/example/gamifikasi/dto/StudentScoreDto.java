package com.example.gamifikasi.dto;

import com.example.gamifikasi.entity.Student;
import com.example.gamifikasi.entity.Tema;
import lombok.Getter;
import lombok.Setter;

/**
 * Skor agregat per siswa–topik. Dihitung dari jawaban siswa, bukan disimpan di DB.
 */
@Getter
@Setter
public class StudentScoreDto {
    private Long id;
    private Student student;
    private Tema topic;
    private Integer correctCount;
    private Integer starCount;
    private Integer totalEarnedScore;
}
