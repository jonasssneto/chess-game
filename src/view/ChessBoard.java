package view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ChessBoard extends JPanel {

    // Cores do tabuleiro baseadas na imagem (azul e branco)
    private static final Color LIGHT_SQUARE = new Color(240, 240, 255); // Branco levemente azulado
    private static final Color DARK_SQUARE = new Color(120, 149, 220); // Azul claro/médio
    private static final Color SELECTED_HIGHLIGHT = new Color(255, 255, 0, 140);
    private static final Color LEGAL_MOVE_HIGHLIGHT = new Color(50, 205, 50, 100); // Verde mais suave
    private static final Color LAST_MOVE_HIGHLIGHT = new Color(255, 165, 0, 120);
    private static final Color CHECK_HIGHLIGHT = new Color(255, 70, 70, 140);

    // Configurações visuais
    private static final int BOARD_SIZE = 480;
    private static final int SQUARE_SIZE = BOARD_SIZE / 8;
    private static final Font COORDINATE_FONT = new Font("Segoe UI", Font.BOLD, 11);

    private final JButton[][] squares = new JButton[8][8];
    private final boolean showCoordinates;

    public ChessBoard(boolean showCoordinates) {
        this.showCoordinates = showCoordinates;
        initializeBoard();
    }

    public ChessBoard() {
        this(true);
    }

    private void initializeBoard() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(BOARD_SIZE + 40, BOARD_SIZE + 40));
        setBackground(new Color(45, 45, 45));

        // Painel principal do tabuleiro
        JPanel boardPanel = new JPanel(new GridLayout(8, 8, 0, 0));
        boardPanel.setPreferredSize(new Dimension(BOARD_SIZE, BOARD_SIZE));

        // Cria os quadrados do tabuleiro
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                SquareButton square = new SquareButton(row, col);
                squares[row][col] = square;
                boardPanel.add(square);
            }
        }

        if (showCoordinates) {
            add(createBoardWithCoordinates(boardPanel), BorderLayout.CENTER);
        } else {
            add(boardPanel, BorderLayout.CENTER);
        }
    }

    private JPanel createBoardWithCoordinates(JPanel boardPanel) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(45, 45, 45));
        wrapper.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Coordenadas das colunas (a-h) - topo
        JPanel topCoords = new JPanel(new GridLayout(1, 8));
        topCoords.setOpaque(false);
        topCoords.setPreferredSize(new Dimension(BOARD_SIZE, 20));

        for (int col = 0; col < 8; col++) {
            JLabel label = new JLabel(String.valueOf((char) ('a' + col)), SwingConstants.CENTER);
            label.setFont(COORDINATE_FONT);
            label.setForeground(Color.WHITE);
            topCoords.add(label);
        }

        // Coordenadas das linhas (8-1) - esquerda
        JPanel leftCoords = new JPanel(new GridLayout(8, 1));
        leftCoords.setOpaque(false);
        leftCoords.setPreferredSize(new Dimension(20, BOARD_SIZE));

        for (int row = 0; row < 8; row++) {
            JLabel label = new JLabel(String.valueOf(8 - row), SwingConstants.CENTER);
            label.setFont(COORDINATE_FONT);
            label.setForeground(Color.WHITE);
            leftCoords.add(label);
        }

        wrapper.add(topCoords, BorderLayout.NORTH);
        wrapper.add(leftCoords, BorderLayout.WEST);
        wrapper.add(boardPanel, BorderLayout.CENTER);

        return wrapper;
    }

    public JButton getSquare(int row, int col) {
        return squares[row][col];
    }

    public JButton[][] getAllSquares() {
        return squares;
    }

    /**
     * Botão customizado para representar um quadrado do tabuleiro.
     */
    private static class SquareButton extends JButton {

        private final int row;
        private final int col;
        private final Color baseColor;
        private HighlightType highlightType = HighlightType.NONE;

        public SquareButton(int row, int col) {
            this.row = row;
            this.col = col;
            this.baseColor = (row + col) % 2 == 0 ? LIGHT_SQUARE : DARK_SQUARE;

            initializeSquare();
        }

        private void initializeSquare() {
            setOpaque(true);
            setBackground(baseColor);
            setBorderPainted(false);
            setFocusPainted(false);
            setContentAreaFilled(true);
            setPreferredSize(new Dimension(SQUARE_SIZE, SQUARE_SIZE));

            // Cursor de mão para indicar interatividade
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Efeito hover sutil
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (highlightType == HighlightType.NONE) {
                        setBackground(baseColor.brighter());
                    }
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    updateBackground();
                }
            });
        }

        public void setHighlight(HighlightType type) {
            this.highlightType = type;
            updateBackground();
        }

        private void updateBackground() {
            Color bgColor = switch (highlightType) {
                case SELECTED -> blendColors(baseColor, SELECTED_HIGHLIGHT);
                case LEGAL_MOVE -> blendColors(baseColor, LEGAL_MOVE_HIGHLIGHT);
                case LAST_MOVE -> blendColors(baseColor, LAST_MOVE_HIGHLIGHT);
                case CHECK -> blendColors(baseColor, CHECK_HIGHLIGHT);
                case NONE -> baseColor;
            };
            setBackground(bgColor);
        }

        private Color blendColors(Color base, Color overlay) {
            float alpha = overlay.getAlpha() / 255.0f;
            int red = (int) (overlay.getRed() * alpha + base.getRed() * (1 - alpha));
            int green = (int) (overlay.getGreen() * alpha + base.getGreen() * (1 - alpha));
            int blue = (int) (overlay.getBlue() * alpha + base.getBlue() * (1 - alpha));
            return new Color(red, green, blue);
        }

        public int getBoardRow() {
            return row;
        }

        public int getBoardCol() {
            return col;
        }
    }

    public enum HighlightType {
        NONE, SELECTED, LEGAL_MOVE, LAST_MOVE, CHECK
    }

    public void setSquareHighlight(int row, int col, HighlightType type) {
        if (squares[row][col] instanceof SquareButton square) {
            square.setHighlight(type);
        }
    }

    public void clearAllHighlights() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                setSquareHighlight(row, col, HighlightType.NONE);
            }
        }
    }
}
