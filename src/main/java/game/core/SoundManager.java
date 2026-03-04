package game.core;

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
    private final Map<String, AudioClip> effects = new HashMap<>();
    private MediaPlayer bgmPlayer;
    private final Random random = new Random();

    // รายชื่อไฟล์ BGM ในโฟลเดอร์ resources ที่ระบบจะสุ่มนำมาเล่น
    private final List<String> bgmFiles = List.of("/bgm1.mp3", "/bgm2.mp3", "/bgm3.mp3");

    private SoundManager() {
        // โหลดเสียง Effects ต่างๆ เตรียมไว้ล่วงหน้า
        loadEffect("shoot", "/shoot.mp3");
        loadEffect("step", "/step.mp3");
        loadEffect("hit", "/hit.mp3");
        loadEffect("melee", "/melee.mp3");
        loadEffect("die", "/die.mp3"); // เพิ่มโหลดเสียงตอนตกตาย
        loadEffect("pickup", "/pickup.mp3");
        loadEffect("click", "/click.mp3"); // เพิ่มเสียงคลิกปุ่ม
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
            // เผื่อกรณีรันผ่าน IDE โดยตรงแล้วหา Resource ไม่เจอ
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
            // สุ่ม Pitch (ระดับเสียงและความเร็ว) เล็กน้อยเพื่อให้เสียงดูมีมิติ ไม่ซ้ำซากเกินไป
            if ("shoot".equals(name) || "step".equals(name) || "hit".equals(name)) {
                clip.setRate(0.9 + random.nextDouble() * 0.2);
            } else {
                clip.setRate(1.0);
            }
            clip.play();
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
                bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE); // เล่นวนซ้ำ
                bgmPlayer.setVolume(0.35); // ปรับระดับความดังเพลง
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
                bgmPlayer.setVolume(0.40); // ปรับระดับความดังเพลงหน้าเมนู
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
}