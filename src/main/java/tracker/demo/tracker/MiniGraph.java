package tracker.demo.tracker;

import javax.swing.*;
import java.awt.*;

public class MiniGraph extends JPanel {
    private final java.util.List<Integer> history = new java.util.LinkedList<>();
    private final Color color;

    public MiniGraph(Color color) {
        this.color = color;
        setPreferredSize(new Dimension(0, 40));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        setBackground(new Color(43, 43, 43));
    }

    public void addValue(int val) {
        history.add(val);
        if (history.size() > 50) history.remove(0);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (history.size() < 2) return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);

        int w = getWidth();
        int h = getHeight();
        double xStep = (double) w / 49;

        for (int i = 0; i < history.size() - 1; i++) {
            int y1 = h - (history.get(i) * h / 100);
            int y2 = h - (history.get(i + 1) * h / 100);
            g2.drawLine((int)(i * xStep), y1, (int)((i + 1) * xStep), y2);
        }
    }
}