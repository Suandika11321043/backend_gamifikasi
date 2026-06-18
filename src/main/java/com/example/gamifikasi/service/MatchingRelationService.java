package com.example.gamifikasi.service;

import com.example.gamifikasi.dto.MatchingRelationDto;
import com.example.gamifikasi.entity.MatchingRelation;
import com.example.gamifikasi.entity.QuestionOptions;
import com.example.gamifikasi.entity.Questions;
import com.example.gamifikasi.repository.MatchingRelationRepository;
import com.example.gamifikasi.repository.QuestionOptionsRepository;
import com.example.gamifikasi.repository.QuestionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MatchingRelationService {

    @Autowired
    private MatchingRelationRepository matchingRelationRepository;

    @Autowired
    private QuestionsRepository questionsRepository;

    @Autowired
    private QuestionOptionsRepository questionOptionsRepository;

    private MatchingRelationDto convertToDto(MatchingRelation rel) {
        return new MatchingRelationDto(
                rel.getId(),
                rel.getQuestions() != null ? rel.getQuestions().getId() : null,
                rel.getOpsiPertanyaan() != null ? rel.getOpsiPertanyaan().getId() : null,
                rel.getOpsiJawaban() != null ? rel.getOpsiJawaban().getId() : null
        );
    }

    // Create
    public MatchingRelationDto createRelation(Long questionId, Long opsiPertanyaanId, Long opsiJawabanId) {
        Questions question = questionsRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Soal tidak ditemukan: " + questionId));

        validateExactPair(question, opsiPertanyaanId, opsiJawabanId, null);

        MatchingRelation rel = new MatchingRelation();
        rel.setQuestions(question);

        QuestionOptions opsiP = questionOptionsRepository.findById(opsiPertanyaanId).orElse(null);
        rel.setOpsiPertanyaan(opsiP);

        QuestionOptions opsiJ = questionOptionsRepository.findById(opsiJawabanId).orElse(null);
        rel.setOpsiJawaban(opsiJ);

        return convertToDto(matchingRelationRepository.save(rel));
    }

    // Get All
    public List<MatchingRelationDto> getAllRelations() {
        return matchingRelationRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Get by ID
    public Optional<MatchingRelationDto> getRelationById(Long id) {
        return matchingRelationRepository.findById(id).map(this::convertToDto);
    }

    // Get by Question ID
    public List<MatchingRelationDto> getRelationsByQuestionId(Long questionId) {
        return questionsRepository.findById(questionId)
                .map(q -> matchingRelationRepository.findByQuestions(q).stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    // Update
    public Optional<MatchingRelationDto> updateRelation(Long id, Long questionId, Long opsiPertanyaanId, Long opsiJawabanId) {
        Optional<MatchingRelation> existingOpt = matchingRelationRepository.findById(id);
        if (existingOpt.isEmpty()) return Optional.empty();

        Questions question = questionsRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Soal tidak ditemukan: " + questionId));

        validateExactPair(question, opsiPertanyaanId, opsiJawabanId, id);

        MatchingRelation rel = existingOpt.get();
        rel.setQuestions(question);

        QuestionOptions opsiP = questionOptionsRepository.findById(opsiPertanyaanId).orElse(null);
        rel.setOpsiPertanyaan(opsiP);

        QuestionOptions opsiJ = questionOptionsRepository.findById(opsiJawabanId).orElse(null);
        rel.setOpsiJawaban(opsiJ);

        return Optional.of(convertToDto(matchingRelationRepository.save(rel)));
    }

    // Delete
    public boolean deleteRelation(Long id) {
        if (matchingRelationRepository.existsById(id)) {
            matchingRelationRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /** Hanya tolak jika kombinasi kiri+kanan persis sama sudah ada. */
    private void validateExactPair(Questions question, Long opsiPertanyaanId, Long opsiJawabanId, Long excludeId) {
        for (MatchingRelation r : matchingRelationRepository.findByQuestions(question)) {
            if (excludeId != null && excludeId.equals(r.getId())) continue;

            Long leftId = r.getOpsiPertanyaan() != null ? r.getOpsiPertanyaan().getId() : null;
            Long rightId = r.getOpsiJawaban() != null ? r.getOpsiJawaban().getId() : null;

            if (leftId != null && rightId != null
                    && leftId.equals(opsiPertanyaanId) && rightId.equals(opsiJawabanId)) {
                throw new IllegalArgumentException("Pasangan ini sudah ada.");
            }
        }
    }
}
