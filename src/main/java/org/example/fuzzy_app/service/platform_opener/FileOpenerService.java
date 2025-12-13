package org.example.fuzzy_app.service.platform_opener;

import java.io.IOException;
import java.nio.file.Path;

public class FileOpenerService {
    private static final FileServiceStrategy strategy;

    static {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            strategy = new WindowsFileServiceStrategy();
        } else if (os.contains("mac")) {
            strategy = new MacOSFileServiceStrategy();
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            strategy = new LinuxFileServiceStrategy();
        } else {
            throw new UnsupportedOperationException("Platform not supported: " + os);
        }
    }

    public static void openFile(Path file) throws IOException {
        strategy.openFile(file);
    }

    public static void openWithEditor(Path file, String editorCommand) throws IOException {
        strategy.openWithEditor(file, editorCommand);
    }

    public static void openFolder(Path folder) throws IOException {
        strategy.openFolder(folder);
    }
}
