package org.example.fuzzy_app.service;

import javafx.scene.image.Image;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;


public class FileContentService {
    
    private static final int MAX_CONTENT_SIZE = 200_000;
    private static final double NON_PRINTABLE_THRESHOLD = 0.1;
    
    // Image file extensions
    private static final String[] IMAGE_EXTENSIONS = {
        ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".ico", ".webp"
    };
    
    // Common binary file extensions (excluding images)
    private static final String[] BINARY_EXTENSIONS = {
        ".exe", ".dll", ".so", ".dylib", ".bin", ".class", ".jar", ".war",
        ".zip", ".rar", ".7z", ".tar", ".gz", ".bz2", ".xz",
        ".svg", // SVG is treated as binary/text, not image
        ".mp3", ".mp4", ".avi", ".mov", ".wmv", ".flv", ".mkv",
        ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx",
        ".db", ".sqlite", ".mdb"
    };
    

    public static boolean isImageFile(Path path) {
        try {
            String fileName = path.getFileName().toString().toLowerCase();
            for (String ext : IMAGE_EXTENSIONS) {
                if (fileName.endsWith(ext)) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
    
    public static boolean isBinaryFile(Path path) {
        try {
            String fileName = path.getFileName().toString().toLowerCase();
            
            // Images are not treated as binary files (they're handled separately)
            if (isImageFile(path)) {
                return false;
            }
            
            // Check for PDF
            if (fileName.endsWith(".pdf")) {
                return true;
            }
            
            // Check for common binary extensions
            for (String ext : BINARY_EXTENSIONS) {
                if (fileName.endsWith(ext)) {
                    return true;
                }
            }
            
            // Check file content for binary patterns
            if (Files.size(path) > 0) {
                byte[] firstBytes = new byte[Math.min(512, (int) Files.size(path))];
                try (var inputStream = Files.newInputStream(path)) {
                    int bytesRead = inputStream.read(firstBytes);
                    if (bytesRead > 0) {
                        // Check for null bytes (strong indicator of binary)
                        for (int i = 0; i < bytesRead; i++) {
                            if (firstBytes[i] == 0) {
                                return true;
                            }
                        }
                        
                        // Check if file is valid UTF-8
                        try {
                            new String(firstBytes, 0, bytesRead, StandardCharsets.UTF_8);
                        } catch (Exception e) {
                            return true;
                        }
                    }
                }
            }
            
            return false;
        } catch (Exception e) {
            return true; // Assume binary on error
        }
    }

    public static void readFileContentAsync(
            Path path,
            Consumer<String> onSuccess,
            Consumer<String> onError,
            Consumer<String> onBinary) {
        
        CompletableFuture.runAsync(() -> {
            try {
                // Check if binary or PDF
                if (isBinaryFile(path)) {
                    if (onBinary != null) {
                        onBinary.accept("[Binary file or PDF - cannot display content]");
                    }
                    return;
                }
                
                byte[] bytes = Files.readAllBytes(path);
                String content = new String(bytes, StandardCharsets.UTF_8);
                
                // Check if content contains too many non-printable characters (might be binary)
                long nonPrintableCount = content.chars()
                        .filter(ch -> ch < 32 && ch != 9 && ch != 10 && ch != 13)
                        .count();
                        
                if (nonPrintableCount > content.length() * NON_PRINTABLE_THRESHOLD) {
                    if (onBinary != null) {
                        onBinary.accept("[File appears to be binary - cannot display content]");
                    }
                    return;
                }
                
                // Truncate very large files
                if (content.length() > MAX_CONTENT_SIZE) {
                    content = content.substring(0, MAX_CONTENT_SIZE) + 
                             "\n\n...truncated (file too large)...";
                }
                
                if (onSuccess != null) {
                    onSuccess.accept(content);
                }
            } catch (IOException ex) {
                if (onError != null) {
                    onError.accept("Error reading file: " + ex.getMessage());
                }
            }
        });
    }
    
    // Loads an image file asynchronously
    public static void loadImageAsync(
            Path path,
            Consumer<Image> onSuccess,
            Consumer<String> onError) {
        
        CompletableFuture.runAsync(() -> {
            try {
                if (!Files.exists(path)) {
                    if (onError != null) {
                        onError.accept("Image file not found: " + path);
                    }
                    return;
                }
                
                if (!isImageFile(path)) {
                    if (onError != null) {
                        onError.accept("File is not a supported image format");
                    }
                    return;
                }
                
                // Load image from file
                Image image = new Image(new FileInputStream(path.toFile()));
                
                if (image.isError()) {
                    if (onError != null) {
                        onError.accept("Failed to load image: " + image.getException().getMessage());
                    }
                    return;
                }
                
                if (onSuccess != null) {
                    onSuccess.accept(image);
                }
            } catch (IOException ex) {
                if (onError != null) {
                    onError.accept("Error loading image: " + ex.getMessage());
                }
            } catch (Exception ex) {
                if (onError != null) {
                    onError.accept("Unexpected error loading image: " + ex.getMessage());
                }
            }
        });
    }
}

