package de.sanduhr32.Core;

import com.sun.management.OperatingSystemMXBean;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SysView extends JFrame {
    private JPanel contentPane;
    private JButton buttonCancel;
    private JProgressBar systemBar;
    private JProgressBar processBar;
    private JLabel processLabel;
    private JLabel systemLabel;
    private ScheduledExecutorService exe = Executors.newSingleThreadScheduledExecutor();
    static FileOutputStream outStream;

    public SysView() {
        try {
            outStream = new FileOutputStream(new File(".\\cpu.log"),true);
            outStream.write(("\r\n\r\n------------CPU Log started------------\r\n\r\n").getBytes());
            outStream.write(("Current time: " + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.YYYY HH:mm:ss")) + "\r\n\r\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonCancel);

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        exe.scheduleAtFixedRate(this::update, 0, (long) 1.5, TimeUnit.SECONDS);

        pack();
        setVisible(true);
    }

    private void onCancel() {
        exe.shutdownNow();
        dispose();
    }

    private void update() {
        double sysdouble = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getSystemCpuLoad();
        double prodouble = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getProcessCpuLoad();
        Long syslong = Math.round(sysdouble * 100);
        Long prolong = Math.round(prodouble * 100);
        String sysstring = new DecimalFormat("###.#%").format(sysdouble);
        String prostring = new DecimalFormat("###.#%").format(prodouble);
        systemBar.setValue(syslong.intValue());
        processBar.setValue(prolong.intValue());
        processLabel.setText(prostring);
        systemLabel.setText(sysstring);

        try {
            outStream.write((sysstring + " // " + prostring + "\r\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
