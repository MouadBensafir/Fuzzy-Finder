package org.example.fuzzy_app.algorithm;

import java.util.List;

public class Algo implements ScoringStrategy {
    public int score(String query, String filename, Slab slab) {
        return Algorithm.fuzzyMatchV2(true, true, true, filename, query, true, slab).score;
    }

    public static void main(String[] args) {
        var slab = new Slab();
        var algo = new Algo();
        System.out.println(algo.score("ahmed", "ahed", slab));
        String[] myFiles = {
                "src/main/java/Main.java",
                "src/utils/Helper.java",
                "README.md",
                "pom.xml",
                "target/classes/Main.class"
        };

        String query = "m"; // looking for Main.java

        List<String> results = FileSorter.sortAllFiles(myFiles, query);

        results.forEach(System.out::println);
    }

    public static List<String> SortAllFiles(String[] files, String query) {
        return FileSorter.sortAllFiles(files, query);
    }
}
