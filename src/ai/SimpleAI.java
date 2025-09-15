package ai;

import controller.Game;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import model.board.Position;
import model.pieces.Pawn;
import model.pieces.Piece;

/**
 * IA simples que usa heurísticas básicas.
 * Representa os níveis Fácil e Médio.
 */
public class SimpleAI implements ChessAI {

    private final PositionEvaluator evaluator;
    private final Random random;
    private final String name;
    private final String description;
    private final int lookAheadDepth;
    private final double randomnessFactor;

    public SimpleAI(String name, String description, int lookAheadDepth, double randomnessFactor) {
        this.evaluator = new PositionEvaluator();
        this.random = new Random();
        this.name = name;
        this.description = description;
        this.lookAheadDepth = lookAheadDepth;
        this.randomnessFactor = randomnessFactor;
    }

    @Override
    public AIMove getBestMove(Game game, boolean isWhite) {
        if (game.isGameOver())
            return null;

        List<Move> allMoves = generateAllMoves(game, isWhite);
        if (allMoves.isEmpty())
            return null;

        List<Move> bestMoves = new ArrayList<>();
        int bestScore = isWhite ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (Move move : allMoves) {
            int score = evaluateMove(move, game, isWhite);

            // Adiciona randomness baseada no fator configurado
            if (randomnessFactor > 0) {
                int randomBonus = (int) (random.nextGaussian() * randomnessFactor * 100);
                score += randomBonus;
            }

            if (isWhite && score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (isWhite && score == bestScore) {
                bestMoves.add(move);
            } else if (!isWhite && score < bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (!isWhite && score == bestScore) {
                bestMoves.add(move);
            }
        }

        // Escolhe aleatoriamente entre os melhores movimentos
        Move chosen = bestMoves.get(random.nextInt(bestMoves.size()));
        return new AIMove(chosen.from, chosen.to, chosen.promotion, bestScore);
    }

    private int evaluateMove(Move move, Game game, boolean isWhite) {
        // Cria snapshot e executa o movimento
        Game tempGame = createSimpleSnapshot(game);
        tempGame.move(move.from, move.to, move.promotion);

        if (lookAheadDepth <= 1) {
            return evaluator.evaluate(tempGame);
        }

        // Look-ahead simples (1 movimento à frente)
        List<Move> opponentMoves = generateAllMoves(tempGame, !isWhite);
        if (opponentMoves.isEmpty()) {
            return evaluator.evaluate(tempGame);
        }

        int bestOpponentScore = !isWhite ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (Move opponentMove : opponentMoves) {
            Game tempGame2 = createSimpleSnapshot(tempGame);
            tempGame2.move(opponentMove.from, opponentMove.to, opponentMove.promotion);

            int score = evaluator.evaluate(tempGame2);
            if (!isWhite && score > bestOpponentScore) {
                bestOpponentScore = score;
            } else if (isWhite && score < bestOpponentScore) {
                bestOpponentScore = score;
            }
        }

        return bestOpponentScore;
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
                            // Para IA simples, sempre promove para Rainha
                            moves.add(new Move(from, to, 'Q'));
                        } else {
                            moves.add(new Move(from, to, null));
                        }
                    }
                }
            }
        }

        return moves;
    }

    private Game createSimpleSnapshot(Game original) {
        // Versão simplificada do snapshot para IA básica
        Game snapshot = new Game();
        snapshot.board().clear();

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

    // Classe auxiliar para movimentos
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
