# bear

Bot with a GUI (Java Swing).

## Download and install

Java is bundled inside the installer — **nothing else needs to be installed**.

### 🪟 Windows

**[⬇ Download bear-1.1.msi](https://github.com/batlindev/task1/releases/download/v1.1/bear-1.1.msi)**

Double-click the `.msi` → it installs, and **bear** appears in the Start menu.

### 🐧 Linux (Ubuntu / Debian)

**[⬇ Download bear_1.1-1_amd64.deb](https://github.com/batlindev/task1/releases/download/v1.1/bear_1.1-1_amd64.deb)**

```bash
sudo dpkg -i bear_1.1-1_amd64.deb
```

Then launch **bear** from the applications menu (or the `bear` command in a terminal).

Uninstall: `sudo dpkg -r bear`

> All versions: [Releases](https://github.com/batlindev/task1/releases).

## 📜 What's new (changelog)

> This section is updated automatically by the `/redme` skill before every commit/merge to `main`.
> Latest changes at the top.

<!-- AUTO-CHANGELOG:START -->
### 2026-06-19
- ♻️ **refactor** — the old generic bot classes (`BotController`, `PixelTrackerTask`, `HealTask`, `PressXTask`, `TelegramAutoClickTask`) moved to the `com.example.bot.legacy` package
- 📦 **dist** — Release `v1.1`: Windows installer `.msi` (47 MB) + Linux `.deb` (34 MB), Java included
- 🔧 **chore** — CI (GitHub Actions): pushing a `vX.Y` tag builds the `.msi` (Windows) and `.deb` (Linux) and uploads them to the Release
- 📝 **docs** — README: separate download sections for Windows / Linux, bumped to v1.1
- 🤖 **skill** — `/redme` scans changes and updates this changelog before a commit/merge to `main`
- 📦 **dist** — first `.deb` build + GitHub Release `v1.0` (Java bundled, double-click install)
- 📝 **docs** — `.deb` download link in the README + install and build instructions
<!-- AUTO-CHANGELOG:END -->

<details>
<summary>📚 Older entries (sample — how it will look with a large history)</summary>

### 2026-06-18
- ✨ **feat** — presets panel: save/load multiple bot configurations
- ✨ **feat** — ping-pong patrol (`SEQUENCE = {0,1,2,1}`) for 3 points
- 🐛 **fix** — STOP no longer killed the scheduler thread on an exception in a task
- ♻️ **refactor** — extracted `RobotActions` from the shared mouse/keyboard helpers
- 🔧 **chore** — bumped `maven-compiler-plugin` to 3.13.0

### 2026-06-17
- ✨ **feat** — inline loot grab before every SPACE (multi-monster points)
- ✨ **feat** — `TelegramClient.sendMessage` ping when loot is collected
- ⚡ **perf** — a single `Robot.createScreenCapture` per minimap scan
- 🐛 **fix** — centroid computed incorrectly with 0 matched pixels
- 📝 **docs** — description of the WALK→ATTACK state machine in CLAUDE.md

### 2026-06-16
- ✨ **feat** — `TaskMapScanner` target-color centroid tracking
- ✨ **feat** — `arriveThreshold` threshold switching WALK→ATTACK
- 🐛 **fix** — color tolerance `±colorTolerance` per channel
- ♻️ **refactor** — `RobotTask` as a base that creates the `Robot` once in the constructor
- ✅ **test** — manual 3-point patrol scenario

### 2026-06-15
- ✨ **feat** — auto-heal (C key) at an HP threshold
- ✨ **feat** — auto-eat (X key) on an interval
- 🐛 **fix** — EDT blocked by the blocking HTTP POST to Telegram
- 🔧 **chore** — `DefaultSettings` loads `defaults.properties` from the classpath
- 📝 **docs** — Configuration section: token and chat_id from the UI

### 2026-06-14
- ✨ **feat** — generic bot skeleton (`BotController` / `BotSettings`)
- ✨ **feat** — `MainWindow` Swing + Bot Control panel
- 🐛 **fix** — `parseColor("R,G,B")` threw on spaces
- ♻️ **refactor** — `UiUtils` parsing helpers (`parseInt`, `parsePoint`)
- 🏷️ **cl** — first internal tag `v0.1`

</details>

## Configuration

You type the Telegram token and chat_id into the Bot Control panel — they start empty, not from a file. Save them in a preset so they survive a restart.

## Build from source (for developers)

Requires JDK 17.

```bash
mvn package          # builds the jar into target/
```

### New release (automated — recommended)

Just push a tag — CI (GitHub Actions) will build the `.msi` (Windows) and `.deb` (Linux)
and attach them to the Release:

```bash
git tag v1.2
git push origin v1.2
```

Then update the version in the download links at the top of the README.

### Manual `.deb` build (Linux)

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
mvn clean package
jpackage --type deb --name bear --app-version 1.2 \
  --input target --main-jar demo-1.0-SNAPSHOT.jar \
  --main-class com.example.Main --dest dist \
  --linux-shortcut --vendor batlin
```

The Windows `.exe`/`.msi` can only be built on Windows — that's why CI does it.
