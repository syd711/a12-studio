---
name: a12-community-npm-and-maven-sources
description: When an SME module is a thin shell over an external @com.mgmtp.a12.* package (editor UI, validation), the package is downloadable anonymously from artifacts.geta12.com as npm tgz and Maven sources jar - read it instead of guessing what SME's editor does
metadata:
  type: reference
---

SME's `client/src/modules/<x>` is often only a shell (module registration, data provider, reference provider) around an external component such as `@com.mgmtp.a12.print/print-typesetting` (imports like `TypesettingModelEditor` from `.../lib/internal/ui/app/...`); `client/node_modules` is not checked out, so the real editor cannot be read in the SME repo. Two anonymous, credential-free sources (verified 2026-09-25):

- **npm** (compiled JS + `.d.ts`, readable): `https://artifacts.geta12.com/artifactory/api/npm/a12-community-npm/@com.mgmtp.a12.print/print-typesetting/-/print-typesetting-3.2.3.tgz` - has the whole UI (`lib/internal/ui/**`: components, `utils/custom-validator.js`, `utils/rule-conversion.js`, localization `en.js`/`de.js`) and `lib/internal/api/generated/model/DomainTypesettingMetaModel.json`. Strip the SPDX license block comment before reading, it is ~30 lines per file.
- **Maven sources**: `https://artifacts.geta12.com/artifactory/a12-community-maven/com/mgmtp/a12/print/print-typesetting/<v>/print-typesetting-<v>-sources.jar` (also `.jar`, `.pom`) - the server-side DTOs and validator, and the kernel meta-model `models/DomainTypesettingMetaModel.json` (field limits + rules).

Versions: SME pins 3.2.1 (`printVersion` in `gradle/a12.versions.toml`), which is **not** published; 3.2.3 (npm and Maven) is the closest. Maven listing: `.../print-typesetting/` shows 3.2.3, 3.2.4, 3.2.5, 4.0.0, 4.0.1. Same repo family as [[formengine-model-sources]]; `GET https://artifacts.geta12.com/artifactory/api/repositories` lists the repos (`a12-<yyyy>-06-community-npm`/`-maven`). EUPL-1.2 OR commercial: a12-studio stays clean-room, port the behavior, not the code.

**Why:** the Print Typesetting task ("run a deep analysis against SME what property editors and validations are required") could not be answered from the SME repo alone; the real answer (three rule tables, regex-shape classification, 20-char limits, roles rules) was only in these packages.

**How to apply:** for any model type whose SME module imports from `@com.mgmtp.a12.*`, download the tgz into the scratchpad (`curl -o x.tgz`, `tar xzf`) before designing the editor/validators; check `lib/internal/ui`, `lib/internal/api/validation` and `lib/internal/api/generated/model/*.json`. Applied for the Print Typesetting Model, see its section in `docs/sme-reference-comparison.md`.
