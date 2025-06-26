package com.beartell.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple search index using a map from tokens to file info list.
 */
public class SearchIndex {
    private final Map<String, List<FileScanner.FileInfo>> index = new HashMap<>();

    public void add(FileScanner.FileInfo info) {
        String name = info.name.toLowerCase();
        String[] tokens = name.split("[^a-z0-9]+");
        for (String token : tokens) {
            if (token.isEmpty()) continue;
            index.computeIfAbsent(token, k -> new ArrayList<>()).add(info);
        }
    }

    public List<FileScanner.FileInfo> search(String term) {
        term = term.toLowerCase();
        List<FileScanner.FileInfo> results = index.get(term);
        return results != null ? results : new ArrayList<>();
    }
}
