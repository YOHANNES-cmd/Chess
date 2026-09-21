package com.yohannes.chess;

import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yohannes.chess.Pieces.Bishop;
import com.yohannes.chess.Pieces.King;
import com.yohannes.chess.Pieces.Knight;
import com.yohannes.chess.Pieces.Pawn;
import com.yohannes.chess.Pieces.Piece;
import com.yohannes.chess.Pieces.Queen;
import com.yohannes.chess.Pieces.Rook;

import java.util.ArrayList;

public class SinglePlayerModewithBotActivity12 extends AppCompatActivity implements View.OnClickListener {

    public TextView aiThinkingOverlay;
    // Core state flags
    private int activeDifficultyLevel = 0; // Default to Beginner
    private IntermediateBotEngine intermediateBot = new IntermediateBotEngine();
    private ProductionBotEngine productionBot = new ProductionBotEngine();

    public Boolean FirstPlayerTurn; // True = White (Human), False = Black (AI)
    public Boolean isAiThinking = false; // Intercepts clicks during computer processing

    // Board structural systems
    public ArrayList<Coordinates> listOfCoordinates = new ArrayList<>();
    public Position[][] Board = new Position[8][8];
    public Position[][] Board2 = new Position[8][8];
    public Boolean AnythingSelected = false;
    public Coordinates lastPos = null;
    public Coordinates clickedPosition = new Coordinates(0, 0);

    // XML Layout Bindings
    public TextView game_over;
    public TextView[][] DisplayBoard = new TextView[8][8];
    public TextView[][] DisplayBoardBackground = new TextView[8][8];
    public ArrayList<Position[][]> LastMoves = new ArrayList<>();
    public LinearLayout pawn_choices;
    public int numberOfMoves;

    // Unified move coordinate indicators tracking both players
    private Coordinates lastMovedFromSquare = null;
    private Coordinates lastMovedToSquare = null;

