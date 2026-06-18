package com.example.gamifikasi.controller;

import com.example.gamifikasi.dto.DashboardStatsDto;
import com.example.gamifikasi.dto.StudentLeaderboardDto;
import com.example.gamifikasi.dto.TopicScoreGroupDto;
import com.example.gamifikasi.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * Statistik ringkas dashboard: total soal, siswa, dan tema.
     * GET /api/dashboard/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    /**
     * Daftar siswa diurutkan berdasarkan total poin (tertinggi dulu).
     * GET /api/dashboard/students/by-total-points
     */
    @GetMapping("/students/by-total-points")
    public ResponseEntity<List<StudentLeaderboardDto>> getStudentsByTotalPoints() {
        return ResponseEntity.ok(dashboardService.getStudentsByTotalPoints());
    }

    /**
     * Daftar siswa berdasarkan poin per tema/topik.
     * GET /api/dashboard/students/by-topic
     * GET /api/dashboard/students/by-topic?topicId=1
     */
    @GetMapping("/students/by-topic")
    public ResponseEntity<List<TopicScoreGroupDto>> getStudentsByTopic(
            @RequestParam(required = false) Long topicId) {
        return ResponseEntity.ok(dashboardService.getStudentsByTopic(topicId));
    }
}
