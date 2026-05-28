package com.example.gamifikasi.service;

import com.example.gamifikasi.dto.*;
import com.example.gamifikasi.entity.*;
import com.example.gamifikasi.repository.*;
import com.example.gamifikasi.util.FileStorageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JigsawPuzzleService {

    @Autowired
    private JigsawPuzzleRepository jigsawPuzzleRepository;

    @Autowired
    private JigsawPieceRepository jigsawPieceRepository;

    @Autowired
    private QuestionsRepository questionsRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentAnswerRepository studentAnswerRepository;

    @Autowired
    private FileStorageUtil fileStorageUtil;

    // ─── Konversi entity → DTO (admin: correctPosition disertakan) ──────────

    private JigsawPieceDto toPieceDto(JigsawPiece piece) {
        return new JigsawPieceDto(
                piece.getId(),
                piece.getPuzzle().getId(),
                piece.getPieceIndex(),
                piece.getCorrectPosition(),
                piece.getPieceImageUrl());
    }

    /** Konversi ke DTO siswa: correctPosition disembunyikan (null). */
    private JigsawPieceDto toPieceDtoForStudent(JigsawPiece piece) {
        return new JigsawPieceDto(
                piece.getId(),
                piece.getPuzzle().getId(),
                piece.getPieceIndex(),
                null,
                piece.getPieceImageUrl());
    }

    private JigsawPuzzleDto toPuzzleDto(JigsawPuzzle puzzle, boolean hideAnswer) {
        List<JigsawPieceDto> pieceDtos = puzzle.getPieces().stream()
                .map(p -> hideAnswer ? toPieceDtoForStudent(p) : toPieceDto(p))
                .collect(Collectors.toList());

        if (hideAnswer) {
            // Acak urutan keping untuk siswa
            Collections.shuffle(pieceDtos);
        }

        Questions q = puzzle.getQuestion();

        JigsawPuzzleDto dto = new JigsawPuzzleDto();
        dto.setId(puzzle.getId());
        dto.setQuestionId(q.getId());
        dto.setImageUrl(puzzle.getImageUrl());
        dto.setGridRows(puzzle.getGridRows());
        dto.setGridCols(puzzle.getGridCols());
        dto.setPieces(pieceDtos);
        // Detail soal — selalu disertakan agar frontend bisa tampilkan instruksi
        dto.setContentInstruction(q.getContentInstruction());
        dto.setContentImage(q.getContentImage());
        dto.setContentAudio(q.getContentAudio());
        dto.setTimeLimitMinutes(q.getTimeLimitMinutes());
        dto.setScorePoint(q.getScorePoint());
        return dto;
    }

    // ─── CRUD Puzzle ─────────────────────────────────────────────────────────

    /**
     * Buat puzzle baru beserta keping-kepingnya (jika disertakan).
     * questionId harus menunjuk ke soal bertipe JIGSAW.
     * imageFile (opsional): jika dikirim, di-upload ke Cloudinary dan hasilnya
     * menggantikan dto.imageUrl.
     */
    @Transactional
    public JigsawPuzzleDto createPuzzle(JigsawPuzzleDto dto, MultipartFile imageFile) throws IOException {
        Questions question = questionsRepository.findById(dto.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Question tidak ditemukan: " + dto.getQuestionId()));

        if (jigsawPuzzleRepository.existsByQuestionId(dto.getQuestionId())) {
            throw new IllegalStateException(
                    "Puzzle untuk question ini sudah ada. Gunakan endpoint update.");
        }

        String imageUrl = (imageFile != null && !imageFile.isEmpty())
                ? fileStorageUtil.storeFile(imageFile, "jigsaw")
                : dto.getImageUrl();

        JigsawPuzzle puzzle = new JigsawPuzzle();
        puzzle.setQuestion(question);
        puzzle.setImageUrl(imageUrl);
        puzzle.setGridRows(dto.getGridRows());
        puzzle.setGridCols(dto.getGridCols());

        JigsawPuzzle saved = jigsawPuzzleRepository.save(puzzle);

        if (imageFile != null && !imageFile.isEmpty()) {
            // Auto-crop gambar menjadi gridRows × gridCols keping dan upload tiap keping
            generatePiecesFromImage(saved, imageFile.getBytes());
        } else if (dto.getPieces() != null) {
            // Fallback: gunakan keping manual jika ada
            for (JigsawPieceDto pieceDto : dto.getPieces()) {
                JigsawPiece piece = new JigsawPiece();
                piece.setPuzzle(saved);
                piece.setPieceIndex(pieceDto.getPieceIndex());
                piece.setCorrectPosition(pieceDto.getCorrectPosition());
                piece.setPieceImageUrl(pieceDto.getPieceImageUrl());
                jigsawPieceRepository.save(piece);
            }
        }

        saved = jigsawPuzzleRepository.findById(saved.getId()).orElse(saved);
        return toPuzzleDto(saved, false);
    }

    /**
     * Crop gambar menjadi gridRows × gridCols keping, upload tiap keping ke
     * Cloudinary,
     * lalu simpan sebagai JigsawPiece.
     *
     * Skema posisi (row-major, 0-based):
     * posisi = row * gridCols + col
     * contoh grid 3×3: 0=kiri-atas, 1=tengah-atas, 2=kanan-atas, ..., 8=kanan-bawah
     *
     * correctPosition == pieceIndex karena setiap keping dipotong tepat dari
     * posisinya.
     * Pengacakan hanya terjadi saat keping ditampilkan ke siswa (di toPuzzleDto).
     */
    private void generatePiecesFromImage(JigsawPuzzle puzzle, byte[] imageBytes) throws IOException {
        BufferedImage fullImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (fullImage == null) {
            throw new IllegalArgumentException(
                    "Format gambar tidak didukung. Gunakan JPG, PNG, atau GIF.");
        }

        int gridRows = puzzle.getGridRows();
        int gridCols = puzzle.getGridCols();
        int pieceWidth = fullImage.getWidth() / gridCols;
        int pieceHeight = fullImage.getHeight() / gridRows;

        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                int position = row * gridCols + col;

                BufferedImage pieceImg = fullImage.getSubimage(
                        col * pieceWidth,
                        row * pieceHeight,
                        pieceWidth,
                        pieceHeight);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(pieceImg, "png", baos);

                String pieceUrl = fileStorageUtil.storeBytes(baos.toByteArray(), "jigsaw/pieces");

                JigsawPiece piece = new JigsawPiece();
                piece.setPuzzle(puzzle);
                piece.setPieceIndex(position); // label keping
                piece.setCorrectPosition(position); // posisi benar = label (karena dipotong urut)
                piece.setPieceImageUrl(pieceUrl);
                jigsawPieceRepository.save(piece);
            }
        }
    }

    /** Update konfigurasi puzzle (imageUrl, gridRows, gridCols). */
    @Transactional
    public JigsawPuzzleDto updatePuzzle(Long puzzleId, JigsawPuzzleDto dto, MultipartFile imageFile)
            throws IOException {
        JigsawPuzzle puzzle = jigsawPuzzleRepository.findById(puzzleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Puzzle tidak ditemukan: " + puzzleId));

        if (imageFile != null && !imageFile.isEmpty()) {
            puzzle.setImageUrl(fileStorageUtil.storeFile(imageFile, "jigsaw"));
        } else if (dto.getImageUrl() != null) {
            puzzle.setImageUrl(dto.getImageUrl());
        }
        if (dto.getGridRows() != null)
            puzzle.setGridRows(dto.getGridRows());
        if (dto.getGridCols() != null)
            puzzle.setGridCols(dto.getGridCols());

        return toPuzzleDto(jigsawPuzzleRepository.save(puzzle), false);
    }

    /** Hapus puzzle beserta semua kepingnya. */
    @Transactional
    public void deletePuzzle(Long puzzleId) {
        jigsawPieceRepository.deleteByPuzzleId(puzzleId);
        jigsawPuzzleRepository.deleteById(puzzleId);
    }

    /** Ambil puzzle (admin view, correctPosition terlihat). */
    public Optional<JigsawPuzzleDto> getPuzzleById(Long puzzleId) {
        return jigsawPuzzleRepository.findById(puzzleId)
                .map(p -> toPuzzleDto(p, false));
    }

    /** Ambil semua puzzle (admin). */
    public List<JigsawPuzzleDto> getAllPuzzles() {
        return jigsawPuzzleRepository.findAll().stream()
                .map(p -> toPuzzleDto(p, false))
                .collect(Collectors.toList());
    }

    /**
     * Ambil puzzle berdasarkan questionId untuk ditampilkan ke siswa.
     * Keping diacak, correctPosition disembunyikan.
     */
    public Optional<JigsawPuzzleDto> getPuzzleByQuestionIdForStudent(Long questionId) {
        return jigsawPuzzleRepository.findByQuestionId(questionId)
                .map(p -> toPuzzleDto(p, true));
    }

    // ─── CRUD Keping ─────────────────────────────────────────────────────────

    /**
     * Tambah satu keping ke puzzle. imageFile (opsional): gambar keping di-upload
     * ke Cloudinary.
     */
    @Transactional
    public JigsawPieceDto addPiece(Long puzzleId, JigsawPieceDto dto, MultipartFile imageFile) throws IOException {
        JigsawPuzzle puzzle = jigsawPuzzleRepository.findById(puzzleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Puzzle tidak ditemukan: " + puzzleId));

        String pieceImageUrl = (imageFile != null && !imageFile.isEmpty())
                ? fileStorageUtil.storeFile(imageFile, "jigsaw/pieces")
                : dto.getPieceImageUrl();

        JigsawPiece piece = new JigsawPiece();
        piece.setPuzzle(puzzle);
        piece.setPieceIndex(dto.getPieceIndex());
        piece.setCorrectPosition(dto.getCorrectPosition());
        piece.setPieceImageUrl(pieceImageUrl);

        return toPieceDto(jigsawPieceRepository.save(piece));
    }

    /** Update data satu keping. imageFile (opsional): ganti gambar keping. */
    @Transactional
    public JigsawPieceDto updatePiece(Long pieceId, JigsawPieceDto dto, MultipartFile imageFile) throws IOException {
        JigsawPiece piece = jigsawPieceRepository.findById(pieceId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Piece tidak ditemukan: " + pieceId));

        if (dto.getPieceIndex() != null)
            piece.setPieceIndex(dto.getPieceIndex());
        if (dto.getCorrectPosition() != null)
            piece.setCorrectPosition(dto.getCorrectPosition());
        if (imageFile != null && !imageFile.isEmpty()) {
            piece.setPieceImageUrl(fileStorageUtil.storeFile(imageFile, "jigsaw/pieces"));
        } else if (dto.getPieceImageUrl() != null) {
            piece.setPieceImageUrl(dto.getPieceImageUrl());
        }

        return toPieceDto(jigsawPieceRepository.save(piece));
    }

    /** Hapus satu keping. */
    @Transactional
    public void deletePiece(Long pieceId) {
        jigsawPieceRepository.deleteById(pieceId);
    }

    /** Ambil semua keping dari sebuah puzzle (admin view). */
    public List<JigsawPieceDto> getPiecesByPuzzleId(Long puzzleId) {
        return jigsawPieceRepository.findByPuzzleId(puzzleId).stream()
                .map(this::toPieceDto)
                .collect(Collectors.toList());
    }

    // ─── Submit Jawaban Siswa ─────────────────────────────────────────────────

    /**
     * Nilai jawaban siswa untuk satu soal Jigsaw Puzzle.
     *
     * Penilaian proporsional:
     * earnedScore = round(correctPieces / totalPieces * question.scorePoint)
     *
     * Hasilnya disimpan ke StudentAnswer agar terintegrasi dengan sistem
     * scoring & ranking yang sudah ada (QuizService#finishQuiz).
     */
    @Transactional
    public JigsawAnswerResponse submitAnswer(JigsawAnswerRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Student tidak ditemukan: " + request.getStudentId()));

        Questions question = questionsRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Question tidak ditemukan: " + request.getQuestionId()));

        JigsawPuzzle puzzle = jigsawPuzzleRepository.findByQuestion(question)
                .orElseThrow(() -> new IllegalStateException(
                        "Puzzle belum dikonfigurasi untuk question: " + request.getQuestionId()));

        List<JigsawPiece> pieces = jigsawPieceRepository.findByPuzzle(puzzle);
        Map<Long, JigsawPiece> pieceMap = pieces.stream()
                .collect(Collectors.toMap(JigsawPiece::getId, p -> p));

        int totalPieces = pieces.size();
        int correctCount = 0;
        List<JigsawPieceResultDto> results = new ArrayList<>();

        for (JigsawPlacementDto placement : request.getPlacements()) {
            JigsawPiece piece = pieceMap.get(placement.getPieceId());
            if (piece == null)
                continue;

            boolean pieceCorrect = piece.getCorrectPosition().equals(placement.getPlacedPosition());
            if (pieceCorrect)
                correctCount++;

            results.add(new JigsawPieceResultDto(
                    piece.getId(),
                    piece.getPieceIndex(),
                    placement.getPlacedPosition(),
                    piece.getCorrectPosition(),
                    pieceCorrect));
        }

        int foundCount = results.size(); // berapa pieceId yang benar-benar ditemukan
        boolean allCorrect = (correctCount == totalPieces && totalPieces > 0);
        int scorePoint = question.getScorePoint() != null ? question.getScorePoint() : 0;
        int earnedScore = totalPieces > 0
                ? (int) Math.round((double) correctCount / totalPieces * scorePoint)
                : 0;

        // Simpan ke StudentAnswer agar terintegrasi dengan finishQuiz
        // Format JSON agar lolos CHECK (JSON_VALID(student_answer)) di MySQL
        String answerJson = String.format(
                "{\"type\":\"JIGSAW\",\"correctPieces\":%d,\"totalPieces\":%d}",
                correctCount, totalPieces);

        StudentAnswer answer = new StudentAnswer();
        answer.setStudent(student);
        answer.setQuestions(question);
        answer.setStudentAnswer(answerJson);
        answer.setIsCorrect(allCorrect);
        answer.setEarnedScore(earnedScore);
        studentAnswerRepository.save(answer);

        return new JigsawAnswerResponse(allCorrect, earnedScore, correctCount,
                totalPieces, foundCount, puzzle.getId(), results);
    }

    // ─── Cek Progress Siswa (tanpa simpan) ───────────────────────────────────

    /**
     * Hitung berapa persen keping puzzle yang sudah diletakkan dengan benar
     * oleh siswa. Dipanggil setiap kali siswa menggerakkan keping di frontend.
     * Tidak menyimpan apapun ke database.
     *
     * correctPosition pada setiap pieceResult disembunyikan (null) agar
     * kunci jawaban tidak bocor ke klien.
     */
    public JigsawProgressResponse checkProgress(JigsawProgressRequest request) {
        Questions question = questionsRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Question tidak ditemukan: " + request.getQuestionId()));

        JigsawPuzzle puzzle = jigsawPuzzleRepository.findByQuestion(question)
                .orElseThrow(() -> new IllegalStateException(
                        "Puzzle belum dikonfigurasi untuk question: " + request.getQuestionId()));

        List<JigsawPiece> pieces = jigsawPieceRepository.findByPuzzle(puzzle);
        Map<Long, JigsawPiece> pieceMap = pieces.stream()
                .collect(Collectors.toMap(JigsawPiece::getId, p -> p));

        int totalPieces = pieces.size();
        int correctCount = 0;
        List<JigsawPieceResultDto> results = new ArrayList<>();

        for (JigsawPlacementDto placement : request.getPlacements()) {
            JigsawPiece piece = pieceMap.get(placement.getPieceId());
            if (piece == null)
                continue;

            boolean pieceCorrect = piece.getCorrectPosition().equals(placement.getPlacedPosition());
            if (pieceCorrect)
                correctCount++;

            // correctPosition sengaja null – tidak bocorkan kunci ke frontend
            results.add(new JigsawPieceResultDto(
                    piece.getId(),
                    piece.getPieceIndex(),
                    placement.getPlacedPosition(),
                    null,
                    pieceCorrect));
        }

        double progressPercent = totalPieces > 0
                ? Math.round((double) correctCount / totalPieces * 10000.0) / 100.0
                : 0.0;

        return new JigsawProgressResponse(correctCount, totalPieces, progressPercent, results);
    }
}
