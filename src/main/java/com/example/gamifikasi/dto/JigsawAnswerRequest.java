package com.example.gamifikasi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request yang dikirim siswa saat mengumpulkan jawaban Jigsaw Puzzle.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JigsawAnswerRequest {
    private Long studentId;
    private Long questionId;
    /** Daftar penempatan keping: satu entry per keping puzzle. */
    private List<JigsawPlacementDto> placements;
}
