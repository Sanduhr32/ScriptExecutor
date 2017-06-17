package de.sanduhr32.Core;

import com.sun.management.OperatingSystemMXBean;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class Core extends JFrame {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea scriptHereTextArea;
    private JButton loadButton;
    private JButton rerunButton;
    private JButton saveButton;
    private JButton systemButton;
    static ScriptEngine scriptEngine;
    static String latestScript;
    static String saveScript;

    public Core() {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException e) {
            e.printStackTrace();
        }

        initSE();
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        setTitle("Scriptexecutor");

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        rerunButton.addActionListener(e -> execute(latestScript));

        saveButton.addActionListener(e -> new Save());

        loadButton.addActionListener(e -> {
            Loader l = new Loader();
            l.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    scriptHereTextArea.setText(latestScript);
                }
            });
        });

        scriptHereTextArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                saveScript = scriptHereTextArea.getText().replaceAll("\n","\r\n");
            }
        });

        systemButton.addActionListener(e -> new Thread(()->{
            SysView sys = new SysView();
            sys.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                        try {
                            SysView.outStream.write(("\r\n\r\n------------CPU Log closed------------\r\n\r\n").getBytes());
                            SysView.outStream.write(("Current time: " + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.YYYY HH:mm:ss"))).getBytes());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
            });
            }).start()
        );

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        contentPane.registerKeyboardAction(e -> new Thread(()->execute(latestScript)), KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        latestScript = scriptHereTextArea.getText().replaceAll("\n","\r\n");
        saveScript = latestScript;
        new Thread(()->execute(latestScript)).start();
    }

    private void onCancel() {
        System.exit(0);
    }

    public static void main(String[] args) {
        Core dialog = new Core();
        dialog.setSize(600, 400);
        dialog.setVisible(true);
    }

    private void initSE() {
        File importer = new File(".\\imports.txt");
        scriptEngine.put("OSMXBean",ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class));
        scriptEngine.put("se", scriptEngine);
        try {
            scriptEngine.eval("var imports = new JavaImporter(" +
                                    loadConfig(importer) +
                                    ")");
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    private void writeGenScript(String path, String name) {
        String now = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("dd_MM_YYYY__HH_mm_ss"));

        writeScript(new File(path+name+now+".txt"));

    }

    private void writeScript(File file) {

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file);
            fileWriter.write(latestScript);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileWriter !=  null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void execute(String script) {

        new Thread(()->writeGenScript(".\\","Script")).start();

        Object obj;

        if (script.isEmpty() || script == null) return;

        try {
            obj = scriptEngine.eval(
                    "{" +
                            "with (imports) {" +
                            script +
                            "}" +
                            "};");
        } catch (ScriptException e) {
            obj = e;
        }

        if (obj == null) return;

        Result r = new Result();
        r.showResult(obj.toString());
    }

    private String loadConfig(File file) {
        if (!file.exists() || !file.canRead()) {
            FileWriter fw = null;
            try {
                fw = new FileWriter(file);
                fw.write("java.awt,\r\n" +
                        "java.io,\r\n" +
                        "java.lang,\r\n" +
                        "java.lang.management,\r\n" +
                        "java.math,\r\n" +
                        "java.nio,\r\n" +
                        "java.sql,\r\n" +
                        "java.text,\r\n" +
                        "java.time,\r\n" +
                        "java.util\r\n" );
            } catch (IOException ignored) {
            } finally {
                if (fw != null) {
                    try {
                        fw.close();
                    } catch (IOException ignored) {
                    }
                }
            }
            System.out.println("CREATING IMPORTS, PROGRAMM STOPPED");
            System.exit(-1);
        }
        List<String> fileLines = null;
        try {
            fileLines = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (fileLines != null) ? (!fileLines.isEmpty()) ? fileLines.stream().collect(Collectors.joining("\n")) : null : null;
    }
}
