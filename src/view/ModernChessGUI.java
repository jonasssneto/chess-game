package view;

import ai.*;
import controller.*;
import model.board.*;
import model.pieces.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class ModernChessGUI extends JFrame {

    // Cores do tema
    private static final Color BACKGROUND_COLOR = new Color(30, 30, 30);
    private static final Color PANEL_COLOR = new Color(45, 45, 45);
    private static final Color TEXT_COLOR = new Color(220, 220, 220);
    private static final Color ACCENT_COLOR = new Color(64, 81, 181);

    // Componentes principais
    private final Game game;
    private final ModernChessBoard chessBoard;
    private final JLabel statusLabel;
    private final JTextArea historyArea;
    private final DifficultySelector difficultySelector;
    private final JCheckBox aiEnabledCheckbox;
    private final JLabel aiThinkingLabel;

    // Estado da interface
    private Position selectedSquare = null;
    private List<Position> legalMoves = new ArrayList<>();
    private Position lastMoveFrom = null;
    private Position lastMoveTo = null;

    // IA
    private ChessAI currentAI;
    private boolean aiThinking = false;
    private SwingWorker<Void, Void> aiWorker = null;

    public ModernChessGUI() {
        this.game = new Game();
        this.chessBoard = new ModernChessBoard(true);
        this.statusLabel = new JLabel("Vez das Brancas");
        this.historyArea = new JTextArea(20, 25);
        this.difficultySelector = new DifficultySelector();
        this.aiEnabledCheckbox = new JCheckBox("Jogar contra IA", false);
        this.aiThinkingLabel = new JLabel("");

        // Inicializa com IA fácil por padrão
        this.currentAI = AIFactory.createAI(AIFactory.Difficulty.EASY);

        initializeUI();
        setupEventHandlers();
        updateBoard();
    }

    private void initializeUI() {
        setTitle("Chess");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(BACKGROUND_COLOR);

        // Layout principal
        setLayout(new BorderLayout(15, 15));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Painel do tabuleiro (centro)
        add(chessBoard, BorderLayout.CENTER);

        // Painel direito (controles e histórico)
        JPanel rightPanel = createRightPanel();
        add(rightPanel, BorderLayout.EAST);

        // Barra de status (inferior)
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);

        // Menu
        setJMenuBar(createMenuBar());

        // Configurações da janela
        pack();
        setLocationRelativeTo(null);
        setMinimumSize(getSize());

        // Look and Feel
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            // Fallback para default L&F
        }
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setPreferredSize(new Dimension(320, 0));

        // Controles superiores
        JPanel controlsPanel = createControlsPanel();
        panel.add(controlsPanel, BorderLayout.NORTH);

        // Histórico de movimentos
        JPanel historyPanel = createHistoryPanel();
        panel.add(historyPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createControlsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70), 1),
                new EmptyBorder(15, 15, 15, 15)));

        // Título
        JLabel titleLabel = new JLabel("Controles do Jogo");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);

        panel.add(Box.createVerticalStrut(15));

        // Checkbox IA
        aiEnabledCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        aiEnabledCheckbox.setForeground(TEXT_COLOR);
        aiEnabledCheckbox.setOpaque(false);
        aiEnabledCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(aiEnabledCheckbox);

        panel.add(Box.createVerticalStrut(10));

        // Seletor de dificuldade
        JLabel diffLabel = new JLabel("Dificuldade da IA:");
        diffLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        diffLabel.setForeground(TEXT_COLOR);
        diffLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(diffLabel);

        panel.add(Box.createVerticalStrut(5));

        difficultySelector.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(difficultySelector);

        panel.add(Box.createVerticalStrut(15));

        // Status da IA
        aiThinkingLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        aiThinkingLabel.setForeground(ACCENT_COLOR);
        aiThinkingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(aiThinkingLabel);

        panel.add(Box.createVerticalStrut(15));

        // Painel de botões com grid layout
        JPanel buttonsGrid = createButtonsGrid();
        panel.add(buttonsGrid);

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70), 1),
                new EmptyBorder(15, 15, 15, 15)));

        // Título
        JLabel titleLabel = new JLabel("Histórico de Movimentos");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_COLOR);
        panel.add(titleLabel, BorderLayout.NORTH);

        // Área de texto
        historyArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        historyArea.setBackground(new Color(35, 35, 35));
        historyArea.setForeground(TEXT_COLOR);
        historyArea.setEditable(false);
        historyArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scrollPane = new JScrollPane(historyArea);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(35, 35, 35));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70), 1),
                new EmptyBorder(10, 15, 10, 15)));

        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(TEXT_COLOR);
        panel.add(statusLabel, BorderLayout.WEST);

        return panel;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(PANEL_COLOR);
        menuBar.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70), 1));

        // Menu Jogo
        JMenu gameMenu = new JMenu("Jogo");
        gameMenu.setForeground(TEXT_COLOR);

        JMenuItem newGameItem = new JMenuItem("Novo Jogo");
        newGameItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        newGameItem.addActionListener(e -> startNewGame());

        JMenuItem exitItem = new JMenuItem("Sair");
        exitItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        exitItem.addActionListener(e -> System.exit(0));

        gameMenu.add(newGameItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);

        // Menu Ajuda
        JMenu helpMenu = new JMenu("Ajuda");
        helpMenu.setForeground(TEXT_COLOR);

        JMenuItem aboutItem = new JMenuItem("Sobre");
        aboutItem.addActionListener(e -> showAboutDialog());

        helpMenu.add(aboutItem);

        menuBar.add(gameMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private void setupEventHandlers() {
        // Eventos do tabuleiro
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                final int r = row;
                final int c = col;
                chessBoard.getSquare(row, col).addActionListener(e -> handleSquareClick(r, c));
            }
        }

        // Eventos da seleção de dificuldade
        difficultySelector.setOnDifficultyChanged(difficulty -> {
            AIFactory.Difficulty diff = switch (difficulty) {
                case "EASY" -> AIFactory.Difficulty.EASY;
                case "MEDIUM" -> AIFactory.Difficulty.MEDIUM;
                case "HARD" -> AIFactory.Difficulty.HARD;
                default -> AIFactory.Difficulty.EASY;
            };
            currentAI = AIFactory.createAI(diff);
        });

        // Evento checkbox IA
        aiEnabledCheckbox.addActionListener(e -> {
            if (aiEnabledCheckbox.isSelected() && !game.whiteToMove() && !aiThinking) {
                triggerAIMove();
            }
        });
    }

    private void handleSquareClick(int row, int col) {
        if (game.isGameOver() || aiThinking)
            return;

        // Se IA está ativa e é vez das pretas, ignora cliques
        if (aiEnabledCheckbox.isSelected() && !game.whiteToMove())
            return;

        Position clickedPos = new Position(row, col);
        Piece clickedPiece = game.board().get(clickedPos);

        if (selectedSquare == null) {
            // Nenhuma peça selecionada - seleciona se for peça da vez
            if (clickedPiece != null && clickedPiece.isWhite() == game.whiteToMove()) {
                selectedSquare = clickedPos;
                legalMoves = game.legalMovesFrom(selectedSquare);
                updateBoardHighlights();
            }
        } else {
            // Já tem peça selecionada
            if (legalMoves.contains(clickedPos)) {
                // Movimento legal - executa
                executeMove(selectedSquare, clickedPos);
            } else if (clickedPiece != null && clickedPiece.isWhite() == game.whiteToMove()) {
                // Troca seleção para outra peça da vez
                selectedSquare = clickedPos;
                legalMoves = game.legalMovesFrom(selectedSquare);
                updateBoardHighlights();
            } else {
                // Clique inválido - limpa seleção
                clearSelection();
            }
        }
    }

    private void executeMove(Position from, Position to) {
        Piece movingPiece = game.board().get(from);
        Character promotion = null;

        // Verifica promoção
        if (movingPiece instanceof Pawn && game.isPromotion(from, to)) {
            promotion = askForPromotion();
        }

        // Executa movimento
        game.move(from, to, promotion);

        // Atualiza estado visual
        lastMoveFrom = from;
        lastMoveTo = to;
        clearSelection();
        updateBoard();
        checkGameEnd();

        // Trigger IA se necessário
        if (aiEnabledCheckbox.isSelected() && !game.whiteToMove() && !game.isGameOver()) {
            triggerAIMove();
        }
    }

    private Character askForPromotion() {
        String[] options = { "Rainha", "Torre", "Bispo", "Cavalo" };
        int choice = JOptionPane.showOptionDialog(
                this,
                "Escolha a peça para promoção:",
                "Promoção de Peão",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        return switch (choice) {
            case 1 -> 'R';
            case 2 -> 'B';
            case 3 -> 'N';
            default -> 'Q';
        };
    }

    private void triggerAIMove() {
        if (aiThinking || currentAI == null)
            return;

        aiThinking = true;
        aiThinkingLabel.setText("IA pensando...");
        statusLabel.setText("Vez das Pretas - IA pensando");

        aiWorker = new SwingWorker<Void, Void>() {
            private ai.AIMove aiMove;

            @Override
            protected Void doInBackground() throws Exception {
                // Pequena pausa para melhorar a experiência do usuário
                Thread.sleep(500);
                aiMove = currentAI.getBestMove(game, false);
                return null;
            }

            @Override
            protected void done() {
                aiThinking = false;
                aiThinkingLabel.setText("");

                if (aiMove != null && !game.isGameOver()) {
                    game.move(aiMove.getFrom(), aiMove.getTo(), aiMove.getPromotion());
                    lastMoveFrom = aiMove.getFrom();
                    lastMoveTo = aiMove.getTo();
                    updateBoard();
                    checkGameEnd();
                }

                aiWorker = null;
            }
        };

        aiWorker.execute();
    }

    private void clearSelection() {
        selectedSquare = null;
        legalMoves.clear();
        updateBoardHighlights();
    }

    private void updateBoard() {
        // Atualiza ícones das peças
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position pos = new Position(row, col);
                Piece piece = game.board().get(pos);
                JButton square = chessBoard.getSquare(row, col);

                if (piece == null) {
                    square.setIcon(null);
                    square.setText("");
                } else {
                    // Tenta carregar ícone da peça
                    char symbol = piece.getSymbol().charAt(0);
                    int iconSize = computeIconSize();
                    ImageIcon icon = ImageUtil.getPieceIcon(piece.isWhite(), symbol, iconSize);

                    if (icon != null) {
                        square.setIcon(icon);
                        square.setText("");
                    } else {
                        // Fallback para Unicode
                        square.setIcon(null);
                        square.setText(getUnicodeSymbol(piece));
                        square.setFont(new Font("Segoe UI Emoji", Font.PLAIN, iconSize / 2));
                    }
                }
            }
        }

        updateBoardHighlights();
        updateStatus();
        updateHistory();
    }

    private void updateBoardHighlights() {
        // Limpa todos os highlights
        chessBoard.clearAllHighlights();

        // Última jogada
        if (lastMoveFrom != null) {
            chessBoard.setSquareHighlight(lastMoveFrom.getRow(), lastMoveFrom.getColumn(),
                    ModernChessBoard.HighlightType.LAST_MOVE);
        }
        if (lastMoveTo != null) {
            chessBoard.setSquareHighlight(lastMoveTo.getRow(), lastMoveTo.getColumn(),
                    ModernChessBoard.HighlightType.LAST_MOVE);
        }

        // Peça selecionada
        if (selectedSquare != null) {
            chessBoard.setSquareHighlight(selectedSquare.getRow(), selectedSquare.getColumn(),
                    ModernChessBoard.HighlightType.SELECTED);

            // Movimentos legais
            for (Position move : legalMoves) {
                chessBoard.setSquareHighlight(move.getRow(), move.getColumn(),
                        ModernChessBoard.HighlightType.LEGAL_MOVE);
            }
        }

        // Xeque
        if (game.inCheck(game.whiteToMove())) {
            // Encontra e destaca o rei em xeque
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    Position pos = new Position(row, col);
                    Piece piece = game.board().get(pos);
                    if (piece != null && piece.getSymbol().equals("K") &&
                            piece.isWhite() == game.whiteToMove()) {
                        chessBoard.setSquareHighlight(row, col, ModernChessBoard.HighlightType.CHECK);
                        break;
                    }
                }
            }
        }
    }

    private void updateStatus() {
        if (game.isGameOver()) {
            if (game.isCheckmate(true)) {
                statusLabel.setText("Xeque-mate! Pretas vencem!");
            } else if (game.isCheckmate(false)) {
                statusLabel.setText("Xeque-mate! Brancas vencem!");
            } else {
                statusLabel.setText("Empate!");
            }
        } else {
            String turn = game.whiteToMove() ? "Brancas" : "Pretas";
            String check = game.inCheck(game.whiteToMove()) ? " - Xeque!" : "";
            statusLabel.setText("Vez das " + turn + check);
        }
    }

    private void updateHistory() {
        StringBuilder sb = new StringBuilder();
        List<String> history = game.history();

        for (int i = 0; i < history.size(); i++) {
            if (i % 2 == 0) {
                sb.append(String.format("%d. ", (i / 2) + 1));
            }
            sb.append(history.get(i));
            if (i % 2 == 0) {
                sb.append(" ");
            } else {
                sb.append("\n");
            }
        }

        historyArea.setText(sb.toString());
        historyArea.setCaretPosition(historyArea.getDocument().getLength());
    }

    private void checkGameEnd() {
        if (game.isGameOver()) {
            String message;
            if (game.isCheckmate(true)) {
                message = "Xeque-mate! As pretas venceram!";
            } else if (game.isCheckmate(false)) {
                message = "Xeque-mate! As brancas venceram!";
            } else {
                message = "Empate por afogamento!";
            }

            JOptionPane.showMessageDialog(this, message, "Fim de Jogo",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void startNewGame() {
        // Cancela IA se estiver pensando
        if (aiWorker != null && !aiWorker.isDone()) {
            aiWorker.cancel(true);
        }

        // Reinicia o jogo
        game.newGame();
        selectedSquare = null;
        legalMoves.clear();
        lastMoveFrom = null;
        lastMoveTo = null;
        aiThinking = false;
        aiThinkingLabel.setText("");

        updateBoard();
    }

    private void showAboutDialog() {
        String message = """
                Xadrez Moderno v2.0

                Desenvolvido por Jonas
                Interface moderna com IA avançada

                Funcionalidades:
                • IA com 3 níveis de dificuldade
                • Interface gráfica moderna
                • Algoritmo Minimax com poda Alfa-Beta
                • Todas as regras do xadrez implementadas

                Pressione F1 para este diálogo
                Ctrl+N para novo jogo
                Ctrl+Q para sair
                """;

        JOptionPane.showMessageDialog(this, message, "Sobre o Xadrez Moderno",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createButtonsGrid() {
        // Container principal com título
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(PANEL_COLOR);

        // Título dos controles
        JLabel controlsTitle = new JLabel("Controles do Jogo");
        controlsTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        controlsTitle.setForeground(TEXT_COLOR);
        controlsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(controlsTitle);
        
        container.add(Box.createVerticalStrut(10));

        // Grid de botões
        JPanel buttonsPanel = new JPanel(new GridLayout(2, 2, 12, 8));
        buttonsPanel.setBackground(PANEL_COLOR);
        buttonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Linha 1: Novo Jogo e Desistir
        ModernButton newGameButton = new ModernButton("Novo Jogo", ModernButton.ButtonStyle.SUCCESS);
        newGameButton.addActionListener(e -> startNewGame());
        buttonsPanel.add(newGameButton);

        ModernButton surrenderButton = new ModernButton("Desistir", ModernButton.ButtonStyle.DANGER);
        surrenderButton.addActionListener(e -> surrenderGame());
        buttonsPanel.add(surrenderButton);

        // Linha 2: Salvar e Carregar
        ModernButton saveButton = new ModernButton("Salvar", ModernButton.ButtonStyle.PRIMARY);
        saveButton.addActionListener(e -> saveGame());
        buttonsPanel.add(saveButton);

        ModernButton loadButton = new ModernButton("Carregar", ModernButton.ButtonStyle.WARNING);
        loadButton.addActionListener(e -> loadGame());
        buttonsPanel.add(loadButton);

        container.add(buttonsPanel);
        return container;
    }

    private int computeIconSize() {
        JButton sampleSquare = chessBoard.getSquare(0, 0);
        int squareSize = Math.min(sampleSquare.getWidth(), sampleSquare.getHeight());
        return Math.max(32, squareSize - 8);
    }

    private String getUnicodeSymbol(Piece piece) {
        boolean isWhite = piece.isWhite();
        return switch (piece.getSymbol()) {
            case "K" -> isWhite ? "♔" : "♚";
            case "Q" -> isWhite ? "♕" : "♛";
            case "R" -> isWhite ? "♖" : "♜";
            case "B" -> isWhite ? "♗" : "♝";
            case "N" -> isWhite ? "♘" : "♞";
            case "P" -> isWhite ? "♙" : "♟";
            default -> "?";
        };
    }

    private void surrenderGame() {
        if (game.isGameOver()) {
            JOptionPane.showMessageDialog(this, "O jogo já terminou!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja desistir do jogo?",
                "Confirmar Desistência",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Cancela IA se estiver pensando
            if (aiWorker != null && !aiWorker.isDone()) {
                aiWorker.cancel(true);
            }

            String winner = game.whiteToMove() ? "Pretas" : "Brancas";
            JOptionPane.showMessageDialog(this,
                    winner + " vencem por desistência!\n\nIniciando novo jogo...",
                    "Jogo Finalizado",
                    JOptionPane.INFORMATION_MESSAGE);

            // Recomeça automaticamente um novo jogo
            startNewGame();
        }
    }

    private void saveGame() {
        JOptionPane.showMessageDialog(this,
                "Funcionalidade de salvar jogo ainda não implementada\nna versão moderna da interface.",
                "Em Desenvolvimento",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void loadGame() {
        JOptionPane.showMessageDialog(this,
                "Funcionalidade de carregar jogo ainda não implementada\nna versão moderna da interface.",
                "Em Desenvolvimento",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ModernChessGUI().setVisible(true);
        });
    }
}
