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
/**
 * Represents the shared multiplayer camera.
 */
public final class SharedMultiplayerCamera {

    /**
     * Internal constant for epsilon.
     */
    private static final double EPSILON = 0.0001;

    /**
     * Internal state field for viewport width.
     */
    private final double viewportWidth;
    /**
     * Internal state field for viewport height.
     */
    private final double viewportHeight;
    /**
     * Internal state field for world min x.
     */
    private final double worldMinX;
    /**
     * Internal state field for world max x.
     */
    private final double worldMaxX;
    /**
     * Internal state field for world min y.
     */
    private final double worldMinY;
    /**
     * Internal state field for world max y.
     */
    private final double worldMaxY;
    /**
     * Internal state field for min zoom.
     */
    private final double minZoom;
    /**
     * Internal state field for max zoom.
     */
    private final double maxZoom;
    /**
     * Internal state field for dynamic zoom.
     */
    private final boolean dynamicZoom;
    /**
     * Internal state field for fixed zoom.
     */
    private final double fixedZoom;
    /**
     * Internal state field for padding x.
     */
    private final double paddingX;
    /**
     * Internal state field for padding y.
     */
    private final double paddingY;
    /**
     * Internal state field for follow speed.
     */
    private final double followSpeed;

    /**
     * Internal state field for center x.
     */
    private double centerX;
    /**
     * Internal state field for center y.
     */
    private double centerY;
    /**
     * Internal state field for zoom.
     */
    private double zoom;
    /**
     * Internal state field for initialized.
     */
    private boolean initialized;

    /**
     * Creates a new shared multiplayer camera instance.
     *
     * @param viewportWidth parameter value
     * @param viewportHeight parameter value
     * @param worldMinX parameter value
     * @param worldMaxX parameter value
     * @param worldMinY parameter value
     * @param worldMaxY parameter value
     * @param minZoom parameter value
     * @param maxZoom parameter value
     * @param dynamicZoom parameter value
     * @param fixedZoom parameter value
     * @param paddingX parameter value
     * @param paddingY parameter value
     * @param followSpeed parameter value
     */
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

    /**
     * Creates a new shared multiplayer camera instance.
     *
     * @param viewportWidth parameter value
     * @param viewportHeight parameter value
     * @param worldMinX parameter value
     * @param worldMaxX parameter value
     * @param worldMinY parameter value
     * @param worldMaxY parameter value
     * @param minZoom parameter value
     * @param maxZoom parameter value
     * @param paddingX parameter value
     * @param paddingY parameter value
     * @param followSpeed parameter value
     */
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
    /**
     * Updates this object state for the current frame.
     *
     * @param deltaSeconds parameter value
     * @param trackedBounds parameter value
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

    /**
     * Executes snap to targets.
     *
     * @param trackedBounds parameter value
     */
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

    /**
     * Returns the camera x.
     *
     * @return the camera x
     */
    public double getCameraX() {
        return centerX - (getViewWidth() * 0.5);
    }

    /**
     * Returns the camera y.
     *
     * @return the camera y
     */
    public double getCameraY() {
        return centerY - (getViewHeight() * 0.5);
    }

    /**
     * Returns the zoom.
     *
     * @return the zoom
     */
    public double getZoom() {
        return zoom;
    }

    /**
     * Returns the view width.
     *
     * @return the view width
     */
    public double getViewWidth() {
        return viewportWidth / zoom;
    }

    /**
     * Returns the view height.
     *
     * @return the view height
     */
    public double getViewHeight() {
        return viewportHeight / zoom;
    }

    /**
     * Internal helper for compute target.
     *
     * @param trackedBounds parameter value
     * @return the resulting value
     */
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

    /**
     * Internal helper for smooth.
     *
     * @param current parameter value
     * @param target parameter value
     * @param alpha parameter value
     * @return the resulting value
     */
    private static double smooth(double current, double target, double alpha) {
        double next = current + (target - current) * alpha;
        return Math.abs(next - target) < EPSILON ? target : next;
    }

    /**
     * Internal helper for clamp.
     *
     * @param value parameter value
     * @param min parameter value
     * @param max parameter value
     * @return the resulting value
     */
    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Immutable target state computed from tracked bounds.
     *
     * @param centerX target camera center x-coordinate
     * @param centerY target camera center y-coordinate
     * @param zoom target camera zoom level
     */
    private record CameraTarget(double centerX, double centerY, double zoom) {
    }
}
