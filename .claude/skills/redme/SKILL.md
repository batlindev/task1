---
name: redme
description: >
  This skill should be used when the user wants to refresh the README changelog before a
  commit or merge to main, says "/redme", "/readme", "zaktualizuj readme", "update readme",
  "wpisz zmiany do readme", "dodaj do changelog", or asks to scan what changed and add it to
  the README "Co nowego" section in a nice visual way. Run it right before committing or
  merging to main so the changelog stays current.
---

# /redme — auto-changelog do README

Skanuje zmiany (staged + względem `main`) i dopisuje ładny wpis do sekcji changelog w
[README.md](../../../README.md), między markerami `<!-- AUTO-CHANGELOG:START -->` i
`<!-- AUTO-CHANGELOG:END -->`. Najnowsze na górze. **Nigdy nie ruszaj** zwijanego bloku
`<details>` ze starszymi/przykładowymi wpisami — to tylko wizualny przykład.

## Kiedy

Uruchom **przed** commitem lub merge na `main` (ręcznie albo na życzenie usera). Jeśli chcesz
to w pełni automatycznie przy KAŻDYM commicie → potrzebny git hook (`pre-commit`), nie skill —
zaproponuj userowi konfigurację hooka, ale sam skill działa na wywołanie.

## Workflow — wykonaj po kolei

### Krok 1 — Zbierz zmiany

```bash
git status --porcelain
git diff --cached --stat            # staged
git diff --stat origin/main...HEAD  # względem main (jeśli na branchu)
git diff --cached                   # treść staged, do klasyfikacji
```

Jeśli nic nie ma w staged → użyj `git diff` (unstaged) + untracked. Jeśli zero zmian → nic nie
rób, powiedz userowi "brak zmian do wpisania".

### Krok 2 — Sklasyfikuj i opisz

Pogrupuj zmienione pliki w logiczne punkty (NIE jeden punkt na plik — łącz powiązane).
Każdy punkt: jedno krótkie zdanie po polsku, z badge typu i emoji. Mapowanie typ→emoji:

| Typ | Emoji | Kiedy |
|---|---|---|
| `feat` | ✨ | nowa funkcja |
| `fix` | 🐛 | poprawka błędu |
| `docs` | 📝 | dokumentacja / README |
| `refactor` | ♻️ | przebudowa bez zmiany zachowania |
| `perf` | ⚡ | wydajność |
| `test` | ✅ | testy |
| `chore` | 🔧 | konfiguracja / zależności / build |
| `dist` | 📦 | release / pakowanie / `.deb` |
| `cl` | 🏷️ | tag / wersja |
| `skill` | 🤖 | nowy/zmieniony skill `.claude/skills/` |

Format punktu: `- {emoji} **{typ}** — {opis}`

### Krok 3 — Data

```bash
date +%F   # np. 2026-06-19
```

### Krok 4 — Wstaw do README

Edytuj WYŁĄCZNIE obszar między `<!-- AUTO-CHANGELOG:START -->` a `<!-- AUTO-CHANGELOG:END -->`.

- Jeśli pierwszy nagłówek `### {dzisiejsza-data}` już istnieje tuż pod START → dopisz nowe
  punkty na górze listy tej daty.
- Jeśli nie → wstaw nowy blok zaraz po markerze START:
  ```
  ### {dzisiejsza-data}
  - {emoji} **{typ}** — {opis}
  ...
  ```
- Starsze daty zostają niżej, w obrębie markerów. Najnowsza data zawsze pierwsza.

Nie duplikuj punktu, który już jest na liście danej daty.

### Krok 5 — Raport

Pokaż userowi dopisane punkty i przypomnij: teraz commit/merge (np. przez `/tak`).

## Edge cases

- Brak markerów w README → odtwórz sekcję `## 📜 Co nowego (changelog)` z parą markerów i
  pustą datą, potem wstaw wpis.
- Zmiana dotyczy tylko samego README/changelog → nie zapętlaj, nie dodawaj punktu „update readme”.
- Bardzo dużo zmian → maks ~6-8 punktów na dzień, zgrupuj resztę; nie wypisuj każdego pliku.
