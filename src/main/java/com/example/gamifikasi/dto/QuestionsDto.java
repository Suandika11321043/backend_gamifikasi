package com.example.gamifikasi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionsDto {
    private Long id;
    private Long topicId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate learningDate;
    private String questionType;
    private String contentInstruction;
    private String contentImage;
    private String contentAudio;
    private Integer timeLimitMinutes;
    private Integer scorePoint;
    private Boolean isAvailable;
}
