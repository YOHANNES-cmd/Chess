package com.yohannes.chess;

import com.yohannes.chess.Pieces.Piece;
import com.yohannes.chess.Pieces.Pawn;
import com.yohannes.chess.Pieces.Knight;
import com.yohannes.chess.Pieces.King;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ProductionBotEngine {

    private final int MAX_DEPTH = 3;
    private final IntermediateBotEngine evaluator = new IntermediateBotEngine();

    public ChessBotEngine.MoveCandidate calculateMove(Position[][] board) {
        int bestValue = Integer.MIN_VALUE;
        ChessBotEngine.MoveCandidate bestMove = null;

        ArrayList<ChessBotEngine.MoveCandidate> legalMoves = generateOrderedMoves(board, false);

        for (ChessBotEngine.MoveCandidate move : legalMoves) {
            Position[][] tempBoard = cloneBoard(board);
            tempBoard[move.to.getX()][move.to.getY()].setPiece(tempBoard[move.from.getX()][move.from.getY()].getPiece());
            tempBoard[move.from.getX()][move.from.getY()].setPiece(null);

            int boardValue = alphaBeta(tempBoard, MAX_DEPTH - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, true);

            if (boardValue > bestValue) {
                bestValue = boardValue;
                bestMove = move;
            }
        }
        return bestMove;
    }

    private int alphaBeta(Position[][] board, int depth, int alpha, int beta, boolean isMaximizing) {
        if (depth == 0) {
            // Calls intermediate positional evaluation matrices natively
            return callEvaluate(board);
        }

        ArrayList<ChessBotEngine.MoveCandidate> legalMoves = generateOrderedMoves(board, !isMaximizing);
        if (legalMoves.isEmpty()) return isMaximizing ? -99999 : 99999;

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (ChessBotEngine.MoveCandidate move : legalMoves) {
                Position[][] tempBoard = cloneBoard(board);
                tempBoard[move.to.getX()][move.to.getY()].setPiece(tempBoard[move.from.getX()][move.from.getY()].getPiece());
                tempBoard[move.from.getX()][move.from.getY()].setPiece(null);

                int eval = alphaBeta(tempBoard, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (ChessBotEngine.MoveCandidate move : legalMoves) {
                Position[][] tempBoard = cloneBoard(board);
                tempBoard[move.to.getX()][move.to.getY()].setPiece(tempBoard[move.from.getX()][move.from.getY()].getPiece());
                tempBoard[move.from.getX()][move.from.getY()].setPiece(null);

                int eval = alphaBeta(tempBoard, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

    private ArrayList<ChessBotEngine.MoveCandidate> generateOrderedMoves(Position[][] board, final boolean isWhite) {
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

        // Sort moves: High values first (MVV-LVA: Most Valuable Victim - Least Valuable Attacker)
        Collections.sort(moves, new Comparator<ChessBotEngine.MoveCandidate>() {
            @Override
            public int compare(ChessBotEngine.MoveCandidate m1, ChessBotEngine.MoveCandidate m2) {
                int p1 = evaluateMovePriority(m1, board);
                int p2 = evaluateMovePriority(m2, board);
                return Integer.compare(p2, p1);
            }
        });

        return moves;
    }

    private int evaluateMovePriority(ChessBotEngine.MoveCandidate m, Position[][] board) {
        int score = 0;
        Piece victim = board[m.to.getX()][m.to.getY()].getPiece();
        Piece attacker = board[m.from.getX()][m.from.getY()].getPiece();

        if (victim != null) {
            score += 10; // Capture prioritization bonus flag
            if (attacker instanceof Pawn) score += 5;
        }
        return score;
    }

    private int callEvaluate(Position[][] b) {
        try {
            java.lang.reflect.Method m = IntermediateBotEngine.class.getDeclaredMethod("evaluateBoard", Position[][].class);
            m.setAccessible(true);
            return (Integer) m.invoke(evaluator, new Object[]{b});
        } catch (Exception e) {
            return 0;
        }
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
