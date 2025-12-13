package org.example.fuzzy_app.service.platform_opener;

import java.io.IOException;
import java.nio.file.Path;

public class MacOSFileServiceStrategy implements FileServiceStrategy {
    @Override
    public void openFile(Path file) throws IOException {
        Runtime.getRuntime().exec(new String[]{"open", file.toString()});
    }

    @Override
    public void openWithEditor(Path file, String editorCommand) throws IOException {
        if (editorCommand == null || editorCommand.isEmpty()) {
            Runtime.getRuntime().exec(new String[]{"open", "-a", "Visual Studio Code", file.toString()});
        } else {
            Runtime.getRuntime().exec(new String[]{"open", "-a", editorCommand, file.toString()});
        }
    }

    @Override
    public void openFolder(Path folder) throws IOException {
        Runtime.getRuntime().exec(new String[]{"open", folder.toString()});
    }
}