---
name: title
description: >
  This skill should be used when the user says "/title", "zaproponuj tytuł", "wymyśl tytuł",
  "co napisać w PR", "jaki commit", "podpowiedz tytuł", or wants Claude to analyze current
  git changes and suggest a commit message and PR title. After user confirms, saves the draft
  so /tak can use it without asking for type/title again.
---

# Title Suggester

Analyze current git changes, propose commit message + PR title, save draft on confirmation.

## Step 1 — Read changes

Run all three to get full picture:

```bash
git diff --staged
git diff
git status --porcelain
```

If all empty → tell user "no changes detected" and stop.

## Step 2 — Pick type and generate titles

Choose the **best-fit primary type** from: `feat` · `fix` · `chore` · `test` · `cl` · `refactor` · `docs`

Type selection guide:
- `feat` — new capability added
- `fix` — bug corrected
- `chore` — build, deps, config, tooling — no production logic change
- `test` — tests only
- `cl` — changelog / release notes
- `refactor` — restructure without behavior change
- `docs` — documentation only

Generate:
- **commit**: `{type}: {short imperative summary}` (≤50 chars, lowercase after colon)
- **title**: same as the part after `{type}: ` (this is what `/tak` uses as `{title}`)

## Step 3 — Present proposal

Show in this exact format:

```
Propozycja:

  type:   {type}
  commit: {commit}
  PR:     [TAK-NNN] {commit}

Alternatywy (jeśli pasują):
  • {alt-type}: {alt-commit}
  • ...

Zatwierdzasz? Tak → zapisuję draft. Nie → powiedz co zmienić.
```

- `[TAK-NNN]` is a placeholder — actual number assigned by `/tak`
- List 1-2 alternatives only if they genuinely fit; skip if only one type makes sense
- Keep titles tight — no "implement", "add support for", "update the" padding

## Step 4 — On confirmation

When user says yes / tak / ok / good / ✓ or similar:

Write `.claude/tak-draft.json` in the repo root:

```json
{
  "type": "{type}",
  "title": "{title}",
  "commit": "{commit}"
}
```

Then tell user: "Draft zapisany. Odpal `/tak` — tytuły gotowe."

## Step 5 — On rejection / correction

If user says no or provides correction:
- Adjust type/title per feedback
- Re-show the proposal (Step 3 format)
- Repeat until confirmed

## Notes

- Do NOT run `/tak` automatically — user decides when
- If user picks an alternative, save that one
- `.claude/tak-draft.json` is consumed and deleted by `/tak` after use
