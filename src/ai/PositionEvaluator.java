package ai;

import controller.Game;
import java.util.Random;
import model.board.Position;
import model.pieces.*;

/**
 * Avaliador de posições de xadrez usando princípios estratégicos clássicos.
 * Implementa avaliação baseada em material, posição, segurança do rei e
 * estrutura de peões.
 */
public class PositionEvaluator {

    // Valores das peças (centipawns)
    private static final int PAWN_VALUE = 100;
    private static final int KNIGHT_VALUE = 320;
    private static final int BISHOP_VALUE = 330;
    private static final int ROOK_VALUE = 500;
    private static final int QUEEN_VALUE = 900;
    private static final int KING_VALUE = 20000;

    // Tabelas de posição para cada tipo de peça (endgame simplificado)
    private static final int[][] PAWN_TABLE = {
            { 0, 0, 0, 0, 0, 0, 0, 0 },
            { 50, 50, 50, 50, 50, 50, 50, 50 },
            { 10, 10, 20, 30, 30, 20, 10, 10 },
            { 5, 5, 10, 25, 25, 10, 5, 5 },
            { 0, 0, 0, 20, 20, 0, 0, 0 },
            { 5, -5, -10, 0, 0, -10, -5, 5 },
            { 5, 10, 10, -20, -20, 10, 10, 5 },
            { 0, 0, 0, 0, 0, 0, 0, 0 }
    };

    private static final int[][] KNIGHT_TABLE = {
            { -50, -40, -30, -30, -30, -30, -40, -50 },
            { -40, -20, 0, 0, 0, 0, -20, -40 },
            { -30, 0, 10, 15, 15, 10, 0, -30 },
            { -30, 5, 15, 20, 20, 15, 5, -30 },
            { -30, 0, 15, 20, 20, 15, 0, -30 },
            { -30, 5, 10, 15, 15, 10, 5, -30 },
            { -40, -20, 0, 5, 5, 0, -20, -40 },
            { -50, -40, -30, -30, -30, -30, -40, -50 }
    };

    private static final int[][] BISHOP_TABLE = {
            { -20, -10, -10, -10, -10, -10, -10, -20 },
            { -10, 0, 0, 0, 0, 0, 0, -10 },
            { -10, 0, 5, 10, 10, 5, 0, -10 },
            { -10, 5, 5, 10, 10, 5, 5, -10 },
            { -10, 0, 10, 10, 10, 10, 0, -10 },
            { -10, 10, 10, 10, 10, 10, 10, -10 },
            { -10, 5, 0, 0, 0, 0, 5, -10 },
            { -20, -10, -10, -10, -10, -10, -10, -20 }
    };

    private static final int[][] ROOK_TABLE = {
            { 0, 0, 0, 0, 0, 0, 0, 0 },
            { 5, 10, 10, 10, 10, 10, 10, 5 },
            { -5, 0, 0, 0, 0, 0, 0, -5 },
            { -5, 0, 0, 0, 0, 0, 0, -5 },
            { -5, 0, 0, 0, 0, 0, 0, -5 },
            { -5, 0, 0, 0, 0, 0, 0, -5 },
            { -5, 0, 0, 0, 0, 0, 0, -5 },
            { 0, 0, 0, 5, 5, 0, 0, 0 }
    };

    private static final int[][] QUEEN_TABLE = {
            { -20, -10, -10, -5, -5, -10, -10, -20 },
            { -10, 0, 0, 0, 0, 0, 0, -10 },
            { -10, 0, 5, 5, 5, 5, 0, -10 },
            { -5, 0, 5, 5, 5, 5, 0, -5 },
            { 0, 0, 5, 5, 5, 5, 0, -5 },
            { -10, 5, 5, 5, 5, 5, 0, -10 },
            { -10, 0, 5, 0, 0, 0, 0, -10 },
            { -20, -10, -10, -5, -5, -10, -10, -20 }
    };

