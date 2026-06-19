# bear

Bot z interfejsem (Java Swing).

## Pobierz i zainstaluj

Java jest spakowana w instalatorze — **nic więcej instalować nie trzeba**.

### 🪟 Windows

**[⬇ Pobierz bear-1.1.msi](https://github.com/batlindev/task1/releases/download/v1.1/bear-1.1.msi)**

Dwuklik w `.msi` → instaluje się, **bear** pojawia się w menu Start.

### 🐧 Linux (Ubuntu / Debian)

**[⬇ Pobierz bear_1.1-1_amd64.deb](https://github.com/batlindev/task1/releases/download/v1.1/bear_1.1-1_amd64.deb)**

```bash
sudo dpkg -i bear_1.1-1_amd64.deb
```

Potem uruchom **bear** z menu aplikacji (albo komenda `bear` w terminalu).

Odinstalowanie: `sudo dpkg -r bear`

> Wszystkie wersje: [Releases](https://github.com/batlindev/task1/releases).

## 📜 Co nowego (changelog)

> Ta sekcja jest aktualizowana automatycznie przez skill `/redme` przed każdym commitem/merge na `main`.
> Najnowsze zmiany na górze.

<!-- AUTO-CHANGELOG:START -->
### 2026-06-19
- 📦 **dist** — Release `v1.1`: instalator Windows `.msi` (47 MB) + Linux `.deb` (34 MB), Java w środku
- 🔧 **chore** — CI (GitHub Actions): push taga `vX.Y` buduje `.msi` (Windows) i `.deb` (Linux) i wrzuca do Release
- 📝 **docs** — README: osobne sekcje pobierania Windows / Linux, bump na v1.1
- 🤖 **skill** — `/redme` skanuje zmiany i aktualizuje ten changelog przed commitem/merge na `main`
- 📦 **dist** — pierwszy build `.deb` + GitHub Release `v1.0` (Java spakowana, instalacja dwuklikiem)
- 📝 **docs** — link do pobrania `.deb` w README + instrukcja instalacji i buildu
<!-- AUTO-CHANGELOG:END -->

<details>
<summary>📚 Starsze wpisy (przykładowe — jak będzie wyglądać przy dużej historii)</summary>

### 2026-06-18
- ✨ **feat** — panel presetów: zapis/odczyt wielu konfiguracji bota
- ✨ **feat** — ping-pong patrol (`SEQUENCE = {0,1,2,1}`) dla 3 punktów
- 🐛 **fix** — STOP nie zabijał wątku schedulera przy wyjątku w tasku
- ♻️ **refactor** — wydzielenie `RobotActions` ze wspólnych helperów myszy/klawiatury
- 🔧 **chore** — bump `maven-compiler-plugin` do 3.13.0

### 2026-06-17
- ✨ **feat** — inline loot grab przed każdym SPACE (multi-monster punkty)
- ✨ **feat** — `TelegramClient.sendMessage` ping przy zebranym loocie
- ⚡ **perf** — pojedynczy `Robot.createScreenCapture` na skan minimapy
- 🐛 **fix** — centroid liczony błędnie przy 0 dopasowanych pikseli
- 📝 **docs** — opis maszyny stanów WALK→ATTACK w CLAUDE.md

### 2026-06-16
- ✨ **feat** — `TaskMapScanner` centroid trackingu koloru celu
- ✨ **feat** — próg `arriveThreshold` przełączający WALK→ATTACK
- 🐛 **fix** — tolerancja koloru `±colorTolerance` per kanał
- ♻️ **refactor** — `RobotTask` jako baza tworząca `Robot` raz w konstruktorze
- ✅ **test** — ręczny scenariusz patrolu 3-punktowego

### 2026-06-15
- ✨ **feat** — auto-heal (klawisz C) przy progu HP
- ✨ **feat** — auto-eat (klawisz X) na interwale
- 🐛 **fix** — EDT blokowany przez blokujący HTTP POST do Telegrama
- 🔧 **chore** — `DefaultSettings` ładuje `defaults.properties` z classpath
- 📝 **docs** — sekcja Konfiguracja: token i chat_id z UI

### 2026-06-14
- ✨ **feat** — szkielet generic bota (`BotController` / `BotSettings`)
- ✨ **feat** — `MainWindow` Swing + Bot Control panel
- 🐛 **fix** — `parseColor("R,G,B")` rzucał przy spacjach
- ♻️ **refactor** — `UiUtils` parsujące helpery (`parseInt`, `parsePoint`)
- 🏷️ **cl** — pierwszy tag wewnętrzny `v0.1`

</details>

## Konfiguracja

Token Telegrama i chat_id wpisujesz w panelu Bot Control — startują puste, nie z pliku. Zapisz je w presecie, żeby przetrwały restart.

## Build ze źródeł (dla developera)

Wymaga JDK 17.

```bash
mvn package          # buduje jar do target/
```

### Nowy release (automat — zalecane)

Wystarczy push taga — CI (GitHub Actions) zbuduje `.msi` (Windows) i `.deb` (Linux)
i dołączy je do Release:

```bash
git tag v1.2
git push origin v1.2
```

Potem zaktualizuj wersję w linkach do pobrania na górze README.

### Ręczny build `.deb` (Linux)

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
mvn clean package
jpackage --type deb --name bear --app-version 1.2 \
  --input target --main-jar demo-1.0-SNAPSHOT.jar \
  --main-class com.example.Main --dest dist \
  --linux-shortcut --vendor batlin
```

Windows `.exe`/`.msi` da się zbudować tylko na Windows — dlatego robi to CI.
