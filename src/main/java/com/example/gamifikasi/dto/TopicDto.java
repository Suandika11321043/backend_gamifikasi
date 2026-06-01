package com.example.gamifikasi.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopicDto {
    private Long id;
    private String nameTopic;
    private String description;
    private String icon;
    private Boolean isActive;
}
