package org.example.fuzzy_app.model.algorithm;

public class ScoredFile {
    public final String path;
    public final Result result;

    public ScoredFile(String path, Result result) {
        this.path = path;
        this.result = result;
    }
}
