package com.example.gamifikasi.service;

import com.example.gamifikasi.dto.StudentScoreDto;
import com.example.gamifikasi.entity.Student;
import com.example.gamifikasi.entity.Tema;
import com.example.gamifikasi.repository.StudentAnswerRepository;
import com.example.gamifikasi.repository.StudentScoreRepository;
import com.example.gamifikasi.repository.StudentRepository;
import com.example.gamifikasi.repository.TemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class StudentTopicScoreService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private StudentAnswerRepository studentAnswerRepository;

    @Autowired
    private StudentScoreRepository studentScoreRepository;

    public List<StudentScoreDto> getScoresByStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Siswa tidak ditemukan: " + studentId));

        Set<Long> topicIds = new LinkedHashSet<>();
        studentScoreRepository.findByStudent(student).forEach(score -> {
            if (score.getTopic() != null) {
                topicIds.add(score.getTopic().getId());
            }
        });
        topicIds.addAll(studentAnswerRepository.findDistinctTopicIdsByStudentId(studentId));

        List<StudentScoreDto> scores = new ArrayList<>();
        for (Long topicId : topicIds) {
            scores.add(buildScore(student, topicId));
        }
        return scores;
    }

    public StudentScoreDto getScoreByStudentAndTopic(Long studentId, Long topicId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Siswa tidak ditemukan: " + studentId));
        if (!temaRepository.existsById(topicId)) {
            throw new RuntimeException("Topik tidak ditemukan: " + topicId);
        }
        return buildScore(student, topicId);
    }

    public StudentScoreDto buildScore(Student student, Long topicId) {
        Tema topic = temaRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topik tidak ditemukan: " + topicId));

        int correctCount = studentAnswerRepository
                .countLatestCorrectByStudentIdAndTopicId(student.getId(), topicId);
        int earnedScore = studentAnswerRepository
                .sumLatestEarnedScoreByStudentIdAndTopicId(student.getId(), topicId);
        int dayStars = studentScoreRepository.sumStarsByStudentAndTopic(student, topic);
        int starCount = Math.max(correctCount, dayStars);

        StudentScoreDto score = new StudentScoreDto();
        score.setStudent(student);
        score.setTopic(topic);
        score.setCorrectCount(correctCount);
        score.setStarCount(starCount);
        score.setTotalEarnedScore(earnedScore);
        return score;
    }

    public int sumTopicStarsForStudent(Student student) {
        int totalStars = studentScoreRepository.sumStarsByStudent(student);
        if (totalStars > 0) {
            return totalStars;
        }
        return studentAnswerRepository.countLatestCorrectByStudentId(student.getId());
    }
}
