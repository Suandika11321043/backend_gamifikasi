package com.example.gamifikasi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Satu penempatan keping oleh siswa:
 * "saya meletakkan piece pieceId di posisi grid placedPosition".
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JigsawPlacementDto {
    private Long pieceId;
    private Integer placedPosition;
}
