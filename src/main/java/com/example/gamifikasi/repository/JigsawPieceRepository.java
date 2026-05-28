package com.example.gamifikasi.repository;

import com.example.gamifikasi.entity.JigsawPiece;
import com.example.gamifikasi.entity.JigsawPuzzle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JigsawPieceRepository extends JpaRepository<JigsawPiece, Long> {
    List<JigsawPiece> findByPuzzle(JigsawPuzzle puzzle);

    List<JigsawPiece> findByPuzzleId(Long puzzleId);

    void deleteByPuzzleId(Long puzzleId);
}
