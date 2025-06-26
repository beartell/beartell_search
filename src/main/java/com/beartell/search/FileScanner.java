package com.beartell.search;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility to recursively scan the file system and collect file metadata.
 */
public class FileScanner {

    public static class FileInfo {
        public final Path path;
        public final String name;
        public final String extension;
        public final long creationTime;

        public FileInfo(Path path, String name, String extension, long creationTime) {
            this.path = path;
            this.name = name;
            this.extension = extension;
            this.creationTime = creationTime;
        }
    }

    private final List<FileInfo> files = new ArrayList<>();

    public List<FileInfo> getFiles() {
        return files;
    }

    public void scan() {
        File[] roots = File.listRoots();
        for (File root : roots) {
            scanDirectory(root.toPath());
        }
    }

    private void scanDirectory(Path path) {
        if (!Files.isReadable(path)) {
            return;
        }
        try {
            Files.list(path).forEach(p -> {
                try {
                    if (Files.isDirectory(p)) {
                        scanDirectory(p);
                    } else {
                        BasicFileAttributes attrs = Files.readAttributes(p, BasicFileAttributes.class);
                        String name = p.getFileName().toString();
                        String ext = "";
                        int idx = name.lastIndexOf('.');
                        if (idx >= 0) {
                            ext = name.substring(idx + 1).toLowerCase();
                        }
                        FileInfo info = new FileInfo(p, name, ext, attrs.creationTime().toMillis());
                        files.add(info);
                    }
                } catch (IOException e) {
                    // ignore
                }
            });
        } catch (IOException e) {
            // ignore
        }
    }
}
