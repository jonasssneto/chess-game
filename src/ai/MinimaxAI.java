package ai;

import controller.Game;
import java.util.ArrayList;
import java.util.List;
import model.board.Position;
import model.pieces.Pawn;
import model.pieces.Piece;

/**
 * Implementação de IA usando algoritmo Minimax com poda Alfa-Beta.
 * Representa o nível mais difícil de IA.
 */
public class MinimaxAI implements ChessAI {

    private final PositionEvaluator evaluator;
    private final int maxDepth;
    private final String name;
    private final String description;

    // Construtor para diferentes níveis de dificuldade
    public MinimaxAI(int depth, String name, String description) {
        this.evaluator = new PositionEvaluator();
        this.maxDepth = depth;
        this.name = name;
        this.description = description;
    }

    @Override
    public AIMove getBestMove(Game game, boolean isWhite) {
        if (game.isGameOver())
            return null;

        List<Move> allMoves = generateAllMoves(game, isWhite);
        if (allMoves.isEmpty())
            return null;

        // Ordena movimentos para melhor poda alfa-beta
        orderMoves(allMoves, game);

        AIMove bestMove = null;
        int bestValue = isWhite ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (Move move : allMoves) {
            // Cria snapshot do jogo para simular o movimento
            Game tempGame = createGameSnapshot(game);
            tempGame.move(move.from, move.to, move.promotion);

            int value = minimax(tempGame, maxDepth - 1, !isWhite, alpha, beta);

            if (isWhite && value > bestValue) {
                bestValue = value;
                bestMove = new AIMove(move.from, move.to, move.promotion, value);
                alpha = Math.max(alpha, value);
            } else if (!isWhite && value < bestValue) {
                bestValue = value;
                bestMove = new AIMove(move.from, move.to, move.promotion, value);
                beta = Math.min(beta, value);
            }

            // Poda alfa-beta
            if (beta <= alpha)
                break;
        }

        return bestMove;
    }

    private int minimax(Game game, int depth, boolean isMaximizing, int alpha, int beta) {
        if (depth == 0 || game.isGameOver()) {
            return evaluator.evaluate(game);
        }

        List<Move> moves = generateAllMoves(game, isMaximizing);
        if (moves.isEmpty()) {
            return evaluator.evaluate(game);
        }

        orderMoves(moves, game);

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : moves) {
                Game tempGame = createGameSnapshot(game);
                tempGame.move(move.from, move.to, move.promotion);

                int eval = minimax(tempGame, depth - 1, false, alpha, beta);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);

                if (beta <= alpha)
                    break; // Poda alfa-beta
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : moves) {
                Game tempGame = createGameSnapshot(game);
                tempGame.move(move.from, move.to, move.promotion);

                int eval = minimax(tempGame, depth - 1, true, alpha, beta);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);

                if (beta <= alpha)
                    break; // Poda alfa-beta
            }
            return minEval;
        }
    }

    private List<Move> generateAllMoves(Game game, boolean isWhite) {
        List<Move> moves = new ArrayList<>();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position from = new Position(row, col);
                Piece piece = game.board().get(from);

                if (piece != null && piece.isWhite() == isWhite) {
                    List<Position> legalMoves = game.legalMovesFrom(from);

                    for (Position to : legalMoves) {
                        if (piece instanceof Pawn && game.isPromotion(from, to)) {
                            // Adiciona todas as promoções possíveis
                            moves.add(new Move(from, to, 'Q'));
                            moves.add(new Move(from, to, 'R'));
                            moves.add(new Move(from, to, 'B'));
                            moves.add(new Move(from, to, 'N'));
                        } else {
                            moves.add(new Move(from, to, null));
                        }
                    }
                }
            }
        }

        return moves;
    }

    private void orderMoves(List<Move> moves, Game game) {
        // Ordena movimentos priorizando capturas e movimentos para o centro
        moves.sort((m1, m2) -> {
            int score1 = getMoveOrderingScore(m1, game);
            int score2 = getMoveOrderingScore(m2, game);
            return Integer.compare(score2, score1); // Ordem decrescente
        });
    }

    private int getMoveOrderingScore(Move move, Game game) {
        int score = 0;

        Piece target = game.board().get(move.to);
        if (target != null) {
            // Capturas têm prioridade
            score += switch (target.getSymbol()) {
                case "Q" -> 900;
                case "R" -> 500;
                case "B", "N" -> 300;
                case "P" -> 100;
                default -> 0;
            };
        }

        // Bônus para movimentos centrais
        int row = move.to.getRow();
        int col = move.to.getColumn();
        if ((row >= 3 && row <= 4) && (col >= 3 && col <= 4)) {
            score += 10;
        }

        return score;
    }

    private Game createGameSnapshot(Game original) {
        // Cria novo jogo e copia o estado
        Game snapshot = new Game();

        // Limpa o tabuleiro do snapshot
        snapshot.board().clear();

        // Copia todas as peças
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position pos = new Position(row, col);
                Piece piece = original.board().get(pos);
                if (piece != null) {
                    Piece copy = piece.copyFor(snapshot.board());
                    snapshot.board().set(pos, copy);
                }
            }
        }

        return snapshot;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDifficultyDescription() {
        return description;
    }

    // Classe auxiliar para representar movimentos
    private static class Move {
        final Position from;
        final Position to;
        final Character promotion;

        Move(Position from, Position to, Character promotion) {
            this.from = from;
            this.to = to;
            this.promotion = promotion;
        }
    }
}
