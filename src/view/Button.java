package view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Button extends JButton {

    private static final Color PRIMARY_COLOR = new Color(64, 81, 181);
    private static final Color PRIMARY_HOVER = new Color(92, 107, 192);
    private static final Color SUCCESS_COLOR = new Color(76, 175, 80);
    private static final Color SUCCESS_HOVER = new Color(102, 187, 106);
    private static final Color WARNING_COLOR = new Color(255, 152, 0);
    private static final Color WARNING_HOVER = new Color(255, 167, 38);
    private static final Color DANGER_COLOR = new Color(244, 67, 54);
    private static final Color DANGER_HOVER = new Color(229, 115, 115);

    public enum ButtonStyle {
        PRIMARY(PRIMARY_COLOR, PRIMARY_HOVER),
        SUCCESS(SUCCESS_COLOR, SUCCESS_HOVER),
        WARNING(WARNING_COLOR, WARNING_HOVER),
        DANGER(DANGER_COLOR, DANGER_HOVER);

        private final Color normalColor;
        private final Color hoverColor;

        ButtonStyle(Color normalColor, Color hoverColor) {
            this.normalColor = normalColor;
            this.hoverColor = hoverColor;
        }
    }

    private final ButtonStyle style;
    private boolean isHovered = false;
    private boolean isPressed = false;

    public Button(String text, ButtonStyle style) {
        super(text);
        this.style = style;

        initializeButton();
    }

    public Button(String text) {
        this(text, ButtonStyle.PRIMARY);
    }

    private void initializeButton() {
        setFont(new Font("Segoe UI", Font.BOLD, 12));
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(new EmptyBorder(10, 20, 10, 20));

        // Mouse listeners para efeitos visuais
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Determina a cor baseada no estado
        Color bgColor;
        if (!isEnabled()) {
            bgColor = new Color(200, 200, 200);
        } else if (isPressed) {
            bgColor = style.normalColor.darker();
        } else if (isHovered) {
            bgColor = style.hoverColor;
        } else {
            bgColor = style.normalColor;
        }

        // Gradiente sutil
        GradientPaint gradient = new GradientPaint(
                0, 0, bgColor,
                0, height, bgColor.darker());
        g2d.setPaint(gradient);
        g2d.fillRoundRect(0, 0, width, height, 8, 8);

        // Sombra interna quando pressionado
        if (isPressed) {
            g2d.setColor(new Color(0, 0, 0, 50));
            g2d.fillRoundRect(2, 2, width - 4, height - 4, 6, 6);
        }

        g2d.dispose();

        // Desenha o texto
        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        return new Dimension(Math.max(size.width, 100), Math.max(size.height, 36));
    }
}
