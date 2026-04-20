package com.example.gamifikasi.service;

import com.example.gamifikasi.dto.LevelDto;
import com.example.gamifikasi.entity.Level;
import com.example.gamifikasi.repository.LevelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LevelService {

    @Autowired
    private LevelRepository levelRepository;

    // Convert Entity to DTO
    private LevelDto convertToDto(Level level) {
        return new LevelDto(level.getId(), level.getNameLevel(), level.getDescription());
    }

    // Convert DTO to Entity
    private Level convertToEntity(LevelDto levelDto) {
        Level level = new Level();
        level.setId(levelDto.getId());
        level.setNameLevel(levelDto.getNameLevel());
        level.setDescription(levelDto.getDescription());
        return level;
    }

    // Create Level
    public LevelDto createLevel(LevelDto levelDto) {
        Level level = convertToEntity(levelDto);
        level.setId(null); // Ensure ID is null for new entities
        Level savedLevel = levelRepository.save(level);
        return convertToDto(savedLevel);
    }

    // Get all Levels
    public List<LevelDto> getAllLevels() {
        return levelRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Get Level by ID
    public Optional<LevelDto> getLevelById(Long id) {
        return levelRepository.findById(id)
                .map(this::convertToDto);
    }

    // Get Level by Name
    public Optional<LevelDto> getLevelByName(String nameLevel) {
        return levelRepository.findByNameLevel(nameLevel)
                .map(this::convertToDto);
    }

    // Update Level
    public Optional<LevelDto> updateLevel(Long id, LevelDto levelDto) {
        return levelRepository.findById(id)
                .map(existingLevel -> {
                    existingLevel.setNameLevel(levelDto.getNameLevel());
                    existingLevel.setDescription(levelDto.getDescription());
                    Level updatedLevel = levelRepository.save(existingLevel);
                    return convertToDto(updatedLevel);
                });
    }

    // Delete Level
    public boolean deleteLevel(Long id) {
        if (levelRepository.existsById(id)) {
            levelRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
