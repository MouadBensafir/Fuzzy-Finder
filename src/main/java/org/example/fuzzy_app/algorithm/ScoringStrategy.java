package org.example.fuzzy_app.algorithm;

public interface ScoringStrategy {
    int score(String query, String path, Slab slab);
}
