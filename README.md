[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/IY9augGa)

# Gun Mayhem Arena - Project Map

This README is a quick guide for finding what lives where in this project.

## Quick Start

- Run game: `.\gradlew.bat run`
- Run tests: `.\gradlew.bat test`
- Generate Javadoc: `.\gradlew.bat javadoc`
- Build JAR: `.\gradlew.bat jar`
- Javadoc output: `build/docs/javadoc/index.html`
- JAR output: `build/libs/Project-1.0-SNAPSHOT.jar`

## Root Files (Top Level)

- `build.gradle`: Gradle build configuration and JavaFX setup
- `settings.gradle`: Gradle project settings
- `SUBMISSION_CHECKLIST.md`: submission checklist
- `*.puml`: PlantUML source diagrams in the project root
- `gradlew`, `gradlew.bat`, `gradle/wrapper/*`: Gradle wrapper files

## Main Source Code

Base path: `src/main/java/game`

- `GameMain.java`: app entry point and scene routing
- `config/GameSettings.java`: game constants/config values
- `entities/`: core gameplay entities
- `entities/weapons/`: gun interfaces + gun implementations
- `entities/traps/`: traps and explosion-related entities
- `entities/powerups/`: power-up entities and effects
- `logic/`: shared logic interfaces/services (camera, sound, timer, etc.)
- `map/`: map data, platform surfaces, map rendering
- `ui/`: JavaFX panels, overlays, match/gameplay screen UI

## Resources (Images, Sounds, UML PNGs)

Base path: `src/main/resources`

- `images/backgrounds/`: menu/background images
- `images/maps/`: map previews and map images
- `images/players/`: player sprites
- `images/weapons/`: weapon sprites
- `images/traps/`: trap sprites
- `images/effects/`: hit/explosion/blood effects
- `images/sunset/`: parallax sunset map layers
- `sounds/backgrounds/`: BGM tracks
- `sounds/effects/`: SFX clips
- `puml/`: exported UML images

## Tests

Base path: `src/test/java/game`

- `entities/`: tests for player, bullet, weapons, powerups
- `logic/`: tests for timer, camera, sound, update/render contracts
- `map/`: map/platform tests
- `ui/`: UI and drop-coordinator tests
- `testutil/`: shared test utilities

## Build Output

Base path: `build`

- `classes/`: compiled `.class` files
- `docs/javadoc/`: generated API docs
- `libs/`: generated `.jar` files
- `distributions/`: packaged distribution archives
- `test-results/`: XML test results
- `reports/` (if generated): Gradle reports

## Submission Artifacts (Tracked In Git)

- JavaDoc home: [build/docs/javadoc/index.html](build/docs/javadoc/index.html)
- JavaDoc overview: [build/docs/javadoc/overview-summary.html](build/docs/javadoc/overview-summary.html)
- Runnable artifact: [build/libs/Project-1.0-SNAPSHOT.jar](build/libs/Project-1.0-SNAPSHOT.jar)

Before pushing latest docs/artifacts:

```powershell
.\gradlew.bat javadoc jar
git add build/docs/javadoc build/libs/Project-1.0-SNAPSHOT.jar
```

## Where To Edit (Common Tasks)

- Change movement/combat constants: `src/main/java/game/config/GameSettings.java`
- Change player behavior: `src/main/java/game/entities/Player.java`
- Add/edit weapon logic: `src/main/java/game/entities/weapons/`
- Add/edit trap logic: `src/main/java/game/entities/traps/`
- Add/edit power-ups: `src/main/java/game/entities/powerups/`
- Change map collision/layout data: `src/main/java/game/map/GameMap.java`
- Change HUD/gameplay screen UI: `src/main/java/game/ui/GamePanel.java`
- Change menu/select screens: `src/main/java/game/ui/MenuPanel.java`, `MapSelectPanel.java`, `WeaponSelectPanel.java`
- Change sounds/music: `src/main/java/game/logic/SoundManager.java` and `src/main/resources/sounds/`

## Notes

- `build/` content is generated; source of truth is under `src/`.
- `.gitignore` is configured to keep most `build/` outputs ignored, except `build/docs/javadoc/**` and `build/libs/*.jar` for submission.
- Diagrams exist as both `.puml` (root) and rendered `.png` (`src/main/resources/puml`).
