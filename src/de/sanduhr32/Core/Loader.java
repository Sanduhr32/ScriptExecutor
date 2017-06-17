package de.sanduhr32.Core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

public class Loader extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textField1;
    private JTextArea textArea;

    public Loader() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        textField1.setText(System.getProperty("user.dir").replaceAll("\\/","\\"));

        pack();
        setVisible(true);
    }

    private void onOK() {
        String s = loadScript(new File(textField1.getText()));
        Core.latestScript = s;

        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private String loadScript(File file) {
        if (!file.exists() || !file.canRead()) {
            return null;
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
