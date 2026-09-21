package com.yohannes.chess;

import com.yohannes.chess.Pieces.Bishop;
import com.yohannes.chess.Pieces.King;
import com.yohannes.chess.Pieces.Knight;
import com.yohannes.chess.Pieces.Pawn;
import com.yohannes.chess.Pieces.Piece;
import com.yohannes.chess.Pieces.Queen;
import com.yohannes.chess.Pieces.Rook;
import java.util.ArrayList;

public class IntermediateBotEngine {

    private final int MAX_DEPTH = 2; // Looks 2 full plies ahead

    // Positional matrices: Encourages pieces to control the center
    private final int[][] knightTable = {
            {-50,-40,-30,-30,-30,-30,-40,-50},
            {-40,-20,  0,  5,  5,  0,-20,-40},
            {-30,  5, 10, 15, 15, 10,  5,-30},
            {-30,  0, 15, 20, 20, 15,  0,-30},
            {-30,  5, 15, 20, 20, 15,  5,-30},
            {-30,  0, 10, 15, 15, 10,  0,-30},
            {-40,-20,  0,  0,  0,  0,-20,-40},
            {-50,-40,-30,-30,-30,-30,-40,-50}
    };

    private final int[][] pawnTable = {
            { 0,  0,  0,  0,  0,  0,  0,  0},
            { 5, 10, 10,-20,-20, 10, 10,  5},
            { 5, -5,-10,  0,  0,-10, -5,  5},
            { 0,  0,  0, 20, 20,  0,  0,  0},
            { 5,  5, 10, 25, 25, 10,  5,  5},
            {10, 10, 20, 30, 30, 20, 10, 10},
            {50, 50, 50, 50, 50, 50, 50, 50},
            { 0,  0,  0,  0,  0,  0,  0,  0}
    };

    public ChessBotEngine.MoveCandidate calculateMove(Position[][] board) {
        int bestValue = Integer.MIN_VALUE;
        ChessBotEngine.MoveCandidate bestMove = null;

        ArrayList<ChessBotEngine.MoveCandidate> legalMoves = generateAllLegalMoves(board, false);

        for (ChessBotEngine.MoveCandidate move : legalMoves) {
            // Clone board state to simulate move
            Position[][] tempBoard = cloneBoard(board);
            tempBoard[move.to.getX()][move.to.getY()].setPiece(tempBoard[move.from.getX()][move.from.getY()].getPiece());
            tempBoard[move.from.getX()][move.from.getY()].setPiece(null);

            // Minimax evaluation step (it's white's turn next in simulation, so we minimize)
            int boardValue = minimax(tempBoard, MAX_DEPTH - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, true);

            if (boardValue > bestValue) {
                bestValue = boardValue;
                bestMove = move;
            }
        }
        return bestMove;
    }

    private int minimax(Position[][] board, int depth, int alpha, int beta, boolean isMaximizingPlayer) {
        if (depth == 0) {
            return evaluateBoard(board);
        }

        ArrayList<ChessBotEngine.MoveCandidate> legalMoves = generateAllLegalMoves(board, !isMaximizingPlayer);
        if (legalMoves.isEmpty()) return isMaximizingPlayer ? -9999 : 9999;

        if (isMaximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (ChessBotEngine.MoveCandidate move : legalMoves) {
                Position[][] tempBoard = cloneBoard(board);
                tempBoard[move.to.getX()][move.to.getY()].setPiece(tempBoard[move.from.getX()][move.from.getY()].getPiece());
                tempBoard[move.from.getX()][move.from.getY()].setPiece(null);

                int eval = minimax(tempBoard, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break; // Alpha-Beta pruning cutoff
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (ChessBotEngine.MoveCandidate move : legalMoves) {
                Position[][] tempBoard = cloneBoard(board);
                tempBoard[move.to.getX()][move.to.getY()].setPiece(tempBoard[move.from.getX()][move.from.getY()].getPiece());
                tempBoard[move.from.getX()][move.from.getY()].setPiece(null);

                int eval = minimax(tempBoard, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

    private int evaluateBoard(Position[][] board) {
        int totalScore = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece p = board[i][j].getPiece();
                if (p != null) {
                    int pieceValue = 0;
                    int positionalBonus = 0;

                    if (p instanceof Pawn) { pieceValue = 100; positionalBonus = pawnTable[i][j]; }
                    else if (p instanceof Knight) { pieceValue = 320; positionalBonus = knightTable[i][j]; }
                    else if (p instanceof Bishop) { pieceValue = 330; }
                    else if (p instanceof Rook) { pieceValue = 500; }
                    else if (p instanceof Queen) { pieceValue = 900; }
                    else if (p instanceof King) { pieceValue = 20000; }

                    // Black (AI) wants to maximize score, White (Human) subtracts from it
                    if (!p.isWhite()) {
                        totalScore += (pieceValue + positionalBonus);
                    } else {
                        totalScore -= (pieceValue + positionalBonus);
                    }
                }
            }
        }
        return totalScore;
    }

    private ArrayList<ChessBotEngine.MoveCandidate> generateAllLegalMoves(Position[][] board, boolean isWhite) {
        ArrayList<ChessBotEngine.MoveCandidate> moves = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece p = board[i][j].getPiece();
                if (p != null && p.isWhite() == isWhite) {
                    Coordinates from = new Coordinates(i, j);
                    ArrayList<Coordinates> allowed = p.AllowedMoves(from, board);
                    for (Coordinates to : allowed) {
                        moves.add(new ChessBotEngine.MoveCandidate(from, to));
                    }
                }
            }
        }
        return moves;
    }

    private Position[][] cloneBoard(Position[][] source) {
        Position[][] target = new Position[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                target[i][j] = new Position(source[i][j].getPiece());
            }
        }
        return target;
    }
}
