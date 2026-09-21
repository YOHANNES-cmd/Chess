package com.yohannes.chess;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.yohannes.chess.Pieces.Bishop;
import com.yohannes.chess.Pieces.King;
import com.yohannes.chess.Pieces.Knight;
import com.yohannes.chess.Pieces.Pawn;
import com.yohannes.chess.Pieces.Piece;
import com.yohannes.chess.Pieces.Queen;
import com.yohannes.chess.Pieces.Rook;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class SinglePlayerModewithBotActivity  extends AppCompatActivity implements View.OnClickListener {

    public TextView aiThinkingOverlay;
    // Core state flags
    private int activeDifficultyLevel = 0; // Default to Beginner

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
    // Track the AI's last move coordinates for the smart indicator highlights
    private Coordinates aiLastFromSquare = null;
    private Coordinates aiLastToSquare = null;
    Piece bKing;
    Piece wKing;

    Piece bQueen;
    Piece wQueen;

    Piece bKnight1;
    Piece bKnight2;
    Piece wKnight1;
    Piece wKnight2;

    Piece bRook1;
    Piece bRook2;
    Piece wRook1;
    Piece wRook2;

    Piece bBishop1;
    Piece bBishop2;
    Piece wBishop1;
    Piece wBishop2;

    Piece bPawn1;
    Piece bPawn2;
    Piece bPawn3;
    Piece bPawn4;
    Piece bPawn5;
    Piece bPawn6;
    Piece bPawn7;
    Piece bPawn8;

    Piece wPawn1;
    Piece wPawn2;
    Piece wPawn3;
    Piece wPawn4;
    Piece wPawn5;
    Piece wPawn6;
    Piece wPawn7;
    Piece wPawn8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }
        setContentView(R.layout.activity_main);
