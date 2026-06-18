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
public class LearningDateAvailabilityDto {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate learningDate;

    private Boolean isAvailable;

    private Integer questionCount;
}
