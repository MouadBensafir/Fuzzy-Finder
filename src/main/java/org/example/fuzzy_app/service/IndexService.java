package org.example.fuzzy_app.service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


// Service for indexing file system directories
// Indexes files once and allows fast in-memory searches

public class IndexService {
    
    private final Map<Path, String> filePaths = new ConcurrentHashMap<>();
    private final Map<Path, String> directoryPaths = new ConcurrentHashMap<>();
    private final Set<Path> indexedRoots = ConcurrentHashMap.newKeySet();
    private final AtomicInteger totalFiles = new AtomicInteger(0);
    private final AtomicInteger totalDirs = new AtomicInteger(0);
    

    // Indexes a directory tree and stores all file/directory paths in memory when a root is added
    public void indexDirectory(Path root) throws IOException {
        if (!Files.exists(root) || !Files.isDirectory(root)) {
            throw new IllegalArgumentException("Invalid directory: " + root);
        }
        
        // Already indexed root
        if (indexedRoots.contains(root)) {
            return; 
        }
        
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                try {
                    if (!Files.isHidden(file)) {
                        filePaths.put(file, file.toString());
                        totalFiles.incrementAndGet();
                    }
                } catch (Exception e) {
                    // Skip files that can't be processed
                }
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                try {
                    if (!Files.isHidden(dir) && !dir.equals(root)) {
                        directoryPaths.put(dir, dir.toString());
                        totalDirs.incrementAndGet();
                    }
                } catch (Exception e) {
                    // Skip directories that can't be processed
                }
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                // Handle access denied and other errors
                return FileVisitResult.CONTINUE;
            }
        });
        
        indexedRoots.add(root);
    }
    
    // Removes a directory from the index.
    public void removeDirectory(Path root) {
        indexedRoots.remove(root);
        filePaths.entrySet().removeIf(entry -> entry.getKey().startsWith(root));
        directoryPaths.entrySet().removeIf(entry -> entry.getKey().startsWith(root));
        
        // Recalculate counts
        totalFiles.set(filePaths.size());
        totalDirs.set(directoryPaths.size());
    }
    
    // Gets all indexed file paths as strings.
    public List<String> getAllFilePaths() {
        return new ArrayList<>(filePaths.values());
    }
    
    // Gets all indexed directory paths as strings.
    public List<String> getAllDirectoryPaths() {
        return new ArrayList<>(directoryPaths.values());
    }
    
    // Gets all indexed paths (files + directories) as strings.
    public List<String> getAllPaths() {
        List<String> allPaths = new ArrayList<>();
        allPaths.addAll(filePaths.values());
        allPaths.addAll(directoryPaths.values());
        return allPaths;
    }
    
    // Gets statistics about the index
    public IndexStats getStats() {
        return new IndexStats(
            totalFiles.get(),
            totalDirs.get(),
            indexedRoots.size()
        );
    }
    
    // Clears the entire index
    public void clear() {
        filePaths.clear();
        directoryPaths.clear();
        indexedRoots.clear();
        totalFiles.set(0);
        totalDirs.set(0);
    }
    

    // Checks if a root is already indexed
    public boolean isIndexed(Path root) {
        return indexedRoots.contains(root);
    }
    
    // Statistics about the index for display purposes
    public record IndexStats(int totalFiles, int totalDirs, int indexedRoots) {
        @Override
        public String toString() {
            return String.format("Files: %d | Directories: %d | Roots: %d", 
                totalFiles, totalDirs, indexedRoots);
        }
    }
}

