package org.example.fuzzy_app.algorithm;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileSorter {
    private static final ThreadLocal<Slab> threadLocalSlab = ThreadLocal.withInitial(Slab::new);
    public static List<String> sortAllFiles(String[] files, String query) {

        if (query == null || query.isEmpty()) {
            return Arrays.asList(Arrays.copyOf(files, Math.min(files.length, 100)));
        }

        return Arrays.stream(files)
                .parallel()
                .filter(path -> BitInclusion.match(query, path))
                .map(file -> {
                    Slab slab = threadLocalSlab.get();
                    Result res = Algorithm.fuzzyMatchV2(
                            false, true, true, file, query, false, slab
                    );
                    return new ScoredFile(file, res);
                })
                .filter(item -> item.result.score > 0)
                .sorted((a, b) -> Integer.compare(b.result.score, a.result.score))
                .limit(100)
                .map(item -> item.path)
                .collect(Collectors.toList());
    }
}