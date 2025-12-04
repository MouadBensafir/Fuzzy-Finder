package org.example.fuzzy_app.algorithm;

public class Algorithm {

    private static int[] asciiFuzzyIndex(String text, String pattern, boolean caseSensitive) {
        if (pattern.isEmpty()) return new int[]{0, 0};
        int firstIdx = -1;
        int lastIdx = 0;
        int currentTextIdx = 0;

        for (int pIdx = 0; pIdx < pattern.length(); pIdx++) {
            char pChar = pattern.charAt(pIdx);
            int indexOfPattern = indexOf(text, pChar, currentTextIdx, caseSensitive);
            if (indexOfPattern == -1) {
                return null;
            } else {
                currentTextIdx = indexOfPattern + 1;
                if (pIdx == 0) {
                    firstIdx = indexOfPattern;
                }
                lastIdx = indexOfPattern;
            }
        }
        char b = pattern.charAt(pattern.length() - 1);
        for (int idx = text.length() - 1; idx > -1; idx--) {
            if (caseSensitive) {
                if (text.charAt(idx) == b) {
                    lastIdx = idx;
                    break;
                }
            } else {
                char lower = b;
                char upper = b;
                if (b >= 'a' && b <= 'z') upper = (char) (b - 32);
                else if (b >= 'A' && b <= 'Z') lower = (char) (b + 32);
                if (text.charAt(idx) == lower || text.charAt(idx) == upper) {
                    lastIdx = idx;
                    break;
                }
            }
        }

        return new int[]{firstIdx, lastIdx + 1};
    }

    private static int indexOf(String text, char b, int fromIndex, boolean caseSensitive) {
        if (fromIndex >= text.length()) return -1;

        if (caseSensitive) {
            for (int i = fromIndex; i < text.length(); i++) {
                if (text.charAt(i) == b) return i;
            }
            return -1;
        }

        char lower = b;
        char upper = b;
        if (b >= 'a' && b <= 'z') upper = (char) (b - 32);
        else if (b >= 'A' && b <= 'Z') lower = (char) (b + 32);
        for (int i = fromIndex; i < text.length(); i++) {
            if (text.charAt(i) == lower || text.charAt(i) == upper) return i;
        }
        return -1;
    }

    public static Result fuzzyMatchV2(boolean caseSensitive, boolean normalize, boolean forward, String text, String pattern, boolean withPos, Slab slab) {
        if (pattern.isEmpty()) {
            return Result.Empty;
        }
        int[] slices = asciiFuzzyIndex(text, pattern, caseSensitive);
        if (slices == null) {
            return Result.Empty;
        }
        int N = slices[1] - slices[0];
        int M = pattern.length();
        int offset16 = 0;
        int offset32 = 0;
        int H0 = 0;
        int C0 = N;
        int B = 2*N;
        offset16 = 3*N;
        int F = 0;
        int T = M;
        offset32 = N + M;
        int prevClass = Constants.CHAR_NON_WORD;
        int thisClass = Constants.CHAR_WHITE;
        short bonus;
        char letter;
        int pidx = 0;
        char pchar = pattern.charAt(0);
        for (int i = 0; i < N; i++) {
            letter = text.charAt(slices[0] + i);
            thisClass = letter < 128 ? Constants.asciiCharClasses[letter] : Constants.CHAR_NON_WORD;
            bonus = Constants.bonusMatrix[prevClass][thisClass];
            slab.buffer16[B + i] = bonus;
            if (!caseSensitive && 'A' <= letter && letter <= 'Z') {
                letter += 32;
            }
            if (letter == pchar) {
                if (pidx < M) {
                    slab.buffer32[F + pidx] = i;
                    pidx++;
                    if (pidx < M) {
                        pchar = pattern.charAt(pidx);
                    }
                }
            }
            slab.buffer32[T + i] = letter;
            prevClass = thisClass;
        }
        if (pidx != M) {
            return Result.Empty;
        }
        if (M == 1) {
            int maxScore = -1;
            int maxPos = -1;
            int score = -1;
            for (int i = 0; i < N; i++) {
                if (slab.buffer32[T + i] == pattern.charAt(0)) {
                    score = Constants.SCORE_MATCH + (slab.buffer16[B + i] * Constants.BONUS_FIRST_CHAR_MULTIPLIER);
                    if (score > maxScore) {
                        maxScore = score;
                        maxPos = i;
                    }
                }
            }
            return new Result(slices[0] + maxPos, slices[0] + maxPos + 1, maxScore);
        }
        int prevH0 = 0;
        int score = 0;
        boolean inGap = false;
        for (int i = 0; i < N; i++) {
            if (slab.buffer32[T + i] == pattern.charAt(0)) {
                score = Constants.SCORE_MATCH + (slab.buffer16[B + i] * Constants.BONUS_FIRST_CHAR_MULTIPLIER);
                slab.buffer16[H0 + i] = (short) score;
                slab.buffer16[C0 + i] = 1;
                inGap = false;
            } else {
                prevH0 = prevH0 + (inGap ? Constants.SCORE_GAP_EXTENSION: Constants.SCORE_GAP_START);
                score = prevH0;
                score = Math.max(score, 0);
                slab.buffer16[H0 + i] = (short) score;
                slab.buffer16[C0 + i] = 0;
                inGap = true;
            }
            prevH0 = slab.buffer16[H0 + i];
        }
        int H = offset16;
        offset16 += M * N;
        int C = offset16;
        offset16 += M * N;
        System.arraycopy(slab.buffer16, H0, slab.buffer16, H, N);
        System.arraycopy(slab.buffer16, C0, slab.buffer16, C, N);
        int maxScore = -1;
        int maxScorePos = -1;
        for (int pIdx = 1; pIdx < M; pIdx++) {
            int prevHRow = H + (pIdx - 1)*N;
            int nextHRow = H + pIdx * N;
            int prevCRow = C + (pIdx - 1) * N;
            int nextCRow = C + pIdx * N;
            int f = slab.buffer32[F + pIdx];
            int pChar = pattern.charAt(pIdx);
            inGap = false;
            slab.buffer16[nextHRow + f - 1] = 0;
            for (int i = f; i < N; i++) {
                int s2 = slab.buffer16[nextHRow + i - 1] + (inGap ? Constants.SCORE_GAP_EXTENSION : Constants.SCORE_GAP_START);
                int s1 = Short.MIN_VALUE;
                if (slab.buffer32[T + i] == pChar) {
                    int diag = slab.buffer16[prevHRow + i - 1];
                    bonus = slab.buffer16[B + i];
                    int consecutive = slab.buffer16[prevCRow + i - 1] + 1;

                    if (consecutive > 1) {
                        int firstBonus = slab.buffer16[B + i - consecutive + 1];
                        bonus = (short) Math.max(bonus, Math.max(Constants.BONUS_CONSECUTIVE, firstBonus));
                    }
                    s1 = diag + Constants.SCORE_MATCH + bonus;

                    slab.buffer16[nextCRow + i] = (short) consecutive;
                } else {
                    slab.buffer16[nextCRow + i] = 0;
                }
                inGap = s2 > s1;
                score = Math.max(s1, s2);
                score = Math.max(score, 0);

                slab.buffer16[nextHRow + i] = (short) score;
                if (pIdx == M - 1 && score > maxScore) {
                    maxScore = score;
                    maxScorePos = i;
                }
            }
        }
        return new Result(slices[0] + maxScorePos - M + 1, slices[0] + maxScorePos + 1, maxScore);
    }
}