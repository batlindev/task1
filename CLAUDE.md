# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Compile
mvn compile

# Package (fat jar)
mvn package

# Run from source
mvn exec:java

# Run packaged jar
java -jar target/demo-1.0-SNAPSHOT.jar
```

Java 17, no external dependencies — pure Java SE + Swing. No test suite.

## Configuration

Copy `src/main/resources/config.properties.example` → `config.properties` (next to the jar, or in `src/main/resources/`) and fill in Telegram credentials. `AppConfig` tries three sources in order: `./config.properties` (working dir) → classpath `config.properties` → classpath `config.properties.example`.

## Architecture

### Entry point and threading model

`Main` → `SwingUtilities.invokeLater(MainWindow::show)`. The UI runs on the EDT. Bot tasks run on a dedicated `ScheduledExecutorService.newSingleThreadScheduledExecutor()` — all tasks for one bot share a single background thread, so no locking is needed between tasks. The `guard()` wrapper in each Controller catches any `Throwable` and logs it instead of letting it silently kill the schedule.

### Bot structure (Bear and Wasp are parallel)

Each bot follows the same pattern:

| Layer | Bear | Wasp | Generic bot |
|---|---|---|---|
| Controller | `BearController` | `WaspController` | `BotController` |
| Config | `BearConfig` (Builder) | `WaspConfig` (Builder) | `BotSettings` (Builder) |
| Main task | `BearPixelTrackerTask` | `WaspPixelTrackerTask` | `PixelTrackerTask` |
| Heal | `BearHealTask` | `WaspHealTask` | `HealTask` |
| Loot | `BearMessageLootTracker` | `WaspMessageLootTracker` | — |
| UI Window | `BearWindow` | `WaspWindow` | `MainWindow` |

Config objects are **immutable** — captured once when START is pressed and passed into the controller. Never mutate them after creation.

### Bear state machine (the most complex bot)

`BearPixelTrackerTask` is a two-state machine (`WALK` → `ATTACK` → `WALK`):

- **WALK**: `BearMapScanner` takes one `Robot.createScreenCapture` of the minimap rect and finds the centroid of pixels matching the current target color (with ±`colorTolerance` per channel). If the centroid is within `arriveThreshold` pixels of the minimap center (= player position), switches to `ATTACK`. Otherwise clicks the centroid to trigger auto-walk.
- **ATTACK**: Hovers mouse over `targetX/Y`. Pixel turns `WHITE` → press SPACE once. Pixel reads `robakColor` → no monster, advance patrol sequence and switch back to `WALK`.

Patrol order is a ping-pong: `SEQUENCE = {0, 1, 2, 1}` (index into `config.points[3]`).

**Loot grace window**: after killing a monster the loot message appears slightly after the kill. `lootScanUntilMs` keeps `BearMessageLootTracker.shouldScanLoot()` returning `true` for 4 seconds after the tracker transitions back to WALK, so the loot message isn't missed.

### Shared utilities

- `RobotTask` — base `TimerTask` that creates a `java.awt.Robot` once in the constructor; all tasks extend this.
- `RobotActions` — static helpers: `clickMouse`, `eatFood` (presses X), `healIfNeeded` (presses C), `sleep`.
- `TelegramClient.sendMessage` — blocking HTTP POST to `api.telegram.org`; call only from bot threads, never from the EDT.
- `UiUtils` — parsing helpers (`parseInt`, `parseColor("R,G,B")`, `parsePoint`) used in all UI windows.

### Adding a new bot

1. Create `com.example.config.XxxConfig` with an inner `Builder` (copy `BearConfig` as template).
2. Create `com.example.bot.xxx/` package with `XxxController`, `XxxPixelTrackerTask`, `XxxHealTask`, `XxxMessageLootTracker`.
3. `XxxController.start()` must use `newSingleThreadScheduledExecutor` + `scheduleWithFixedDelay` + `guard()`.
4. Create `com.example.ui.XxxWindow` and wire a button for it in `MainWindow`.
