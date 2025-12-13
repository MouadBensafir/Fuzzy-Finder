package org.example.fuzzy_app.service;

import org.example.fuzzy_app.algorithm.FileSorter;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;


// Service responsible for performing fuzzy file searches using the FileSorter algorithm
// against pre-indexed file paths in memory

public class FileSearchService {
    
    private static final int MAX_RESULTS_WHEN_EMPTY = 50;
    
    public static CompletableFuture<List<String>> searchAsync(
            List<String> indexedPaths,
            String query,
            Consumer<List<String>> onComplete) {
        
        return CompletableFuture.<List<String>>supplyAsync(() -> {
            if (indexedPaths.isEmpty()) {
                return List.<String>of();
            }
            
            if (query == null || query.trim().isEmpty()) {
                // Show first N items when query is empty
                return indexedPaths.stream()
                        .limit(MAX_RESULTS_WHEN_EMPTY)
                        .collect(Collectors.toList());
            }
            
            // Use FileSorter to get fuzzy matches on indexed paths
            String[] filesArray = indexedPaths.toArray(new String[0]);
            return FileSorter.sortAllFiles(filesArray, query.trim(), true);
        }).thenApply((List<String> results) -> {
            if (onComplete != null) {
                onComplete.accept(results);
            }
            return results;
        });
    }
}
