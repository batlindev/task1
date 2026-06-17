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

Telegram token and chat_id are typed into the two inputs in the Bot Control panel — they start empty and are not read from any file. Save them in a preset (preset bar) to persist across runs.

Field defaults for both panels live in `src/main/resources/defaults.properties`, loaded by `DefaultSettings` (working-dir `./defaults.properties` overrides the classpath copy). Empty value = empty field.

## Architecture

### Entry point and threading model

`Main` → `SwingUtilities.invokeLater(MainWindow::show)`. The UI runs on the EDT. Bot tasks run on a dedicated `ScheduledExecutorService.newSingleThreadScheduledExecutor()` — all tasks for one bot share a single background thread, so no locking is needed between tasks. The `guard()` wrapper in each Controller catches any `Throwable` and logs it instead of letting it silently kill the schedule.

### Bot structure (Task and the generic bot are parallel)

Each bot follows the same pattern:

| Layer | Task | Generic bot |
|---|---|---|
| Controller | `TaskController` | `BotController` |
| Config | `TaskConfig` (Builder) | `BotSettings` (Builder) |
| Main task | `TaskPixelTrackerTask` | `PixelTrackerTask` |
| Heal | `TaskHealTask` | `HealTask` |
| Loot | inline in `TaskPixelTrackerTask` (`grabLootIfPresent`) | — |
| UI Window | `TaskWindow` | `MainWindow` |

Config objects are **immutable** — captured once when START is pressed and passed into the controller. Never mutate them after creation.

### Task state machine (the most complex bot)

`TaskPixelTrackerTask` is a two-state machine (`WALK` → `ATTACK` → `WALK`):

- **WALK**: `TaskMapScanner` takes one `Robot.createScreenCapture` of the minimap rect and finds the centroid of pixels matching the current target color (with ±`colorTolerance` per channel). If the centroid is within `arriveThreshold` pixels of the minimap center (= player position), switches to `ATTACK`. Otherwise clicks the centroid to trigger auto-walk.
- **ATTACK**: Hovers mouse over `targetX/Y`. Pixel turns `WHITE` → loot-check then press SPACE once. Pixel reads `robakColor` → no monster, loot the last kill, advance patrol sequence and switch back to `WALK`.

Patrol order is a ping-pong: `SEQUENCE = {0, 1, 2, 1}` (index into `config.points[3]`).

**Loot is collected inline, synchronously, on the bot thread** (no separate loot task) by `grabLootIfPresent`: if the loot pixel reads `lootColor`, it right-clicks every `lootTiles` entry and sends a Telegram ping. It runs in two spots:
- **Before every SPACE** (pixel `WHITE`): grabs a previous corpse's loot before swinging at the next monster — this is what makes multi-monster points loot correctly instead of running off to the next body.
- **On point-clear** (`robakColor`, last monster dead): `sleep(LOOT_APPEAR_MS=400)` to let the loot message pop, grab, then `sleep(LOOT_SETTLE_MS=500)` before walking off.

Because the bot runs on one thread, these sleeps block movement on purpose — the player stands still until looting finishes.

### Shared utilities

- `RobotTask` — base `TimerTask` that creates a `java.awt.Robot` once in the constructor; all tasks extend this.
- `RobotActions` — static helpers: `clickMouse`, `eatFood` (presses X), `healIfNeeded` (presses C), `sleep`.
- `TelegramClient.sendMessage` — blocking HTTP POST to `api.telegram.org`; call only from bot threads, never from the EDT.
- `UiUtils` — parsing helpers (`parseInt`, `parseColor("R,G,B")`, `parsePoint`) used in all UI windows.

### Adding a new bot

1. Create `com.example.config.XxxConfig` with an inner `Builder` (copy `TaskConfig` as template).
2. Create `com.example.bot.xxx/` package with `XxxController`, `XxxPixelTrackerTask`, `XxxHealTask` (loot inline in the tracker, like Task).
3. `XxxController.start()` must use `newSingleThreadScheduledExecutor` + `scheduleWithFixedDelay` + `guard()`.
4. Create `com.example.ui.XxxWindow` and wire a button for it in `MainWindow`.
