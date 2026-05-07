package com.example.gamifikasi.repository;

import com.example.gamifikasi.entity.Student;
import com.example.gamifikasi.entity.StudentRank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRankRepository extends JpaRepository<StudentRank, Integer> {
    Optional<StudentRank> findByStudent(Student student);
}
