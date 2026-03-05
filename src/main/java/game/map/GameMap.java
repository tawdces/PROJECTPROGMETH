package game.map;

import game.config.GameSettings;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Represents the game map.
 */
public final class GameMap {
    /**
     * Internal constant for empty image.
     */
    private static final Image EMPTY_IMAGE = new WritableImage(1, 1);

    /**
     * Internal constant for map source width.
     */
    private static final double MAP_SOURCE_WIDTH = 598.0;
    /**
     * Internal constant for map source height.
     */
    private static final double MAP_SOURCE_HEIGHT = 348.0;
    /**
     * Internal constant for sunset platform source width.
     */
    private static final double SUNSET_PLATFORM_SOURCE_WIDTH = 1898.0;
    /**
     * Internal constant for sunset platform source height.
     */
    private static final double SUNSET_PLATFORM_SOURCE_HEIGHT = 894.0;

    /**
     * Internal constant for default p1 spawn x.
     */
    private static final double DEFAULT_P1_SPAWN_X = classicX(100);
    /**
     * Internal constant for default p2 spawn x.
     */
    private static final double DEFAULT_P2_SPAWN_X = classicX(500);
    /**
     * Internal constant for default spawn ground y.
     */
    private static final double DEFAULT_SPAWN_GROUND_Y = classicY(214);

    /**
     * Internal constant for available maps.
     */
    private static final List<GameMap> AVAILABLE_MAPS = List.of(
            new GameMap("/images/maps/Map1.png", "/images/maps/Map1.png", "Map 1", Color.web("#d9ecff"), false, map1Surfaces()),
            new GameMap("/images/maps/Map2.png", "/images/maps/Map2.png", "Map 2", Color.web("#d9ecff"), false, map2Surfaces()),
            new GameMap("/images/maps/Map3.png", "/images/maps/Map3.png", "Map 3", Color.web("#d9ecff"), false, map3Surfaces()),
            new GameMap("/images/sunset/Background.png", "/images/maps/Map4.png", "Sunset", Color.web("#a97b74"), true, sunsetSurfaces())
    );

    /**
     * Internal state field for resource path.
     */
    private final String resourcePath;
    /**
     * Internal state field for preview resource path.
     */
    private final String previewResourcePath;
    /**
     * Internal state field for label.
     */
    private final String label;
    /**
     * Internal state field for background color.
     */
    private final Color backgroundColor;
    /**
     * Internal state field for sunset style.
     */
    private final boolean sunsetStyle;
    /**
     * Internal state field for surfaces.
     */
    private final List<PlatformSurface> surfaces;
    /**
     * Internal state field for player one spawn x.
     */
    private final double playerOneSpawnX;
    /**
     * Internal state field for player two spawn x.
     */
    private final double playerTwoSpawnX;
    /**
     * Internal state field for spawn ground y.
     */
    private final double spawnGroundY;

    /**
     * Tracks whether map assets were loaded lazily.
     */
    private volatile boolean assetsLoaded;
    /**
     * Internal state field for base image.
     */
    private Image baseImage = EMPTY_IMAGE;
    /**
     * Internal state field for sunset background image.
     */
    private Image sunsetBackgroundImage = EMPTY_IMAGE;
    /**
     * Internal state field for sunset sun image.
     */
    private Image sunsetSunImage = EMPTY_IMAGE;
    /**
     * Internal state field for sunset mountain image.
     */
    private Image sunsetMountainImage = EMPTY_IMAGE;
    /**
     * Internal state field for sunset city far image.
     */
    private Image sunsetCityFarImage = EMPTY_IMAGE;
    /**
     * Internal state field for sunset city mid image.
     */
    private Image sunsetCityMidImage = EMPTY_IMAGE;
    /**
     * Internal state field for sunset city near image.
     */
    private Image sunsetCityNearImage = EMPTY_IMAGE;
    /**
     * Internal state field for sunset platform image.
     */
    private Image sunsetPlatformImage = EMPTY_IMAGE;

