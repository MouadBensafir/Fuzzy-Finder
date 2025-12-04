package org.example.fuzzy_app.file_integeration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.example.fuzzy_app.algorithm.*;

public class FileLister {
    private static List<String> listAllPaths(Path startPath) {
        try {
            // Walk the file tree, converts every path to a string and collect to a list
            return Files.walk(startPath).map(Path::toString).collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error scanning path: " + e.getMessage());
            return List.of(); 
        }
    }


    public static void main(String[] args) {
        Path startPath = Path.of("src/main/java/org/example/fuzzy_app");

        System.out.println("=== List all file paths under ===");
        List<String> allPaths = listAllPaths(startPath);
        allPaths.forEach(System.out::println);

        System.out.println("=== List all file paths filtered by scoring ===");
        List<String> sortedPaths = FileSorter.sortAllFiles(allPaths.toArray(new String[0]), "algo");
        System.out.println(sortedPaths.size() + " results found:");
        sortedPaths.forEach(System.out::println);
    }
}
