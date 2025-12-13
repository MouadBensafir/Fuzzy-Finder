package org.example.fuzzy_app.service.platform_opener;

import java.io.IOException;
import java.nio.file.Path;

public class LinuxFileServiceStrategy implements FileServiceStrategy {
    @Override
    public void openFile(Path file) throws IOException {
        Runtime.getRuntime().exec(new String[]{"xdg-open", file.toString()});
    }

    @Override
    public void openWithEditor(Path file, String editorCommand) throws IOException {
        if (editorCommand == null || editorCommand.isEmpty()) {
            Runtime.getRuntime().exec(new String[]{"code", file.toString()});
        } else {
            Runtime.getRuntime().exec(new String[]{editorCommand, file.toString()});
        }
    }

    @Override
    public void openFolder(Path folder) throws IOException {
        Runtime.getRuntime().exec(new String[]{"xdg-open", folder.toString()});
    }
}
