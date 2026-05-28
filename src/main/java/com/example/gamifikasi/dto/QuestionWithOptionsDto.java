package com.example.gamifikasi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Soal beserta opsi-opsinya yang dikirim ke siswa.
 * Kunci jawaban (kunciJawaban, urutanBenar) TIDAK disertakan agar tidak bocor
 * ke client.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionWithOptionsDto {

    private Long questionId;
    private String questionType;
    private String contentInstruction;
    private String contentImage;
    private String contentAudio;
    private Integer timeLimitMinutes;

    /** Poin yang diperoleh jika menjawab soal ini dengan benar. */
    private Integer scorePoint;

    /** Semua opsi soal ini (tanpa kunci jawaban). */
    private List<OptionDto> options;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionDto {
        private Long optionId;
        private String teksOpsi;
        private String mediaOpsi;
        /** PERTANYAAN atau JAWABAN – dibutuhkan oleh tipe MATCHING di frontend. */
        private String tipeItem;
    }
}
