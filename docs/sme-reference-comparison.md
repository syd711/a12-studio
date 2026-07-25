# SME Reference Comparison

This document exists to help anyone working on a12-studio compare it against **SME (Simple Model Editor)**,
the original/reference implementation at `C:\workspace\sme`, part of the mgm A12 low-code platform. a12-studio is a
from-scratch **Java reimplementation** of the same modeling tool concept — not a port. Use this doc to see what
SME's editors do, what a12-studio currently has, and what's missing.

Last analyzed: 2026-07-17. SME evolves independently of this repo — re-verify specifics (file paths, endpoint
names) against `C:\workspace\sme` before relying on them for anything but general orientation.

## Architecture: how the two projects actually relate

SME is a polyglot monorepo: Kotlin/Spring Boot backend (`backend/`) + TypeScript/React/Redux frontend (`client/src`,
split into `core`, `modules` [one per model type], `app`, `a12Extension`, `packages`), packaged as an Electron
desktop app.

**Key insight:** SME's backend is not a distinct architectural layer conceptually — it's a thin REST wrapper around
the A12 kernel libraries (`com.mgmtp.a12.kernel:*`, `com.mgmtp.a12.tdg:*`, `com.mgmtp.a12.print:*`). It has
no database, no file persistence, no session state; every endpoint takes model JSON in and returns a computed
result. File load/save is purely a frontend/Electron filesystem concern.

**a12-studio does not depend on the kernel.** `a12-studio-data-services/build.gradle` has no kernel/print/base
dependencies today — only `project(':a12-studio-models')` plus test libraries. An earlier version of this doc
claimed a12-studio "pulls in the same kernel libraries directly" and listed specific coordinates as already present;
that was never actually true (no such `build.gradle` entries exist in git history, and there is no
`ValidationRuleService.java` in this repo). The kernel's Community-edition artifacts (e.g. `kernel-md-facade`,
`kernel-md-model`) are in fact anonymously downloadable from `artifacts.geta12.com` — dual-licensed EUPL-1.2/commercial,
same as this repo's own `LICENSE` — so an in-process kernel dependency is a legally and technically viable path, not
a blocked one. But taking it is a real architectural decision (large transitive dependency footprint; turns
a12-studio from an independent reimplementation into a kernel wrapper for whatever slice uses it), so it hasn't been
taken. The strategy actually in use is a **clean-room, data-driven port**: read the same JSON rule definitions the
kernel/SME ship (e.g. `client/resources/models/documentModel/Domain*.json`) and evaluate them with a
purpose-built interpreter in `a12-studio-data-services`, rather than either reimplementing SME's REST endpoints or
depending on the real kernel jars. See the `SchemaVersionValidator`, `DuplicateIdValidator`,
`NumberFieldValueLimitValidator`, `MultiSelectGroupValidator`, `AttachmentGroupValidator`, and
`BasicConsistencyValidator` classes for the (currently hand-ported, not yet data-driven) document-model-level
rules, and the planned meta-model validation rule engine (below) for the much
larger family of field/group/rule/computation config-validation rules SME's `Domain*.json` files define.

---

## Document Model

The one editor that exists in a12-studio today (`a12-studio-ui/src/main/java/de/a12/studio/ui/editors/documentmodel/`,
data model in `a12-studio-data-services/.../models/documentmodel/`).

### Data model — well aligned

a12-studio's data classes (`GroupConfig`, `FieldElement`, `RuleElement`, `ComputationElement`, and all field-type
classes: String/Number/Enumeration/Boolean/Confirm/Date/DateTime/Time/DateRange/DateFragment/Custom/TypeDef) mirror
SME's schema (`commonDocumentModel/api/document/serializedDocumentModel.ts`) closely, including `usageType` on
`GroupConfig` for attachment/multi-select groups and `modelAlias` for includes.

SME's shape, for reference:

```
Model { header, content: { modelInfo, modelConfig, typeDefinitions?, modelRoot: { rootGroups: Group[] } } }
Element = Group | Field | Rule | Computation   (discriminated by `type`)
```

