package game.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatchTimerServiceTest {

    @Test
    void start_increasesElapsed_andStop_freezesIt() throws Exception {
        MatchTimerService timer = new MatchTimerService();

        timer.start();
        Thread.sleep(140); // scheduler ticks every 50ms; give it enough time

        long elapsed1 = timer.getElapsedMillis();
        assertTrue(elapsed1 > 0, "Elapsed millis should increase after start()");

        timer.stop();

        long stoppedAt = timer.getElapsedMillis();
        Thread.sleep(120);

        long elapsedAfterStop = timer.getElapsedMillis();
        assertEquals(stoppedAt, elapsedAfterStop, "Elapsed should stop changing after stop()");
    }

    @Test
    void stop_beforeStart_doesNotThrow() {
        MatchTimerService timer = new MatchTimerService();
        assertDoesNotThrow(timer::stop);
    }

    @Test
    void start_isIdempotent_doesNotResetWhileRunning() throws Exception {
        MatchTimerService timer = new MatchTimerService();

        timer.start();
        Thread.sleep(120);
        long before = timer.getElapsedMillis();

        timer.start(); // should be a no-op while running
        Thread.sleep(80);
        long after = timer.getElapsedMillis();

        assertTrue(after >= before, "Calling start() again should not reset elapsed while already running");

        timer.stop();
    }

    @Test
    void getElapsedSeconds_matchesMillis() throws Exception {
        MatchTimerService timer = new MatchTimerService();

        timer.start();
        Thread.sleep(120);

        long ms = timer.getElapsedMillis();
        double sec = timer.getElapsedSeconds();

        assertEquals(ms / 1000.0, sec, 1e-9);

        timer.stop();
    }
}
