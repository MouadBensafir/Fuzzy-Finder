package org.example.fuzzy_app.model.contants;

public class Constants {
    public static final int CHAR_WHITE = 0;
    public static final int CHAR_NON_WORD = 1;
    public static final int CHAR_DELIMITER = 2;
    public static final int CHAR_LOWER = 3;
    public static final int CHAR_UPPER = 4;
    public static final int CHAR_LETTER = 5;
    public static final int CHAR_NUMBER = 6;
    public static final short[][] bonusMatrix = new short[8][8];
    public static final byte[] asciiCharClasses = new byte[128];
    public static final int SCORE_MATCH = 16;
    public static final int SCORE_GAP_START = -3;
    public static final int SCORE_GAP_EXTENSION = -1;
    public static final int BONUS_BOUNDARY = SCORE_MATCH / 2;
    public static final int BONUS_NON_WORD = SCORE_MATCH / 2;
    public static final int BONUS_CAMEL123 = BONUS_BOUNDARY + SCORE_GAP_EXTENSION;
    public static final int BONUS_CONSECUTIVE = -(SCORE_GAP_START + SCORE_GAP_EXTENSION);
    public static final int BONUS_FIRST_CHAR_MULTIPLIER = 2;
    public static final int BONUS_BOUNDARY_WHITE = BONUS_BOUNDARY + 2;
    public static final int BONUS_BOUNDARY_DELIMITER = BONUS_BOUNDARY + 1;


    static {
        for (int i = 0; i < 128; i++) {
            if ('a' <= i && i <= 'z') {
                asciiCharClasses[i] = CHAR_LOWER;
            } else if ('A' <= i && i <= 'Z') {
                asciiCharClasses[i] = CHAR_UPPER;
            } else if ('0' <= i && i <= '9') {
                asciiCharClasses[i] = CHAR_NUMBER;
            } else if (i == '/' || i == ',' || i == ':' || i == ';' || i == '|' || i == '\\') {
                asciiCharClasses[i] = CHAR_DELIMITER;
            } else if (9 <= i && i <= 13 || i == ' ') {
                asciiCharClasses[i] = CHAR_WHITE;
            } else {
                asciiCharClasses[i] = CHAR_NON_WORD;
            }
        }
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                bonusMatrix[i][j] = computeBonus(i, j);
            }
        }
    }

    private static short computeBonus(int prev, int curr) {
        if (curr > CHAR_NON_WORD) {
            if (prev == CHAR_WHITE) {
                return (short) BONUS_BOUNDARY_WHITE;
            } else if (prev == CHAR_DELIMITER) {
                return (short) BONUS_BOUNDARY_DELIMITER;
            } else if (prev == CHAR_NON_WORD) {
                return (short) BONUS_BOUNDARY;
            } else if (prev == CHAR_LOWER && curr == CHAR_UPPER) {
                return (short) BONUS_CAMEL123;
            } else if (prev != CHAR_NUMBER && curr == CHAR_NUMBER) {
                return (short) BONUS_CAMEL123;
            }
        }
        if (curr == CHAR_NON_WORD || curr == CHAR_DELIMITER) {
            return (short) BONUS_NON_WORD;
        }
        if (curr == CHAR_WHITE) {
            return (short) BONUS_BOUNDARY_WHITE;
        }
        return 0;
    }
}
