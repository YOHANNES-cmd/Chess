package com.yohannes.chess;

import com.yohannes.chess.Pieces.Piece;
import java.util.ArrayList;
import java.util.Random;

public class ChessBotEngine {

    private Random random = new Random();

    // Struct to store a valid move candidate
    public static class MoveCandidate {
        public Coordinates from;
        public Coordinates to;

        public MoveCandidate(Coordinates from, Coordinates to) {
            this.from = from;
            this.to = to;
        }
    }

    /**
     * Scans the local grid matrix and selects a legal move locally without an AI API
     */
    public MoveCandidate calculateCounterMove(Position[][] currentBoard) {
        ArrayList<MoveCandidate> allLegalMoves = new ArrayList<>();

        // 1. Scan the entire 8x8 matrix board grid looking for Black pieces
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = currentBoard[i][j].getPiece();

                // Find pieces belonging to the Computer player (Black / false)
                if (piece != null && !piece.isWhite()) {
                    Coordinates currentPos = new Coordinates(i, j);

                    // Generate all allowed rule moves for this piece
                    ArrayList<Coordinates> allowedDestinations = piece.AllowedMoves(currentPos, currentBoard);

                    // Compile destination pairs
                    for (Coordinates dest : allowedDestinations) {
                        allLegalMoves.add(new MoveCandidate(currentPos, dest));
                    }
                }
            }
        }

        // 2. Select the best tactical option from the compiled list
        if (allLegalMoves.isEmpty()) {
            return null; // Checkmate or Stalemate fallback protection
        }

        // Tactical Selection Rule Layer: Prioritize Captures (pieces sitting on the landing tile)
        ArrayList<MoveCandidate> capturingMoves = new ArrayList<>();
        for (MoveCandidate move : allLegalMoves) {
            if (currentBoard[move.to.getX()][move.to.getY()].getPiece() != null) {
                capturingMoves.add(move);
            }
        }

        // If capture moves exist, pick a random capture move; otherwise, pick a random normal legal move
        if (!capturingMoves.isEmpty()) {
            return capturingMoves.get(random.nextInt(capturingMoves.size()));
        } else {
            return allLegalMoves.get(random.nextInt(allLegalMoves.size()));
        }
    }
}
