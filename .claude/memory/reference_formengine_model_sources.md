---
name: formengine-model-sources
description: The A12 Form Engine model library (include expansion, form model classes, consistency rules) is downloadable WITH SOURCES from the anonymous community Maven repo - read it instead of guessing Form Engine behavior
metadata:
  type: reference
---

`https://artifacts.geta12.com/artifactory/a12-community-maven/com/mgmtp/a12/formengine/formengine-model/<version>/formengine-model-<version>-sources.jar` (also `.jar` and `.pom`) - no credentials. Versions seen 2026-09-20: 38.4.0-38.4.3, 39.0.0/39.0.1. SME's own pin (38.3.0, `gradle/a12.versions.toml` `formengineSupportedVersion`) is **not** published; 38.4.3 is the closest. EUPL-1.2 OR commercial; a12-studio stays clean-room, so port the *behavior*, don't paste code.

What is in it that matters here: `com.mgmtp.a12.melies.model.include.IncludeExpansion`/`IncludeMapper` (Form Model include expansion, the source of truth for `FormIncludeExpander`), `melies.model.visitor.ModelWalker` (what a full traversal of a form model covers), the `consistency.rules.*` checks (candidates for form validators), the JSON (de)serializers.

**Why:** SME's editor delegates form-engine behavior to this library (e.g. include expansion is a Gradle batch task, not editor code), so SME's TypeScript alone cannot tell you what "correct" is - the TODO #8 design pass assumed SME had include-insertion code and it does not. **How to apply:** before designing/porting anything that SME does via `com.mgmtp.a12.formengine:*`, download this sources jar (curl into the scratchpad, `unzip`) and read the relevant classes. The kernel equivalent is described in `docs/sme-reference-comparison.md` ("Kernel dependency spike").
