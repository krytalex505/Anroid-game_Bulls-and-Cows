package com.example.bullsandcows;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameEngine {

    public static class CheckResult {
        public int bulls;
        public int cows;
        public List<String> bullsList;
        public List<String> cowsList;
        public List<String> zerosList;

        public CheckResult() {
            bullsList = new ArrayList<>();
            cowsList = new ArrayList<>();
            zerosList = new ArrayList<>();
        }
    }

    public static CheckResult checkWord(String guess, String target) {
        CheckResult result = new CheckResult();
        int length = target.length();
        boolean[] targetUsed = new boolean[length];
        boolean[] guessUsed = new boolean[length];

        // Сначала ищем быков
        for (int i = 0; i < length; i++) {
            if (i < guess.length() && i < target.length() &&
                    guess.charAt(i) == target.charAt(i)) {
                result.bulls++;
                targetUsed[i] = true;
                guessUsed[i] = true;

                String letter = String.valueOf(guess.charAt(i));
                if (!result.bullsList.contains(letter)) {
                    result.bullsList.add(letter);
                }
            }
        }

        // Затем ищем коров
        for (int i = 0; i < length; i++) {
            if (guessUsed[i] || i >= guess.length()) continue;

            for (int j = 0; j < length; j++) {
                if (!targetUsed[j] && j < target.length() &&
                        guess.charAt(i) == target.charAt(j)) {
                    result.cows++;
                    targetUsed[j] = true;

                    String letter = String.valueOf(guess.charAt(i));
                    if (!result.bullsList.contains(letter) && !result.cowsList.contains(letter)) {
                        result.cowsList.add(letter);
                    }
                    break;
                }
            }
        }

        // Остальные - нулевые
        for (int i = 0; i < length; i++) {
            if (!guessUsed[i] && i < guess.length()) {
                String letter = String.valueOf(guess.charAt(i));
                if (!result.bullsList.contains(letter) && !result.cowsList.contains(letter) &&
                        !result.zerosList.contains(letter)) {
                    result.zerosList.add(letter);
                }
            }
        }

        return result;
    }

    public static String selectRandomWord(List<String> wordList, int length) {
        List<String> candidates = new ArrayList<>();
        for (String word : wordList) {
            if (word.length() == length) {
                candidates.add(word);
            }
        }
        if (candidates.isEmpty()) return null;
        Random random = new Random();
        return candidates.get(random.nextInt(candidates.size()));
    }

    public static boolean isValidWord(String word, int length) {
        if (word.length() != length) return false;
        for (int i = 0; i < length; i++) {
            char c = word.charAt(i);
            if (!((c >= 'А' && c <= 'Я') || c == 'Ё')) {
                return false;
            }
            for (int j = i + 1; j < length; j++) {
                if (word.charAt(i) == word.charAt(j)) {
                    return false; // Повтор букв
                }
            }
        }
        return true;
    }
}