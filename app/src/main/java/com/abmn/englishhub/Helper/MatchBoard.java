package com.abmn.englishhub.Helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The rules of one "match pairs" board, free of Android types so they can be
 * unit-tested: which word card sits where, which meaning card sits where, and
 * whether a tapped word/meaning combination is a match.
 *
 * Cards are identified by their index in {@link #pairs()} (the word's own
 * pair), so word card i and meaning card i belong together no matter where
 * either one is shown.
 */
public final class MatchBoard {

    /** Cards per column on one board. */
    public static final int BOARD_SIZE = 5;

    public static final class Pair {
        public final int wordId;
        public final String word;
        public final String meaning;

        public Pair(int wordId, String word, String meaning) {
            this.wordId = wordId;
            this.word = word;
            this.meaning = meaning;
        }
    }

    private final List<Pair> pairs;
    private final List<Integer> wordOrder = new ArrayList<>();
    private final List<Integer> meaningOrder = new ArrayList<>();
    private final boolean[] matched;
    private int matchedCount = 0;

    public MatchBoard(List<Pair> pairs, Random random) {
        this.pairs = new ArrayList<>(pairs);
        this.matched = new boolean[pairs.size()];
        for (int i = 0; i < pairs.size(); i++) {
            wordOrder.add(i);
            meaningOrder.add(i);
        }
        Collections.shuffle(wordOrder, random);
        Collections.shuffle(meaningOrder, random);
    }

    /** Splits a whole round into boards of {@link #BOARD_SIZE} (last one may be smaller). */
    public static List<List<Pair>> split(List<Pair> all) {
        List<List<Pair>> boards = new ArrayList<>();
        for (int i = 0; i < all.size(); i += BOARD_SIZE) {
            boards.add(new ArrayList<>(all.subList(i, Math.min(all.size(), i + BOARD_SIZE))));
        }
        return boards;
    }

    public List<Pair> pairs() {
        return pairs;
    }

    /** Pair indexes in the order the word cards are shown, top to bottom. */
    public List<Integer> wordOrder() {
        return wordOrder;
    }

    /** Pair indexes in the order the meaning cards are shown, top to bottom. */
    public List<Integer> meaningOrder() {
        return meaningOrder;
    }

    /**
     * A tap on word card {@code wordPair} followed by meaning card
     * {@code meaningPair}. Returns true (and remembers it) when they belong
     * together and were not already matched.
     */
    public boolean tryMatch(int wordPair, int meaningPair) {
        if (wordPair != meaningPair || wordPair < 0 || wordPair >= matched.length || matched[wordPair]) {
            return false;
        }
        matched[wordPair] = true;
        matchedCount++;
        return true;
    }

    public boolean isMatched(int pairIndex) {
        return pairIndex >= 0 && pairIndex < matched.length && matched[pairIndex];
    }

    public int matchedCount() {
        return matchedCount;
    }

    public boolean isComplete() {
        return matchedCount == pairs.size();
    }
}
