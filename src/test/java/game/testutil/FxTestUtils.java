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

    public static void runOnFxThreadAndWait(Runnable action) {
        initJavaFx();

        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        final RuntimeException[] thrown = new RuntimeException[1];

        Platform.runLater(() -> {
            try {
                action.run();
            } catch (RuntimeException e) {
                thrown[0] = e;
            } finally {
                latch.countDown();
            }
        });

        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for JavaFX task to complete");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for JavaFX task", e);
        }

        if (thrown[0] != null) {
            throw thrown[0];
        }
    }
}
