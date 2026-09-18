package com.yohannes.chess;

import com.yohannes.chess.Pieces.Piece;

/**
 *
 * updated by JOHANNES KANKO ON 17/09/2026
 */

public class Position {
    private Piece piece;


    Position(Piece piece ) {
        this.piece = piece;
    }

    public Piece getPiece() {
        return piece;

    }

    void setPiece(Piece piece) {
        this.piece = piece;
    }

}
