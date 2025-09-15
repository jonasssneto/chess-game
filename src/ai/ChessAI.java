package ai;

import controller.Game;
import model.board.Position;
import model.pieces.Piece;

/**
 * Interface para implementações de IA de xadrez.
 * Permite diferentes estratégias e níveis de dificuldade.
 */
public interface ChessAI {

    /**
     * Calcula o melhor movimento para o lado especificado.
     * 
     * @param game    Estado atual do jogo
     * @param isWhite true se for vez das brancas, false para pretas
     * @return Melhor movimento encontrado ou null se não houver movimentos
     */
    AIMove getBestMove(Game game, boolean isWhite);

    /**
     * Retorna o nome da implementação da IA.
     */
    String getName();

    /**
     * Retorna a descrição do nível de dificuldade.
     */
    String getDifficultyDescription();
}
