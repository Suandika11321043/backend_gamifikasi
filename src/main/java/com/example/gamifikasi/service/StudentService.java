package com.example.gamifikasi.service;

import com.example.gamifikasi.dto.StudentDto;
import com.example.gamifikasi.entity.Student;
import com.example.gamifikasi.repository.StudentAnswerRepository;
import com.example.gamifikasi.repository.StudentScoreRepository;
import com.example.gamifikasi.repository.StudentRepository;
import com.example.gamifikasi.repository.QuestionTimerSessionRepository;
import com.example.gamifikasi.util.FileStorageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private StudentTopicScoreService studentTopicScoreService;

    @Autowired
    private QuestionTimerSessionRepository questionTimerSessionRepository;

    private StudentDto convertToDto(Student student) {
        StudentDto dto = new StudentDto();
        dto.setId(student.getId());
        dto.setName(student.getName());
        dto.setGroup(student.getGroup());
        dto.setAvatar(student.getAvatar());

        // Total earned score: jumlah skor tertinggi per topik
        dto.setTotalEarnedScore(studentAnswerRepository.sumMaxEarnedScorePerTopicByStudentId(student.getId()));

        // Total bintang = jumlah bintang per hari (semua topik)
        int totalStars = studentTopicScoreService.sumTopicStarsForStudent(student);
        dto.setTotalStars(totalStars);

        return dto;
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nama siswa wajib diisi.");
        }
        return name.trim();
    }

    private String normalizeGroup(String group) {
        if (group == null || group.isBlank()) {
            throw new IllegalArgumentException("Grup siswa wajib diisi.");
        }
        return group.trim();
    }

    // Create Student
    @Transactional
    public StudentDto createStudent(StudentDto studentDto, MultipartFile avatarFile) throws IOException {
        Student student = new Student();
        student.setName(normalizeName(studentDto.getName()));
        student.setGroup(normalizeGroup(studentDto.getGroup()));

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
    @Transactional
    public Optional<StudentDto> updateStudent(Long id, StudentDto studentDto, MultipartFile avatarFile)
            throws IOException {
        Optional<Student> existingOpt = studentRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }

        Student existing = existingOpt.get();
        existing.setName(normalizeName(studentDto.getName()));
        existing.setGroup(normalizeGroup(studentDto.getGroup()));
        if (avatarFile != null && !avatarFile.isEmpty()) {
            fileStorageUtil.deleteFile(existing.getAvatar());
            String filename = fileStorageUtil.storeFile(avatarFile);
            existing.setAvatar(filename);
        }

        Student updated = studentRepository.save(existing);
        return Optional.of(convertToDto(updated));
    }

    // Delete Student beserta jawaban, skor, dan data terkait
    @Transactional
    public boolean deleteStudent(Long id) {
        return studentRepository.findById(id)
                .map(student -> {
                    Long studentId = student.getId();
                    questionTimerSessionRepository.deleteByStudentId(studentId);
                    studentAnswerRepository.deleteByStudentId(studentId);
                        studentScoreRepository.deleteByStudent(student);
                    try {
                        fileStorageUtil.deleteFile(student.getAvatar());
                    } catch (IOException e) {
                        // log but continue with deletion
                    }
                    studentRepository.delete(student);
                    return true;
                })
                .orElse(false);
    }
}
