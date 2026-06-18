package com.example.gamifikasi.service;

import com.example.gamifikasi.dto.*;
import com.example.gamifikasi.entity.*;
import com.example.gamifikasi.repository.*;
import com.example.gamifikasi.util.FileStorageUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

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

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
        String imageUrl = puzzle.getImageUrl();
        if (imageUrl == null || imageUrl.isBlank()) {
            imageUrl = q.getContentImage();
        }
        dto.setImageUrl(imageUrl);
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
     * Potong gambar menjadi keping puzzle berbentuk nyata (dengan tab & blank di setiap sisi).
     * Setiap keping di-upload ke Cloudinary dan disimpan sebagai JigsawPiece.
     *
     * Skema posisi (row-major, 0-based): posisi = row * gridCols + col.
     * correctPosition == pieceIndex; pengacakan dilakukan di toPuzzleDto.
     */
    private void generatePiecesFromImage(JigsawPuzzle puzzle, byte[] imageBytes) throws IOException {
        BufferedImage fullImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (fullImage == null) {
            throw new IllegalArgumentException(
                    "Format gambar tidak didukung. Gunakan JPG, PNG, atau GIF.");
        }

        int gridRows    = puzzle.getGridRows();
        int gridCols    = puzzle.getGridCols();
        int pieceWidth  = fullImage.getWidth()  / gridCols;
        int pieceHeight = fullImage.getHeight() / gridRows;
        // Tab size: 25% of piece dimension (capped at minimum 4px)
        int tabW = Math.max(4, pieceWidth  / 4);
        int tabH = Math.max(4, pieceHeight / 4);

        // hEdge[r][c] = true → tab protrudes DOWN between rows r and r+1
        // vEdge[r][c] = true → tab protrudes RIGHT between cols c and c+1
        Random rng        = new Random();
        boolean[][] hEdge = new boolean[Math.max(1, gridRows - 1)][gridCols];
        boolean[][] vEdge = new boolean[gridRows][Math.max(1, gridCols - 1)];
        for (int r = 0; r < gridRows - 1; r++)
            for (int c = 0; c < gridCols; c++)
                hEdge[r][c] = rng.nextBoolean();
        for (int r = 0; r < gridRows; r++)
            for (int c = 0; c < gridCols - 1; c++)
                vEdge[r][c] = rng.nextBoolean();

        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                int position = row * gridCols + col;

                boolean topBorder    = (row == 0);
                boolean bottomBorder = (row == gridRows - 1);
                boolean leftBorder   = (col == 0);
                boolean rightBorder  = (col == gridCols - 1);

                // tab = protrudes OUTWARD from this piece
                boolean topTab    = !topBorder    && !hEdge[row - 1][col];
                boolean bottomTab = !bottomBorder &&  hEdge[row][col];
                boolean leftTab   = !leftBorder   && !vEdge[row][col - 1];
                boolean rightTab  = !rightBorder  &&  vEdge[row][col];

                // Canvas is larger than the base piece to accommodate tabs
                int canvasW = pieceWidth  + 2 * tabW;
                int canvasH = pieceHeight + 2 * tabH;

                BufferedImage canvas = new BufferedImage(canvasW, canvasH, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = canvas.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY);

                Path2D.Double path = createPiecePath(
                        tabW, tabH, pieceWidth, pieceHeight, tabW, tabH,
                        topBorder, topTab,
                        rightBorder, rightTab,
                        bottomBorder, bottomTab,
                        leftBorder, leftTab);

                // Draw source image clipped to the puzzle piece shape
                g.setClip(path);
                g.drawImage(fullImage, tabW - col * pieceWidth, tabH - row * pieceHeight, null);

                // Subtle dark outline for visual definition
                g.setClip(null);
                g.setStroke(new BasicStroke(1.5f));
                g.setColor(new Color(0, 0, 0, 80));
                g.draw(path);

                g.dispose();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(canvas, "png", baos);
                String pieceUrl = fileStorageUtil.storeBytes(baos.toByteArray(), "jigsaw/pieces");

                JigsawPiece piece = new JigsawPiece();
                piece.setPuzzle(puzzle);
                piece.setPieceIndex(position);
                piece.setCorrectPosition(position);
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

        // Simpan ke StudentAnswer dengan full placements agar bisa di-review
        String answerJson;
        try {
            Map<String, Object> answerMap = new LinkedHashMap<>();
            answerMap.put("type", "JIGSAW");
            answerMap.put("correctPieces", correctCount);
            answerMap.put("totalPieces", totalPieces);
            // simpan placements: [{pieceId, placedPosition}, ...]
            List<Map<String, Object>> placementList = new ArrayList<>();
            for (JigsawPlacementDto p : request.getPlacements()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("pieceId", p.getPieceId());
                m.put("placedPosition", p.getPlacedPosition());
                placementList.add(m);
            }
            answerMap.put("placements", placementList);
            answerJson = OBJECT_MAPPER.writeValueAsString(answerMap);
        } catch (Exception ex) {
            answerJson = String.format(
                    "{\"type\":\"JIGSAW\",\"correctPieces\":%d,\"totalPieces\":%d}",
                    correctCount, totalPieces);
        }

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

    // ─── Review: bandingkan jawaban siswa vs jawaban benar ───────────────────

    /**
     * Ambil review puzzle siswa – membandingkan posisi keping yang diletakkan
     * siswa dengan correctPosition yang sebenarnya.
     * GET /api/jigsaw/students/{studentId}/questions/{questionId}/review
     */
    public JigsawReviewDto getJigsawReview(Long studentId, Long questionId) {
        if (!studentRepository.existsById(studentId))
            throw new IllegalArgumentException("Student tidak ditemukan: " + studentId);

        Questions question = questionsRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Question tidak ditemukan: " + questionId));

        JigsawPuzzle puzzle = jigsawPuzzleRepository.findByQuestion(question)
                .orElseThrow(() -> new IllegalStateException(
                        "Puzzle belum dikonfigurasi untuk question: " + questionId));

        List<JigsawPiece> pieces = jigsawPieceRepository.findByPuzzle(puzzle);

        // Ambil jawaban tersimpan siswa
        Map<Long, Integer> studentPlacements = new HashMap<>();
        Boolean storedCorrect = null;
        Integer storedEarnedScore = null;

        Optional<StudentAnswer> saOpt =
                studentAnswerRepository.findTopByStudentIdAndQuestionsIdOrderByIdDesc(studentId, questionId);
        if (saOpt.isPresent()) {
            StudentAnswer sa = saOpt.get();
            storedCorrect = sa.getIsCorrect();
            storedEarnedScore = sa.getEarnedScore();
            try {
                // Parse placements dari JSON yang disimpan
                Map<String, Object> parsed = OBJECT_MAPPER.readValue(
                        sa.getStudentAnswer(), new TypeReference<Map<String, Object>>() {});
                Object placementsRaw = parsed.get("placements");
                if (placementsRaw instanceof List) {
                    for (Object entry : (List<?>) placementsRaw) {
                        if (entry instanceof Map) {
                            Map<?, ?> m = (Map<?, ?>) entry;
                            Long pieceId = ((Number) m.get("pieceId")).longValue();
                            Integer placedPos = ((Number) m.get("placedPosition")).intValue();
                            studentPlacements.put(pieceId, placedPos);
                        }
                    }
                }
            } catch (Exception ignored) {
                // Jawaban lama (format ringkas) – placements tidak tersedia
            }
        }

        int correctCount = 0;
        List<JigsawReviewDto.PieceComparisonDto> comparisons = new ArrayList<>();
        for (JigsawPiece piece : pieces) {
            Integer placed = studentPlacements.get(piece.getId());
            boolean isCorrect = placed != null && placed.equals(piece.getCorrectPosition());
            if (isCorrect) correctCount++;
            comparisons.add(new JigsawReviewDto.PieceComparisonDto(
                    piece.getId(),
                    piece.getPieceIndex(),
                    piece.getPieceImageUrl(),
                    piece.getCorrectPosition(),
                    placed,
                    isCorrect));
        }

        // Urutkan berdasarkan correctPosition agar mudah dibaca
        comparisons.sort(Comparator.comparingInt(c -> c.getCorrectPosition()));

        boolean allCorrect = storedCorrect != null ? storedCorrect
                : (correctCount == pieces.size() && !pieces.isEmpty());
        int earnedScore = storedEarnedScore != null ? storedEarnedScore : 0;

        String imageUrl = puzzle.getImageUrl();
        if (imageUrl == null || imageUrl.isBlank()) {
            imageUrl = question.getContentImage();
        }

        return new JigsawReviewDto(
                puzzle.getId(),
                questionId,
                imageUrl,
                puzzle.getGridRows(),
                puzzle.getGridCols(),
                question.getContentInstruction(),
                question.getScorePoint(),
                allCorrect,
                correctCount,
                pieces.size(),
                earnedScore,
                comparisons);
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

    // ─── Puzzle Piece Shape Helpers ──────────────────────────────────────────

    /**
     * Build the puzzle piece outline path in canvas coordinates.
     * Base rect: (x0, y0) → (x0+pw, y0+ph). Tab areas extend beyond this rect.
     * For border edges the path is a straight line; interior edges get a bezier tab or blank.
     */
    private Path2D.Double createPiecePath(
            int x0, int y0, int pw, int ph, int tabW, int tabH,
            boolean topBorder,    boolean topTab,
            boolean rightBorder,  boolean rightTab,
            boolean bottomBorder, boolean bottomTab,
            boolean leftBorder,   boolean leftTab) {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(x0, y0);
        // Top edge (left → right)
        if (topBorder)    path.lineTo(x0 + pw, y0);
        else              addHEdge(path, x0, y0, pw, topTab ? -tabH : tabH);
        // Right edge (top → bottom)
        if (rightBorder)  path.lineTo(x0 + pw, y0 + ph);
        else              addVEdge(path, x0 + pw, y0, ph, rightTab ? tabW : -tabW);
        // Bottom edge (right → left)
        if (bottomBorder) path.lineTo(x0, y0 + ph);
        else              addHEdge(path, x0 + pw, y0 + ph, -pw, bottomTab ? tabH : -tabH);
        // Left edge (bottom → top)
        if (leftBorder)   path.lineTo(x0, y0);
        else              addVEdge(path, x0, y0 + ph, -ph, leftTab ? -tabW : tabW);
        path.closePath();
        return path;
    }

    /**
     * Append a horizontal puzzle edge to the path.
     * Travels from (px, py) by `length` pixels in x (negative = right-to-left).
     * tabHeight > 0 → bump goes downward; tabHeight < 0 → bump goes upward.
     */
    private void addHEdge(Path2D.Double path,
            double px, double py, double length, double tabHeight) {
        double s        = Math.signum(length);
        double p30      = px + length * 0.30;
        double p70      = px + length * 0.70;
        double mid      = px + length * 0.50;
        double baseHalf = Math.abs(length) * 0.10 * s; // half-width of tab base
        path.lineTo(p30, py);
        path.curveTo(p30,            py + tabHeight * 0.5,
                     mid - baseHalf, py + tabHeight,
                     mid,            py + tabHeight);
        path.curveTo(mid + baseHalf, py + tabHeight,
                     p70,            py + tabHeight * 0.5,
                     p70,            py);
        path.lineTo(px + length, py);
    }

    /**
     * Append a vertical puzzle edge to the path.
     * Travels from (px, py) by `length` pixels in y (negative = bottom-to-top).
     * tabWidth > 0 → bump goes rightward; tabWidth < 0 → bump goes leftward.
     */
    private void addVEdge(Path2D.Double path,
            double px, double py, double length, double tabWidth) {
        double s        = Math.signum(length);
        double p30      = py + length * 0.30;
        double p70      = py + length * 0.70;
        double mid      = py + length * 0.50;
        double baseHalf = Math.abs(length) * 0.10 * s;
        path.lineTo(px, p30);
        path.curveTo(px + tabWidth * 0.5, p30,
                     px + tabWidth,       mid - baseHalf,
                     px + tabWidth,       mid);
        path.curveTo(px + tabWidth,       mid + baseHalf,
                     px + tabWidth * 0.5, p70,
                     px,                 p70);
        path.lineTo(px, py + length);
    }
}
