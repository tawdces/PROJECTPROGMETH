package game.logic;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents the match timer service.
 */
public final class MatchTimerService {

    /**
     * Internal state field for elapsed millis.
     */
    private final AtomicLong elapsedMillis = new AtomicLong();
    /**
     * Internal state field for scheduler.
     */
    private ScheduledExecutorService scheduler;
    /**
     * Internal state field for start nanos.
     */
    private long startNanos;

    /**
     * Creates a timer service in a stopped state.
     */
    public MatchTimerService() {
    }

    /**
     * Starts this component.
     */
    public synchronized void start() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }

        elapsedMillis.set(0L);
        startNanos = System.nanoTime();
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "match-timer-thread");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(() -> {
            long elapsed = (System.nanoTime() - startNanos) / 1_000_000L;
            elapsedMillis.set(elapsed);
        }, 0L, 50L, TimeUnit.MILLISECONDS);
    }

    /**
     * Returns the elapsed millis.
     *
     * @return the elapsed millis
     */
    public long getElapsedMillis() {
        return elapsedMillis.get();
    }

    /**
     * Returns the elapsed seconds.
     *
     * @return the elapsed seconds
     */
    public double getElapsedSeconds() {
        return elapsedMillis.get() / 1000.0;
    }

    /**
     * Stops this component.
     */
    public synchronized void stop() {
        if (scheduler == null) {
            return;
        }
        scheduler.shutdownNow();
        scheduler = null;
    }
}
