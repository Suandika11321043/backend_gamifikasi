package com.example.gamifikasi.repository;

import com.example.gamifikasi.entity.Topic;
import com.example.gamifikasi.entity.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    Optional<Topic> findByNameTopic(String nameTopic);
    List<Topic> findByLevelId(Level levelId);
}
