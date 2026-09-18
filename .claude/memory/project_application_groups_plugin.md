---
name: project-application-groups-plugin
description: "Application Group feature migration from core into a new plugin module, and the 5 new plugin extension points it introduced"
metadata:
  node_type: memory
  type: project
  originSessionId: b8ac6e85-7ef1-4752-aa0a-7d75a9a9881d
  modified: 2026-08-28T11:33:25.139Z
---

The Application Group feature (prefixes every model's id/filename with a shared group name) was converted from a built-in core feature into a new plugin module, `a12-studio-plugins/application-groups`, package `de.a12.studio.plugin.applicationgroups`. Full plan: see plan file used during implementation (2026-08-28 session) — key points below for future reference.

This required growing the plugin system from one extension point (`ICreateItemMenuEntry`) to six, all in `a12-studio-plugin-manager`'s `de.a12.studio.plugin.manager` package:
- `IProjectSettingsPanelContribution` (`"projectSettingsPanel"`) — see [[feedback_plugin_extension_point_naming]] for why it's not called `ISettingsPanelContribution`. A sibling `IGlobalSettingsPanelContribution` (`"globalSettingsPanel"`) for the app-wide Preferences section is anticipated but not yet built.
- `IModelSaveInterceptor` (`"modelSave"`)
- `IModelValidatorContribution` (`"modelValidator"`)
- `INewModelNameInterceptor` (`"newModelName"`)
- `IProjectOpenedListener` (`"projectOpened"`) — added specifically so the plugin could replicate the old core "auto-enable feature if any model already has the annotation" behavior that had nowhere else to live.

**Why:** the user wants plugins to be able to extend project settings, intercept model save, contribute model validators, intercept new-model filename creation, and react to project-open — generically, not just for this one feature. The application-groups plugin is the first (and so far only) consumer of all five.

**How to apply:** when adding a new plugin capability to this codebase, check whether one of these five extension points already covers it before inventing a new one. `ProjectItem`/`NewModelFactory` in `a12-studio-models` hold small plugin-agnostic static hook registries (`registerBeforeSaveHook`, `registerNameHook`) that `a12-studio-ui`'s `Studio.java` populates from `PluginManager` at startup — this pattern (generic hook in the lower module, plugin-aware adapter registration in the ui module) is how any future core-touchpoint extension point should be wired, since `a12-studio-models` cannot depend on `a12-studio-plugin-manager`.
