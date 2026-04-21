package com.example.gamifikasi.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LevelDto {
    private Long id;

    @NotBlank(message = "nameLevel is required")
    private String nameLevel;

    @NotBlank(message = "description is required ")
    private String description;
}
