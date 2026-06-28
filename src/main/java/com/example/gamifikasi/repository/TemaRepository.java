package com.example.gamifikasi.repository;

import com.example.gamifikasi.entity.Tema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemaRepository extends JpaRepository<Tema, Long> {
    Optional<Tema> findByNameTopic(String nameTopic);
}
