package com.beartell.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Very simple clustering based on file extension.
 * Each extension becomes a cluster of files.
 */
public class FileClusterer {

    public static class Cluster {
        public final String label;
        public final List<FileScanner.FileInfo> items;
        public Cluster(String label, List<FileScanner.FileInfo> items) {
            this.label = label;
            this.items = items;
        }
    }

    public List<Cluster> cluster(List<FileScanner.FileInfo> files) {
        Map<String, List<FileScanner.FileInfo>> map = new HashMap<>();
        for (FileScanner.FileInfo info : files) {
            map.computeIfAbsent(info.extension.isEmpty() ? "<no-ext>" : info.extension, k -> new ArrayList<>()).add(info);
        }
        return map.entrySet().stream()
                .map(e -> new Cluster(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }
}
