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
        MatchingRelation rel = new MatchingRelation();

        Questions question = questionsRepository.findById(questionId).orElse(null);
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

        MatchingRelation rel = existingOpt.get();
        Questions question = questionsRepository.findById(questionId).orElse(null);
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
}
