package com.abmn.englishhub.Helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class QuizRoundsTest {

    @Test
    public void pictureRoundIsHidden() {
        assertTrue(QuizRounds.isHidden(3));
        assertFalse(QuizRounds.isHidden(1));
        assertFalse(QuizRounds.isHidden(2));
        assertFalse(QuizRounds.isHidden(4));
        assertFalse(QuizRounds.isHidden(5));
    }

    @Test
    public void playerSeesRoundsOneToFour() {
        assertEquals(1, QuizRounds.displayNumber(1));
        assertEquals(2, QuizRounds.displayNumber(2));
        assertEquals(3, QuizRounds.displayNumber(4)); // Listening
        assertEquals(4, QuizRounds.displayNumber(5)); // Writing
    }

    @Test
    public void versionParameterJoinsExistingQueries() {
        assertEquals("https://x/level-map/3?v=2", QuizRounds.withVersion("https://x/level-map/3"));
        assertEquals("https://x/quiz?count=5&v=2", QuizRounds.withVersion("https://x/quiz?count=5"));
    }
}
