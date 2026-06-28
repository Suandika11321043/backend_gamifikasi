package com.example.gamifikasi.service;

import com.example.gamifikasi.dto.*;
import com.example.gamifikasi.entity.Student;
import com.example.gamifikasi.entity.Tema;
import com.example.gamifikasi.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private QuestionsRepository questionsRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private StudentAnswerRepository studentAnswerRepository;

    @Autowired
    private StudentTopicScoreService studentTopicScoreService;

    public DashboardStatsDto getStats() {
        DashboardStatsDto stats = new DashboardStatsDto();
        stats.setTotalSoal(questionsRepository.count());
        stats.setTotalStudents(studentRepository.count());
        stats.setTotalTopics(temaRepository.count());
        return stats;
    }

    public List<StudentLeaderboardDto> getStudentsByTotalPoints() {
        List<Student> students = studentRepository.findAll();

        List<StudentLeaderboardDto> leaderboard = students.stream()
                .map(this::toLeaderboardDto)
                .sorted(Comparator
                        .comparing(StudentLeaderboardDto::getTotalEarnedScore, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .reversed()
                        .thenComparing(StudentLeaderboardDto::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .collect(Collectors.toList());

        for (int i = 0; i < leaderboard.size(); i++) {
            leaderboard.get(i).setRank(i + 1);
        }

        return leaderboard;
    }

    public List<TopicScoreGroupDto> getStudentsByTopic(Long topicId) {
        if (topicId != null) {
            Tema topic = temaRepository.findById(topicId)
                    .orElseThrow(() -> new RuntimeException("Tema tidak ditemukan: " + topicId));
            TopicScoreGroupDto group = buildTopicScoreGroup(topic);
            return List.of(group);
        }

        return temaRepository.findAll().stream()
                .map(this::buildTopicScoreGroup)
                .collect(Collectors.toList());
    }

    private TopicScoreGroupDto buildTopicScoreGroup(Tema topic) {
        TopicScoreGroupDto group = new TopicScoreGroupDto();
        group.setTopicId(topic.getId());
        group.setTopicName(topic.getNameTopic());

        List<TopicStudentScoreDto> studentScores = studentRepository.findAll().stream()
                .map(student -> toTopicStudentScoreDto(student, topic))
                .sorted(Comparator
                        .comparing(TopicStudentScoreDto::getTotalEarnedScore, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .reversed()
                        .thenComparing(TopicStudentScoreDto::getStudentName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .collect(Collectors.toList());

        for (int i = 0; i < studentScores.size(); i++) {
            studentScores.get(i).setRank(i + 1);
        }

        group.setStudents(studentScores);
        return group;
    }

    private TopicStudentScoreDto toTopicStudentScoreDto(Student student, Tema topic) {
        StudentScoreDto score = studentTopicScoreService.buildScore(student, topic.getId());
        TopicStudentScoreDto dto = new TopicStudentScoreDto();
        dto.setStudentId(student.getId());
        dto.setStudentName(student.getName());
        dto.setStudentGroup(student.getGroup());
        dto.setAvatar(student.getAvatar());
        dto.setTotalEarnedScore(score.getTotalEarnedScore() != null ? score.getTotalEarnedScore() : 0);
        dto.setStarCount(score.getStarCount() != null ? score.getStarCount() : 0);
        return dto;
    }

    private StudentLeaderboardDto toLeaderboardDto(Student student) {
        StudentLeaderboardDto dto = new StudentLeaderboardDto();
        dto.setId(student.getId());
        dto.setName(student.getName());
        dto.setGroup(student.getGroup());
        dto.setAvatar(student.getAvatar());
        dto.setTotalEarnedScore(studentAnswerRepository.sumMaxEarnedScorePerTopicByStudentId(student.getId()));

        int totalStars = studentTopicScoreService.sumTopicStarsForStudent(student);
        dto.setTotalStars(totalStars);

        return dto;
    }
}
