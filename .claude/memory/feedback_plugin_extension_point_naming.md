---
name: feedback-plugin-extension-point-naming
description: "Plugin extension-point interfaces must be named for their specific scope, not generically"
metadata:
  node_type: memory
  type: feedback
  originSessionId: b8ac6e85-7ef1-4752-aa0a-7d75a9a9881d
  modified: 2026-08-28T11:33:13.938Z
---

When adding a new plugin extension-point interface to `a12-studio-plugin-manager` (`de.a12.studio.plugin.manager`), name it for its specific scope rather than a generic name, even if only one variant exists today.

Concrete case: when contributing a settings panel that extends the **project**-settings section of the Preferences dialog, the interface must be `IProjectSettingsPanelContribution`, not `ISettingsPanelContribution` — the user explicitly wants a distinct future `IGlobalSettingsPanelContribution` for the app-wide settings section, and a generic name would collide/be ambiguous once that's added.

**Why:** the user corrected this during planning for the application-groups plugin (see [[project_application_groups_plugin]]) — they think ahead to sibling extension points in the same family (e.g. project vs. global settings) and want the naming to already disambiguate, rather than renaming later.

**How to apply:** before naming any new `I*Contribution`/`I*Interceptor`/`I*Listener` extension-point interface in this codebase, ask whether a plausible sibling variant (different scope, different trigger) could exist later, and bake that scope into the name up front (e.g. `IProjectXxx` vs `IGlobalXxx`, `IBeforeXxx` vs `IAfterXxx`).
