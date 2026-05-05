package com.example.gamifikasi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatchingRelationDto {
    private Long id;
    private Long questionId;
    private Long opsiPertanyaanId;
    private Long opsiJawabanId;
}
