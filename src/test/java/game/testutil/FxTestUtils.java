package game.testutil;

import javafx.application.Platform;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class FxTestUtils {
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    private FxTestUtils() {
    }

    public static void initJavaFx() {
        if (STARTED.compareAndSet(false, true)) {
            CountDownLatch latch = new CountDownLatch(1);
            try {
                Platform.startup(latch::countDown);
            } catch (IllegalStateException alreadyStarted) {
                latch.countDown();
            }
            try {
                if (!latch.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("JavaFX Platform failed to start within timeout");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while starting JavaFX Platform", e);
            }
        }
    }
}
