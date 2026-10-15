package com.abmn.englishhub.Helper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class ReviewPromptHelperTest {

    private static final long WEEK_PLUS = TimeUnit.DAYS.toMillis(8);
    private static final long NEVER = -1L;

    @Test
    public void asksAfterGoodResultOnceInstalledAWeek() {
        assertTrue(ReviewPromptHelper.shouldAsk(3, true, WEEK_PLUS, 0, NEVER));
    }

    @Test
    public void staysQuietWhenResultWasNotGood() {
        assertFalse(ReviewPromptHelper.shouldAsk(10, false, WEEK_PLUS, 0, NEVER));
    }

    @Test
    public void needsAFewCompletedQuizzes() {
        assertFalse(ReviewPromptHelper.shouldAsk(2, true, WEEK_PLUS, 0, NEVER));
    }

    @Test
    public void waitsAWeekAfterInstall() {
        assertFalse(ReviewPromptHelper.shouldAsk(5, true, TimeUnit.DAYS.toMillis(6), 0, NEVER));
        assertTrue(ReviewPromptHelper.shouldAsk(5, true, TimeUnit.DAYS.toMillis(7), 0, NEVER));
    }

    @Test
    public void unknownInstallAgeIsTreatedAsNo() {
        assertFalse(ReviewPromptHelper.shouldAsk(5, true, -1L, 0, NEVER));
    }

    @Test
    public void keepsSixtyDaysBetweenAsks() {
        assertFalse(ReviewPromptHelper.shouldAsk(5, true, WEEK_PLUS, 1, TimeUnit.DAYS.toMillis(59)));
        assertTrue(ReviewPromptHelper.shouldAsk(5, true, WEEK_PLUS, 1, TimeUnit.DAYS.toMillis(61)));
    }

    @Test
    public void neverAsksMoreThanTwiceInALifetime() {
        assertFalse(ReviewPromptHelper.shouldAsk(50, true, TimeUnit.DAYS.toMillis(400), 2, TimeUnit.DAYS.toMillis(200)));
    }
}