    /**
     * Creates a private game map instance.
     *
     * @param resourcePath parameter value
     * @param previewResourcePath parameter value
     * @param label parameter value
     * @param backgroundColor parameter value
     * @param sunsetStyle parameter value
     * @param surfaces parameter value
     */
    private GameMap(
            String resourcePath,
            String previewResourcePath,
            String label,
            Color backgroundColor,
            boolean sunsetStyle,
            List<PlatformSurface> surfaces
    ) {
        this.resourcePath = resourcePath;
        this.previewResourcePath = previewResourcePath;
        this.label = label;
        this.backgroundColor = backgroundColor;
        this.sunsetStyle = sunsetStyle;
        this.surfaces = List.copyOf(surfaces);
        this.playerOneSpawnX = DEFAULT_P1_SPAWN_X;
        this.playerTwoSpawnX = DEFAULT_P2_SPAWN_X;
        this.spawnGroundY = DEFAULT_SPAWN_GROUND_Y;
    }

    /**
     * Executes available maps.
     *
     * @return the resulting value
     */
    public static List<GameMap> availableMaps() {
        return AVAILABLE_MAPS;
    }

    /**
     * Executes default map.
     *
     * @return the resulting value
     */
    public static GameMap defaultMap() {
        return AVAILABLE_MAPS.get(0);
    }

    /**
     * Executes from resource.
     *
     * @param resourcePath parameter value
     * @return the resulting value
     */
    public static GameMap fromResource(String resourcePath) {
        for (GameMap map : AVAILABLE_MAPS) {
            if (map.resourcePath.equals(resourcePath)) {
                return map;
            }
        }
        return defaultMap();
    }

    /**
     * Executes resource path.
     *
     * @return the resulting value
     */
    public String resourcePath() {
        return resourcePath;
    }

    /**
     * Executes preview resource path.
     *
     * @return the resulting value
     */
    public String previewResourcePath() {
        return previewResourcePath;
    }

    /**
     * Executes label.
     *
     * @return the resulting value
     */
    public String label() {
        return label;
    }

    /**
     * Executes background color.
     *
     * @return the resulting value
     */
    public Color backgroundColor() {
        return backgroundColor;
    }

    /**
     * Executes surfaces.
     *
     * @return the resulting value
     */
    public List<PlatformSurface> surfaces() {
        return surfaces;
    }

    /**
     * Executes player one spawn x.
     *
     * @return the resulting value
     */
    public double playerOneSpawnX() {
        return playerOneSpawnX;
    }

    /**
     * Executes player two spawn x.
     *
     * @return the resulting value
     */
    public double playerTwoSpawnX() {
        return playerTwoSpawnX;
    }

    /**
     * Executes spawn ground y.
     *
     * @return the resulting value
     */
    public double spawnGroundY() {
        return spawnGroundY;
    }

    /**
     * Renders this object.
     *
     * @param gc parameter value
     * @param cameraX parameter value
     * @param cameraY parameter value
     * @param extendMargin parameter value
     */
    public void render(GraphicsContext gc, double cameraX, double cameraY, double extendMargin) {
        ensureAssetsLoaded();
        if (sunsetStyle) {
            renderSunsetParallax(gc, cameraX, cameraY, extendMargin);
            return;
        }

        gc.drawImage(
                baseImage,
                -extendMargin,
                -extendMargin,
                GameSettings.WIDTH + (extendMargin * 2.0),
                GameSettings.HEIGHT + (extendMargin * 2.0)
        );
    }

    /**
     * Renders internal sunset parallax.
     *
     * @param gc parameter value
     * @param cameraX parameter value
     * @param cameraY parameter value
     * @param extendMargin parameter value
     */
    private void renderSunsetParallax(GraphicsContext gc, double cameraX, double cameraY, double extendMargin) {
        drawParallaxLayer(gc, sunsetBackgroundImage, -extendMargin * 2.0, -extendMargin * 2.0, GameSettings.WIDTH + (extendMargin * 4.0), GameSettings.HEIGHT + (extendMargin * 4.0), 0.02, 0.02, cameraX, cameraY);
        drawParallaxLayer(gc, sunsetSunImage, -220.0, -90.0, 1400.0, 860.0, 0.08, 0.05, cameraX, cameraY);
        drawParallaxLayer(gc, sunsetMountainImage, -280.0, 52.0, 1720.0, 610.0, 0.18, 0.12, cameraX, cameraY);
        drawParallaxLayer(gc, sunsetCityFarImage, -260.0, 180.0, 1500.0, 710.0, 0.32, 0.20, cameraX, cameraY);
        drawParallaxLayer(gc, sunsetCityMidImage, -220.0, 198.0, 1450.0, 690.0, 0.46, 0.26, cameraX, cameraY);
        drawParallaxLayer(gc, sunsetCityNearImage, -220.0, 214.0, 1450.0, 690.0, 0.62, 0.34, cameraX, cameraY);
        drawParallaxLayer(gc, sunsetPlatformImage, 0.0, 0.0, GameSettings.WIDTH, GameSettings.HEIGHT, 1.0, 1.0, cameraX, cameraY);
    }

