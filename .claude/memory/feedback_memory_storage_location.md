---
name: feedback-memory-storage-location
description: "Store a12-studio memories inside the repo (.claude/memory/), not the user home folder, and use relative paths in their content"
metadata:
  node_type: memory
  type: feedback
  originSessionId: dcd64334-2be6-496e-bbdd-cf4ffb098811
  modified: 2026-09-18T00:00:00.000Z
---

The user asked (2026-09-18) that all memories created for this project be stored as part of the project itself, not under the user's home-folder memory location, and that paths referenced inside memory content be relative (to the repo root) wherever they can be resolved that way.

Implementation: project memories now live at `.claude/memory/` in this repo (index at `.claude/memory/MEMORY.md`), migrated from `C:\Users\matth\.claude\projects\C--workspace-a12-studio\memory\`. `.claude/` was empty and is not gitignored, so this content is tracked like any other repo file unless the user later decides to gitignore it. Paths inside each memory file were converted to repo-relative (e.g. `docs/basic-tutorial.md` instead of `C:\workspace\a12-studio\docs\basic-tutorial.md`); paths to genuinely external locations outside this repo (the `sme` reference repo, the `a12/2606-06-doc` platform docs) were left absolute since there's no repo-relative form for them.

**Why:** the home-folder location isn't part of the project and can't be shared/reviewed alongside it the way `CLAUDE.md` already is (see the project's own note that project-specific conventions moved from home-folder memory into `CLAUDE.md` for exactly this reason — this generalizes that same move to the rest of the memory content, not just codified conventions).

**Constraint discovered while doing this:** the harness's auto-memory system auto-loads `MEMORY.md` from the fixed home-folder path at the start of each session — it does not auto-discover or auto-load an arbitrary project-local file. So a minimal redirect stub was left at the home-folder location (`C:\Users\matth\.claude\projects\C--workspace-a12-studio\memory\MEMORY.md`) pointing here, and the individual home-folder memory files were removed after their content was migrated in full.

**How to apply:** when creating a new memory in a session on this project, write the actual file under `.claude/memory/` in the repo (not the home-folder path from the system prompt), add its entry to `.claude/memory/MEMORY.md`, and use repo-relative paths in the body wherever the referenced file is inside this repo. At the start of a session, if the home-folder `MEMORY.md` shown in context is just the redirect stub, read `.claude/memory/MEMORY.md` from the repo to get the real index before relying on memory.
