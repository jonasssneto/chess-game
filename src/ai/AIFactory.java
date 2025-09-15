package ai;

/**
 * Factory para criar diferentes implementações de IA.
 * Centraliza a criação dos três níveis de dificuldade.
 */
public class AIFactory {

    public enum Difficulty {
        EASY("Fácil", ""),
        MEDIUM("Médio", ""),
        HARD("Difícil", "");

        private final String displayName;
        private final String description;

        Difficulty(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Cria uma instância de IA baseada no nível de dificuldade.
     */
    public static ChessAI createAI(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> new SimpleAI(
                    "IA Fácil",
                    "Joga movimentos básicos com algumas decisões aleatórias",
                    1, // profundidade mínima
                    0.8 // alta randomness
                );

            case MEDIUM -> new SimpleAI(
                    "IA Médio",
                    "Analisa movimentos básicos e algumas táticas simples",
                    2, // analisa 1-2 movimentos à frente
                    0.3 // baixa randomness
                );

            case HARD -> new MinimaxAI(
                    4, // profundidade 4 (analisa 4 movimentos à frente)
                    "IA Difícil",
                    "Usa algoritmo Minimax avançado com avaliação posicional");
        };
    }

    /**
     * Retorna todas as dificuldades disponíveis.
     */
    public static Difficulty[] getAllDifficulties() {
        return Difficulty.values();
    }
}
