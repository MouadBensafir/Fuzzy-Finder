package org.example.fuzzy_app.service.platform_opener;

import java.io.IOException;
import java.nio.file.Path;

public class WindowsFileServiceStrategy implements FileServiceStrategy {
    @Override
    public void openFile(Path file) throws IOException {
        Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "\"\"", file.toString()});
    }

    @Override
    public void openWithEditor(Path file, String editorCommand) throws IOException {
        if (editorCommand == null || editorCommand.isEmpty()) {
            String codePath = System.getenv("LOCALAPPDATA") + "\\Programs\\Microsoft VS Code\\Code.exe";
            if (java.nio.file.Files.exists(java.nio.file.Paths.get(codePath))) {
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "\"\"", codePath, file.toString()});
            } else {
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "code", file.toString()});
            }
        } else {
            Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "\"\"", editorCommand, file.toString()});
        }
    }

    @Override
    public void openFolder(Path folder) throws IOException {
        Runtime.getRuntime().exec(new String[]{"explorer", folder.toString()});
    }
}
