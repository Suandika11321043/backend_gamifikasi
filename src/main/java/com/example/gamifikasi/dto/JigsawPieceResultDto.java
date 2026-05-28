package com.example.gamifikasi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Detail hasil penilaian satu keping puzzle.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JigsawPieceResultDto {
    private Long pieceId;
    private Integer pieceIndex;
    private Integer placedPosition;
    private Integer correctPosition;
    private Boolean isCorrect;
}
