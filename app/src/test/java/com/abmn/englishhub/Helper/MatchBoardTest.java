package com.abmn.englishhub.Helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.junit.Test;

public class MatchBoardTest {

    private static List<MatchBoard.Pair> pairs(int n) {
        List<MatchBoard.Pair> list = new ArrayList<>();
        for (int i = 0; i < n; i++) list.add(new MatchBoard.Pair(100 + i, "w" + i, "m" + i));
        return list;
    }

    @Test
    public void splitsTenIntoTwoBoardsOfFive() {
        List<List<MatchBoard.Pair>> boards = MatchBoard.split(pairs(10));
        assertEquals(2, boards.size());
        assertEquals(5, boards.get(0).size());
        assertEquals(5, boards.get(1).size());
    }

    @Test
    public void keepsAShortLastBoard() {
        List<List<MatchBoard.Pair>> boards = MatchBoard.split(pairs(7));
        assertEquals(2, boards.size());
        assertEquals(2, boards.get(1).size());
    }

    @Test
    public void everyCardAppearsExactlyOnceOnBothSides() {
        MatchBoard board = new MatchBoard(pairs(5), new Random(42));
        HashSet<Integer> all = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4));
        assertEquals(all, new HashSet<>(board.wordOrder()));
        assertEquals(all, new HashSet<>(board.meaningOrder()));
        assertEquals(5, board.wordOrder().size());
        assertEquals(5, board.meaningOrder().size());
    }

    @Test
    public void onlyTheOwnMeaningMatches() {
        MatchBoard board = new MatchBoard(pairs(5), new Random(1));
        assertFalse(board.tryMatch(0, 1));
        assertEquals(0, board.matchedCount());
        assertTrue(board.tryMatch(2, 2));
        assertTrue(board.isMatched(2));
        assertEquals(1, board.matchedCount());
    }

    @Test
    public void aMatchedPairCannotBeScoredTwice() {
        MatchBoard board = new MatchBoard(pairs(5), new Random(1));
        assertTrue(board.tryMatch(3, 3));
        assertFalse(board.tryMatch(3, 3));
        assertEquals(1, board.matchedCount());
    }

    @Test
    public void completesAfterAllPairsAreMatched() {
        MatchBoard board = new MatchBoard(pairs(5), new Random(7));
        for (int i = 0; i < 5; i++) {
            assertFalse(board.isComplete());
            assertTrue(board.tryMatch(i, i));
        }
        assertTrue(board.isComplete());
    }

    @Test
    public void rejectsOutOfRangeTaps() {
        MatchBoard board = new MatchBoard(pairs(5), new Random(1));
        assertFalse(board.tryMatch(-1, -1));
        assertFalse(board.tryMatch(5, 5));
    }
}