// Add this inside your onCreate method right below your other findViewById calls
        aiThinkingOverlay = (TextView) findViewById(R.id.ai_thinking_overlay);

        initializeBoard();

        game_over = (TextView) findViewById(R.id.game_over);
        pawn_choices = (LinearLayout) findViewById(R.id.pawn_chioces);

        game_over.setVisibility(View.INVISIBLE);
        pawn_choices.setVisibility(View.INVISIBLE);

        setupClickListeners();
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
        // Protect turn sequence from rapid tapping while the Bot calculates a move
        if (isAiThinking || !FirstPlayerTurn) {
            return;
        }

        int id = v.getId();
        updateClickedPositionCoordinates(id);

        if (!AnythingSelected) {
            Piece selectedPiece = Board[clickedPosition.getX()][clickedPosition.getY()].getPiece();

            if (selectedPiece == null) {
                isKingInDanger();
                return;
            } else {
                // Verify the piece belongs to the player whose turn it currently is
                if (selectedPiece.isWhite() != FirstPlayerTurn) {
                    isKingInDanger();
                    return;
                } else {
                    // 1. Clear any old lingering coordinate path selections
                    listOfCoordinates.clear();

                    // 2. Calculate the raw geometric allowed moves for this piece
                    ArrayList<Coordinates> rawMoves = selectedPiece.AllowedMoves(clickedPosition, Board);

                    // 3. 🌟 STRICT FILTER: Only keep moves that do NOT leave or put your King in check
                    for (Coordinates targetTile : rawMoves) {
                        if (isMoveSafeFromCheck(clickedPosition, targetTile, selectedPiece.isWhite())) {
                            listOfCoordinates.add(targetTile);
                        }
                    }

                    // 4. If the piece has no legal options (e.g. it is absolute pinned), block selection
                    if (listOfCoordinates.isEmpty()) {
                        isKingInDanger();
                        return;
                    }

                    // 5. Mark the state as "Selected" safely
                    AnythingSelected = true;
                    lastPos = new Coordinates(clickedPosition.getX(), clickedPosition.getY());

                    // 6. Force redraw layout: setBoard handles painting the legal filtered highlights
                    setBoard();
                }
            }
        } else {
            // A piece was already selected; the user is now tapping a landing destination square
            processMoveSelection();
        }
    }



    private void updateClickedPositionCoordinates(int id) {
        // Automatically maps layout View IDs back into structured X and Y integer grid limits
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
    private void processMoveSelection() {
        Piece targetedPiece = Board[clickedPosition.getX()][clickedPosition.getY()].getPiece();

        if (targetedPiece == null || targetedPiece.isWhite() != FirstPlayerTurn) {
            // Tapping an empty square or an enemy piece -> try to execute the move
            if (moveIsAllowed(listOfCoordinates, clickedPosition)) {
                executeMoveOnBoard();
            } else {
                cancelCurrentSelection();
                setBoard();
            }
        } else {
            // 🌟 Switch focus: The user tapped another allied piece instead of moving!
            cancelCurrentSelection();

            // Recalculate legal move vectors for the newly selected piece instantly
            listOfCoordinates = Board[clickedPosition.getX()][clickedPosition.getY()].getPiece().AllowedMoves(clickedPosition, Board);
            AnythingSelected = true;
            lastPos = new Coordinates(clickedPosition.getX(), clickedPosition.getY());

            setBoard(); // Updates layout and draws highlights for the new piece
        }
    }
    private void executeMoveOnBoard() {
        saveBoard();

        // Wipe highlights from previous selection coordinates before changing array positions
        resetColorAtAllowedPosition(listOfCoordinates);
        resetColorAtLastPosition(lastPos);

        Board[clickedPosition.getX()][clickedPosition.getY()].setPiece(Board[lastPos.getX()][lastPos.getY()].getPiece());
        Board[lastPos.getX()][lastPos.getY()].setPiece(null);

        DisplayBoard[lastPos.getX()][lastPos.getY()].setBackgroundResource(0);

        AnythingSelected = false;
        FirstPlayerTurn = !FirstPlayerTurn; // Switches turn focus down to computer bot engine
        checkForPawn();
        setBoard(); // Redraws fresh state layout parameters completely clean

        // Check if it's now the AI's turn
        if (!FirstPlayerTurn) {
            triggerAiEngineSequence();
        }
    }


    private void cancelCurrentSelection() {
        resetColorAtLastPosition(lastPos);
        resetColorAtAllowedPosition(listOfCoordinates);
        AnythingSelected = false;
        listOfCoordinates.clear(); // Empty active path maps safely
    }


    private final ChessBotEngine localBotEngine = new ChessBotEngine();
    private final IntermediateBotEngine intermediateBot = new IntermediateBotEngine();
    private final ProductionBotEngine productionBot = new ProductionBotEngine();

    private void triggerAiEngineSequence() {
        isAiThinking = true;

        // Bring up the processing text overlay container block instantly
        aiThinkingOverlay.setVisibility(View.VISIBLE);

        // postDelayed simulates human thinking latency processing time frame delays locally
        aiThinkingOverlay.postDelayed(new Runnable() {
            @Override
            public void run() {
                ChessBotEngine.MoveCandidate computedMove = null;

                // 🌟 DYNAMIC DIFFICULTY ROUTING ENGINE SELECTION LAYER
                if (activeDifficultyLevel == 0) {
                    // Level 0: Casual Woodcutter Bot (Greedy/Random selection)
                    computedMove = localBotEngine.calculateCounterMove(Board);
                } else if (activeDifficultyLevel == 1) {
                    // Level 1: Master Tactician Bot (Minimax Depth 2 with Position Maps)
                    computedMove = intermediateBot.calculateMove(Board);
                } else if (activeDifficultyLevel == 2) {
                    // Level 2: Grandmaster Core Bot (Alpha-Beta Depth 3 with Ordered Priority)
                    computedMove = productionBot.calculateMove(Board);
                }

                // If a valid legal counter-move was calculated, execute it on screen
                if (computedMove != null) {
                    aiThinkingOverlay.setVisibility(View.INVISIBLE);

                    // Transform coordinate points cleanly back into UCI long algebraic notation strings
                    char fromCol = (char) ('a' + computedMove.from.getX());
                    int fromRow = 8 - computedMove.from.getY();
                    char toCol = (char) ('a' + computedMove.to.getX());
                    int toRow = 8 - computedMove.to.getY();

                    String moveToken = "" + fromCol + fromRow + toCol + toRow;

                    // Fire layout updates and switch turn permissions back to White human user
                    executeAiMoveOnBoard(moveToken);
                } else {
                    // Fallback path if the engine evaluates zero valid choices (Checkmate / Stalemate)
                    isAiThinking = false;
                    FirstPlayerTurn = true;
                    aiThinkingOverlay.setVisibility(View.INVISIBLE);
                }
            }
        }, 400); // 400 milliseconds compute buffer time slot delay
    }



    private void resetAiStateOnFailure() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isAiThinking = false;
                FirstPlayerTurn = true;
                aiThinkingOverlay.setVisibility(View.INVISIBLE); // Hide overlay on network drop
            }
        });
    }


    private void executeAiMoveOnBoard(String move) {
        // Translates standard UCI string notation tokens (e.g. "e2e4") back to array indexes
        int fromX = move.charAt(0) - 'a';
        int fromY = '8' - move.charAt(1);
        int toX = move.charAt(2) - 'a';
        int toY = '8' - move.charAt(3);

        // Store the exact path boundaries to render visual highlight borders later
        aiLastFromSquare = new Coordinates(fromX, fromY);
        aiLastToSquare = new Coordinates(toX, toY);

        lastPos = new Coordinates(fromX, fromY);
        clickedPosition = new Coordinates(toX, toY);

        saveBoard();
        Board[toX][toY].setPiece(Board[fromX][fromY].getPiece());
        Board[fromX][fromY].setPiece(null);

        DisplayBoard[fromX][fromY].setBackgroundResource(0);
        isKingInDanger();
        FirstPlayerTurn = true; // Return control back to human user
        isAiThinking = false;
        checkForPawn();
        setBoard(); // This updates piece graphics and also redraws color backgrounds
    }


    private String generateCurrentFenString() {
        StringBuilder fen = new StringBuilder();

        // 1. Piece Placement: Loop through vertical rows (j) from index 0 down to 7
        for (int j = 0; j < 8; j++) {
            int emptySquares = 0;

            for (int i = 0; i < 8; i++) {
                Piece p = Board[i][j].getPiece();

                if (p == null) {
                    emptySquares++;
                } else {
                    // If there were empty squares before this piece, append the number
                    if (emptySquares > 0) {
                        fen.append(emptySquares);
                        emptySquares = 0;
                    }

                    // Convert your piece classes to standard FEN characters
                    char pChar = 'p';
                    if (p instanceof King) pChar = 'k';
                    else if (p instanceof Queen) pChar = 'q';
                    else if (p instanceof Rook) pChar = 'r';
                    else if (p instanceof Bishop) pChar = 'b';
                    else if (p instanceof Knight) pChar = 'n';

                    // Uppercase = White pieces, Lowercase = Black pieces
                    fen.append(p.isWhite() ? Character.toUpperCase(pChar) : pChar);
                }
            }

            // If the row ends with empty squares, append the remaining count
            if (emptySquares > 0) {
                fen.append(emptySquares);
            }

            // Add a row separator slash (but do not add one after the last row)
            if (j < 7) {
                fen.append("/");
            }
        }

        // 2. Active Side Turn Flag: 'w' for White, 'b' for Black
        // Since this runs immediately after White moves, FirstPlayerTurn is false, marking it as Black's turn ('b').
        fen.append(FirstPlayerTurn ? " w " : " b ");

        // 3. Status Placeholders: Castling Rights, En Passant Targets, Halfmove Clock, Fullmove Number
        fen.append("KQkq - 0 1");

        return fen.toString();
    }

    private boolean hasAnyLegalMoves(boolean checkWhite) {
        // Scan the entire board square by square
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece p = Board[i][j].getPiece();
                // Find pieces belonging to the side whose turn it is
                if (p != null && p.isWhite() == checkWhite) {
                    Coordinates from = new Coordinates(i, j);
                    ArrayList<Coordinates> rawMoves = p.AllowedMoves(from, Board);

                    // Test each move to see if it actually escapes/blocks the check
                    for (Coordinates to : rawMoves) {
                        if (isMoveSafeFromCheck(from, to, checkWhite)) {
                            return true; // Found at least one legal move!
                        }
                    }
                }
            }
        }
        return false; // No legal moves available
    }

    private boolean isMoveSafeFromCheck(Coordinates from, Coordinates to, boolean isWhite) {
        // 1. Simulate the move on your real board matrix
        Piece movingPiece = Board[from.getX()][from.getY()].getPiece();
        Piece capturedPiece = Board[to.getX()][to.getY()].getPiece();

        Board[to.getX()][to.getY()].setPiece(movingPiece);
        Board[from.getX()][from.getY()].setPiece(null);

        // 2. Check if your king would be safe if you made this move
        boolean kingSafe = true;
        Coordinates kingPos = findKingPosition(isWhite);

        if (kingPos != null) {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    Piece enemy = Board[i][j].getPiece();
                    if (enemy != null && enemy.isWhite() != isWhite) {
                        ArrayList<Coordinates> enemyMoves = enemy.AllowedMoves(new Coordinates(i, j), Board);
                        for (Coordinates em : enemyMoves) {
                            if (em.getX() == kingPos.getX() && em.getY() == kingPos.getY()) {
                                kingSafe = false; // An enemy piece can strike the king!
                                break;
                            }
                        }
                    }
                    if (!kingSafe) break;
                }
                if (!kingSafe) break;
            }
        }

        // 3. Undo the simulation immediately to restore the original board layout
        Board[from.getX()][from.getY()].setPiece(movingPiece);
        Board[to.getX()][to.getY()].setPiece(capturedPiece);

        return kingSafe;
    }


    private Coordinates findKingPosition(boolean isWhite) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece p = Board[i][j].getPiece();
                if (p instanceof King && p.isWhite() == isWhite) {
                    return new Coordinates(i, j);
                }
            }
        }
        return null;
    }

    private boolean isKingCurrentlyInCheck(boolean isWhite) {
        Coordinates kingPos = findKingPosition(isWhite);
        if (kingPos == null) return false;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece enemy = Board[i][j].getPiece();
                if (enemy != null && enemy.isWhite() != isWhite) {
                    ArrayList<Coordinates> moves = enemy.AllowedMoves(new Coordinates(i, j), Board);
                    for (Coordinates c : moves) {
                        if (c.getX() == kingPos.getX() && c.getY() == kingPos.getY()) {
                            return true; // King is in check
                        }
                    }
                }
            }
        }
        return false;
    }

    private void verifyEndGameStates() {
        final boolean sideToMove = FirstPlayerTurn; // True = White, False = Black
        boolean hasMoves = hasAnyLegalMoves(sideToMove);
        boolean isCurrentlyInCheck = isKingCurrentlyInCheck(sideToMove);

        if (!hasMoves) {
            isAiThinking = true; // Lock all touch capabilities on the grid layout
            game_over.setVisibility(View.VISIBLE);

            String dialogTitle;
            String dialogMessage;

            if (isCurrentlyInCheck) {
                // Checkmate Scenario
                if (sideToMove) {
                    dialogTitle = "🏁 Checkmate! Game Over";
                    dialogMessage = "The Bot has checkmated your King. Black wins! 🤖";
                    game_over.setText("Checkmate! Black wins!");
                } else {
                    dialogTitle = "🏆 Checkmate! Victory!";
                    dialogMessage = "Congratulations! You have successfully checkmated the Bot! 🎉";
                    game_over.setText("Checkmate! White wins!");
                }
            } else {
                // Stalemate Scenario
                dialogTitle = "🤝 Stalemate (Draw)";
                dialogMessage = "The active player has no legal moves, but is not in check. The match ends in a draw.";
                game_over.setText("Stalemate! Draw!");
            }

            // Launch an un-skippable Endgame Dialog overlay prompt
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle(dialogTitle);
            builder.setMessage(dialogMessage);
            builder.setCancelable(false); // Prevents the user from clicking away outside the box

            builder.setPositiveButton("Play Again", new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(android.content.DialogInterface dialog, int which) {
                    recreate(); // Refresh and completely reboot the activity layout state
                }
            });

            builder.setNegativeButton("Exit to Menu", new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(android.content.DialogInterface dialog, int which) {
                    finish(); // Closes this screen and pops back to FirstPage.java
                }
            });

            if (!isFinishing()) {
                builder.show();
            }
        } else {
            // 🌟 THE KING IS IN CHECK, BUT HAS LEGAL MOVES LEFT TO ESCAPE
            if (isCurrentlyInCheck) {
                if (sideToMove) {
                    // It is White's turn, meaning Black just checked the human player
                    android.widget.Toast.makeText(this, "⚠️ Check! Your King is under attack!", android.widget.Toast.LENGTH_SHORT).show();
                } else {
                    // It is Black's turn, meaning the human player just checked the bot
                    android.widget.Toast.makeText(this, "⚔️ Check! The Bot's King is in danger!", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        }
    }



    private void initializeBoard() {
        bKing = new King(false);
        wKing = new King(true);

        bQueen = new Queen(false);
        wQueen = new Queen(true);

        bRook1 = new Rook(false);
        bRook2 = new Rook(false);
        wRook1 = new Rook(true);
        wRook2 = new Rook(true);

        bKnight1 = new Knight(false);
        bKnight2 = new Knight(false);
        wKnight1 = new Knight(true);
        wKnight2 = new Knight(true);

        bBishop1 = new Bishop(false);
        bBishop2 = new Bishop(false);
        wBishop1 = new Bishop(true);
        wBishop2 = new Bishop(true);

        bPawn1 = new Pawn(false);
        bPawn2 = new Pawn(false);
        bPawn3 = new Pawn(false);
        bPawn4 = new Pawn(false);
        bPawn5 = new Pawn(false);
        bPawn6 = new Pawn(false);
        bPawn7 = new Pawn(false);
        bPawn8 = new Pawn(false);

        wPawn1 = new Pawn(true);
        wPawn2 = new Pawn(true);
        wPawn3 = new Pawn(true);
        wPawn4 = new Pawn(true);
        wPawn5 = new Pawn(true);
        wPawn6 = new Pawn(true);
        wPawn7 = new Pawn(true);
        wPawn8 = new Pawn(true);

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Board[i][j] = new Position(null);
                Board2[i][j] = new Position(null);
            }
        }

        Board[0][7].setPiece(wRook1);
        Board[1][7].setPiece(wKnight1);
        Board[2][7].setPiece(wBishop1);
        Board[3][7].setPiece(wQueen);
        Board[4][7].setPiece(wKing);
        Board[5][7].setPiece(wBishop2);
        Board[6][7].setPiece(wKnight2);
        Board[7][7].setPiece(wRook2);

        Board[0][6].setPiece(wPawn1);
        Board[1][6].setPiece(wPawn2);
        Board[2][6].setPiece(wPawn3);
        Board[3][6].setPiece(wPawn4);
        Board[4][6].setPiece(wPawn5);
        Board[5][6].setPiece(wPawn6);
        Board[6][6].setPiece(wPawn7);
        Board[7][6].setPiece(wPawn8);

        Board[0][0].setPiece(bRook1);
        Board[1][0].setPiece(bKnight1);
        Board[2][0].setPiece(bBishop1);
        Board[3][0].setPiece(bQueen);
        Board[4][0].setPiece(bKing);
        Board[5][0].setPiece(bBishop2);
        Board[6][0].setPiece(bKnight2);
        Board[7][0].setPiece(bRook2);

        Board[0][1].setPiece(bPawn1);
        Board[1][1].setPiece(bPawn2);
        Board[2][1].setPiece(bPawn3);
        Board[3][1].setPiece(bPawn4);
        Board[4][1].setPiece(bPawn5);
        Board[5][1].setPiece(bPawn6);
        Board[6][1].setPiece(bPawn7);
        Board[7][1].setPiece(bPawn8);

        DisplayBoard[0][0] = (TextView) findViewById(R.id.R00);
        DisplayBoardBackground[0][0] = (TextView) findViewById(R.id.R000);
        DisplayBoard[1][0] = (TextView) findViewById(R.id.R10);
        DisplayBoardBackground[1][0] = (TextView) findViewById(R.id.R010);
        DisplayBoard[2][0] = (TextView) findViewById(R.id.R20);
        DisplayBoardBackground[2][0] = (TextView) findViewById(R.id.R020);
        DisplayBoard[3][0] = (TextView) findViewById(R.id.R30);
        DisplayBoardBackground[3][0] = (TextView) findViewById(R.id.R030);
        DisplayBoard[4][0] = (TextView) findViewById(R.id.R40);
        DisplayBoardBackground[4][0] = (TextView) findViewById(R.id.R040);
        DisplayBoard[5][0] = (TextView) findViewById(R.id.R50);
        DisplayBoardBackground[5][0] = (TextView) findViewById(R.id.R050);
        DisplayBoard[6][0] = (TextView) findViewById(R.id.R60);
        DisplayBoardBackground[6][0] = (TextView) findViewById(R.id.R060);
        DisplayBoard[7][0] = (TextView) findViewById(R.id.R70);
        DisplayBoardBackground[7][0] = (TextView) findViewById(R.id.R070);

        DisplayBoard[0][1] = (TextView) findViewById(R.id.R01);
        DisplayBoardBackground[0][1] = (TextView) findViewById(R.id.R001);
        DisplayBoard[1][1] = (TextView) findViewById(R.id.R11);
        DisplayBoardBackground[1][1] = (TextView) findViewById(R.id.R011);
        DisplayBoard[2][1] = (TextView) findViewById(R.id.R21);
        DisplayBoardBackground[2][1] = (TextView) findViewById(R.id.R021);
        DisplayBoard[3][1] = (TextView) findViewById(R.id.R31);
        DisplayBoardBackground[3][1] = (TextView) findViewById(R.id.R031);
        DisplayBoard[4][1] = (TextView) findViewById(R.id.R41);
        DisplayBoardBackground[4][1] = (TextView) findViewById(R.id.R041);
        DisplayBoard[5][1] = (TextView) findViewById(R.id.R51);
        DisplayBoardBackground[5][1] = (TextView) findViewById(R.id.R051);
        DisplayBoard[6][1] = (TextView) findViewById(R.id.R61);
        DisplayBoardBackground[6][1] = (TextView) findViewById(R.id.R061);
        DisplayBoard[7][1] = (TextView) findViewById(R.id.R71);
        DisplayBoardBackground[7][1] = (TextView) findViewById(R.id.R071);

        DisplayBoard[0][2] = (TextView) findViewById(R.id.R02);
        DisplayBoardBackground[0][2] = (TextView) findViewById(R.id.R002);
        DisplayBoard[1][2] = (TextView) findViewById(R.id.R12);
        DisplayBoardBackground[1][2] = (TextView) findViewById(R.id.R012);
        DisplayBoard[2][2] = (TextView) findViewById(R.id.R22);
        DisplayBoardBackground[2][2] = (TextView) findViewById(R.id.R022);
        DisplayBoard[3][2] = (TextView) findViewById(R.id.R32);
        DisplayBoardBackground[3][2] = (TextView) findViewById(R.id.R032);
        DisplayBoard[4][2] = (TextView) findViewById(R.id.R42);
        DisplayBoardBackground[4][2] = (TextView) findViewById(R.id.R042);
        DisplayBoard[5][2] = (TextView) findViewById(R.id.R52);
        DisplayBoardBackground[5][2] = (TextView) findViewById(R.id.R052);
        DisplayBoard[6][2] = (TextView) findViewById(R.id.R62);
        DisplayBoardBackground[6][2] = (TextView) findViewById(R.id.R062);
        DisplayBoard[7][2] = (TextView) findViewById(R.id.R72);
        DisplayBoardBackground[7][2] = (TextView) findViewById(R.id.R072);

        DisplayBoard[0][3] = (TextView) findViewById(R.id.R03);
        DisplayBoardBackground[0][3] = (TextView) findViewById(R.id.R003);
        DisplayBoard[1][3] = (TextView) findViewById(R.id.R13);
        DisplayBoardBackground[1][3] = (TextView) findViewById(R.id.R013);
        DisplayBoard[2][3] = (TextView) findViewById(R.id.R23);
        DisplayBoardBackground[2][3] = (TextView) findViewById(R.id.R023);
        DisplayBoard[3][3] = (TextView) findViewById(R.id.R33);
        DisplayBoardBackground[3][3] = (TextView) findViewById(R.id.R033);
        DisplayBoard[4][3] = (TextView) findViewById(R.id.R43);
        DisplayBoardBackground[4][3] = (TextView) findViewById(R.id.R043);
        DisplayBoard[5][3] = (TextView) findViewById(R.id.R53);
        DisplayBoardBackground[5][3] = (TextView) findViewById(R.id.R053);
        DisplayBoard[6][3] = (TextView) findViewById(R.id.R63);
        DisplayBoardBackground[6][3] = (TextView) findViewById(R.id.R063);
        DisplayBoard[7][3] = (TextView) findViewById(R.id.R73);
        DisplayBoardBackground[7][3] = (TextView) findViewById(R.id.R073);

        DisplayBoard[0][4] = (TextView) findViewById(R.id.R04);
        DisplayBoardBackground[0][4] = (TextView) findViewById(R.id.R004);
        DisplayBoard[1][4] = (TextView) findViewById(R.id.R14);
        DisplayBoardBackground[1][4] = (TextView) findViewById(R.id.R014);
        DisplayBoard[2][4] = (TextView) findViewById(R.id.R24);
        DisplayBoardBackground[2][4] = (TextView) findViewById(R.id.R024);
        DisplayBoard[3][4] = (TextView) findViewById(R.id.R34);
        DisplayBoardBackground[3][4] = (TextView) findViewById(R.id.R034);
        DisplayBoard[4][4] = (TextView) findViewById(R.id.R44);
        DisplayBoardBackground[4][4] = (TextView) findViewById(R.id.R044);
        DisplayBoard[5][4] = (TextView) findViewById(R.id.R54);
        DisplayBoardBackground[5][4] = (TextView) findViewById(R.id.R054);
        DisplayBoard[6][4] = (TextView) findViewById(R.id.R64);
        DisplayBoardBackground[6][4] = (TextView) findViewById(R.id.R064);
        DisplayBoard[7][4] = (TextView) findViewById(R.id.R74);
        DisplayBoardBackground[7][4] = (TextView) findViewById(R.id.R074);

        DisplayBoard[0][5] = (TextView) findViewById(R.id.R05);
        DisplayBoardBackground[0][5] = (TextView) findViewById(R.id.R005);
        DisplayBoard[1][5] = (TextView) findViewById(R.id.R15);
        DisplayBoardBackground[1][5] = (TextView) findViewById(R.id.R015);
        DisplayBoard[2][5] = (TextView) findViewById(R.id.R25);
        DisplayBoardBackground[2][5] = (TextView) findViewById(R.id.R025);
        DisplayBoard[3][5] = (TextView) findViewById(R.id.R35);
        DisplayBoardBackground[3][5] = (TextView) findViewById(R.id.R035);
        DisplayBoard[4][5] = (TextView) findViewById(R.id.R45);
        DisplayBoardBackground[4][5] = (TextView) findViewById(R.id.R045);
        DisplayBoard[5][5] = (TextView) findViewById(R.id.R55);
        DisplayBoardBackground[5][5] = (TextView) findViewById(R.id.R055);
        DisplayBoard[6][5] = (TextView) findViewById(R.id.R65);
        DisplayBoardBackground[6][5] = (TextView) findViewById(R.id.R065);
        DisplayBoard[7][5] = (TextView) findViewById(R.id.R75);
        DisplayBoardBackground[7][5] = (TextView) findViewById(R.id.R075);

        DisplayBoard[0][6] = (TextView) findViewById(R.id.R06);
        DisplayBoardBackground[0][6] = (TextView) findViewById(R.id.R006);
        DisplayBoard[1][6] = (TextView) findViewById(R.id.R16);
        DisplayBoardBackground[1][6] = (TextView) findViewById(R.id.R016);
        DisplayBoard[2][6] = (TextView) findViewById(R.id.R26);
        DisplayBoardBackground[2][6] = (TextView) findViewById(R.id.R026);
        DisplayBoard[3][6] = (TextView) findViewById(R.id.R36);
        DisplayBoardBackground[3][6] = (TextView) findViewById(R.id.R036);
        DisplayBoard[4][6] = (TextView) findViewById(R.id.R46);
        DisplayBoardBackground[4][6] = (TextView) findViewById(R.id.R046);
        DisplayBoard[5][6] = (TextView) findViewById(R.id.R56);
        DisplayBoardBackground[5][6] = (TextView) findViewById(R.id.R056);
        DisplayBoard[6][6] = (TextView) findViewById(R.id.R66);
        DisplayBoardBackground[6][6] = (TextView) findViewById(R.id.R066);
        DisplayBoard[7][6] = (TextView) findViewById(R.id.R76);
        DisplayBoardBackground[7][6] = (TextView) findViewById(R.id.R076);

        DisplayBoard[0][7] = (TextView) findViewById(R.id.R07);
        DisplayBoardBackground[0][7] = (TextView) findViewById(R.id.R007);
        DisplayBoard[1][7] = (TextView) findViewById(R.id.R17);
        DisplayBoardBackground[1][7] = (TextView) findViewById(R.id.R017);
        DisplayBoard[2][7] = (TextView) findViewById(R.id.R27);
        DisplayBoardBackground[2][7] = (TextView) findViewById(R.id.R027);
        DisplayBoard[3][7] = (TextView) findViewById(R.id.R37);
        DisplayBoardBackground[3][7] = (TextView) findViewById(R.id.R037);
        DisplayBoard[4][7] = (TextView) findViewById(R.id.R47);
        DisplayBoardBackground[4][7] = (TextView) findViewById(R.id.R047);
        DisplayBoard[5][7] = (TextView) findViewById(R.id.R57);
        DisplayBoardBackground[5][7] = (TextView) findViewById(R.id.R057);
        DisplayBoard[6][7] = (TextView) findViewById(R.id.R67);
        DisplayBoardBackground[6][7] = (TextView) findViewById(R.id.R067);
        DisplayBoard[7][7] = (TextView) findViewById(R.id.R77);
        DisplayBoardBackground[7][7] = (TextView) findViewById(R.id.R077);

        for(int g=0;g<8;g++){
            for(int h=0;h<8;h++){
                if(Board[g][h].getPiece()==null){
                    Board2[g][h].setPiece(null);
                }else{
                    Board2[g][h].setPiece(Board[g][h].getPiece());
                }
            }
        }

        numberOfMoves = 0;
        AnythingSelected = false;
        FirstPlayerTurn = true;
        setBoard();
    }

    private void setBoard() {
        // 1. Reset background tile colors, protecting active paths and selections
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {

                // If a piece highlight is active and this tile is in the allowed list, skip resetting it
                if (AnythingSelected && isTileInAllowedMoves(i, j)) {
                    continue;
                }

                // Protect the highlight background of the selected piece itself
                if (AnythingSelected && lastPos != null && lastPos.getX() == i && lastPos.getY() == j) {
                    continue;
                }

                // Otherwise, draw standard checkered pattern background squares
                if ((i + j) % 2 == 0) {
                    DisplayBoardBackground[i][j].setBackgroundResource(R.color.colorBoardDark);
                } else {
                    DisplayBoardBackground[i][j].setBackgroundResource(R.color.colorBoardLight);
                }
            }
        }

        // 2. NOW APPLY THE HIGHLIGHT COLORS DYNAMICALLY
        if (AnythingSelected && lastPos != null) {
            // Highlight the selected piece square
            DisplayBoardBackground[lastPos.getX()][lastPos.getY()].setBackgroundResource(R.color.colorSelected);

            // Loop through allowed paths and highlight available slots
            for (Coordinates coord : listOfCoordinates) {
                if (Board[coord.getX()][coord.getY()].getPiece() == null) {
                    // Empty square = normal move option
                    DisplayBoardBackground[coord.getX()][coord.getY()].setBackgroundResource(R.color.colorPositionAvailable);
                } else {
                    // Occupied by enemy = attack/danger option
                    DisplayBoardBackground[coord.getX()][coord.getY()].setBackgroundResource(R.color.colorDanger);
                }
            }
        }

        // 3. Inject Smart Engine Move Indicator Colors if the AI just completed a turn
        if (aiLastFromSquare != null && aiLastToSquare != null && !AnythingSelected) {
            DisplayBoardBackground[aiLastFromSquare.getX()][aiLastFromSquare.getY()].setBackgroundResource(R.color.colorSelected);
            DisplayBoardBackground[aiLastToSquare.getX()][aiLastToSquare.getY()].setBackgroundResource(R.color.colorSelected);
        }


        // [Keep your standard switch-case loops for drawing piece images like wking, bking, etc. here]


        // 4. Render all standard piece graphical resource assets row by row
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
        verifyEndGameStates();
    }

    // Helper validation to trace active target indices inside our array stacks
    private boolean isTileInAllowedMoves(int x, int y) {
        for (Coordinates coord : listOfCoordinates) {
            if (coord.getX() == x && coord.getY() == y) return true;
        }
        return false;
    }



    public void undo(View v) {
        // If the computer engine is currently processing a search request, ignore undo clicks
        if (isAiThinking) {
            return;
        }

        // Clear the smart move highlights so they don't persist on the rewound board state
        aiLastFromSquare = null;
        aiLastToSquare = null;

        // Single Player mode requires popping two states to undo a full round of turns
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
                // Remove the reference snapshot from history tracking array list
                LastMoves.remove(numberOfMoves - 1);
                numberOfMoves--;
            }
        }

        // Force redraw layout graphics assets updates mapping pieces positions
        setBoard();

        // Clear and restore original board square accent design styling background maps
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if ((i + j) % 2 == 0) {
                    DisplayBoardBackground[i][j].setBackgroundResource(R.color.colorBoardDark);
                } else {
                    DisplayBoardBackground[i][j].setBackgroundResource(R.color.colorBoardLight);
                }
            }
        }

        isKingInDanger();
        AnythingSelected = false;
        FirstPlayerTurn = true; // Ensure control focus initializes on the human user
        game_over.setVisibility(View.INVISIBLE);
        game_over.setText("Game Over !!"); // Reset layout strings back to default
        isAiThinking = false; // Unlock click capabilities safely
    }


    public void pawnChoice(View v){
        int x = v.getId();

        if (x == R.id.pawn_queen) {
            if(clickedPosition.getY() == 0){
                Board[clickedPosition.getX()][clickedPosition.getY()].setPiece(new Queen(true));
                DisplayBoard[clickedPosition.getX()][clickedPosition.getY()].setBackgroundResource(R.drawable.wqueen);
            }else{
                Board[clickedPosition.getX()][clickedPosition.getY()].setPiece(new Queen(false));
                DisplayBoard[clickedPosition.getX()][clickedPosition.getY()].setBackgroundResource(R.drawable.bqueen);
            }
        } else if (x == R.id.pawn_rook) {
            if(clickedPosition.getY() == 0){
                Board[clickedPosition.getX()][clickedPosition.getY()].setPiece(new Rook(true));
                DisplayBoard[clickedPosition.getX()][clickedPosition.getY()].setBackgroundResource(R.drawable.wrook);
            }else{
                Board[clickedPosition.getX()][clickedPosition.getY()].setPiece(new Rook(false));
                DisplayBoard[clickedPosition.getX()][clickedPosition.getY()].setBackgroundResource(R.drawable.brook);
            }
        } else if (x == R.id.pawn_bishop) {
            if(clickedPosition.getY() == 0){
                Board[clickedPosition.getX()][clickedPosition.getY()].setPiece(new Bishop(true));
                DisplayBoard[clickedPosition.getX()][clickedPosition.getY()].setBackgroundResource(R.drawable.wbishop);
            }else{
                Board[clickedPosition.getX()][clickedPosition.getY()].setPiece(new Bishop(false));
                DisplayBoard[clickedPosition.getX()][clickedPosition.getY()].setBackgroundResource(R.drawable.bbishop);
            }
        } else if (x == R.id.pawn_knight) {
            if(clickedPosition.getY() == 0){
                Board[clickedPosition.getX()][clickedPosition.getY()].setPiece(new Knight(true));
                DisplayBoard[clickedPosition.getX()][clickedPosition.getY()].setBackgroundResource(R.drawable.wknight);
            }else{
                Board[clickedPosition.getX()][clickedPosition.getY()].setPiece(new Knight(false));
                DisplayBoard[clickedPosition.getX()][clickedPosition.getY()].setBackgroundResource(R.drawable.bknight);
            }
        }

        pawn_choices.setVisibility(View.INVISIBLE);
    }

    private void resetColorAtAllowedPosition(ArrayList<Coordinates> listOfCoordinates) {
        for(int i=0; i<listOfCoordinates.size(); i++){
            if((listOfCoordinates.get(i).getX() + listOfCoordinates.get(i).getY())%2==0){
                DisplayBoardBackground[listOfCoordinates.get(i).getX()][listOfCoordinates.get(i).getY()].setBackgroundResource(R.color.colorBoardDark);
            }else {
                DisplayBoardBackground[listOfCoordinates.get(i).getX()][listOfCoordinates.get(i).getY()].setBackgroundResource(R.color.colorBoardLight);
            }
        }
    }

    void setColorAtAllowedPosition(ArrayList<Coordinates> list) {
        // Identify the color of the player currently making a move
        boolean isWhiteTurn = Board[lastPos.getX()][lastPos.getY()].getPiece().isWhite();

        for (int i = 0; i < list.size(); i++) {
            Coordinates targetTile = list.get(i);

            // 🌟 STRICT RULE ENFORCEMENT: Filter out moves that leave your own King in check
            if (!isMoveSafeFromCheck(lastPos, targetTile, isWhiteTurn)) {
                continue; // Skip highlighting this move because it's illegal!
            }

            // Highlight legal empty squares vs legal enemy capture targets
            if (Board[targetTile.getX()][targetTile.getY()].getPiece() == null) {
                DisplayBoardBackground[targetTile.getX()][targetTile.getY()]
                        .setBackgroundResource(R.color.colorPositionAvailable);
            } else {
                DisplayBoardBackground[targetTile.getX()][targetTile.getY()]
                        .setBackgroundResource(R.color.colorDanger);
            }
        }
    }

    private boolean moveIsAllowed(ArrayList<Coordinates> piece, Coordinates coordinate) {
        Boolean Allowed = false;
        for(int i =0;i<piece.size();i++){
            if(piece.get(i).getX() == coordinate.getX()  &&  piece.get(i).getY() == coordinate.getY()){
                Allowed = true;
                break;
            }
        }
        return Allowed;
    }

    private void resetColorAtLastPosition(Coordinates lastPos){
        if((lastPos.getX() + lastPos.getY())%2==0){
            DisplayBoardBackground[lastPos.getX()][lastPos.getY()].setBackgroundResource(R.color.colorBoardDark);
        }else {
            DisplayBoardBackground[lastPos.getX()][lastPos.getY()].setBackgroundResource(R.color.colorBoardLight);
        }
    }

    private void isKingInDanger() {
        boolean whiteKingInCheck = isKingCurrentlyInCheck(true);
        boolean blackKingInCheck = isKingCurrentlyInCheck(false);

        if (whiteKingInCheck) {
            Coordinates wKingPos = findKingPosition(true);
            if (wKingPos != null) {
                DisplayBoardBackground[wKingPos.getX()][wKingPos.getY()]
                        .setBackgroundResource(R.color.colorKingInDanger); // Paints White King Red
            }
        }

        if (blackKingInCheck) {
            Coordinates bKingPos = findKingPosition(false);
            if (bKingPos != null) {
                DisplayBoardBackground[bKingPos.getX()][bKingPos.getY()]
                        .setBackgroundResource(R.color.colorKingInDanger); // Paints Black King Red
            }
        }
    }


    private void checkForPawn(){
        if(Board[clickedPosition.getX()][clickedPosition.getY()].getPiece() instanceof Pawn){
            if(Board[clickedPosition.getX()][clickedPosition.getY()].getPiece().isWhite()){
                if(clickedPosition.getY() == 0){
                    pawn_choices.setVisibility(View.VISIBLE);
                }
            }else{
                if(clickedPosition.getY() == 7){
                    pawn_choices.setVisibility(View.VISIBLE);
                    pawn_choices.setRotation(180);
                }
            }
        }
        isKingInDanger();
    }

    public void saveBoard(){
        numberOfMoves++;
        LastMoves.add(numberOfMoves-1 ,Board2 );

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                LastMoves.get(numberOfMoves-1)[i][j] = new Position(null);
            }
        }

        for(int g=0;g<8;g++){
            for(int h=0;h<8;h++){
                if(Board[g][h].getPiece()==null){
                    LastMoves.get(numberOfMoves-1)[g][h].setPiece(null);
                }else{
                    LastMoves.get(numberOfMoves-1)[g][h].setPiece(Board[g][h].getPiece());
                }
            }
        }
    }
}
