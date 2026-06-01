package com.example.gamifikasi.service;

import com.example.gamifikasi.dto.StudentDto;
import com.example.gamifikasi.entity.Student;
import com.example.gamifikasi.entity.StudentRank;
import com.example.gamifikasi.repository.StudentAnswerRepository;
import com.example.gamifikasi.repository.StudentRankRepository;
import com.example.gamifikasi.repository.StudentRepository;
import com.example.gamifikasi.repository.StudentScoreRepository;
import com.example.gamifikasi.util.FileStorageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FileStorageUtil fileStorageUtil;

    @Autowired
    private StudentAnswerRepository studentAnswerRepository;

    @Autowired
    private StudentScoreRepository studentScoreRepository;

    @Autowired
    private StudentRankRepository studentRankRepository;

    private StudentDto convertToDto(Student student) {
        StudentDto dto = new StudentDto();
        dto.setId(student.getId());
        dto.setName(student.getName());
        dto.setGroup(student.getGroup());
        dto.setAvatar(student.getAvatar());

        // Total earned score: jumlah skor tertinggi per topik
        dto.setTotalEarnedScore(studentAnswerRepository.sumMaxEarnedScorePerTopicByStudentId(student.getId()));

        // Total bintang dan rank dari student_rank
        Optional<StudentRank> rankOpt = studentRankRepository.findByStudent(student);
        if (rankOpt.isPresent()) {
            StudentRank rank = rankOpt.get();
            dto.setTotalStars(rank.getTotalStars() != null ? rank.getTotalStars() : 0);
            dto.setRankName(rank.getRankName() != null ? rank.getRankName().name() : null);
        } else {
            dto.setTotalStars(studentScoreRepository.sumStarsByStudent(student));
            dto.setRankName(null);
        }

        return dto;
    }

    // Create Student
    public StudentDto createStudent(StudentDto studentDto, MultipartFile avatarFile) throws IOException {
        Student student = new Student();
        student.setName(studentDto.getName());
        student.setGroup(studentDto.getGroup());

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String filename = fileStorageUtil.storeFile(avatarFile);
            student.setAvatar(filename);
        }

        Student saved = studentRepository.save(student);
        return convertToDto(saved);
    }

    // Get all Students
    public List<StudentDto> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Get Student by ID
    public Optional<StudentDto> getStudentById(Long id) {
        return studentRepository.findById(id)
                .map(this::convertToDto);
    }

    // Get Students by Group
    public List<StudentDto> getStudentsByGroup(String group) {
        return studentRepository.findByGroup(group).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Update Student
    public Optional<StudentDto> updateStudent(Long id, StudentDto studentDto, MultipartFile avatarFile)
            throws IOException {
        Optional<Student> existingOpt = studentRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }

        Student existing = existingOpt.get();
        existing.setName(studentDto.getName());
        existing.setGroup(studentDto.getGroup());
        if (avatarFile != null && !avatarFile.isEmpty()) {
            fileStorageUtil.deleteFile(existing.getAvatar());
            String filename = fileStorageUtil.storeFile(avatarFile);
            existing.setAvatar(filename);
        }

        Student updated = studentRepository.save(existing);
        return Optional.of(convertToDto(updated));
    }

    // Delete Student
    public boolean deleteStudent(Long id) {
        return studentRepository.findById(id)
                .map(student -> {
                    try {
                        fileStorageUtil.deleteFile(student.getAvatar());
                    } catch (IOException e) {
                        // log but continue with deletion
                    }
                    studentRepository.deleteById(id);
                    return true;
                })
                .orElse(false);
    }
}
