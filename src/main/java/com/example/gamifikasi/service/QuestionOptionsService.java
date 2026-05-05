package com.example.gamifikasi.service;

import com.example.gamifikasi.dto.QuestionOptionsDto;
import com.example.gamifikasi.entity.QuestionOptions;
import com.example.gamifikasi.entity.Questions;
import com.example.gamifikasi.repository.QuestionOptionsRepository;
import com.example.gamifikasi.repository.QuestionsRepository;
import com.example.gamifikasi.util.FileStorageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuestionOptionsService {

    @Autowired
    private QuestionOptionsRepository questionOptionsRepository;

    @Autowired
    private QuestionsRepository questionsRepository;

    @Autowired
    private FileStorageUtil fileStorageUtil;

    private QuestionOptionsDto convertToDto(QuestionOptions opt) {
        return new QuestionOptionsDto(
                opt.getId(),
                opt.getQuestions() != null ? opt.getQuestions().getId() : null,
                opt.getTeksOpsi(),
                opt.getMediaOpsi(),
                opt.getKunciJawaban(),
                opt.getUrutanBenar(),
                opt.getTipeItem()
        );
    }

    // Create
    public QuestionOptionsDto createOption(Long questionId, String teksOpsi, MultipartFile mediaOpsiFile,
                                           Boolean kunciJawaban, Integer urutanBenar,
                                           QuestionOptions.TipeItem tipeItem) throws IOException {
        QuestionOptions opt = new QuestionOptions();
        if (questionId != null) {
            Questions question = questionsRepository.findById(questionId).orElse(null);
            opt.setQuestions(question);
        }
        opt.setTeksOpsi(teksOpsi);
        opt.setKunciJawaban(kunciJawaban != null ? kunciJawaban : false);
        opt.setUrutanBenar(urutanBenar);
        opt.setTipeItem(tipeItem != null ? tipeItem : QuestionOptions.TipeItem.JAWABAN);
        if (mediaOpsiFile != null && !mediaOpsiFile.isEmpty()) {
            opt.setMediaOpsi(fileStorageUtil.storeFile(mediaOpsiFile));
        }
        return convertToDto(questionOptionsRepository.save(opt));
    }

    // Get All
    public List<QuestionOptionsDto> getAllOptions() {
        return questionOptionsRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Get by ID
    public Optional<QuestionOptionsDto> getOptionById(Long id) {
        return questionOptionsRepository.findById(id).map(this::convertToDto);
    }

    // Get by Question ID
    public List<QuestionOptionsDto> getOptionsByQuestionId(Long questionId) {
        return questionsRepository.findById(questionId)
                .map(q -> questionOptionsRepository.findByQuestions(q).stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    // Update
    public Optional<QuestionOptionsDto> updateOption(Long id, Long questionId, String teksOpsi,
                                                      MultipartFile mediaOpsiFile, Boolean kunciJawaban,
                                                      Integer urutanBenar, QuestionOptions.TipeItem tipeItem) throws IOException {
        Optional<QuestionOptions> existingOpt = questionOptionsRepository.findById(id);
        if (existingOpt.isEmpty()) return Optional.empty();

        QuestionOptions opt = existingOpt.get();
        if (questionId != null) {
            Questions question = questionsRepository.findById(questionId).orElse(null);
            opt.setQuestions(question);
        }
        opt.setTeksOpsi(teksOpsi);
        opt.setKunciJawaban(kunciJawaban != null ? kunciJawaban : false);
        opt.setUrutanBenar(urutanBenar);
        opt.setTipeItem(tipeItem != null ? tipeItem : QuestionOptions.TipeItem.JAWABAN);
        if (mediaOpsiFile != null && !mediaOpsiFile.isEmpty()) {
            fileStorageUtil.deleteFile(opt.getMediaOpsi());
            opt.setMediaOpsi(fileStorageUtil.storeFile(mediaOpsiFile));
        }
        return Optional.of(convertToDto(questionOptionsRepository.save(opt)));
    }

    // Delete
    public boolean deleteOption(Long id) {
        return questionOptionsRepository.findById(id)
                .map(opt -> {
                    try {
                        fileStorageUtil.deleteFile(opt.getMediaOpsi());
                    } catch (IOException e) {
                        // continue deletion
                    }
                    questionOptionsRepository.deleteById(id);
                    return true;
                })
                .orElse(false);
    }
}

