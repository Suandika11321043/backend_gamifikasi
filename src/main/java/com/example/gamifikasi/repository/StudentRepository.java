package com.example.gamifikasi.repository;

import com.example.gamifikasi.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByName(String name);
    List<Student> findByGroup(String group);
    List<Student> findByLevel(String level);
}
