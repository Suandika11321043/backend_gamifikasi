package com.example.gamifikasi.controller;

import com.example.gamifikasi.dto.*;
import com.example.gamifikasi.service.JigsawPuzzleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller untuk soal tipe Jigsaw Puzzle.
 *
 * Admin Endpoints:
 * POST /api/jigsaw/puzzles → buat puzzle baru
 * GET /api/jigsaw/puzzles → daftar semua puzzle
 * GET /api/jigsaw/puzzles/{puzzleId} → detail puzzle (dengan correctPosition)
 * PUT /api/jigsaw/puzzles/{puzzleId} → update puzzle
 * DELETE /api/jigsaw/puzzles/{puzzleId} → hapus puzzle
 * POST /api/jigsaw/puzzles/{puzzleId}/pieces → tambah keping
 * PUT /api/jigsaw/pieces/{pieceId} → update keping
 * DELETE /api/jigsaw/pieces/{pieceId} → hapus keping
 * GET /api/jigsaw/puzzles/{puzzleId}/pieces → daftar keping (admin)
 *
 * Student Endpoints:
 * GET /api/jigsaw/questions/{questionId}/puzzle → ambil puzzle (keping diacak,
 * tanpa kunci)
 * POST /api/jigsaw/submit → submit jawaban siswa
 */
@RestController
@RequestMapping("/api/jigsaw")
@CrossOrigin(origins = "*")
public class JigsawPuzzleController {

    @Autowired
    private JigsawPuzzleService jigsawPuzzleService;

    // ─── Admin: Puzzle CRUD ──────────────────────────────────────────────────

