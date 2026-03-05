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

public final class GameMap {
    private static final Image EMPTY_IMAGE = new WritableImage(1, 1);

    private static final double MAP_SOURCE_WIDTH = 598.0;
    private static final double MAP_SOURCE_HEIGHT = 348.0;
    private static final double SUNSET_PLATFORM_SOURCE_WIDTH = 1898.0;
    private static final double SUNSET_PLATFORM_SOURCE_HEIGHT = 894.0;

    private static final double DEFAULT_P1_SPAWN_X = classicX(100);
    private static final double DEFAULT_P2_SPAWN_X = classicX(500);
    private static final double DEFAULT_SPAWN_GROUND_Y = classicY(214);

    private static final List<GameMap> AVAILABLE_MAPS = List.of(
            new GameMap("/images/maps/Map1.png", "/images/maps/Map1.png", "Map 1", Color.web("#d9ecff"), false, map1Surfaces()),
            new GameMap("/images/maps/Map2.png", "/images/maps/Map2.png", "Map 2", Color.web("#d9ecff"), false, map2Surfaces()),
            new GameMap("/images/maps/Map3.png", "/images/maps/Map3.png", "Map 3", Color.web("#d9ecff"), false, map3Surfaces()),
            new GameMap("/images/sunset/Background.png", "/images/maps/Map4.png", "Sunset", Color.web("#a97b74"), true, sunsetSurfaces())
    );

    private final String resourcePath;
    private final String previewResourcePath;
    private final String label;
    private final Color backgroundColor;
    private final boolean sunsetStyle;
    private final List<PlatformSurface> surfaces;
    private final double playerOneSpawnX;
    private final double playerTwoSpawnX;
    private final double spawnGroundY;

    private volatile boolean assetsLoaded;
    private Image baseImage = EMPTY_IMAGE;
    private Image sunsetBackgroundImage = EMPTY_IMAGE;
    private Image sunsetSunImage = EMPTY_IMAGE;
    private Image sunsetMountainImage = EMPTY_IMAGE;
    private Image sunsetCityFarImage = EMPTY_IMAGE;
    private Image sunsetCityMidImage = EMPTY_IMAGE;
    private Image sunsetCityNearImage = EMPTY_IMAGE;
    private Image sunsetPlatformImage = EMPTY_IMAGE;

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

    public static List<GameMap> availableMaps() {
        return AVAILABLE_MAPS;
    }

    public static GameMap defaultMap() {
        return AVAILABLE_MAPS.get(0);
    }

    public static GameMap fromResource(String resourcePath) {
        for (GameMap map : AVAILABLE_MAPS) {
            if (map.resourcePath.equals(resourcePath)) {
                return map;
            }
        }
        return defaultMap();
    }

    public String resourcePath() {
        return resourcePath;
    }

    public String previewResourcePath() {
        return previewResourcePath;
    }

    public String label() {
        return label;
    }

    public Color backgroundColor() {
        return backgroundColor;
    }

    public List<PlatformSurface> surfaces() {
        return surfaces;
    }

    public double playerOneSpawnX() {
        return playerOneSpawnX;
    }

    public double playerTwoSpawnX() {
        return playerTwoSpawnX;
    }

    public double spawnGroundY() {
        return spawnGroundY;
    }

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

    private void renderSunsetParallax(GraphicsContext gc, double cameraX, double cameraY, double extendMargin) {
        drawParallaxLayer(gc, sunsetBackgroundImage, -extendMargin * 2.0, -extendMargin * 2.0, GameSettings.WIDTH + (extendMargin * 4.0), GameSettings.HEIGHT + (extendMargin * 4.0), 0.02, 0.02, cameraX, cameraY);
        drawParallaxLayer(gc, sunsetSunImage, -220.0, -90.0, 1400.0, 860.0, 0.08, 0.05, cameraX, cameraY);
        drawParallaxLayer(gc, sunsetMountainImage, -280.0, 52.0, 1720.0, 610.0, 0.18, 0.12, cameraX, cameraY);
        drawParallaxLayer(gc, sunsetCityFarImage, -260.0, 180.0, 1500.0, 710.0, 0.32, 0.20, cameraX, cameraY);
        drawParallaxLayer(gc, sunsetCityMidImage, -220.0, 198.0, 1450.0, 690.0, 0.46, 0.26, cameraX, cameraY);
        drawParallaxLayer(gc, sunsetCityNearImage, -220.0, 214.0, 1450.0, 690.0, 0.62, 0.34, cameraX, cameraY);
        drawParallaxLayer(gc, sunsetPlatformImage, 0.0, 0.0, GameSettings.WIDTH, GameSettings.HEIGHT, 1.0, 1.0, cameraX, cameraY);
    }

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

    private static List<PlatformSurface> map2Surfaces() {
        return List.of(
                new PlatformSurface(classicX(60), classicY(70), classicW(160), classicH(12), true),
                new PlatformSurface(classicX(24), classicY(145), classicW(315), classicH(13), true),
                new PlatformSurface(classicX(0), classicY(220), classicW(490), classicH(13), true),
                new PlatformSurface(classicX(-15), classicY(300), classicW(550), classicH(14), true)
        );
    }

    private static List<PlatformSurface> map3Surfaces() {
        return List.of(
                new PlatformSurface(classicX(-30), classicY(150), classicW(120), classicH(10), true),
                new PlatformSurface(classicX(505), classicY(150), classicW(120), classicH(10), true),
                new PlatformSurface(classicX(-45), classicY(230), classicW(120), classicH(12), true),
                new PlatformSurface(classicX(500), classicY(230), classicW(120), classicH(12), true),
                new PlatformSurface(classicX(-30), classicY(315), classicW(650), classicH(16), true)
        );
    }

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

    private static double classicX(double mapX) {
        return (mapX / MAP_SOURCE_WIDTH) * GameSettings.WIDTH;
    }

    private static double classicY(double mapY) {
        return (mapY / MAP_SOURCE_HEIGHT) * GameSettings.HEIGHT;
    }

    private static double classicW(double mapWidth) {
        return (mapWidth / MAP_SOURCE_WIDTH) * GameSettings.WIDTH;
    }

    private static double classicH(double mapHeight) {
        return (mapHeight / MAP_SOURCE_HEIGHT) * GameSettings.HEIGHT;
    }

    private static double sunsetX(double sourceX) {
        return (sourceX / SUNSET_PLATFORM_SOURCE_WIDTH) * GameSettings.WIDTH;
    }

    private static double sunsetY(double sourceY) {
        return (sourceY / SUNSET_PLATFORM_SOURCE_HEIGHT) * GameSettings.HEIGHT;
    }

    private static double sunsetW(double sourceWidth) {
        return (sourceWidth / SUNSET_PLATFORM_SOURCE_WIDTH) * GameSettings.WIDTH;
    }

    private static double sunsetH(double sourceHeight) {
        return (sourceHeight / SUNSET_PLATFORM_SOURCE_HEIGHT) * GameSettings.HEIGHT;
    }
}