    Piece bKing, wKing, bQueen, wQueen;
    Piece bKnight1, bKnight2, wKnight1, wKnight2;
    Piece bRook1, bRook2, wRook1, wRook2;
    Piece bBishop1, bBishop2, wBishop1, wBishop2;
    Piece bPawn1, bPawn2, bPawn3, bPawn4, bPawn5, bPawn6, bPawn7, bPawn8;
    Piece wPawn1, wPawn2, wPawn3, wPawn4, wPawn5, wPawn6, wPawn7, wPawn8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }
        setContentView(R.layout.activity_main);

        aiThinkingOverlay = (TextView) findViewById(R.id.ai_thinking_overlay);

        initializeBoard();

        game_over = (TextView) findViewById(R.id.game_over);
        pawn_choices = (LinearLayout) findViewById(R.id.pawn_chioces);

        game_over.setVisibility(View.INVISIBLE);
        pawn_choices.setVisibility(View.INVISIBLE);

        setupClickListeners();
        activeDifficultyLevel = getIntent().getIntExtra("SELECTED_BOT_LEVEL", 0);
    }

    private void setupClickListeners() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                DisplayBoard[i][j].setOnClickListener(this);
            }
        }
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == R.id.R00) {
            clickedPosition = new Coordinates(0, 0);
        } else if (id == R.id.R10) {
            clickedPosition.setX(1);
            clickedPosition.setY(0);
        } else if (id == R.id.R20) {
            clickedPosition.setX(2);
            clickedPosition.setY(0);
        } else if (id == R.id.R30) {
            clickedPosition.setX(3);
            clickedPosition.setY(0);
        } else if (id == R.id.R40) {
            clickedPosition.setX(4);
            clickedPosition.setY(0);
        } else if (id == R.id.R50) {
            clickedPosition.setX(5);
            clickedPosition.setY(0);
        } else if (id == R.id.R60) {
            clickedPosition.setX(6);
            clickedPosition.setY(0);
        } else if (id == R.id.R70) {
            clickedPosition.setX(7);
            clickedPosition.setY(0);
        } else if (id == R.id.R01) {
            clickedPosition.setX(0);
            clickedPosition.setY(1);
        } else if (id == R.id.R11) {
            clickedPosition.setX(1);
            clickedPosition.setY(1);
        } else if (id == R.id.R21) {
            clickedPosition.setX(2);
            clickedPosition.setY(1);
        } else if (id == R.id.R31) {
            clickedPosition.setX(3);
            clickedPosition.setY(1);
        } else if (id == R.id.R41) {
            clickedPosition.setX(4);
            clickedPosition.setY(1);
        } else if (id == R.id.R51) {
            clickedPosition.setX(5);
            clickedPosition.setY(1);
        } else if (id == R.id.R61) {
            clickedPosition.setX(6);
            clickedPosition.setY(1);
        } else if (id == R.id.R71) {
            clickedPosition.setX(7);
            clickedPosition.setY(1);
        } else if (id == R.id.R02) {
            clickedPosition.setX(0);
            clickedPosition.setY(2);
        } else if (id == R.id.R12) {
            clickedPosition.setX(1);
            clickedPosition.setY(2);
        } else if (id == R.id.R22) {
            clickedPosition.setX(2);
            clickedPosition.setY(2);
        } else if (id == R.id.R32) {
            clickedPosition.setX(3);
            clickedPosition.setY(2);
        } else if (id == R.id.R42) {
            clickedPosition.setX(4);
            clickedPosition.setY(2);
        } else if (id == R.id.R52) {
            clickedPosition.setX(5);
            clickedPosition.setY(2);
        } else if (id == R.id.R62) {
            clickedPosition.setX(6);
            clickedPosition.setY(2);
        } else if (id == R.id.R72) {
            clickedPosition.setX(7);
            clickedPosition.setY(2);
        } else if (id == R.id.R03) {
            clickedPosition.setX(0);
            clickedPosition.setY(3);
        } else if (id == R.id.R13) {
            clickedPosition.setX(1);
            clickedPosition.setY(3);
        } else if (id == R.id.R23) {
            clickedPosition.setX(2);
            clickedPosition.setY(3);
        } else if (id == R.id.R33) {
            clickedPosition.setX(3);
            clickedPosition.setY(3);
        } else if (id == R.id.R43) {
            clickedPosition.setX(4);
            clickedPosition.setY(3);
        } else if (id == R.id.R53) {
            clickedPosition.setX(5);
            clickedPosition.setY(3);
        } else if (id == R.id.R63) {
            clickedPosition.setX(6);
            clickedPosition.setY(3);
        } else if (id == R.id.R73) {
            clickedPosition.setX(7);
            clickedPosition.setY(3);
        } else if (id == R.id.R04) {
            clickedPosition.setX(0);
            clickedPosition.setY(4);
        } else if (id == R.id.R14) {
            clickedPosition.setX(1);
            clickedPosition.setY(4);
        } else if (id == R.id.R24) {
            clickedPosition.setX(2);
            clickedPosition.setY(4);
        } else if (id == R.id.R34) {
            clickedPosition.setX(3);
            clickedPosition.setY(4);
        } else if (id == R.id.R44) {
            clickedPosition.setX(4);
            clickedPosition.setY(4);
        } else if (id == R.id.R54) {
            clickedPosition.setX(5);
            clickedPosition.setY(4);
        } else if (id == R.id.R64) {
            clickedPosition.setX(6);
            clickedPosition.setY(4);
        } else if (id == R.id.R74) {
            clickedPosition.setX(7);
            clickedPosition.setY(4);
        } else if (id == R.id.R05) {
            clickedPosition.setX(0);
            clickedPosition.setY(5);
        } else if (id == R.id.R15) {
            clickedPosition.setX(1);
            clickedPosition.setY(5);
        } else if (id == R.id.R25) {
            clickedPosition.setX(2);
            clickedPosition.setY(5);
        } else if (id == R.id.R35) {
            clickedPosition.setX(3);
            clickedPosition.setY(5);
        } else if (id == R.id.R45) {
            clickedPosition.setX(4);
            clickedPosition.setY(5);
        } else if (id == R.id.R55) {
            clickedPosition.setX(5);
            clickedPosition.setY(5);
        } else if (id == R.id.R65) {
            clickedPosition.setX(6);
            clickedPosition.setY(5);
        } else if (id == R.id.R75) {
            clickedPosition.setX(7);
            clickedPosition.setY(5);
        } else if (id == R.id.R06) {
            clickedPosition.setX(0);
            clickedPosition.setY(6);
        } else if (id == R.id.R16) {
            clickedPosition.setX(1);
            clickedPosition.setY(6);
        } else if (id == R.id.R26) {
            clickedPosition.setX(2);
            clickedPosition.setY(6);
        } else if (id == R.id.R36) {
            clickedPosition.setX(3);
            clickedPosition.setY(6);
        } else if (id == R.id.R46) {
            clickedPosition.setX(4);
            clickedPosition.setY(6);
        } else if (id == R.id.R56) {
            clickedPosition.setX(5);
            clickedPosition.setY(6);
        } else if (id == R.id.R66) {
            clickedPosition.setX(6);
            clickedPosition.setY(6);
        } else if (id == R.id.R76) {
            clickedPosition.setX(7);
            clickedPosition.setY(6);
        } else if (id == R.id.R07) {
            clickedPosition.setX(0);
            clickedPosition.setY(7);
        } else if (id == R.id.R17) {
            clickedPosition.setX(1);
            clickedPosition.setY(7);
        } else if (id == R.id.R27) {
            clickedPosition.setX(2);
            clickedPosition.setY(7);
        } else if (id == R.id.R37) {
            clickedPosition.setX(3);
            clickedPosition.setY(7);
        } else if (id == R.id.R47) {
            clickedPosition.setX(4);
            clickedPosition.setY(7);
        } else if (id == R.id.R57) {
            clickedPosition.setX(5);
            clickedPosition.setY(7);
        } else if (id == R.id.R67) {
            clickedPosition.setX(6);
            clickedPosition.setY(7);
        } else if (id == R.id.R77) {
            clickedPosition.setX(7);
            clickedPosition.setY(7);
        }


        if (!AnythingSelected) {
            if(Board[clickedPosition.getX()][clickedPosition.getY()].getPiece() == null) {
                isKingInDanger();
                return;
            }else{
                if(Board[clickedPosition.getX()][clickedPosition.getY()].getPiece().isWhite() != FirstPlayerTurn){
                    isKingInDanger();
                    return;
                }else{
                    listOfCoordinates.clear();
                    listOfCoordinates = Board[clickedPosition.getX()][clickedPosition.getY()].getPiece().AllowedMoves(clickedPosition, Board);
                    DisplayBoardBackground[clickedPosition.getX()][clickedPosition.getY()].setBackgroundResource(R.color.colorSelected);
                    setColorAtAllowedPosition(listOfCoordinates);
                    AnythingSelected = true;
                }
            }
        } else {
            if(Board[clickedPosition.getX()][clickedPosition.getY()].getPiece() == null){
                if(moveIsAllowed(listOfCoordinates , clickedPosition)){

                    saveBoard();
                    if(Board[clickedPosition.getX()][clickedPosition.getY()].getPiece() instanceof King){
                        if(Board[clickedPosition.getX()][clickedPosition.getY()].getPiece().isWhite() != FirstPlayerTurn){
                            game_over.setVisibility(View.VISIBLE);
                        }
                    }
                    Board[clickedPosition.getX()][clickedPosition.getY()].setPiece(Board[lastPos.getX()][lastPos.getY()].getPiece());
                    Board[lastPos.getX()][lastPos.getY()].setPiece(null);

                    isKingInDanger();
                    resetColorAtAllowedPosition(listOfCoordinates);
                    DisplayBoard[lastPos.getX()][lastPos.getY()].setBackgroundResource(0);
                    resetColorAtLastPosition(lastPos);
                    AnythingSelected = false;
                    FirstPlayerTurn = !FirstPlayerTurn;
                    checkForPawn();

                }else{
                    resetColorAtLastPosition(lastPos);
                    resetColorAtAllowedPosition(listOfCoordinates);
                    AnythingSelected = false;
                }

            }else{
                if(Board[clickedPosition.getX()][clickedPosition.getY()].getPiece() == null) {
                    isKingInDanger();
                    return;

                }else{
                    if(Board[clickedPosition.getX()][clickedPosition.getY()].getPiece() !=null){
                        if(Board[clickedPosition.getX()][clickedPosition.getY()].getPiece().isWhite() != FirstPlayerTurn){
                            if(moveIsAllowed(listOfCoordinates , clickedPosition)){

                                saveBoard();
                                if(Board[clickedPosition.getX()][clickedPosition.getY()].getPiece() instanceof King){
                                    if(Board[clickedPosition.getX()][clickedPosition.getY()].getPiece().isWhite() != FirstPlayerTurn){
                                        game_over.setVisibility(View.VISIBLE);
                                    }
                                }
                                Board[clickedPosition.getX()][clickedPosition.getY()].setPiece(Board[lastPos.getX()][lastPos.getY()].getPiece());
                                Board[lastPos.getX()][lastPos.getY()].setPiece(null);

                                resetColorAtAllowedPosition(listOfCoordinates);
                                DisplayBoard[lastPos.getX()][lastPos.getY()].setBackgroundResource(0);
                                resetColorAtLastPosition(lastPos);

                                AnythingSelected = false;
                                FirstPlayerTurn = !FirstPlayerTurn;
                                checkForPawn();
                            }else{
                                resetColorAtLastPosition(lastPos);
                                resetColorAtAllowedPosition(listOfCoordinates);
                                AnythingSelected = false;
                            }

                        }else{
                            if(Board[clickedPosition.getX()][clickedPosition.getY()].getPiece().isWhite() != FirstPlayerTurn){
                                isKingInDanger();
                                return;
                            }

                            resetColorAtLastPosition(lastPos);
                            resetColorAtAllowedPosition(listOfCoordinates);

                            listOfCoordinates.clear();
                            listOfCoordinates = Board[clickedPosition.getX()][clickedPosition.getY()].getPiece().AllowedMoves(clickedPosition, Board);
                            DisplayBoardBackground[clickedPosition.getX()][clickedPosition.getY()].setBackgroundResource(R.color.colorSelected);
                            setColorAtAllowedPosition(listOfCoordinates);
                            AnythingSelected = true;
                        }
                    }
                }
            }
        }

        isKingInDanger();
        lastPos = new Coordinates(clickedPosition.getX(), clickedPosition.getY());
        setBoard();
    }



    private void processMoveSelection() {
        Piece targetedPiece = Board[clickedPosition.getX()][clickedPosition.getY()].getPiece();

        if (targetedPiece == null || targetedPiece.isWhite() != FirstPlayerTurn) {
            if (moveIsAllowed(listOfCoordinates, clickedPosition)) {
                executeMoveOnBoard();
            } else {
                cancelCurrentSelection();
            }
        } else {
            // Instantly switch highlight focus onto the newly selected allied piece
            cancelCurrentSelection();
            listOfCoordinates.clear();
            listOfCoordinates = Board[clickedPosition.getX()][clickedPosition.getY()].getPiece().AllowedMoves(clickedPosition, Board);
            DisplayBoardBackground[clickedPosition.getX()][clickedPosition.getY()].setBackgroundResource(R.color.colorSelected);
            setColorAtAllowedPosition(listOfCoordinates);
            AnythingSelected = true;
        }
    }

    private void executeMoveOnBoard() {
        saveBoard();

        // 🌟 Capture human move coordinates for the smart indicator highlights
        lastMovedFromSquare = new Coordinates(lastPos.getX(), lastPos.getY());
        lastMovedToSquare = new Coordinates(clickedPosition.getX(), clickedPosition.getY());

        // Execute the physical move transition across coordinates
        Board[clickedPosition.getX()][clickedPosition.getY()].setPiece(Board[lastPos.getX()][lastPos.getY()].getPiece());
        Board[lastPos.getX()][lastPos.getY()].setPiece(null);

        resetColorAtAllowedPosition(listOfCoordinates);
        DisplayBoard[lastPos.getX()][lastPos.getY()].setBackgroundResource(0);
        resetColorAtLastPosition(lastPos);

        AnythingSelected = false;
        FirstPlayerTurn = !FirstPlayerTurn; // Hand focus to the bot engine
        checkForPawn();
        setBoard();

        // Check if it's now the AI's turn
        if (!FirstPlayerTurn) {
            triggerAiEngineSequence();
        }
    }

    private void cancelCurrentSelection() {
        resetColorAtLastPosition(lastPos);
        resetColorAtAllowedPosition(listOfCoordinates);
        AnythingSelected = false;
    }

    private void updateClickedPositionCoordinates(int id) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                String resName = "R" + i + j;
                int checkId = getResources().getIdentifier(resName, "id", getPackageName());
                if (id == checkId) {
                    clickedPosition.setX(i);
                    clickedPosition.setY(j);
                    return;
                }
            }
        }
    }

  /*  private void processMoveSelection() {
        Piece targetedPiece = Board[clickedPosition.getX()][clickedPosition.getY()].getPiece();

        if (targetedPiece == null || targetedPiece.isWhite() != FirstPlayerTurn) {
            if (moveIsAllowed(listOfCoordinates, clickedPosition)) {
                executeMoveOnBoard();
            } else {
                cancelCurrentSelection();
            }
        } else {
            cancelCurrentSelection();
            listOfCoordinates.clear();
            listOfCoordinates = Board[clickedPosition.getX()][clickedPosition.getY()].getPiece().AllowedMoves(clickedPosition, Board);
            DisplayBoardBackground[clickedPosition.getX()][clickedPosition.getY()].setBackgroundResource(R.color.colorSelected);
            setColorAtAllowedPosition(listOfCoordinates);
            AnythingSelected = true;
        }
    }

    private void executeMoveOnBoard() {
        saveBoard();

        // Save Human Move properties for indicator highlights
        lastMovedFromSquare = new Coordinates(lastPos.getX(), lastPos.getY());
        lastMovedToSquare = new Coordinates(clickedPosition.getX(), clickedPosition.getY());

        Board[clickedPosition.getX()][clickedPosition.getY()].setPiece(Board[lastPos.getX()][lastPos.getY()].getPiece());
        Board[lastPos.getX()][lastPos.getY()].setPiece(null);

        resetColorAtAllowedPosition(listOfCoordinates);
        DisplayBoard[lastPos.getX()][lastPos.getY()].setBackgroundResource(0);
        resetColorAtLastPosition(lastPos);

        AnythingSelected = false;
        FirstPlayerTurn = !FirstPlayerTurn;
        checkForPawn();
        setBoard();

        if (!FirstPlayerTurn) {
            triggerAiEngineSequence();
        }
    }

    private void cancelCurrentSelection() {
        resetColorAtLastPosition(lastPos);
        resetColorAtAllowedPosition(listOfCoordinates);
        AnythingSelected = false;
    }*/

    private ChessBotEngine localBotEngine = new ChessBotEngine();

    private void triggerAiEngineSequence() {
        isAiThinking = true;
        aiThinkingOverlay.setVisibility(View.VISIBLE);

        aiThinkingOverlay.postDelayed(new Runnable() {
            @Override
            public void run() {
                ChessBotEngine.MoveCandidate computedMove = null;

                if (activeDifficultyLevel == 0) {
                    computedMove = localBotEngine.calculateCounterMove(Board);
                } else if (activeDifficultyLevel == 1) {
                    computedMove = intermediateBot.calculateMove(Board);
                } else if (activeDifficultyLevel == 2) {
                    computedMove = productionBot.calculateMove(Board);
                }

                if (computedMove != null) {
                    aiThinkingOverlay.setVisibility(View.INVISIBLE);
                    char fromCol = (char) ('a' + computedMove.from.getX());
                    int fromRow = 8 - computedMove.from.getY();
                    char toCol = (char) ('a' + computedMove.to.getX());
                    int toRow = 8 - computedMove.to.getY();

                    String moveToken = "" + fromCol + fromRow + toCol + toRow;
                    executeAiMoveOnBoard(moveToken);
                } else {
                    isAiThinking = false;
                    FirstPlayerTurn = true;
                    aiThinkingOverlay.setVisibility(View.INVISIBLE);
                }
            }
        }, 400);
    }

    private void executeAiMoveOnBoard(String move) {
        int fromX = move.charAt(0) - 'a';
        int fromY = '8' - move.charAt(1);
        int toX = move.charAt(2) - 'a';
        int toY = '8' - move.charAt(3);

        // Save Bot Move properties for indicator highlights
        lastMovedFromSquare = new Coordinates(fromX, fromY);
        lastMovedToSquare = new Coordinates(toX, toY);

        lastPos = new Coordinates(fromX, fromY);
        clickedPosition = new Coordinates(toX, toY);

        saveBoard();
        Board[toX][toY].setPiece(Board[fromX][fromY].getPiece());
        Board[fromX][fromY].setPiece(null);

        DisplayBoard[fromX][fromY].setBackgroundResource(0);
        isKingInDanger();
        FirstPlayerTurn = true;
        isAiThinking = false;
        checkForPawn();
        setBoard();
    }

    private void setBoard() {
        // 1. Reset ALL background squares back to defaults first
        // 1. Reset ALL background squares back to defaults first
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if ((i + j) % 2 == 0) {
                    DisplayBoardBackground[i][j].setBackgroundResource(R.color.colorBoardDark);
                } else {
                    DisplayBoardBackground[i][j].setBackgroundResource(R.color.colorBoardLight);
                }
            }
        }

        // 2. Draw Smart Indicators for the most recent active move (Human or Bot)
        if (lastMovedFromSquare != null && lastMovedToSquare != null) {
            DisplayBoardBackground[lastMovedFromSquare.getX()][lastMovedFromSquare.getY()]
                    .setBackgroundResource(R.color.colorSelected);
            DisplayBoardBackground[lastMovedToSquare.getX()][lastMovedToSquare.getY()]
                    .setBackgroundResource(R.color.colorSelected);
        }

        // 3. Render pieces
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece p = Board[i][j].getPiece();
                int x;
                if (Board[i][j].getPiece() != null) {
                    if (p instanceof King) x = 0;
                    else if (p instanceof Queen) x = 1;
                    else if (p instanceof Rook) x = 2;
                    else if (p instanceof Bishop) x = 3;
                    else if (p instanceof Knight) x = 4;
                    else if (p instanceof Pawn) x = 5;
                    else x = 6;

                    switch (x) {
                        case 0:
                            DisplayBoard[i][j].setBackgroundResource(p.isWhite() ? R.drawable.wking : R.drawable.bking);
                            break;
                        case 1:
                            DisplayBoard[i][j].setBackgroundResource(p.isWhite() ? R.drawable.wqueen : R.drawable.bqueen);
                            break;
                        case 2:
                            DisplayBoard[i][j].setBackgroundResource(p.isWhite() ? R.drawable.wrook : R.drawable.brook);
                            break;
                        case 3:
                            DisplayBoard[i][j].setBackgroundResource(p.isWhite() ? R.drawable.wbishop : R.drawable.bbishop);
                            break;
                        case 4:
                            DisplayBoard[i][j].setBackgroundResource(p.isWhite() ? R.drawable.wknight : R.drawable.bknight);
                            break;
                        case 5:
                            DisplayBoard[i][j].setBackgroundResource(p.isWhite() ? R.drawable.wpawn : R.drawable.bpawn);
                            break;
                        default:
                            break;
                    }
                } else {
                    DisplayBoard[i][j].setBackgroundResource(0);
                }
            }
        }
        isKingInDanger();
    }

    public void undo(View v) {
        if (isAiThinking) {
            return;
        }

        // Reset indicators instantly on rewinds
        lastMovedFromSquare = null;
        lastMovedToSquare = null;

        int statesToRemove = (!FirstPlayerTurn) ? 1 : 2;

        for (int step = 0; step < statesToRemove; step++) {
            if (numberOfMoves > 0) {
                for (int g = 0; g < 8; g++) {
                    for (int h = 0; h < 8; h++) {
                        Position[][] pastBoardState = LastMoves.get(numberOfMoves - 1);
                        if (pastBoardState[g][h].getPiece() == null) {
                            Board[g][h].setPiece(null);
                        } else {
                            Board[g][h].setPiece(pastBoardState[g][h].getPiece());
                        }
                    }
                }
                LastMoves.remove(numberOfMoves - 1);
                numberOfMoves--;
            }
        }

        setBoard();
        AnythingSelected = false;
        FirstPlayerTurn = true;
        game_over.setVisibility(View.INVISIBLE);
    }

    private String generateCurrentFenString() {
        StringBuilder fen = new StringBuilder();
        for (int j = 0; j < 8; j++) {
            int emptySquares = 0;
            for (int i = 0; i < 8; i++) {
                Piece p = Board[i][j].getPiece();
                if (p == null) {
                    emptySquares++;
                } else {
                    if (emptySquares > 0) {
                        fen.append(emptySquares);
                        emptySquares = 0;
                    }
                    char pChar = 'p';
                    if (p instanceof King) pChar = 'k';
                    else if (p instanceof Queen) pChar = 'q';
                    else if (p instanceof Rook) pChar = 'r';
                    else if (p instanceof Bishop) pChar = 'b';
                    else if (p instanceof Knight) pChar = 'n';
                    fen.append(p.isWhite() ? Character.toUpperCase(pChar) : pChar);
                }
            }
            if (emptySquares > 0) fen.append(emptySquares);
            if (j < 7) fen.append("/");
        }
        fen.append(FirstPlayerTurn ? " w " : " b ");
        fen.append("KQkq - 0 1");
        return fen.toString();
    }

    private void initializeBoard() {
        bKing = new King(false); wKing = new King(true);
        bQueen = new Queen(false); wQueen = new Queen(true);
        bRook1 = new Rook(false); bRook2 = new Rook(false); wRook1 = new Rook(true); wRook2 = new Rook(true);
        bKnight1 = new Knight(false); bKnight2 = new Knight(false); wKnight1 = new Knight(true); wKnight2 = new Knight(true);
        bBishop1 = new Bishop(false); bBishop2 = new Bishop(false); wBishop1 = new Bishop(true); wBishop2 = new Bishop(true);
        bPawn1 = new Pawn(false); bPawn2 = new Pawn(false); bPawn3 = new Pawn(false); bPawn4 = new Pawn(false);
        bPawn5 = new Pawn(false); bPawn6 = new Pawn(false); bPawn7 = new Pawn(false); bPawn8 = new Pawn(false);
        wPawn1 = new Pawn(true); wPawn2 = new Pawn(true); wPawn3 = new Pawn(true); wPawn4 = new Pawn(true);
        wPawn5 = new Pawn(true); wPawn6 = new Pawn(true); wPawn7 = new Pawn(true); wPawn8 = new Pawn(true);

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Board[i][j] = new Position(null);
                Board2[i][j] = new Position(null);
            }
        }

        // Restructured precise index boundaries mapping to the 8x8 matrix grid layout
        Board[0][7].setPiece(wRook1); Board[1][7].setPiece(wKnight1); Board[2][7].setPiece(wBishop1);
        Board[3][7].setPiece(wQueen); Board[4][7].setPiece(wKing); Board[5][7].setPiece(wBishop2);
        Board[6][7].setPiece(wKnight2); Board[7][7].setPiece(wRook2);

        Board[0][6].setPiece(wPawn1); Board[1][6].setPiece(wPawn2); Board[2][6].setPiece(wPawn3);
        Board[3][6].setPiece(wPawn4); Board[4][6].setPiece(wPawn5); Board[5][6].setPiece(wPawn6);
        Board[6][6].setPiece(wPawn7); Board[7][6].setPiece(wPawn8);

        Board[0][0].setPiece(bRook1); Board[1][0].setPiece(bKnight1); Board[2][0].setPiece(bBishop1);
        Board[3][0].setPiece(bQueen); Board[4][0].setPiece(bKing); Board[5][0].setPiece(bBishop2);
        Board[6][0].setPiece(bKnight2); Board[7][0].setPiece(bRook2);

        Board[0][1].setPiece(bPawn1); Board[1][1].setPiece(bPawn2); Board[2][1].setPiece(bPawn3);
        Board[3][1].setPiece(bPawn4); Board[4][1].setPiece(bPawn5); Board[5][1].setPiece(bPawn6);
        Board[6][1].setPiece(bPawn7); Board[7][1].setPiece(bPawn8);

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                String resName = "R" + i + j;
                String bgResName = "R0" + i + j;
                DisplayBoard[i][j] = (TextView) findViewById(getResources().getIdentifier(resName, "id", getPackageName()));
                DisplayBoardBackground[i][j] = (TextView) findViewById(getResources().getIdentifier(bgResName, "id", getPackageName()));
            }
        }

        for (int g = 0; g < 8; g++) {
            for (int h = 0; h < 8; h++) {
                Board2[g][h].setPiece(Board[g][h].getPiece());
            }
        }

        numberOfMoves = 0; AnythingSelected = false; FirstPlayerTurn = true;
        setBoard();
    }

    public void pawnChoice(View v) {
        int x = v.getId();
        if (x == R.id.pawn_queen) {
            Board[clickedPosition.getX()][clickedPosition.getY()].setPiece(new Queen(clickedPosition.getY() == 0));
        } else if (x == R.id.pawn_rook) {
            Board[clickedPosition.getX()][clickedPosition.getY()].setPiece(new Rook(clickedPosition.getY() == 0));
        } else if (x == R.id.pawn_bishop) {
            Board[clickedPosition.getX()][clickedPosition.getY()].setPiece(new Bishop(clickedPosition.getY() == 0));
        } else if (x == R.id.pawn_knight) {
            Board[clickedPosition.getX()][clickedPosition.getY()].setPiece(new Knight(clickedPosition.getY() == 0));
        }
        pawn_choices.setVisibility(View.INVISIBLE);
        setBoard();
    }

    private void resetColorAtAllowedPosition(ArrayList<Coordinates> list) {
        for (int i = 0; i < list.size(); i++) {
            DisplayBoardBackground[list.get(i).getX()][list.get(i).getY()]
                    .setBackgroundResource((list.get(i).getX() + list.get(i).getY()) % 2 == 0 ? R.color.colorBoardDark : R.color.colorBoardLight);
        }
    }

    void setColorAtAllowedPosition(ArrayList<Coordinates> list) {
        for (int i = 0; i < list.size(); i++) {
            DisplayBoardBackground[list.get(i).getX()][list.get(i).getY()]
                    .setBackgroundResource(Board[list.get(i).getX()][list.get(i).getY()].getPiece() == null ? R.color.colorPositionAvailable : R.color.colorDanger);
        }
    }

    private boolean moveIsAllowed(ArrayList<Coordinates> piece, Coordinates coordinate) {
        for (int i = 0; i < piece.size(); i++) {
            if (piece.get(i).getX() == coordinate.getX() && piece.get(i).getY() == coordinate.getY()) {
                return true;
            }
        }
        return false;
    }

    private void resetColorAtLastPosition(Coordinates lastPos) {
        if ((lastPos.getX() + lastPos.getY()) % 2 == 0) {
            DisplayBoardBackground[lastPos.getX()][lastPos.getY()].setBackgroundResource(R.color.colorBoardDark);
        } else {
            DisplayBoardBackground[lastPos.getX()][lastPos.getY()].setBackgroundResource(R.color.colorBoardLight);
        }
    }

    private void isKingInDanger() {
        ArrayList<Coordinates> movementList = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (Board[i][j].getPiece() != null) {
                    movementList.clear();
                    Coordinates currentCoord = new Coordinates(i, j);
                    movementList = Board[i][j].getPiece().AllowedMoves(currentCoord, Board);

                    for (int x = 0; x < movementList.size(); x++) {
                        Coordinates targetTile = movementList.get(x);
                        Piece targetPiece = Board[targetTile.getX()][targetTile.getY()].getPiece();

                        if (targetPiece instanceof King) {
                            // Check if the king is of the opposite color (genuine threat scenario)
                            if (Board[i][j].getPiece().isWhite() != targetPiece.isWhite()) {
                                DisplayBoardBackground[targetTile.getX()][targetTile.getY()]
                                        .setBackgroundResource(R.color.colorKingInDanger);
                            }
                        }
                    }
                }
            }
        }
    }

    private void checkForPawn() {
        Piece activePiece = Board[clickedPosition.getX()][clickedPosition.getY()].getPiece();

        if (activePiece instanceof Pawn) {
            if (activePiece.isWhite() && clickedPosition.getY() == 0) {
                pawn_choices.setVisibility(View.VISIBLE);
            } else if (!activePiece.isWhite() && clickedPosition.getY() == 7) {
                pawn_choices.setVisibility(View.VISIBLE);
                pawn_choices.setRotation(180f);
            }
        }
        isKingInDanger();
    }
    public void saveBoard() {
        numberOfMoves++;

        // 1. Create a pristine 8x8 matrix grid layer for this history frame snapshot
        Position[][] historyFrame = new Position[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                historyFrame[i][j] = new Position(null);
            }
        }

        // 2. Clone the absolute current board pieces state deep into the history frame
        for (int g = 0; g < 8; g++) {
            for (int h = 0; h < 8; h++) {
                Piece currentPiece = Board[g][h].getPiece();
                if (currentPiece == null) {
                    historyFrame[g][h].setPiece(null);
                } else {
                    historyFrame[g][h].setPiece(currentPiece);
                }
            }
        }

        // 3. Push this unique frame deep into your LastMoves backtracking history stack
        LastMoves.add(numberOfMoves - 1, historyFrame);
    }

}
