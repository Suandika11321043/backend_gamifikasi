package com.example.gamifikasi.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StudentLeaderboardDto {
    private int rank;
    private Long id;
    private String name;
    private String group;
    private String avatar;
    private Integer totalEarnedScore;
    private Integer totalStars;
    private String rankName;
}
