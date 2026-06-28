package com.example.gamifikasi.config;

import com.example.gamifikasi.entity.Questions;
import com.example.gamifikasi.repository.QuestionsRepository;
import com.example.gamifikasi.repository.TemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Migrasi sekali jalan: salin is_available dari tema_learning_date ke Questions, lalu hapus tabel lama.
 */
@Component
public class TemaLearningDateMigrator implements ApplicationRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private QuestionsRepository questionsRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!tableExists("tema_learning_date")) {
            return;
        }

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT topic_id, learning_date, is_available FROM tema_learning_date");

        for (Map<String, Object> row : rows) {
            Long topicId = ((Number) row.get("topic_id")).longValue();
            LocalDate learningDate = ((java.sql.Date) row.get("learning_date")).toLocalDate();
            boolean available = toBoolean(row.get("is_available"));

            temaRepository.findById(topicId).ifPresent(topic -> {
                List<Questions> questions = questionsRepository.findByTopicAndLearningDate(topic, learningDate);
                for (Questions q : questions) {
                    q.setIsAvailable(available);
                }
                questionsRepository.saveAll(questions);
            });
        }

        jdbcTemplate.execute("DROP TABLE tema_learning_date");
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables "
                        + "WHERE table_schema = DATABASE() AND table_name = ?",
                Integer.class,
                tableName);
        return count != null && count > 0;
    }

    private boolean toBoolean(Object value) {
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof Number n) {
            return n.intValue() != 0;
        }
        if (value instanceof byte[] bytes && bytes.length > 0) {
            return bytes[0] != 0;
        }
        return false;
    }
}
