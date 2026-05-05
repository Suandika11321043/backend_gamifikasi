package com.example.gamifikasi.dto;

import com.example.gamifikasi.entity.QuestionOptions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionOptionsDto {
    private Long id;
    private Long questionId;
    private String teksOpsi;
    private String mediaOpsi;
    private Boolean kunciJawaban;
    private Integer urutanBenar;
    private QuestionOptions.TipeItem tipeItem;
}
