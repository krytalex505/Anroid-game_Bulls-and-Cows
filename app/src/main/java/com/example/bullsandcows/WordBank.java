package com.example.bullsandcows;

import java.util.ArrayList;
import java.util.List;

public class WordBank {
    private List<String> bulls;
    private List<String> cows;
    private List<String> zeros;
    private int maxSize;

    public WordBank(int maxSize) {
        this.maxSize = maxSize;
        this.bulls = new ArrayList<>();
        this.cows = new ArrayList<>();
        this.zeros = new ArrayList<>();
    }

    public void addBull(String letter) {
        if (!bulls.contains(letter) && bulls.size() < maxSize) {
            bulls.add(letter);
            cows.remove(letter);
            zeros.remove(letter);
        }
    }

    public void addCow(String letter) {
        if (!cows.contains(letter) && !bulls.contains(letter) && cows.size() < maxSize) {
            cows.add(letter);
            zeros.remove(letter);
        }
    }

    public void addZero(String letter) {
        if (!zeros.contains(letter) && !bulls.contains(letter) && !cows.contains(letter) &&
                zeros.size() < maxSize) {
            zeros.add(letter);
        }
    }

    public void removeZeroFromOthers() {
        for (String zero : zeros) {
            bulls.remove(zero);
            cows.remove(zero);
        }
    }

    public void clear() {
        bulls.clear();
        cows.clear();
        zeros.clear();
    }

    public List<String> getBulls() { return bulls; }
    public List<String> getCows() { return cows; }
    public List<String> getZeros() { return zeros; }
}