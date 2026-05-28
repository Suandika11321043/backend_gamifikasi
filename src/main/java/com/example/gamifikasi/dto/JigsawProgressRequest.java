package com.example.gamifikasi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request yang dikirim siswa saat menggerakkan keping untuk cek progress.
 * Tidak menyimpan jawaban – hanya menghitung persentase keping yang sudah
 * benar.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JigsawProgressRequest {
    private Long questionId;
    /**
     * Posisi keping saat ini di frontend (satu entry per keping yang sudah
     * diletakkan).
     */
    private List<JigsawPlacementDto> placements;
}
