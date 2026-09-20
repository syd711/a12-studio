# Memory Index (project-local)

This directory holds a12-studio-specific memories, stored in the repo (not under the user's home folder) so they travel with the project. Paths inside these files are relative to the repo root unless noted.

(Project-specific conventions and *codified* rules previously stored here now live in `CLAUDE.md` at the repo root, so they're committed and shared with other developers. This directory is for looser project/feedback/reference memories not yet promoted to CLAUDE.md.)

- [Plugin extension-point naming](feedback_plugin_extension_point_naming.md) — name for specific scope (IProjectSettingsPanelContribution not ISettingsPanelContribution) to leave room for siblings
- [Application-groups plugin](project_application_groups_plugin.md) — feature migrated from core to a plugin; introduced 5 new plugin extension points, hook-registry pattern for models->ui wiring
- [Real Form Model preview](project_form_preview_real_rendering.md) — SME's fmm-support in-editor preview vs. Deploy->Preview App; what real rendering in a12-studio would require; user checking licensing/sources
- [Basic tutorial doc](reference_basic_tutorial_doc.md) — A12 "Basic Modeling" tutorial converted to `docs/basic-tutorial.md`
- [Web-build compile errors](feedback_web_build_compile_errors.md) — jspecify annotation placement on qualified types breaks Lombok project-wide; check local variable types match wrapper return types
- [Self-update excludes JRE](project_self_update_no_jre.md) — self-update must never bundle/restage the JRE on any OS; JRE updates require a full reinstall
- [Memory storage location](feedback_memory_storage_location.md) — project memories live in `.claude/memory/` in the repo, not the user home folder; home-folder memory only keeps a redirect pointer
- [Out-of-scope features](project_out_of_scope_features.md) — AI-assisted DM generation and Model diff/compare editor will NOT be built (decided 2026-09-19); don't propose them
- [Ad hoc testing: open, will be done](project_ad_hoc_testing_open.md) — DM ad hoc testing (Alt+T reduced model + preview) deferred 2026-09-20, NOT won't-do; blocked on kernel decision + preview engine
- [Known failing tests at HEAD](project_known_failing_tests.md) — two pre-existing red tests (Query validator, application-groups) as of 2026-09-19; plus the FxTestSupport JavaFX test harness
- [Form Engine sources](reference_formengine_model_sources.md) — `formengine-model` is downloadable with sources from the community repo; read it for Form Engine behavior (include expansion etc.) instead of guessing
- [Properties bundles: append only](feedback_properties_bundles_mixed_encoding.md) — messages*/validation-messages* mix raw UTF-8 and \u escapes and hold uncommitted work; never re-encode or rewrite them
