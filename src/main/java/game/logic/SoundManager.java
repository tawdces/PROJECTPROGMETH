package game.logic;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Represents the sound manager.
 */
public final class SoundManager {
    /**
     * Private helper type for bgm mode.
     */
    private enum BgmMode {
        /** No background music is currently active. */
        NONE,
        /** Menu background music is active. */
        MENU,
        /** In-match background music is active. */
        GAME
    }

    /**
     * Internal constant for instance.
     */
    private static final SoundManager INSTANCE = new SoundManager();
    /**
     * Internal constant for menu bgm base volume.
     */
    private static final double MENU_BGM_BASE_VOLUME = 1.0;
    /**
     * Internal constant for game bgm base volume.
     */
    private static final double GAME_BGM_BASE_VOLUME = 0.35;
    /**
     * Internal constant for min volume.
     */
    private static final double MIN_VOLUME = 0.0;
    /**
     * Internal constant for max volume.
     */
    private static final double MAX_VOLUME = 1.0;
    /**
     * Internal constant for menu bgm resource.
     */
    private static final String MENU_BGM_RESOURCE = "/sounds/backgrounds/Menu_bg_music.mp3";

    /**
     * Loaded sound effects indexed by logical effect name.
     */
    private final Map<String, AudioClip> effects = new HashMap<>();
    /**
     * Internal state field for bgm player.
     */
    private MediaPlayer bgmPlayer;
    /**
     * Internal state field for random.
     */
    private final Random random = new Random();
    /**
     * Internal state field for music volume.
     */
    private double musicVolume = 1.0;
    /**
     * Internal state field for effect volume.
     */
    private double effectVolume = 1.0;
    /**
     * Internal state field for current bgm base volume.
     */
    private double currentBgmBaseVolume = MENU_BGM_BASE_VOLUME;
    /**
     * Internal state field for current bgm mode.
     */
    private BgmMode currentBgmMode = BgmMode.NONE;
    /**
     * Internal state field for current bgm resource.
     */
    private String currentBgmResource;

    /**
     * Internal state field for bgm files.
     */
    private final List<String> bgmFiles = List.of("/sounds/backgrounds/Bg_music1.mp3", "/sounds/backgrounds/Bg_music2.mp3", "/sounds/backgrounds/Bg_music3.mp3", "/sounds/backgrounds/Bg_music4.mp3");

    /**
     * Creates a private sound manager instance.
     */
    private SoundManager() {
        loadEffect("shoot", "/sounds/effects/Shoot.mp3");
        loadEffect("step", "/sounds/effects/Step.mp3");
        loadEffect("hit", "/sounds/effects/Hit.mp3");
        loadEffect("melee", "/sounds/effects/Melee.mp3");
        loadEffect("die", "/sounds/effects/Die.mp3");
        loadEffect("pickup", "/sounds/effects/Pickup.mp3");
        loadEffect("click", "/sounds/effects/Click.mp3");
        loadEffect("explosion", "/sounds/effects/Explosion.mp3");
    }

    /**
     * Returns the instance.
     *
     * @return the instance
     */
    public static SoundManager getInstance() {
        return INSTANCE;
    }

    /**
     * Internal helper for load effect.
     *
     * @param name parameter value
     * @param resourcePath parameter value
     */
    private void loadEffect(String name, String resourcePath) {
        try {
            URL url = getClass().getResource(resourcePath);
            if (url != null) {
                effects.put(name, new AudioClip(url.toExternalForm()));
                return;
            }
            Path fallback = Paths.get("src", "main", "resources", resourcePath.replaceFirst("^/", ""));
            if (Files.exists(fallback)) {
                effects.put(name, new AudioClip(fallback.toUri().toString()));
            } else {
                System.out.println("Warning: Sound effect not found: " + resourcePath);
            }
        } catch (Exception e) {
            System.err.println("Failed to load sound effect: " + resourcePath + " (" + e.getMessage() + ")");
        }
    }

    /**
     * Executes play effect.
     *
     * @param name parameter value
     */
    public void playEffect(String name) {
        AudioClip clip = effects.get(name);
        if (clip != null) {
            double rate = 1.0;
            if ("shoot".equals(name) || "step".equals(name) || "hit".equals(name)) {
                rate = 0.9 + random.nextDouble() * 0.2;
            }
            clip.play(effectVolume, 0.0, rate, 0.0, 0);
        }
    }