    /**
     * Buat puzzle baru.
     * Kirim sebagai multipart/form-data:
     * - questionId, gridRows, gridCols (form fields)
     * - image (file, opsional) – di-upload ke Cloudinary, hasilnya jadi imageUrl
     * - imageUrl (form field, opsional) – jika tidak upload file, bisa isi URL
     * langsung
     */
    @PostMapping(value = "/puzzles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JigsawPuzzleDto> createPuzzle(
            @RequestParam("questionId") Long questionId,
            @RequestParam("gridRows") Integer gridRows,
            @RequestParam("gridCols") Integer gridCols,
            @RequestPart(value = "image", required = false) MultipartFile imageFile,
            @RequestParam(value = "imageUrl", required = false) String imageUrl) {
        try {
            JigsawPuzzleDto dto = new JigsawPuzzleDto();
            dto.setQuestionId(questionId);
            dto.setGridRows(gridRows);
            dto.setGridCols(gridCols);
            dto.setImageUrl(imageUrl);
            return new ResponseEntity<>(jigsawPuzzleService.createPuzzle(dto, imageFile), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/puzzles")
    public ResponseEntity<List<JigsawPuzzleDto>> getAllPuzzles() {
        try {
            List<JigsawPuzzleDto> list = jigsawPuzzleService.getAllPuzzles();
            if (list.isEmpty())
                return ResponseEntity.noContent().build();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/puzzles/{puzzleId}")
    public ResponseEntity<JigsawPuzzleDto> getPuzzleById(@PathVariable Long puzzleId) {
        try {
            return jigsawPuzzleService.getPuzzleById(puzzleId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update puzzle.
     * Kirim sebagai multipart/form-data:
     * - gridRows, gridCols (form fields, opsional)
     * - image (file, opsional) – ganti gambar puzzle
     * - imageUrl (form field, opsional) – ganti URL langsung tanpa upload
     */
    @PutMapping(value = "/puzzles/{puzzleId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JigsawPuzzleDto> updatePuzzle(
            @PathVariable Long puzzleId,
            @RequestParam(value = "gridRows", required = false) Integer gridRows,
            @RequestParam(value = "gridCols", required = false) Integer gridCols,
            @RequestPart(value = "image", required = false) MultipartFile imageFile,
            @RequestParam(value = "imageUrl", required = false) String imageUrl) {
        try {
            JigsawPuzzleDto dto = new JigsawPuzzleDto();
            dto.setGridRows(gridRows);
            dto.setGridCols(gridCols);
            dto.setImageUrl(imageUrl);
            return ResponseEntity.ok(jigsawPuzzleService.updatePuzzle(puzzleId, dto, imageFile));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/puzzles/{puzzleId}")
    public ResponseEntity<Void> deletePuzzle(@PathVariable Long puzzleId) {
        try {
            jigsawPuzzleService.deletePuzzle(puzzleId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ─── Admin: Piece CRUD ───────────────────────────────────────────────────

    /**
     * Tambah keping ke puzzle.
     * Kirim sebagai multipart/form-data:
     * - pieceIndex, correctPosition (form fields)
     * - image (file, opsional) – gambar keping di-upload ke Cloudinary
     * - pieceImageUrl (form field, opsional) – alternatif URL langsung
     */
    @PostMapping(value = "/puzzles/{puzzleId}/pieces", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JigsawPieceDto> addPiece(
            @PathVariable Long puzzleId,
            @RequestParam("pieceIndex") Integer pieceIndex,
            @RequestParam("correctPosition") Integer correctPosition,
            @RequestPart(value = "image", required = false) MultipartFile imageFile,
            @RequestParam(value = "pieceImageUrl", required = false) String pieceImageUrl) {
        try {
            JigsawPieceDto dto = new JigsawPieceDto();
            dto.setPieceIndex(pieceIndex);
            dto.setCorrectPosition(correctPosition);
            dto.setPieceImageUrl(pieceImageUrl);
            return new ResponseEntity<>(jigsawPuzzleService.addPiece(puzzleId, dto, imageFile), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/puzzles/{puzzleId}/pieces")
    public ResponseEntity<List<JigsawPieceDto>> getPiecesByPuzzle(@PathVariable Long puzzleId) {
        try {
            List<JigsawPieceDto> list = jigsawPuzzleService.getPiecesByPuzzleId(puzzleId);
            if (list.isEmpty())
                return ResponseEntity.noContent().build();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update keping.
     * Kirim sebagai multipart/form-data:
     * - pieceIndex, correctPosition (form fields, opsional)
     * - image (file, opsional) – ganti gambar keping
     * - pieceImageUrl (form field, opsional) – ganti URL langsung
     */
    @PutMapping(value = "/pieces/{pieceId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JigsawPieceDto> updatePiece(
            @PathVariable Long pieceId,
            @RequestParam(value = "pieceIndex", required = false) Integer pieceIndex,
            @RequestParam(value = "correctPosition", required = false) Integer correctPosition,
            @RequestPart(value = "image", required = false) MultipartFile imageFile,
            @RequestParam(value = "pieceImageUrl", required = false) String pieceImageUrl) {
        try {
            JigsawPieceDto dto = new JigsawPieceDto();
            dto.setPieceIndex(pieceIndex);
            dto.setCorrectPosition(correctPosition);
            dto.setPieceImageUrl(pieceImageUrl);
            return ResponseEntity.ok(jigsawPuzzleService.updatePiece(pieceId, dto, imageFile));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/pieces/{pieceId}")
    public ResponseEntity<Void> deletePiece(@PathVariable Long pieceId) {
        try {
            jigsawPuzzleService.deletePiece(pieceId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ─── Student: Ambil puzzle & submit jawaban ──────────────────────────────

    /**
     * Ambil puzzle untuk dikerjakan siswa.
     * Keping dikembalikan dalam urutan acak, correctPosition disembunyikan.
     */
    @GetMapping("/questions/{questionId}/puzzle")
    public ResponseEntity<JigsawPuzzleDto> getPuzzleForStudent(@PathVariable Long questionId) {
        try {
            return jigsawPuzzleService.getPuzzleByQuestionIdForStudent(questionId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Cek progress puzzle siswa secara real-time.
     * Dipanggil setiap kali siswa menggerakkan/meletakkan keping di frontend.
     * Tidak menyimpan jawaban – hanya mengembalikan persentase keping yang sudah
     * benar.
     *
     * Body: { "questionId": 1, "placements": [{"pieceId": 1, "placedPosition": 0},
     * ...] }
     */
    @PostMapping("/progress")
    public ResponseEntity<JigsawProgressResponse> checkProgress(@RequestBody JigsawProgressRequest request) {
        try {
            return ResponseEntity.ok(jigsawPuzzleService.checkProgress(request));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Siswa mengumpulkan jawaban Jigsaw Puzzle.
     * Mengembalikan hasil penilaian per keping dan skor yang didapat.
     */
    @PostMapping("/submit")
    public ResponseEntity<JigsawAnswerResponse> submitAnswer(@RequestBody JigsawAnswerRequest request) {
        try {
            return ResponseEntity.ok(jigsawPuzzleService.submitAnswer(request));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
