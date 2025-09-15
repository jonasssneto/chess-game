package ai;

import model.board.Position;

/**
 * Representa um movimento da IA com sua avaliação.
 */
public class AIMove {
    private final Position from;
    private final Position to;
    private final Character promotion;
    private final int evaluation;

    public AIMove(Position from, Position to, Character promotion, int evaluation) {
        this.from = from;
        this.to = to;
        this.promotion = promotion;
        this.evaluation = evaluation;
    }

    public AIMove(Position from, Position to, int evaluation) {
        this(from, to, null, evaluation);
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    public Character getPromotion() {
        return promotion;
    }

    public int getEvaluation() {
        return evaluation;
    }

    @Override
    public String toString() {
        return String.format("AIMove{%s->%s, eval=%d%s}",
                from, to, evaluation,
                promotion != null ? ", promo=" + promotion : "");
    }
}
