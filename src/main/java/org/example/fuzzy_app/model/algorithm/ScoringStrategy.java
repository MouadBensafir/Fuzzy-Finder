package org.example.fuzzy_app.model.algorithm;

public interface ScoringStrategy {
    int score(String query, String path, Slab slab);
}
