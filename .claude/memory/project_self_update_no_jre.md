---
name: project_self_update_no_jre
description: "Self-update packages/flows must never bundle the JRE, on any OS — a full reinstall handles JRE updates instead"
metadata:
  node_type: memory
  type: project
  originSessionId: 6e95531b-31af-43cb-8d5e-aa6b96ccd429
  modified: 2026-09-18T07:31:23.419Z
---

Self-update artifacts and code paths (`Updater.java`, `client-update-zip` in `.github/workflows/workflow.yml`, the mac/linux/windows update scripts) must never include or re-stage the bundled `java-runtime` folder, on any operating system (Windows, macOS, Linux) — this applies even though macOS currently uploads a `java-runtime-mac-*.zip` artifact alongside the DMG.

**Why:** the user explicitly decided that JRE updates should go through a full reinstall, not the lightweight self-update path (2026-09-18, during work on bundling a Windows JRE via launch4j's `bundledJrePath` — see [[project_windows_jre_bundling]]). Self-update should stay small/fast (jar + exe/binary only) and assume the JRE staged at install time (`{app}/java-runtime`) stays in place untouched.

**How to apply:** when touching `Updater.java`, any `update-client-*.sh`/`.bat` script, or the `client-update-zip`/similar CI steps, make sure the JRE directory is excluded from what gets downloaded/copied during a self-update. If a JRE version bump is ever needed, that should prompt the user toward the full installer/reinstall rather than extending self-update to carry the runtime. If macOS's existing `java-runtime-mac-*.zip` artifact turns out to actually be consumed by the self-update flow (not just uploaded for other reasons), that's a violation of this rule and should be fixed to match.
