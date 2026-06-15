---
name: tak
description: >
  This skill should be used when the user wants to create a TAK branch and PR, says "/tak",
  "nowy TAK", "zrób TAK", "create tak branch", "open tak PR", "push as TAK", or provides
  a type (feat/fix/chore/test/cl/refactor/docs) and title to wrap into the tak{N}/type/slug
  branch convention with a [TAK-NNN] prefixed PR. Always invoke this skill for any request
  that involves creating a branch in the tak{N}/type/slug pattern or a PR titled [TAK-NNN].
---

# TAK Workflow

Create a git branch in `tak{N}/type/slug` format, stage and commit any changes, push, and
open a PR titled `[TAK-NNN] type: title`.

## Supported types

`feat` · `fix` · `chore` · `test` · `cl` · `refactor` · `docs`

## Workflow — execute in order

### Step 1 — Determine next N

```bash
git branch -a | grep -oE 'tak[0-9]+' | grep -oE '[0-9]+' | sort -n | tail -1
```

- If output is empty → N = 1
- Otherwise → N = (output + 1)
- NNN = zero-pad N to 3 digits (1→001, 10→010, 100→100)

### Step 2 — Get type and title

**First**: check if `.claude/tak-draft.json` exists:

```bash
cat .claude/tak-draft.json 2>/dev/null
```

- If file exists → read `type`, `title`, `commit` from it. Skip asking user.
- If file missing → parse from user input inline: `/tak {type}: {title}`
  - If user gave no type/title → ask: show the format and supported types, wait for reply

Derive `slug`: lowercase `title`, spaces→hyphens, strip special chars, trim edge hyphens.
- Example: "add user auth" → `add-user-auth`

Branch name: `tak{N}/{type}/{slug}`
PR title:    `[TAK-{NNN}] {type}: {title}`
Commit msg:  use `commit` from draft if available, else `{type}: {title}`

### Step 3 — Create branch

```bash
git checkout -b tak{N}/{type}/{slug}
```

### Step 4 — Stage and commit

```bash
git status --porcelain
```

- Changes exist → stage and commit:
  ```bash
  git add -A
  git commit -m "{commit_msg}"
  ```
- No changes → skip commit, note it

### Step 5 — Push and open PR

```bash
git push -u origin tak{N}/{type}/{slug}
```

```bash
gh pr create \
  --title "[TAK-{NNN}] {type}: {title}" \
  --body "$(cat <<'EOF'
## Summary
- {one-line description}

## Test plan
- [ ] Manual test

🤖 Generated with Claude Code
EOF
)"
```

### Step 6 — Cleanup and report

Delete the draft file if it was used:

```bash
rm -f .claude/tak-draft.json
```

Print branch name and PR URL to user.

## Examples

| Input | Branch | PR title |
|---|---|---|
| `/tak feat: add user auth` | `tak1/feat/add-user-auth` | `[TAK-001] feat: add user auth` |
| `/tak fix: null pointer in parser` | `tak2/fix/null-pointer-in-parser` | `[TAK-002] fix: null pointer in parser` |
| `/tak chore: update deps` | `tak5/chore/update-deps` | `[TAK-005] chore: update deps` |
| `/tak cl: release notes v2` | `tak10/cl/release-notes-v2` | `[TAK-010] cl: release notes v2` |

## Edge cases

- No TAK branches exist → N = 1
- `gh` not installed → skip PR, print command manually
- Title has Polish chars → leave as-is in PR title, strip in slug
- User provides N explicitly (e.g. "tak7") → use that N
