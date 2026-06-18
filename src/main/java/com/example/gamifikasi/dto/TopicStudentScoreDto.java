package com.example.gamifikasi.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TopicStudentScoreDto {
    private Long studentId;
    private String studentName;
    private String studentGroup;
    private String avatar;
    private Integer totalEarnedScore;
    private Integer starCount;
    private int rank;
}
