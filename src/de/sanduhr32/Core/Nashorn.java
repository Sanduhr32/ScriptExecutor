package de.sanduhr32.Core;

import javax.script.ScriptEngineManager;
import javax.swing.*;
import java.awt.*;
import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Nashorn extends JFrame {
    private JPanel contentPane;
    private JProgressBar progressBar1;
    private JLabel loadingLabel;
    private JButton buttonCancel;
    private static String[] arg;

    public Nashorn() {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException e) {
            e.printStackTrace();
        }

        setContentPane(contentPane);

        buttonCancel.addActionListener(e -> System.exit(0));

        Core.scriptEngine = new ScriptEngineManager().getEngineByName("nashorn");

        ScheduledExecutorService exe = Executors.newSingleThreadScheduledExecutor();
        ScheduledExecutorService exe32 = Executors.newSingleThreadScheduledExecutor();

        exe.scheduleAtFixedRate(()-> fill(progressBar1, 1, exe), 20, 30, TimeUnit.MILLISECONDS);

        progressBar1.addChangeListener(e -> {
            switch (progressBar1.getValue()) {
                case 20 : {
                    loadingLabel.setText("Creating Engine...");
                    break;
                }
                case 50 : {
                    loadingLabel.setText("Checking system...");

                    long maxRam = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax() / 1000000;
                    if (maxRam < 1024) {
                        SystemTray tray = SystemTray.getSystemTray();
                        TrayIcon trayIcon = new TrayIcon(new ImageIcon("").getImage());

                        try {
                            tray.add(trayIcon);
                        } catch (AWTException e1) {
                            e1.printStackTrace();
                        }

                        trayIcon.displayMessage("WARNING!","You are running the ScriptExecutor with less then 1GB RAM!\nDont use to big variables, functions or loops!", TrayIcon.MessageType.WARNING);

                        exe32.schedule(()-> tray.remove(trayIcon), 1, TimeUnit.MINUTES);
                    }

                    break;
                }
                case 90 : {
                    loadingLabel.setText("Finishing startup...");
                    break;
                }
            }
        });
    }

    public static void main(String[] args) {
        Nashorn dialog = new Nashorn();
        dialog.pack();
        dialog.setVisible(true);
        arg = args;
    }

    private void fill(JProgressBar progressBar, int val, ScheduledExecutorService exe) {
        if (progressBar.getValue() < 100) {
            progressBar.setValue(progressBar.getValue() + val);
        } else {
            Core.main(arg);
            dispose();
            exe.shutdownNow();
        }
    }
}
