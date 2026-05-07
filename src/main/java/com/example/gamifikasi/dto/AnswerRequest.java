package com.example.gamifikasi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Satu jawaban dari siswa untuk satu soal.
 *
 * Isi field bergantung pada tipe soal:
 *  - MULTIPLE_CHOICE : isi selectedOptionIds dengan ID opsi yang dipilih
 *  - ORDERING        : isi orderedOptionIds dengan ID opsi sesuai urutan jawaban siswa
 *  - MATCHING        : isi matchingPairs dengan Map (id_pertanyaan -> id_jawaban)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnswerRequest {

    private Long questionId;

    /** Untuk MULTIPLE_CHOICE: ID opsi yang dipilih siswa (bisa lebih dari satu). */
    private List<Long> selectedOptionIds;

    /** Untuk ORDERING: ID opsi diurutkan sesuai jawaban siswa (indeks 0 = urutan 1). */
    private List<Long> orderedOptionIds;

    /**
     * Untuk MATCHING/DRAG_AND_DROP: pasangan (id_opsi_pertanyaan -> list id_opsi_jawaban) pilihan siswa.
     * Key   = ID opsi bertipe PERTANYAAN
     * Value = List ID opsi bertipe JAWABAN (bisa lebih dari satu)
     */
    private Map<Long, List<Long>> matchingPairs;
}
