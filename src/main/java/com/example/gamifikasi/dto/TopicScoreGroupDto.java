package com.example.gamifikasi.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TopicScoreGroupDto {
    private Long topicId;
    private String topicName;
    private List<TopicStudentScoreDto> students = new ArrayList<>();
}
