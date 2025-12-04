package org.example.fuzzy_app.algorithm;

public class BitInclusion {

    private static int formula(int query, int path) {
        return path | ((~path)&(~query));
    }

    public static boolean match(String query, String path) {
        query = query.toLowerCase();
        path = path.toLowerCase();
        int firstfilter1 = 0;
        int secondfilter1 = 0;
        int thirdfilter1 = 0;
        for (Character cha: query.toCharArray()) {
            if (cha < 32 || cha > 126) {
                continue;
            }
            if (33 <= cha && cha < 65) {
                int q = 1 << (cha - 33);
                firstfilter1 |= q;
            }
            if (65 <= cha && cha < 97) {
                int q = 1 << (cha - 65);
                secondfilter1 |= q;
            }
            if (97 <= cha) {
                int q = 1 << (cha - 97);
                thirdfilter1 |= q;
            }
        }
        int firstfilter2 = 0;
        int secondfilter2 = 0;
        int thirdfilter2 = 0;
        for (Character cha: path.toCharArray()) {
            if (cha < 32 || cha > 126) {
                continue;
            }
            if (33 <= cha && cha < 65) {
                int q = 1 << (cha - 33);
                firstfilter2 |= q;
            }
            if (65 <= cha && cha < 97) {
                int q = 1 << (cha - 65);
                secondfilter2 |= q;
            }
            if (97 <= cha) {
                int q = 1 << (cha - 97);
                thirdfilter2 |= q;
            }
        }
        int verify = ~0;
        return formula(firstfilter1, firstfilter2) == verify && formula(secondfilter1, secondfilter2) == verify && formula(thirdfilter1 ,thirdfilter2) == verify;
    }

    public static void main(String[] args) {
        System.out.println(match("Aaa", "aab"));
    }
}