    /**
     * Executes play random bgm.
     */
    public void playRandomBgm() {
        if (bgmFiles.isEmpty()) return;

        String resourcePath = bgmFiles.get(random.nextInt(bgmFiles.size()));
        if (currentBgmMode == BgmMode.GAME && bgmPlayer != null && resourcePath.equals(currentBgmResource)) {
            currentBgmBaseVolume = GAME_BGM_BASE_VOLUME;
            applyBgmVolume();
            return;
        }
        stopBgm();
        try {
            URL url = getClass().getResource(resourcePath);
            String mediaUrl = null;
            if (url != null) {
                mediaUrl = url.toExternalForm();
            } else {
                Path fallback = Paths.get("src", "main", "resources", resourcePath.replaceFirst("^/", ""));
                if (Files.exists(fallback)) {
                    mediaUrl = fallback.toUri().toString();
                }
            }

            if (mediaUrl != null) {
                Media media = new Media(mediaUrl);
                bgmPlayer = new MediaPlayer(media);
                bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                currentBgmBaseVolume = GAME_BGM_BASE_VOLUME;
                currentBgmMode = BgmMode.GAME;
                currentBgmResource = resourcePath;
                applyBgmVolume();
                bgmPlayer.play();
            } else {
                System.out.println("Warning: BGM not found: " + resourcePath);
            }
        } catch (Exception e) {
            System.err.println("Failed to load BGM: " + resourcePath + " (" + e.getMessage() + ")");
        }
    }

    /**
     * Executes play menu bgm.
     */
    public void playMenuBgm() {
        try {
            String resourcePath = MENU_BGM_RESOURCE;
            if (currentBgmMode == BgmMode.MENU && bgmPlayer != null) {
                currentBgmBaseVolume = MENU_BGM_BASE_VOLUME;
                applyBgmVolume();
                return;
            }
            stopBgm();
            URL url = getClass().getResource(resourcePath);
            String mediaUrl = null;
            if (url != null) {
                mediaUrl = url.toExternalForm();
            } else {
                Path fallback = Paths.get("src", "main", "resources", resourcePath.replaceFirst("^/", ""));
                if (Files.exists(fallback)) {
                    mediaUrl = fallback.toUri().toString();
                }
            }

            if (mediaUrl != null) {
                Media media = new Media(mediaUrl);
                bgmPlayer = new MediaPlayer(media);
                bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                currentBgmBaseVolume = MENU_BGM_BASE_VOLUME;
                currentBgmMode = BgmMode.MENU;
                currentBgmResource = resourcePath;
                applyBgmVolume();
                bgmPlayer.play();
            } else {
                System.out.println("Warning: Menu BGM not found: " + resourcePath);
            }
        } catch (Exception e) {
            System.err.println("Failed to load Menu BGM: (" + e.getMessage() + ")");
        }
    }

    /**
     * Executes stop bgm.
     */
    public void stopBgm() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
            bgmPlayer.dispose();
            bgmPlayer = null;
        }
        currentBgmMode = BgmMode.NONE;
        currentBgmResource = null;
    }

    /**
     * Returns the music volume.
     *
     * @return the music volume
     */
    public double getMusicVolume() {
        return musicVolume;
    }

    /**
     * Sets the music volume.
     *
     * @param volume parameter value
     */
    public void setMusicVolume(double volume) {
        musicVolume = clampVolume(volume);
        applyBgmVolume();
    }

    /**
     * Returns the effect volume.
     *
     * @return the effect volume
     */
    public double getEffectVolume() {
        return effectVolume;
    }

    /**
     * Sets the effect volume.
     *
     * @param volume parameter value
     */
    public void setEffectVolume(double volume) {
        effectVolume = clampVolume(volume);
    }

    /**
     * Returns the master volume.
     *
     * @return the master volume
     */
    public double getMasterVolume() {
        return (musicVolume + effectVolume) * 0.5;
    }

    /**
     * Sets the master volume.
     *
     * @param volume parameter value
     */
    public void setMasterVolume(double volume) {
        setMusicVolume(volume);
        setEffectVolume(volume);
    }

    /**
     * Internal helper for apply bgm volume.
     */
    private void applyBgmVolume() {
        if (bgmPlayer != null) {
            bgmPlayer.setVolume(clampVolume(currentBgmBaseVolume * musicVolume));
        }
    }

    /**
     * Internal helper for clamp volume.
     *
     * @param volume parameter value
     * @return the resulting value
     */
    private static double clampVolume(double volume) {
        if (Double.isNaN(volume)) {
            return MAX_VOLUME;
        }
        return Math.max(MIN_VOLUME, Math.min(MAX_VOLUME, volume));
    }
}