- **Group**: `elements?`, `repeatability`, `indexFieldName?`, `modelAlias?` (Include), `includeLevel`,
  `excludeRules?`/`excludeComputations?`. Special `usageType` variants: `attachment` (fixed field set:
  `original_filename`, `internal_filename`, `content`, `attachment_id`, `size`, `mime_type`, `category`,
  `description`, plus auto-created mutual-exclusivity rules), `multi-select` (repeatability forced to 999999,
  single `value` enum child).
- **Field**: `fieldType` (tagged union — see field-type list above), `label`, `helperText?`, `global?`,
  `transient?`, `requirednessConfig?` (`notRequired` / `absoluteOrRelativeToNextRepAncestor` /
  `relativeToParent`).
- **Rule** (validation rule): `errorEntityRelPath`, `errorCode`, `errorCondition` (kernel condition-language
  expression), `severity` (Error/Warning/Info), `errorMessage` (localized).
- **Computation**: `computedFieldRelPath`, `commonPrecondition?`, `computationAlternatives[]` (each with
  `precondition?`/`operation?`, evaluated in order), `roundingMode?`.
- **TypeDefinition**: a named, reusable `fieldType`, importable from a separate Type Definition Model.

### Editor features — gap list

a12-studio's current editor: tree/detail split view (`DocumentModelElementsTreeController`), group vs. field
detail editors, undo/redo via a `CommandStack`, search, `DeleteNodeCommand`, Settings + Type-Definitions dialogs.
Condition validation/formatting already calls the same kernel APIs as SME.

Missing or worth checking against SME (`commonDocumentModel/api/editor/*`):

