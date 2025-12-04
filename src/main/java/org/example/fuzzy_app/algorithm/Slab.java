package org.example.fuzzy_app.algorithm;

public class Slab {
    public short[] buffer16;
    public int[] buffer32;

    Slab() {
        buffer16 = new short[65536];
        buffer32 = new int[1024*1024];
    }
}
