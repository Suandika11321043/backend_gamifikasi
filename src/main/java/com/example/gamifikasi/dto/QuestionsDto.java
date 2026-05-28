package com.example.gamifikasi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionsDto {
    private Long id;
    private Long topicId;
    private String questionType;
    private String contentInstruction;
    private String contentImage;
    private String contentAudio;
    private Integer timeLimitMinutes;
    private Integer scorePoint;
}
