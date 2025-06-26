package com.beartell.search;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

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

    private final List<FileInfo> files = Collections.synchronizedList(new ArrayList<>());

    public List<FileInfo> getFiles() {
        return files;
    }

    public void scan(Consumer<Integer> progress) {
        File[] roots = File.listRoots();
        ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor();
        AtomicInteger finished = new AtomicInteger();
        int total = roots.length == 0 ? 1 : roots.length;

        for (File root : roots) {
            Path path = root.toPath();
            exec.submit(() -> {
                scanDirectory(path, exec);
                int done = finished.incrementAndGet();
                if (progress != null) {
                    int percent = (int) ((done * 100.0) / total);
                    progress.accept(percent);
                }
            });
        }

        exec.shutdown();
        try {
            exec.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void scanDirectory(Path path, ExecutorService exec) {
        if (!Files.isReadable(path)) {
            return;
        }
        try (var stream = Files.list(path)) {
            stream.forEach(p -> {
                try {
                    if (Files.isDirectory(p)) {
                        exec.submit(() -> scanDirectory(p, exec));
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
