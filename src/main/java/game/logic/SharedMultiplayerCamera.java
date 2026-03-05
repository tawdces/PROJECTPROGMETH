package game.logic;

import javafx.geometry.Rectangle2D;

import java.util.Collection;

/**
 * Shared 2D camera for multiplayer games.
 * <p>
 * The camera computes a bounding box around all tracked players, then smoothly
 * follows the group's center while dynamically adjusting zoom so everyone stays
 * visible with padding.
 */
public final class SharedMultiplayerCamera {

    private static final double EPSILON = 0.0001;

    private final double viewportWidth;
    private final double viewportHeight;
    private final double worldMinX;
    private final double worldMaxX;
    private final double worldMinY;
    private final double worldMaxY;
    private final double minZoom;
    private final double maxZoom;
    private final boolean dynamicZoom;
    private final double fixedZoom;
    private final double paddingX;
    private final double paddingY;
    private final double followSpeed;

    private double centerX;
    private double centerY;
    private double zoom;
    private boolean initialized;

    public SharedMultiplayerCamera(
            double viewportWidth,
            double viewportHeight,
            double worldMinX,
            double worldMaxX,
            double worldMinY,
            double worldMaxY,
            double minZoom,
            double maxZoom,
            boolean dynamicZoom,
            double fixedZoom,
            double paddingX,
            double paddingY,
            double followSpeed
    ) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.worldMinX = worldMinX;
        this.worldMaxX = worldMaxX;
        this.worldMinY = worldMinY;
        this.worldMaxY = worldMaxY;
        this.minZoom = minZoom;
        this.maxZoom = maxZoom;
        this.dynamicZoom = dynamicZoom;
        this.fixedZoom = clamp(fixedZoom, minZoom, maxZoom);
        this.paddingX = paddingX;
        this.paddingY = paddingY;
        this.followSpeed = followSpeed;

        this.centerX = viewportWidth * 0.5;
        this.centerY = viewportHeight * 0.5;
        this.zoom = dynamicZoom ? minZoom : this.fixedZoom;
    }

    public SharedMultiplayerCamera(
            double viewportWidth,
            double viewportHeight,
            double worldMinX,
            double worldMaxX,
            double worldMinY,
            double worldMaxY,
            double minZoom,
            double maxZoom,
            double paddingX,
            double paddingY,
            double followSpeed
    ) {
        this(
                viewportWidth,
                viewportHeight,
                worldMinX,
                worldMaxX,
                worldMinY,
                worldMaxY,
                minZoom,
                maxZoom,
                true,
                minZoom,
                paddingX,
                paddingY,
                followSpeed
        );
    }

    /**
     * Updates camera position/zoom toward the target group.
     *
     * @param deltaSeconds frame delta time in seconds
     * @param trackedBounds player bounds (supports 2-4 players, or any positive count)
     */
    public void update(double deltaSeconds, Collection<Rectangle2D> trackedBounds) {
        CameraTarget target = computeTarget(trackedBounds);
        if (target == null) {
            return;
        }

        if (!initialized || deltaSeconds <= 0.0) {
            centerX = target.centerX();
            centerY = target.centerY();
            zoom = target.zoom();
            initialized = true;
            return;
        }

        // Frame-rate independent smoothing.
        double alpha = 1.0 - Math.exp(-followSpeed * deltaSeconds);
        alpha = clamp(alpha, 0.0, 1.0);

        centerX = smooth(centerX, target.centerX(), alpha);
        centerY = smooth(centerY, target.centerY(), alpha);
        zoom = smooth(zoom, target.zoom(), alpha);
    }

    public void snapToTargets(Collection<Rectangle2D> trackedBounds) {
        CameraTarget target = computeTarget(trackedBounds);
        if (target == null) {
            return;
        }
        centerX = target.centerX();
        centerY = target.centerY();
        zoom = target.zoom();
        initialized = true;
    }

    public double getCameraX() {
        return centerX - (getViewWidth() * 0.5);
    }

    public double getCameraY() {
        return centerY - (getViewHeight() * 0.5);
    }

    public double getZoom() {
        return zoom;
    }

    public double getViewWidth() {
        return viewportWidth / zoom;
    }

    public double getViewHeight() {
        return viewportHeight / zoom;
    }

    private CameraTarget computeTarget(Collection<Rectangle2D> trackedBounds) {
        if (trackedBounds == null || trackedBounds.isEmpty()) {
            return null;
        }

        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        int count = 0;

        for (Rectangle2D bounds : trackedBounds) {
            if (bounds == null) {
                continue;
            }
            minX = Math.min(minX, bounds.getMinX());
            maxX = Math.max(maxX, bounds.getMaxX());
            minY = Math.min(minY, bounds.getMinY());
            maxY = Math.max(maxY, bounds.getMaxY());
            count++;
        }

        if (count == 0) {
            return null;
        }

        double spreadWidth = Math.max(1.0, maxX - minX);
        double spreadHeight = Math.max(1.0, maxY - minY);

        // Padding keeps players away from screen edges.
        double focusWidth = spreadWidth + (paddingX * 2.0);
        double focusHeight = spreadHeight + (paddingY * 2.0);

        double targetZoom;
        if (dynamicZoom) {
            // Dynamic zoom: zoom out when spread increases, zoom in when spread decreases.
            double zoomByWidth = viewportWidth / focusWidth;
            double zoomByHeight = viewportHeight / focusHeight;
            targetZoom = clamp(Math.min(zoomByWidth, zoomByHeight), minZoom, maxZoom);
        } else {
            targetZoom = fixedZoom;
        }

        double targetCenterX = (minX + maxX) * 0.5;
        double targetCenterY = (minY + maxY) * 0.5;

        double targetViewWidth = viewportWidth / targetZoom;
        double targetViewHeight = viewportHeight / targetZoom;

        double minCameraX = worldMinX;
        double maxCameraX = worldMaxX - targetViewWidth;
        double minCameraY = worldMinY;
        double maxCameraY = worldMaxY - targetViewHeight;

        if (maxCameraX < minCameraX) {
            double mid = (minCameraX + maxCameraX) * 0.5;
            minCameraX = mid;
            maxCameraX = mid;
        }
        if (maxCameraY < minCameraY) {
            double mid = (minCameraY + maxCameraY) * 0.5;
            minCameraY = mid;
            maxCameraY = mid;
        }

        double targetCameraX = clamp(targetCenterX - (targetViewWidth * 0.5), minCameraX, maxCameraX);
        double targetCameraY = clamp(targetCenterY - (targetViewHeight * 0.5), minCameraY, maxCameraY);

        return new CameraTarget(
                targetCameraX + (targetViewWidth * 0.5),
                targetCameraY + (targetViewHeight * 0.5),
                targetZoom
        );
    }

    private static double smooth(double current, double target, double alpha) {
        double next = current + (target - current) * alpha;
        return Math.abs(next - target) < EPSILON ? target : next;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private record CameraTarget(double centerX, double centerY, double zoom) {
    }
}
