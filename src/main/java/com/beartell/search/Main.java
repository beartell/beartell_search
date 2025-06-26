package com.beartell.search;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Simple GUI application to scan files and display clusters.
 */
public class Main {
    private JFrame frame;
    private JTextArea textArea;
    private JButton scanButton;
    private JProgressBar progressBar;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().start());
    }

    private void start() {
        frame = new JFrame("Beartell Search");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        JPanel top = new JPanel();
        scanButton = new JButton("Scan Files");
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        top.add(scanButton);
        top.add(searchField);
        top.add(searchButton);
        top.add(progressBar);
        frame.add(top, BorderLayout.NORTH);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        frame.add(new JScrollPane(textArea), BorderLayout.CENTER);

        scanButton.addActionListener(e -> scanAction());
        searchButton.addActionListener(e -> searchAction(searchField.getText()));

        frame.setVisible(true);
    }

    private FileScanner scanner;
    private FileClusterer clusterer;
    private SearchIndex index;

    private void scanAction() {
        textArea.setText("Scanning...\n");
        scanButton.setEnabled(false);
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        progressBar.setValue(0);
        progressBar.setVisible(true);

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                scanner = new FileScanner();
                scanner.scan(p -> publish(p));
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                int p = chunks.get(chunks.size() - 1);
                progressBar.setValue(p);
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                frame.setCursor(Cursor.getDefaultCursor());
                scanButton.setEnabled(true);

                textArea.append("Found " + scanner.getFiles().size() + " files\n");

                clusterer = new FileClusterer();
                List<FileClusterer.Cluster> clusters = clusterer.cluster(scanner.getFiles());
                textArea.append("\nClusters by extension:\n");
                for (FileClusterer.Cluster c : clusters) {
                    textArea.append(c.label + ": " + c.items.size() + " files\n");
                }

                index = new SearchIndex();
                for (FileScanner.FileInfo info : scanner.getFiles()) {
                    index.add(info);
                }
                textArea.append("\nIndex ready. Use search box above.\n");
            }
        };

        worker.execute();
    }

    private void searchAction(String term) {
        if (index == null || term.isEmpty()) {
            return;
        }
        List<FileScanner.FileInfo> results = index.search(term);
        textArea.setText("Search results for '" + term + "':\n");
        for (FileScanner.FileInfo info : results) {
            textArea.append(info.path.toString() + "\n");
        }
        if (results.isEmpty()) {
            textArea.append("No results\n");
        }
    }
}
