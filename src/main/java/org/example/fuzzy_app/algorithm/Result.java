package org.example.fuzzy_app.algorithm;

public class Result {
    public int start;
    public int end;
    public int score;

    public static Result Empty = new Result(-1, -1, 0);

    public Result(int start, int end, int score) {
        this.start = start;
        this.end = end;
        this.score = score;
    }
}
