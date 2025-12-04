package org.example.fuzzy_app.algorithm;

import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) {
        // 1. Initialize the memory Slab (only once!)
        Slab slab = new Slab();

        // 2. Instantiate your Algorithm class
        Algorithm algo = new Algorithm();

        System.out.println("=== Running FZF Java Tests ===\n");

        // Test Case 1: Simple Prefix
        runTest(algo, slab, "file.txt", "file");

        // Test Case 2: Fuzzy with Gaps (The classic "fzz" test)
        runTest(algo, slab, "fuzzyfinder", "ff");

        // Test Case 3: CamelCase Bonus (Should match 'C'amel'C'ase)
        runTest(algo, slab, "CamelCase", "cc");

        // Test Case 4: Acronym/Path (Should favor start of words)
        runTest(algo, slab, "src/main/java/Algorithm.java", "smja");

        // Test Case 5: No Match
        runTest(algo, slab, "banana", "z");

        // Test Case 6: Exact Match Logic (M=1 optimization)
        runTest(algo, slab, "abc", "b");
    }

    private static void runTest(Algorithm algo, Slab slab, String text, String pattern) {
        // Convert Strings to byte arrays

        // Run the algorithm
        // params: caseSensitive=false, normalize=true, forward=true, text, pattern, withPos=true, slab
        Result res = algo.fuzzyMatchV2(false, true, text, pattern, true, slab);

        System.out.print("Pattern: [" + pattern + "] in [" + text + "] ");

        if (res.start == -1) {
            System.out.println("-> NO MATCH ❌");
        } else {
            // Extract the matched substring based on returned indices
            // Note: End index is exclusive in our logic usually, but let's see how it aligns
            // Safe bounds check for printing
            int printStart = Math.max(0, res.start);
            int printEnd = Math.min(text.length(), res.end);

            String matchedPart = text.substring(printStart, printEnd);

            System.out.println("-> MATCH ✅");
            System.out.println("   Score: " + res.score);
            System.out.println("   Range: " + res.start + " to " + res.end);
            System.out.println("   Match: \"" + matchedPart + "\"");
        }
        System.out.println("------------------------------------------------");
    }
}