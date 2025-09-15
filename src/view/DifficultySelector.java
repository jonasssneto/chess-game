package view;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class DifficultySelector extends JPanel {

    private static final Color PRIMARY_COLOR = new Color(64, 81, 181);
    private static final Color ACCENT_COLOR = new Color(92, 107, 192);
    private static final Color SUCCESS_COLOR = new Color(76, 175, 80);
    private static final Color WARNING_COLOR = new Color(255, 152, 0);
    private static final Color ERROR_COLOR = new Color(244, 67, 54);

    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font DESCRIPTION_FONT = new Font("Segoe UI", Font.PLAIN, 11);

    private final ButtonGroup difficultyGroup;
    private Consumer<String> onDifficultyChanged;

    public DifficultySelector() {
        this.difficultyGroup = new ButtonGroup();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new GridLayout(1, 3, 12, 0));
        setOpaque(false);
        setBorder(new EmptyBorder(8, 0, 8, 0));

        // Card Fácil
        ModernRadioCard easyCard = new ModernRadioCard(
                "Fácil",
                "Ideal para iniciantes",
                SUCCESS_COLOR,
                true);
        easyCard.setActionCommand("EASY");

        // Card Médio
        ModernRadioCard mediumCard = new ModernRadioCard(
                "Médio",
                "Desafio equilibrado",
                WARNING_COLOR,
                false);
        mediumCard.setActionCommand("MEDIUM");

        // Card Difícil
        ModernRadioCard hardCard = new ModernRadioCard(
                "Difícil",
                "Para jogadores experientes",
                ERROR_COLOR,
                false);
        hardCard.setActionCommand("HARD");

        // Adiciona ao grupo
        difficultyGroup.add(easyCard);
        difficultyGroup.add(mediumCard);
        difficultyGroup.add(hardCard);

        // Listener para mudanças
        ActionListener difficultyListener = e -> {
            if (onDifficultyChanged != null) {
                onDifficultyChanged.accept(e.getActionCommand());
            }
        };

        easyCard.addActionListener(difficultyListener);
        mediumCard.addActionListener(difficultyListener);
        hardCard.addActionListener(difficultyListener);

        add(easyCard);
        add(mediumCard);
        add(hardCard);
    }

    public void setOnDifficultyChanged(Consumer<String> callback) {
        this.onDifficultyChanged = callback;
    }

    public String getSelectedDifficulty() {
        return difficultyGroup.getSelection() != null ? difficultyGroup.getSelection().getActionCommand() : "EASY";
    }

    /**
     * Radio button customizado com visual de card moderno.
     */
    private static class ModernRadioCard extends JRadioButton {

        private final Color themeColor;
        private final String title;
        private final String description;
        private boolean isHovered = false;

        public ModernRadioCard(String title, String description, Color themeColor, boolean selected) {
            super();
            this.title = title;
            this.description = description;
            this.themeColor = themeColor;

            setSelected(selected);
            setOpaque(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Mouse listeners para hover effect
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });

            setPreferredSize(new Dimension(120, 80));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Background
            Color bgColor;
            if (isSelected()) {
                bgColor = themeColor;
            } else if (isHovered) {
                bgColor = new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 30);
            } else {
                bgColor = new Color(250, 250, 250);
            }

            g2d.setColor(bgColor);
            g2d.fillRoundRect(0, 0, width, height, 12, 12);

            // Border
            Color borderColor = isSelected() ? themeColor : new Color(224, 224, 224);
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(isSelected() ? 2 : 1));
            g2d.drawRoundRect(0, 0, width - 1, height - 1, 12, 12);

            // Text
            Color textColor = isSelected() ? Color.WHITE : new Color(66, 66, 66);
            g2d.setColor(textColor);

            // Title
            g2d.setFont(TITLE_FONT);
            FontMetrics titleMetrics = g2d.getFontMetrics();
            int titleWidth = titleMetrics.stringWidth(title);
            int titleX = (width - titleWidth) / 2;
            int titleY = (height / 2) - 5;
            g2d.drawString(title, titleX, titleY);

            // Description
            g2d.setFont(DESCRIPTION_FONT);
            FontMetrics descMetrics = g2d.getFontMetrics();
            int descWidth = descMetrics.stringWidth(description);
            int descX = (width - descWidth) / 2;
            int descY = titleY + 18;
            g2d.drawString(description, descX, descY);

            // Selection indicator (radio dot)
            if (isSelected()) {
                g2d.setColor(Color.WHITE);
                g2d.fillOval(width - 18, 6, 8, 8);
            }

            g2d.dispose();
        }
    }
}
