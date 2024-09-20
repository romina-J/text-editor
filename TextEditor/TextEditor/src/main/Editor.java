package main;

import javax.swing.*;

import piecelist.PieceListText;
import viewer.Viewer;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;

public class Editor {

    public static java.util.List<JFrame> frames = new ArrayList<JFrame>();
    public static boolean bUpdating = false;

    public static void removeFrame(JFrame frame) {
        frames.remove(frame);
        if (frames.size() == 0) {
            System.exit(0);
        }
    }

    public static void main(String[] arg) {
        final FileDialog fileDialog = new FileDialog(new Frame(), "Select file");
        fileDialog.setVisible(true);

        showFrame(fileDialog.getDirectory() + fileDialog.getFile());
    }

    private static void showFrame(String path) {
        File buff = new File(path);

        //scrollbar
        JScrollBar scrollBar = new JScrollBar(Adjustable.VERTICAL, 0, 0, 0, 0);
        Viewer viewer = new Viewer(new PieceListText(path, "Arial", 11, 0), scrollBar, "Arial", 11, 0);
        JFrame frame = new JFrame();
        JMenuBar menuBar;
        JMenu fileMenu, editMenu;
        JMenuItem openFileMenuItem, saveFileMenuItem;
        JMenuItem cutMenuItem, copyMenuItem, pasteMenuItem, searchMenuItem;

        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");

        openFileMenuItem = new JMenuItem("Open File");
        openFileMenuItem.addActionListener(e -> {
            final FileDialog fileDialog = new FileDialog(frame, "Select file");
            fileDialog.setVisible(true);
            showFrame(fileDialog.getDirectory() + fileDialog.getFile());
        });
        saveFileMenuItem = new JMenuItem("Save");
        saveFileMenuItem.addActionListener(e -> viewer.save());
        fileMenu.add(openFileMenuItem);
        fileMenu.add(saveFileMenuItem);
        menuBar.add(fileMenu);

        editMenu = new JMenu("Edit");
        cutMenuItem = new JMenuItem("Cut");
        cutMenuItem.addActionListener(e -> viewer.cut());
        copyMenuItem = new JMenuItem("Copy");
        copyMenuItem.addActionListener(e -> viewer.copy());
        pasteMenuItem = new JMenuItem("Paste");
        pasteMenuItem.addActionListener(e -> viewer.paste());
        searchMenuItem = new JMenuItem("Search");
        searchMenuItem.addActionListener(e -> viewer.search());
        editMenu.add(cutMenuItem);
        editMenu.add(copyMenuItem);
        editMenu.add(pasteMenuItem);
        editMenu.addSeparator();
        editMenu.add(searchMenuItem);
        menuBar.add(editMenu);

        JToolBar toolBar;
        JButton newButton, saveButton;
        JCheckBox boldCheckBox, italicCheckBox;
        JComboBox<String> fontComboBox, sizeComboBox;

        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        newButton = new JButton("Open File");
        newButton.addActionListener(e -> {
            final FileDialog fileDialog = new FileDialog(frame, "Select file");
            fileDialog.setVisible(true);
            showFrame(fileDialog.getDirectory() + fileDialog.getFile());
        });
        saveButton = new JButton("Save File");
        saveButton.addActionListener(e -> viewer.save());
        boldCheckBox = new JCheckBox("Bold");
        boldCheckBox.addItemListener(e -> {
            if (bUpdating) return;
            viewer.setFontStyle(viewer.getFontStyle() ^ Font.BOLD);
        });
        italicCheckBox = new JCheckBox("Italic");
        italicCheckBox.addItemListener(e -> {
            if (bUpdating) return;
            viewer.setFontStyle(viewer.getFontStyle() ^ Font.ITALIC);
        });
        String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        fontComboBox = new JComboBox<>(fonts);
        fontComboBox.setSelectedItem("Arial");
        fontComboBox.addActionListener(event -> {
            if (bUpdating) return;

            JComboBox comboBox = (JComboBox) event.getSource();
            String font = (String) comboBox.getSelectedItem();
            viewer.setFont(font);
        });
        sizeComboBox = new JComboBox<String>(new String[]{"8", "9", "10", "11", "12", "14", "16", "18", "20", "22"});
        sizeComboBox.setSelectedItem("12");
        sizeComboBox.addActionListener(event -> {
            if (bUpdating) return;
            JComboBox comboBox = (JComboBox) event.getSource();
            String size = (String) comboBox.getSelectedItem();
            viewer.setFontSize(Integer.valueOf(size));
        });

        viewer.addFontEventListener(new FontUpdater(fontComboBox, sizeComboBox, boldCheckBox, italicCheckBox));
        sizeComboBox.setPreferredSize(new Dimension(60, -1));
        sizeComboBox.setMaximumSize(new Dimension(60, 100));

        toolBar.add(newButton);
        toolBar.add(saveButton);
        toolBar.addSeparator();
        toolBar.add(fontComboBox);
        toolBar.add(sizeComboBox);

        toolBar.add(boldCheckBox);
        toolBar.add(italicCheckBox);


        JPanel panel = new JPanel(new BorderLayout());
        panel.add(toolBar, BorderLayout.PAGE_START);
        panel.add(viewer, BorderLayout.CENTER);
        panel.add(scrollBar, BorderLayout.EAST);


        frame.setTitle(buff.getName());
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                removeFrame(frame);
            }
        });
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent componentEvent) {

            }
        });
        frame.setSize(500, 600);
        frame.setResizable(true);
        frame.setJMenuBar(menuBar);
        frame.setContentPane(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.getContentPane().repaint();
        viewer.requestFocus();
        frames.add(frame);
    }


}

