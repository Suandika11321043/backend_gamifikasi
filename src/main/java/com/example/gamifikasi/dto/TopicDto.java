package com.example.gamifikasi.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopicDto {
    private Long id;
    private String nameTopic;
    private Long levelId;
    private String icon;
}