    /**
     * Internal helper for draw parallax layer.
     *
     * @param gc parameter value
     * @param image parameter value
     * @param x parameter value
     * @param y parameter value
     * @param width parameter value
     * @param height parameter value
     * @param speedX parameter value
     * @param speedY parameter value
     * @param cameraX parameter value
     * @param cameraY parameter value
     */
    private static void drawParallaxLayer(
            GraphicsContext gc,
            Image image,
            double x,
            double y,
            double width,
            double height,
            double speedX,
            double speedY,
            double cameraX,
            double cameraY
    ) {
        if (image == EMPTY_IMAGE) {
            return;
        }
        double drawX = x + cameraX * (1.0 - speedX);
        double drawY = y + cameraY * (1.0 - speedY);
        gc.drawImage(image, drawX, drawY, width, height);
    }

    /**
     * Internal helper for ensure assets loaded.
     */
    private void ensureAssetsLoaded() {
        if (assetsLoaded) {
            return;
        }
        synchronized (this) {
            if (assetsLoaded) {
                return;
            }
            baseImage = loadImage(resourcePath);
            if (sunsetStyle) {
                sunsetBackgroundImage = loadImage("/images/sunset/Background.png");
                sunsetSunImage = loadImage("/images/sunset/Sun.png");
                sunsetMountainImage = loadImage("/images/sunset/Mountain.png");
                sunsetCityFarImage = loadImage("/images/sunset/City3.png");
                sunsetCityMidImage = loadImage("/images/sunset/City2.png");
                sunsetCityNearImage = loadImage("/images/sunset/City1.png");
                sunsetPlatformImage = loadImage("/images/sunset/Platform.png");
            }
            assetsLoaded = true;
        }
    }

    /**
     * Internal helper for load image.
     *
     * @param resourcePath parameter value
     * @return the resulting value
     */
    private static Image loadImage(String resourcePath) {
        var url = GameMap.class.getResource(resourcePath);
        if (url != null) {
            Image image = new Image(url.toExternalForm(), false);
            return image.isError() ? EMPTY_IMAGE : image;
        }

        Path fallback = Paths.get("src", "main", "resources", resourcePath.replaceFirst("^/", ""));
        if (Files.exists(fallback)) {
            Image image = new Image(fallback.toUri().toString(), false);
            return image.isError() ? EMPTY_IMAGE : image;
        }
        return EMPTY_IMAGE;
    }

    /**
     * Internal helper for map1 surfaces.
     *
     * @return the resulting value
     */
    private static List<PlatformSurface> map1Surfaces() {
        return List.of(
                new PlatformSurface(classicX(120), classicY(135), classicW(148), classicH(12), true),
                new PlatformSurface(classicX(320), classicY(135), classicW(145), classicH(12), true),
                new PlatformSurface(classicX(-30), classicY(214), classicW(660), classicH(14), true),
                new PlatformSurface(classicX(70), classicY(290), classicW(88), classicH(12), true),
                new PlatformSurface(classicX(425), classicY(290), classicW(89), classicH(12), true),
                new PlatformSurface(classicX(70), classicY(375), classicW(450), classicH(14), true)
        );
    }

