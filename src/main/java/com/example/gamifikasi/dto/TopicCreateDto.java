package com.example.gamifikasi.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopicCreateDto {
    private String nameTopic;
    private String levelId;
    private String icon;
}
