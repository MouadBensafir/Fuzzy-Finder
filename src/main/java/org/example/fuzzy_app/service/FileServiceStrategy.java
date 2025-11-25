package org.example.fuzzy_app.service;

import java.io.IOException;
import java.nio.file.Path;

public interface FileServiceStrategy {
    void openFile(Path file) throws IOException;
    void openWithEditor(Path file, String editorCommand) throws IOException;
    void openFolder(Path folder) throws IOException;
}
