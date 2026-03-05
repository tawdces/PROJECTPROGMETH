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

public final class SoundManager {
    private static final SoundManager INSTANCE = new SoundManager();
    private static final double MENU_BGM_BASE_VOLUME = 1.0;
    private static final double GAME_BGM_BASE_VOLUME = 0.35;
    private static final double MIN_VOLUME = 0.0;
    private static final double MAX_VOLUME = 1.0;

    private final Map<String, AudioClip> effects = new HashMap<>();
    private MediaPlayer bgmPlayer;
    private final Random random = new Random();
    private double musicVolume = 1.0;
    private double effectVolume = 1.0;
    private double currentBgmBaseVolume = MENU_BGM_BASE_VOLUME;

    private final List<String> bgmFiles = List.of("/bgm1.mp3", "/bgm2.mp3", "/bgm3.mp3", "/bgm4.mp3");

    private SoundManager() {
        loadEffect("shoot", "/shoot.mp3");
        loadEffect("step", "/step.mp3");
        loadEffect("hit", "/hit.mp3");
        loadEffect("melee", "/melee.mp3");
        loadEffect("die", "/die.mp3");
        loadEffect("pickup", "/pickup.mp3");
        loadEffect("click", "/click.mp3");
        loadEffect("explosion", "/explosion.mp3");
    }

    public static SoundManager getInstance() {
        return INSTANCE;
    }

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

    public void playRandomBgm() {
        stopBgm();
        if (bgmFiles.isEmpty()) return;

        String resourcePath = bgmFiles.get(random.nextInt(bgmFiles.size()));
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
                applyBgmVolume();
                bgmPlayer.play();
            } else {
                System.out.println("Warning: BGM not found: " + resourcePath);
            }
        } catch (Exception e) {
            System.err.println("Failed to load BGM: " + resourcePath + " (" + e.getMessage() + ")");
        }
    }

    public void playMenuBgm() {
        stopBgm();
        try {
            String resourcePath = "/menu_bgm.mp3";
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
                applyBgmVolume();
                bgmPlayer.play();
            } else {
                System.out.println("Warning: Menu BGM not found: " + resourcePath);
            }
        } catch (Exception e) {
            System.err.println("Failed to load Menu BGM: (" + e.getMessage() + ")");
        }
    }

    public void stopBgm() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
            bgmPlayer.dispose();
            bgmPlayer = null;
        }
    }

    public double getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(double volume) {
        musicVolume = clampVolume(volume);
        applyBgmVolume();
    }

    public double getEffectVolume() {
        return effectVolume;
    }

    public void setEffectVolume(double volume) {
        effectVolume = clampVolume(volume);
    }

    public double getMasterVolume() {
        return (musicVolume + effectVolume) * 0.5;
    }

    public void setMasterVolume(double volume) {
        setMusicVolume(volume);
        setEffectVolume(volume);
    }

    private void applyBgmVolume() {
        if (bgmPlayer != null) {
            bgmPlayer.setVolume(clampVolume(currentBgmBaseVolume * musicVolume));
        }
    }

    private static double clampVolume(double volume) {
        if (Double.isNaN(volume)) {
            return MAX_VOLUME;
        }
        return Math.max(MIN_VOLUME, Math.min(MAX_VOLUME, volume));
    }
}
