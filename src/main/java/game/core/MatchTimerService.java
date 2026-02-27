package game.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class MatchTimerService {

    private final AtomicLong elapsedMillis = new AtomicLong();
    private ScheduledExecutorService scheduler;
    private long startNanos;

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

    public long getElapsedMillis() {
        return elapsedMillis.get();
    }

    public double getElapsedSeconds() {
        return elapsedMillis.get() / 1000.0;
    }

    public synchronized void stop() {
        if (scheduler == null) {
            return;
        }
        scheduler.shutdownNow();
        scheduler = null;
    }
}