| Feature | SME reference | a12-studio status | Won't Fix |
|---|---|---|---|
| Rule contradiction / consistency check | TDG constraint solver (`checkRuleContradictions`, endpoint `/api/document-model/check-rule-contradictions`) detects logically unsatisfiable rule sets (e.g. a field required but an error rule fires whenever it's filled) | Missing — no `tdg` dependency at all | |
| Move/rename refactoring | Auto-rewrites rule/computation condition text referencing a moved/renamed element (`moveElementApi.ts` → backend `/move-element-with-refactoring`) | Only `DeleteNodeCommand` exists; no move/rename refactoring command found | |
| Ad hoc testing / live preview | Select elements (Alt+T), server generates a reduced test Document+Validation model, renders in a popup preview | Missing | |
| Copy elements from another Document Model | "Insert from DM" modal, resolves includes, copies/imports type defs | Missing | |
| Model diff / compare | `hasModelDiffEditor`, full settings/tree/typedef diff | Missing | |
| Drag & drop reorder/reparent in tree | Per-element `dnd` metadata (`draggable`/`droppable`/`reorderable`) | Not confirmed present — check tree FXML/controller | |
| Tree filtering (by type, category, annotated-only, etc.) | Rich filter panel (`dmEditorView` filters) | Not confirmed present | |
| AI-assisted model generation | `documentModel/ai/*` — generates a DM from a prompt/PDF via `@com.mgmtp.ai.generation` | Likely out of scope — needs a conscious decision | |
| Additive Document Model (overlay/inherit/overwrite editing) | Separate module (`additiveDocumentModel`), full editing mode | `kernel-md-join` dependency present but no editor concept yet | |
| Composed Document Model (graph composition via Element Picker) | Separate module (`composedDocumentModel`) | Missing | |
| Multi-select bulk actions | Ctrl+M panel, bulk delete/cut/copy, bulk "Ad hoc Test" | Not confirmed present | |
| Markdown report generation per element | `createMarkdownReport`, used for AI/export tooling | Missing | |

### Load/save/validate flow (SME reference)

- **Load**: file → `EditorDocumentModel` graph via `IOTransformation.toGraph`/`deserializeDocumentModel`, resolving
  relative kernel paths (`errorField`, `computedField`, `indexField`) to internal IDs; migration is delegated to
  the backend (`DMMigrationService.migrate`, wraps kernel `DocumentModelMigrator`); includes/imports are resolved
  via a separate `/expand` call.
- **Save**: `beforeSave` module hook → recompute `header.modelReferences` from Includes → `IOTransformation.toDocument`
  serializes graph back to JSON, optionally collapsing (stripping) included/imported elements. No autosave; explicit
  Save/Save As only. Deleting a Field cascades to remove any Rule/Computation referencing it.
- **Validate**: two layers — client-side structural/form-level validation (kernel document validator against each
  element's meta-model) plus server-side (`DMValidationService.validate`: kernel consistency checker + custom
  structural checks — dangling Include refs, missing index fields, duplicate names in a group, missing computed-field
  target, missing type-def on a `TypeDefType` field, enum with <2 values in a multi-select). Rule-contradiction
  checking (TDG) is a separate, manually-triggered third layer.

---

## Form Model — not started

`a12-studio-ui/.../editors/formmodel/` is currently **empty**. No JVM kernel library for form models exists in
`build.gradle` yet — that's a prerequisite before any editor work here.

SME's `formModel` module (`client/src/modules/formModel/`) is the largest module in the SME codebase. Key points to
plan around:

- **Data shape**: a normalized `DocumentGraph` (`{docs, links}` — flat, `docRef`-keyed elements with typed parent/child
  `Link`s), not a naive nested tree. On disk it's the kernel's nested instance-document shape; conversion happens via
  per-element-type mapping rules (`transformer/mapping/{ddg2json,json2ddg}.ts`).
- **Element types** (16 kinds): `Screen`, `Section`, `MultiColumnSection`, `ControlGrid`, `Row`, `Control`, `TextCell`,
  `ExpressionCell`, `CustomCell`/`CustomScreenElement`, `ButtonPanel`, `DetachedRepeat`, `EmbeddedRepeat`,
  `InlineRepeat`, `BindingRepeat`, `Binding`, `*RepeatOverviewColumn`.
- **Binding layer**: every `Control`/`Repeat` references a Document Model field/group via `elementRef`/`groupRef`;
  presentation config (label, hint, dependent enum, readonly, etc.) lives centrally in
  `fieldConfiguration`/`groupConfiguration` arrays on the form-model root, keyed by that reference — not duplicated
  per screen usage.
- **Self-hosting editor pattern**: editing an element (e.g. a `Control`) opens a small generated form driven by a
  meta document+form model pair (e.g. `Control-Form`) — the editor is built using the same Form Engine the runtime
  app uses.
- **Editor tabs**: Screens (tree), Settings, Data Configuration (dependencies: dependent enums/fields/groups, hide
  conditions), Cleanup (orphaned field/group config entries after DM changes elsewhere).
- **Distinctive features**: live browser preview (postMessage-synced), repeat-type conversion
  (Detached⇄Embedded⇄Inline⇄Binding), dependent controls (show/hide based on a master field), custom row actions,
  responsive layout (per-breakpoint offset/span), style presets, includes (transclude a subtree from another form
  model), Composed-Document-Model relationship bindings (`Binding`/`BindingRepeat`).
- **Validation**: three layers — client-side kernel meta-model validation, ~20 hand-written custom conditions
  (layout column-count consistency, dependent-enum/field/group master-field-required rules, CDM relationship
  cardinality checks, hide-condition completeness), and a server-side structural consistency check between the form
  model and its expanded document model (`/api/form-model/check-consistency`).
- **Cross-module dependency**: hard dependency on Document Model (resolves/expands it via the DM module's own public
  `api/`); `Binding`/`BindingRepeat` additionally depend on Relationship Model + Composed Document Model.

---

## Other model types — survey and priority

Every SME module implements `SMEModule`/`DefaultSMEModule` and (if it's a standalone file type) registers an
`ExplorerEntry`. Load/save nearly always follows the same pattern: parse raw workspace files into an in-memory
document, serialize back to JSON (occasionally YAML) on save.

### Priority order for a12-studio, cross-referenced against existing kernel deps/scaffolding

| # | Module | Why this priority |
|---|---|---|
| 1 | **structuralMappingModel** | Kernel lib present (`kernel-md-structuralmapping-tool`); `SmmService`/`AddFieldMappingDto` scaffolding exists in `a12-studio-data-services`. SME's editor: source-tree/target-tree drag&drop field mapper, resolution-strategy editor for conflicts. Foundational — referenced by mappingModel and combinationModel. |
| 2 | **mappingModel** | Depends on structuralMappingModel + additiveDocumentModel; scaffolding exists (`SMEMappingModelService`, `MappingModelComputationDto`, `StructuralMappingModelGenerationDto`). ETL-style: source DM(s) + target DM + optional precomputation, driven by a referenced SMM. |
| 3 | **combinationModel** | Kernel lib present (`kernel-md-combination-model`); `CombinationModelExpansionService` scaffolding exists. Composes a base DM + ordered `CombinationStep`s (Addition/Selection/Decoration) referencing Additive/Selection models — multi-step tabbed editor with live preview. |
| 4 | **additiveDocumentModel** | Kernel lib present (`kernel-md-join`) but no dedicated data model/editor yet. Hard dependency of both mappingModel and combinationModel — needed before those are fully usable. Overlay editing mode: elements are included/overwritten/purely-additive relative to a base DM. |
| 5 | **relationshipModel** | No current scaffolding, but foundational — link, masterDetailModel, treeModel, modelGraphDiagram, and formModel's `Binding`/`BindingRepeat` all reference it. |
| 6 | **selectionModel** | Needed by combinationModel; reusable filter/subset spec over DM data. |
| 7 | **printModel** | Kernel libs present (`print-engine-api/runtime`); `PrintService`/`DocumentModelResolver`/`PrintParameters` scaffolding exists. Most editor-complex of the print family — relies on an external print-engine component library for the layout canvas. Backend renders PDF only (PDFBox or legacy engine), no HTML path. |
| 8 | **printSettingModel / printTypesettingModel** | Small, no cross-model references — cheap wins once printModel work begins. `print-typesetting` lib already present; no dedicated model/service for either exists yet. |
| 9 | **link / document** | Record-editing modules depending on relationshipModel/documentModel. `document` = data *instances* of a Document Model (don't confuse with Document Model itself). |
| 10 | **queryModel / overviewModel** | Search/filter/list-screen configuration; consumer-side (built on top of a DM), not blocking other model types. |
| — | **appModel, masterDetailModel, modelGraphDiagram, treeModel, transformerModel, contentModel, typeDefinitionModel, umModule, settingsModule, filesModule, attachment, data** | Standalone but lower cross-reference count or explicitly experimental in SME itself (contentModel, mappingModel, queryModel, selectionModel, combinationModel, transformerModel, structuralMappingModel are all marked `isExperimental()` in SME). |
| n/a | **common, preview** | Not model types. `common` = shared editor UI building blocks (element picker, expression editor, tabbed-frame scaffolding) used across modules. `preview` = pure runtime capability (opens a browser window running the live app, synced via postMessage), no persistence, no explorer entry. |

### One-line descriptions of every other module (for orientation)

- **appModel** — overall application structure: navigation, module registration, entry screens.
- **attachment** — binary/file attachments (images, PDFs); opaque content, type inferred from directory location.
- **combinationModel** — see priority table.
- **common** — shared UI/support module, not a model type.
- **contentModel** — CMS-like page/content layout; backed by an external content-engine package; experimental.
- **data** — manages the `data/` directory structure and workspace seed metadata; not itself an editable model.
- **document** — individual data document/record *instances*, validated against a Document Model (e.g. seed data).
- **filesModule** — generic workspace resources (images, theme/CSS assets); no structured content parsing.
- **link** — a relationship-model-backed link between two document instances.
- **mappingModel** — see priority table.
- **masterDetailModel** — master-detail UI screen composition, embedded into appModel.
- **modelGraphDiagram** — visual ER-style diagram over document models and relationship models.
- **overviewModel** — list/table "overview" screens (search, filter, columns, row actions) over a DM or query model.
- **preview** — not a model type; live browser preview capability.
- **printModel** — see priority table.
- **printSettingModel** — print output settings (page size, margins); no cross-model references.
- **printTypesettingModel** — typography/typesetting rules referenced by print models; no cross-model references.
- **queryModel** — reusable structured query (filter/sort/selection tree) over a DM; experimental.
- **relationshipModel** — associations/candidate constraints, ordered links between DM entities — backbone for link/masterDetailModel/treeModel/formModel bindings.
- **selectionModel** — reusable selection/filter spec over DM data; experimental.
- **settingsModule** — single workspace-level `settings.yaml` (deployment exclusions, global project settings) — the one clear YAML (not JSON) file type besides umModule.
- **structuralMappingModel** — see priority table.
- **transformerModel** — computes/derives a DM from another DM plus transformation rules; stores only the rules on disk and reconstructs the result at load time; experimental.
- **treeModel** — hierarchical navigation trees composed of DM/relationship-model-backed nodes.
- **typeDefinitionModel** — restricted DM variant defining only reusable types (enums/structs), no instance data; reuses the whole DM editor infrastructure.
- **umModule** — user-management config: two YAML file types, "roles" and "users".

---

## Backend / kernel capability map

Server-side (or, for a12-studio, in-process kernel) computations that a browser/pure-client editor could not do
on its own:

| Capability | SME backend | a12-studio equivalent |
|---|---|---|
| Rule contradiction / consistency (constraint solver) | `RuleContradictionCheckService` → `com.mgmtp.a12.tdg.lib.TestDataGenerator.checkModel(...)` | **Missing** — no `tdg` dependency |
| Document model structural/consistency validation | `DMValidationService` (kernel `getElementProblems` + custom checks) | Partially present (`ValidationRuleService`, `ComputationRuleService` call the same kernel APIs) |
| Condition/expression language validation & formatting | `ValidationRuleService`, `ComputationRuleService` (Kotlin) | Present — `documentmodel/validationrule`, `documentmodel/computationrule` (Java), same kernel calls |
| Print rendering (PDF) | `PrintService` — PDFBox or legacy engine via `a12.print.engine.runtime` | Scaffolding present (`PrintService.java`, `DocumentModelResolver.java`, print-engine deps) but editor missing |
| Document model expansion (includes/imports) | `ExpansionService` | Not yet confirmed — check `documentmodel/features` |
| Combination Model expansion | `CombinationModelExpansionService` | Present — `services/combinationmodel/` |
| Additive Model join | `AdditiveModelController` (`kernel-md-join`) | Dependency present, service scaffolding not yet confirmed |
| Selection Model join/validate | `SelectionModelController` | Not yet present |
| Structural Mapping Model consistency | `StructuralMappingModelService` (`SmmService`) | Present — `services/structuralmappingmodel/` |
| Mapping Model consistency/generation | `SMEMappingModelService` | Present — `services/mappingmodel/` |
| Test data generation | `TestDataService` (same TDG lib, generative mode) | Missing (needs `tdg` dep) |
| Formula/computation execution over content documents | `DocumentValidationService` (`docRtService.compute(...)`) | Not yet confirmed |
| XSD → Document Model transformation | `TransformerService` | Not present |
| Move/rename refactoring (rewrite condition text) | `MoveRefactoringService` | Not present (see Document Model gap list above) |
| File load/save persistence | None on SME's backend either — frontend/Electron-owned | a12-studio owns this directly (single JVM app) |

`a12-studio-server` currently contains only `A12StudioServer.java`, `A12StudioServerTest.java`, and
`SystemResource.java` — it is not yet where kernel calls happen; that logic currently lives in
`a12-studio-data-services`.

---

## How to keep this doc useful

This is a snapshot (2026-07-17). SME modules marked "experimental" here may graduate or change; kernel dependency
versions will drift. When picking up work in an area covered here, spot-check the relevant SME module/backend
service still looks the way this doc describes before trusting the gap list — SME is under active, independent
development.