    /**
     * Internal helper for map2 surfaces.
     *
     * @return the resulting value
     */
    private static List<PlatformSurface> map2Surfaces() {
        return List.of(
                new PlatformSurface(classicX(60), classicY(70), classicW(160), classicH(12), true),
                new PlatformSurface(classicX(24), classicY(145), classicW(315), classicH(13), true),
                new PlatformSurface(classicX(0), classicY(220), classicW(490), classicH(13), true),
                new PlatformSurface(classicX(-15), classicY(300), classicW(550), classicH(14), true)
        );
    }

    /**
     * Internal helper for map3 surfaces.
     *
     * @return the resulting value
     */
    private static List<PlatformSurface> map3Surfaces() {
        return List.of(
                new PlatformSurface(classicX(-30), classicY(150), classicW(120), classicH(10), true),
                new PlatformSurface(classicX(505), classicY(150), classicW(120), classicH(10), true),
                new PlatformSurface(classicX(-45), classicY(230), classicW(120), classicH(12), true),
                new PlatformSurface(classicX(500), classicY(230), classicW(120), classicH(12), true),
                new PlatformSurface(classicX(-30), classicY(315), classicW(650), classicH(16), true)
        );
    }

    /**
     * Internal helper for sunset surfaces.
     *
     * @return the resulting value
     */
    private static List<PlatformSurface> sunsetSurfaces() {
        return List.of(
                new PlatformSurface(sunsetX(542), sunsetY(50), sunsetW(814), sunsetH(24), true),
                new PlatformSurface(sunsetX(309), sunsetY(305), sunsetW(1280), sunsetH(24), true),
                new PlatformSurface(sunsetX(542), sunsetY(560), sunsetW(814), sunsetH(24), true),
                new PlatformSurface(sunsetX(12), sunsetY(560), sunsetW(273), sunsetH(24), true),
                new PlatformSurface(sunsetX(1613), sunsetY(560), sunsetW(273), sunsetH(24), true),
                new PlatformSurface(sunsetX(703), sunsetY(830), sunsetW(516), sunsetH(22), true)
        );
    }

    /**
     * Internal helper for classic x.
     *
     * @param mapX parameter value
     * @return the resulting value
     */
    private static double classicX(double mapX) {
        return (mapX / MAP_SOURCE_WIDTH) * GameSettings.WIDTH;
    }

    /**
     * Internal helper for classic y.
     *
     * @param mapY parameter value
     * @return the resulting value
     */
    private static double classicY(double mapY) {
        return (mapY / MAP_SOURCE_HEIGHT) * GameSettings.HEIGHT;
    }

    /**
     * Internal helper for classic w.
     *
     * @param mapWidth parameter value
     * @return the resulting value
     */
    private static double classicW(double mapWidth) {
        return (mapWidth / MAP_SOURCE_WIDTH) * GameSettings.WIDTH;
    }

    /**
     * Internal helper for classic h.
     *
     * @param mapHeight parameter value
     * @return the resulting value
     */
    private static double classicH(double mapHeight) {
        return (mapHeight / MAP_SOURCE_HEIGHT) * GameSettings.HEIGHT;
    }

    /**
     * Internal helper for sunset x.
     *
     * @param sourceX parameter value
     * @return the resulting value
     */
    private static double sunsetX(double sourceX) {
        return (sourceX / SUNSET_PLATFORM_SOURCE_WIDTH) * GameSettings.WIDTH;
    }

    /**
     * Internal helper for sunset y.
     *
     * @param sourceY parameter value
     * @return the resulting value
     */
    private static double sunsetY(double sourceY) {
        return (sourceY / SUNSET_PLATFORM_SOURCE_HEIGHT) * GameSettings.HEIGHT;
    }

    /**
     * Internal helper for sunset w.
     *
     * @param sourceWidth parameter value
     * @return the resulting value
     */
    private static double sunsetW(double sourceWidth) {
        return (sourceWidth / SUNSET_PLATFORM_SOURCE_WIDTH) * GameSettings.WIDTH;
    }

    /**
     * Internal helper for sunset h.
     *
     * @param sourceHeight parameter value
     * @return the resulting value
     */
    private static double sunsetH(double sourceHeight) {
        return (sourceHeight / SUNSET_PLATFORM_SOURCE_HEIGHT) * GameSettings.HEIGHT;
    }
}
