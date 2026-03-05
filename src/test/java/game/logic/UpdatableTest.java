package game.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UpdatableTest {

    @Test
    void update_receivesDeltaSeconds() {
        RecordingUpdatable updatable = new RecordingUpdatable();

        updatable.update(0.016);
        updatable.update(0.033);

        assertEquals(2, updatable.updateCalls);
        assertEquals(0.033, updatable.lastDeltaSeconds, 0.000_001);
    }

    private static final class RecordingUpdatable implements Updatable {
        private int updateCalls;
        private double lastDeltaSeconds;

        @Override
        public void update(double deltaSeconds) {
            updateCalls++;
            lastDeltaSeconds = deltaSeconds;
        }
    }
}
