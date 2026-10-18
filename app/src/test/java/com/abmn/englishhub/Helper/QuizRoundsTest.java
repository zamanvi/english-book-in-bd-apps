package com.abmn.englishhub.Helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class QuizRoundsTest {

    @Test
    public void everyRoundIsShown() {
        for (int round = 1; round <= 5; round++) {
            assertFalse(QuizRounds.isHidden(round));
            assertEquals(round, QuizRounds.displayNumber(round));
        }
    }

    @Test
    public void versionParameterJoinsExistingQueries() {
        assertEquals("https://x/level-map/3?v=3", QuizRounds.withVersion("https://x/level-map/3"));
        assertEquals("https://x/quiz?count=5&v=3", QuizRounds.withVersion("https://x/quiz?count=5"));
    }
}
