package tracker.demo.tracker;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import java.awt.*;

public class MyToolWindow implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        SystemMonitor monitor = new SystemMonitor();

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton gcButton = new JButton("Zwolnij RAM");
        gcButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        gcButton.addActionListener(e -> {
            monitor.forceGC();
            Messages.showInfoMessage("Wysłano żądanie czyszczenia pamięci (GC)", "System Monitor");
        });
        mainPanel.add(gcButton);
        mainPanel.add(Box.createVerticalStrut(15));

        JProgressBar cpuBar = createBar("Procesor (CPU)");
        MiniGraph cpuGraph = new MiniGraph(new Color(64, 182, 95));

        JProgressBar ramBar = createBar("Pamięć (RAM)");
        MiniGraph ramGraph = new MiniGraph(new Color(64, 128, 182));

        JLabel netLabel = new JLabel("Sieć: 0 KB/s");
        JLabel diskLabel = new JLabel("Dysk: 0 KB/s"); // Nowa etykieta dla dysku

        JTextArea processArea = new JTextArea(5, 20);
        processArea.setEditable(false);
        processArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(processArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Top 3 Procesy (CPU)"));

        mainPanel.add(new JLabel("Obciążenie systemu:"));
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(cpuBar);
        mainPanel.add(cpuGraph);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(ramBar);
        mainPanel.add(ramGraph);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(netLabel);
        mainPanel.add(diskLabel); // Dodanie dysku do UI
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(scrollPane);

        Timer timer = new Timer(1000, e -> {
            try {
                String stats = monitor.getStats();
                String[] parts = stats.split("\\|");

                if (parts.length >= 5) {
                    int cpu = (int) Double.parseDouble(parts[0].replace(",", "."));
                    int ram = Integer.parseInt(parts[1]);

                    cpuBar.setValue(cpu);
                    cpuBar.setString("CPU: " + cpu + "%");
                    cpuBar.setForeground(getUsageColor(cpu));
                    cpuGraph.addValue(cpu);

                    ramBar.setValue(ram);
                    ramBar.setString("RAM: " + parts[2]);
                    ramBar.setForeground(getUsageColor(ram));
                    ramGraph.addValue(ram);

                    netLabel.setText("Sieć: " + parts[3] + " KB/s");
                    diskLabel.setText("Dysk: " + parts[4] + " KB/s");

                    processArea.setText(monitor.getTopProcesses());
                }
            } catch (Exception ex) {
                processArea.setText("Błąd: " + ex.getMessage());
            }
        });
        timer.start();

        Content content = ContentFactory.getInstance().createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    private JProgressBar createBar(String title) {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setStringPainted(true);
        bar.setString(title + ": 0%");
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        return bar;
    }

    private Color getUsageColor(int value) {
        if (value > 90) return new Color(255, 82, 82);
        if (value > 70) return new Color(255, 185, 0);
        return new Color(83, 175, 101);
    }
}