    private static final int[][] KING_MIDDLE_GAME = {
            { -30, -40, -40, -50, -50, -40, -40, -30 },
            { -30, -40, -40, -50, -50, -40, -40, -30 },
            { -30, -40, -40, -50, -50, -40, -40, -30 },
            { -30, -40, -40, -50, -50, -40, -40, -30 },
            { -20, -30, -30, -40, -40, -30, -30, -20 },
            { -10, -20, -20, -20, -20, -20, -20, -10 },
            { 20, 20, 0, 0, 0, 0, 20, 20 },
            { 20, 30, 10, 0, 0, 10, 30, 20 }
    };

    private final Random random = new Random();

    /**
     * Avalia a posição atual do jogo do ponto de vista das brancas.
     * Valores positivos favorecem as brancas, negativos favorecem as pretas.
     */
    public int evaluate(Game game) {
        if (game.isGameOver()) {
            if (game.isCheckmate(true))
                return -KING_VALUE; // Brancas perderam
            if (game.isCheckmate(false))
                return KING_VALUE; // Pretas perderam
            return 0; // Empate
        }

        int evaluation = 0;

        // Avaliação de material e posição
        evaluation += evaluateMaterialAndPosition(game);

        // Bônus por mobilidade (número de movimentos legais)
        evaluation += evaluateMobility(game);

        // Avaliação da segurança do rei
        evaluation += evaluateKingSafety(game);

        // Pequena randomização para evitar jogos repetitivos
        evaluation += random.nextInt(10) - 5;

        return evaluation;
    }

    private int evaluateMaterialAndPosition(Game game) {
        int evaluation = 0;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position pos = new Position(row, col);
                Piece piece = game.board().get(pos);

                if (piece != null) {
                    int pieceValue = getPieceValue(piece);
                    int positionValue = getPositionValue(piece, row, col);

                    if (piece.isWhite()) {
                        evaluation += pieceValue + positionValue;
                    } else {
                        evaluation -= pieceValue + positionValue;
                    }
                }
            }
        }

        return evaluation;
    }

    private int evaluateMobility(Game game) {
        int whiteMobility = 0;
        int blackMobility = 0;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position pos = new Position(row, col);
                Piece piece = game.board().get(pos);

                if (piece != null) {
                    int moves = game.legalMovesFrom(pos).size();
                    if (piece.isWhite()) {
                        whiteMobility += moves;
                    } else {
                        blackMobility += moves;
                    }
                }
            }
        }

        return (whiteMobility - blackMobility) * 2; // Peso menor que material
    }

    private int evaluateKingSafety(Game game) {
        int evaluation = 0;

        // Penaliza rei em xeque
        if (game.inCheck(true))
            evaluation -= 50;
        if (game.inCheck(false))
            evaluation += 50;

        return evaluation;
    }

    private int getPieceValue(Piece piece) {
        return switch (piece.getSymbol()) {
            case "P" -> PAWN_VALUE;
            case "N" -> KNIGHT_VALUE;
            case "B" -> BISHOP_VALUE;
            case "R" -> ROOK_VALUE;
            case "Q" -> QUEEN_VALUE;
            case "K" -> KING_VALUE;
            default -> 0;
        };
    }

    private int getPositionValue(Piece piece, int row, int col) {
        int[][] table = switch (piece.getSymbol()) {
            case "P" -> PAWN_TABLE;
            case "N" -> KNIGHT_TABLE;
            case "B" -> BISHOP_TABLE;
            case "R" -> ROOK_TABLE;
            case "Q" -> QUEEN_TABLE;
            case "K" -> KING_MIDDLE_GAME;
            default -> new int[8][8]; // Tabela vazia para casos não previstos
        };

        // Para peças pretas, invertemos a tabela verticalmente
        if (!piece.isWhite()) {
            row = 7 - row;
        }

        return table[row][col];
    }
}
