package org.example.fuzzy_app.algorithm;

public class Algorithm {
    private static char getPreparedPatternChar(String pattern, int index, boolean normalize, boolean caseSensitive) {
        char c = pattern.charAt(index);
        if (normalize) {
            c = Normalize.get(c);
        }
        if (!caseSensitive && c >= 'A' && c <= 'Z') {
            c += 32;
        }
        return c;
    }

    private static int[] asciiFuzzyIndex(String text, String pattern, boolean caseSensitive, boolean normalize) {
        if (pattern.isEmpty()) return new int[]{0, 0};
        int firstIdx = -1;
        int lastIdx = 0;
        int currentTextIdx = 0;

        for (int pIdx = 0; pIdx < pattern.length(); pIdx++) {
            char pChar = getPreparedPatternChar(pattern, pIdx, normalize, caseSensitive);
            int indexOfPattern = indexOf(text, pChar, currentTextIdx, caseSensitive, normalize);
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
        char b = getPreparedPatternChar(pattern, pattern.length() - 1, normalize, caseSensitive);
        for (int idx = text.length() - 1; idx > -1; idx--) {
            char tChar = text.charAt(idx);
            if (normalize) tChar = Normalize.get(tChar);
            if (caseSensitive) {
                if (tChar == b) {
                    lastIdx = idx;
                    break;
                }
            } else {
                char lower = b;
                char upper = (char)(b - 32);
                char tLower = (tChar >= 'A' && tChar <= 'Z') ? (char)(tChar + 32) : tChar;
                if (tLower == b) {
                    lastIdx = idx;
                    break;
                }
            }
        }

        return new int[]{firstIdx, lastIdx + 1};
    }

    private static int indexOf(String text, char b, int fromIndex, boolean caseSensitive, boolean normalize) {
        if (fromIndex >= text.length()) return -1;
        if (caseSensitive) {
            for (int i = fromIndex; i < text.length(); i++) {
                char t = text.charAt(i);
                if (normalize) t = Normalize.get(t);
                if (t == b) return i;
            }
            return -1;
        }
        char lower = b;
        char upper = (b >= 'a' && b <= 'z') ? (char)(b - 32) : b;
        for (int i = fromIndex; i < text.length(); i++) {
            char t = text.charAt(i);
            if (normalize) t = Normalize.get(t);
            if (t == lower || t == upper) return i;
        }
        return -1;
    }

    public static Result fuzzyMatchV2(boolean caseSensitive, boolean normalize, String text, String pattern, boolean withPos, Slab slab) {
        if (pattern.isEmpty()) return Result.Empty;
        int[] slices = asciiFuzzyIndex(text, pattern, caseSensitive, normalize);
        if (slices == null) return Result.Empty;
        int N = slices[1] - slices[0];
        int M = pattern.length();

        int offset16 = 3*N;
        int offset32 = N + M;
        int H0=0, C0=N, B=2*N, F=0, T=M;

        int prevClass = Constants.CHAR_NON_WORD;
        int thisClass = Constants.CHAR_WHITE;
        short bonus;
        int pidx = 0;

        char pchar = getPreparedPatternChar(pattern, 0, normalize, caseSensitive);

        for (int i = 0; i < N; i++) {
            char raw = text.charAt(slices[0] + i);
            char letter = normalize ? Normalize.get(raw) : raw;

            thisClass = letter < 128 ? Constants.asciiCharClasses[letter] : Constants.CHAR_NON_WORD;
            bonus = Constants.bonusMatrix[prevClass][thisClass];
            slab.buffer16[B + i] = bonus;

            if (!caseSensitive && letter >= 'A' && letter <= 'Z') {
                letter += 32;
            }
            slab.buffer32[T + i] = letter;

            if (letter == pchar) {
                if (pidx < M) {
                    slab.buffer32[F + pidx] = i;
                    pidx++;
                    if (pidx < M) {
                        pchar = getPreparedPatternChar(pattern, pidx, normalize, caseSensitive);
                    }
                }
            }
            prevClass = thisClass;
        }

        if (pidx != M) return Result.Empty;

        if (M == 1) {
            int maxScore = -1, maxPos = -1, score = -1;
            char pChar0 = getPreparedPatternChar(pattern, 0, normalize, caseSensitive);

            for (int i = 0; i < N; i++) {
                if (slab.buffer32[T + i] == pChar0) {
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
        char pChar0 = getPreparedPatternChar(pattern, 0, normalize, caseSensitive);

        for (int i = 0; i < N; i++) {
            if (slab.buffer32[T + i] == pChar0) {
                score = Constants.SCORE_MATCH + (slab.buffer16[B + i] * Constants.BONUS_FIRST_CHAR_MULTIPLIER);
                slab.buffer16[H0 + i] = (short) score;
                slab.buffer16[C0 + i] = 1;
                inGap = false;
            } else {
                prevH0 = prevH0 + (inGap ? Constants.SCORE_GAP_EXTENSION: Constants.SCORE_GAP_START);
                score = Math.max(prevH0, 0);
                slab.buffer16[H0 + i] = (short) score;
                slab.buffer16[C0 + i] = 0;
                inGap = true;
            }
            prevH0 = slab.buffer16[H0 + i];
        }

        int H = offset16; offset16 += M * N;
        int C = offset16; offset16 += M * N;
        System.arraycopy(slab.buffer16, H0, slab.buffer16, H, N);
        System.arraycopy(slab.buffer16, C0, slab.buffer16, C, N);

        int maxScore = -1, maxScorePos = -1;

        for (int pIdx = 1; pIdx < M; pIdx++) {
            int prevHRow = H + (pIdx - 1)*N;
            int nextHRow = H + pIdx * N;
            int prevCRow = C + (pIdx - 1) * N;
            int nextCRow = C + pIdx * N;
            int f = slab.buffer32[F + pIdx];

            char pChar = getPreparedPatternChar(pattern, pIdx, normalize, caseSensitive);

            inGap = false;
            if (f > 0) slab.buffer16[nextHRow + f - 1] = 0;

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