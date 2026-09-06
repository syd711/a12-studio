# SME Reference Comparison

This document exists to help anyone working on a12-studio compare it against **SME (Simple Model Editor)**,
the original/reference implementation at `C:\workspace\sme`, part of the mgm A12 low-code platform. a12-studio is a
from-scratch **Java reimplementation** of the same modeling tool concept — not a port. Use this doc to see what
SME's editors do, what a12-studio currently has, and what's missing.

Last analyzed: 2026-07-17 (Document Model section's field-level/validator detail refreshed 2026-09-05 — see
"Field-level & validator gap analysis" below; rest of the doc not re-verified on that pass). SME evolves
independently of this repo — re-verify specifics (file paths, endpoint names) against `C:\workspace\sme` before
relying on them for anything but general orientation.

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
`GroupConfig` for attachment/multi-select groups and `includeConfig` for includes (see the correction below —
`modelAlias` is the *older*, superseded shape of this, not what a12-studio actually implements).

SME's shape, for reference (note: SME's own `modelAlias?` shown below is itself the pre-kernel-A12K-4102 shape;
current kernel versions use `includeConfig` instead, matching what a12-studio implements — see correction below):

```
Model { header, content: { modelInfo, modelConfig, typeDefinitions?, modelRoot: { rootGroups: Group[] } } }
Element = Group | Field | Rule | Computation   (discriminated by `type`)
```

**Correction (2026-09-05):** a12-studio previously also had a `GroupConfig.modelAlias` field alongside
`includeConfig`, described here as "for includes" — that was wrong. Kernel changelog A12K-4102
(`documentation/2606-06-doc/kernel-kernel-documentation-dev.md:2208-2215`) confirms a group's `modelAlias` was
replaced by `includeConfig` (reference/excludeRules/excludeComputations/includeLevel all moved under it) in kernel
DM version 28.6.0→29.0.0, with automatic migration; `modelAlias` is now ignored by the kernel entirely.
`includeConfig` — which a12-studio already correctly implements — is the actual, current mechanism; the dead
`GroupConfig.modelAlias` field has been removed.

- **Group**: `elements?`, `repeatability`, `indexFieldName?`, `includeConfig?` (Include: reference,
  `excludeRules?`/`excludeComputations?`, `includeLevel`). Special `usageType` variants: `attachment` (fixed field set:
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

**Correction (2026-09-05):** this section previously claimed condition validation/formatting "already calls the
same kernel APIs as SME" via Java `ValidationRuleService`/`ComputationRuleService` equivalents. Confirmed false
by grep — no such services, no kernel dependency anywhere in `a12-studio-data-services`/`a12-studio-models`. The
"Backend / kernel capability map" table below has the same correction applied to its "Condition/expression
language validation & formatting" row. Practical effect: Rule/Computation condition text (`errorCondition`,
`precondition`, `operation`) is edited as plain text with no semantic validation, the same way
`QueryModelContent.filterDefinition` and `overviewmodel.Column.expression` already work via
`RichtextEditorController` — not blocked on porting a condition-language grammar, but also not actually checked
for validity beyond "non-blank".

Missing or worth checking against SME (`commonDocumentModel/api/editor/*`):

| Feature | SME reference | a12-studio status | Won't Fix |
|---|---|---|---|
| Rule contradiction / consistency check | TDG constraint solver (`checkRuleContradictions`, endpoint `/api/document-model/check-rule-contradictions`) detects logically unsatisfiable rule sets (e.g. a field required but an error rule fires whenever it's filled) | Missing — no `tdg` dependency at all | |
| Move/rename refactoring | Auto-rewrites rule/computation condition text referencing a moved/renamed element (`moveElementApi.ts` → backend `/move-element-with-refactoring`) | Only `DeleteNodeCommand` exists; no move/rename refactoring command found | |
| Ad hoc testing / live preview | Select elements (Alt+T), server generates a reduced test Document+Validation model, renders in a popup preview | Missing | |
| Copy elements from another Document Model | "Insert from DM" modal, resolves includes, copies/imports type defs | Missing | |
| Model diff / compare | `hasModelDiffEditor`, full settings/tree/typedef diff | Missing | |
| Drag & drop reorder/reparent in tree | Per-element `dnd` metadata (`draggable`/`droppable`/`reorderable`) | **Present** (confirmed 2026-09-05) — `DocumentModelElementsTreeController.setupRowDragAndDrop`/`resolveDropPosition`: reorder above/below, reparent into, root-end drop, with fixed-children/attachment-adjacency vetoes | |
| Tree filtering (by type, category, annotated-only, etc.) | Rich filter panel (`dmEditorView` filters) | Not confirmed present | |
| AI-assisted model generation | `documentModel/ai/*` — generates a DM from a prompt/PDF via `@com.mgmtp.ai.generation` | Likely out of scope — needs a conscious decision | |
| Additive Document Model (overlay/inherit/overwrite editing) | Separate module (`additiveDocumentModel`), full editing mode | `kernel-md-join` dependency present but no editor concept yet | |
| Composed Document Model (graph composition via Element Picker) | Separate module (`composedDocumentModel`) | Missing | |
| Multi-select bulk actions | Ctrl+M panel, bulk delete/cut/copy, bulk "Ad hoc Test" | Not confirmed present | |
| Markdown report generation per element | `createMarkdownReport`, used for AI/export tooling | Missing | |

Also undocumented until now: a12-studio has a context-menu action with **no SME equivalent** —
"Create Overview Model from Selection" (`DocumentModelActions.onCreateOverviewModelFromSelection`), multi-select
fields/groups → generates a new Overview Model with one column per field.

### Field-level & validator gap analysis (2026-09-05)

SME's Document Model editor is not hand-coded per-element-type React forms — it's a **self-hosting Form Engine
editor**: Group/Field/Rule/Computation/TypeDefinition editing UI is generated at runtime from meta document+form
model pairs (`client/resources/models/documentModel/Domain*.json` + matching `*.json` form models). The
`Domain*.json` files are themselves Document Models that encode every field, every validation rule
(condition + per-language message + severity), and requiredness — they're the ground truth for "what SME lets you
edit and enforce," more authoritative than any individual `.tsx`. `elementEditorView.tsx` only customizes a
handful of special widgets on top of the generated form: rule/precondition/calculation condition editors with
kernel-language autocomplete, an enum-values table with per-language columns, a read-only computed "Path" display,
and reference autocomplete for `Reference_1`-annotated fields.

Comparing that generated surface field-by-field against a12-studio's hand-built panels surfaces gaps well below
the granularity of the feature table above:

**1. DONE (2026-09-05).** Rule and Computation elements were structurally creatable but functionally uneditable —
the single biggest gap found. `document-model-validation-rule-editor.fxml`/`document-model-computation-rule-editor.fxml`
only wired up the generic General Information/Description/Annotations panels; none of `RuleConfig`'s
`errorEntityRelPath`, `errorCode`, `errorCondition`, `severity`, `errorMessage`, or `ComputationConfig`'s
`computedFieldRelPath`, `computationAlternatives[]` (each with `precondition`/`operation`), `errorMessage` had any
bound UI control, so `BasicConsistencyValidator` correctly flagged the missing fields as errors forever with no UI
path to ever fix them. Fixed: new `RulePropertiesPanelController` (errorCode read-only + severity),
`TargetFieldPanelController` (a new shared "pick a field anywhere in the model" combo, computing the
kernel's relative-path string via the new `ElementIndex.relativePathTo`, reused for both `errorEntityRelPath` and
`computedFieldRelPath`), `ComputationAlternativesPanelController` (repeatable precondition/operation rows,
following `AnnotationsPanelController`'s plain-Java dynamic-row pattern), and two new
`LocalizedTextPanelController.configureRuleErrorMessage()`/`configureComputationErrorMessage()` methods for the
per-language error text. `errorCondition`/`precondition`/`operation` are plain-text `RichtextEditorController`
panels with no semantic validator (see the "Editor features" correction above — there's no condition-language
backend to validate against). Two new `ElementProperty` tags (`RULE_PROPERTIES`/`COMPUTATION_PROPERTIES`) replace
the previous `GENERAL` tag on these checks in `BasicConsistencyValidator`/`MissingReferenceValidator`, so their
errors surface on the new panels instead of colliding with `GeneralInformationPanelController`'s own `GENERAL` tag.

**2. DONE (2026-09-05).** Date, DateTime, Time, and Confirm field types had no data-type configuration panel at
all — `DataTypeConfigurationPanelController` (in the `propertyeditors` package, not `documentmodel` — corrected
from an earlier pass's wrong package guess) only branched on String/Number/DateFragment/DateRange/Custom/Enumeration.
Fixed: a new shared `DataTypeDateConfigurationPanelController` (one controller/FXML for all three of
Date/DateTime/Time, since they're identical in shape — a single `format` string each — switching title/presets/
accessor at runtime via a small internal `FieldTypeKind` enum) and a new `DataTypeConfirmConfigurationPanelController`.
**Correction to this doc's own earlier claim:** "Confirm's `trueValue`/`falseValue`" was wrong — that's SME's shape,
not a12-studio's. Reading `ConfirmFieldType`/`ConfirmTypeOptions` directly shows a12-studio's `ConfirmTypeOptions`
has exactly one field, `notInDCustomTrueValue` — no `falseValue` counterpart exists in this codebase's data model
at all (unlike `BooleanFieldType`, which correctly has zero fields and needed no panel). The new Confirm panel
therefore only exposes that one field; nothing was invented to match SME's shape. (`Unspecified` is still
deliberately excluded — kernel changelog A12K-3981 removed `IUnspecifiedType` entirely, auto-migrating existing
fields to `String`, so no panel was built for it.)

**3. DONE (2026-09-05).** Several documented option fields existed on the data model but were unreachable from
any panel: `StringTypeOptions.noValueValidation` (only ever set programmatically for the attachment `content`
field, now a checkbox next to Line Breaks Permitted/Alphabetical Sorting), `DateRangeTypeOptions.rangeSeparator`/
`youngerThan1900Check`/`interpretationOfYear`/`notInDCustomFormat`/`notInDCustomRangeSeparator`, and
`DateFragmentTypeOptions.youngerThan1900Check`/`notInDCustomFormat` — all now plain controls on their respective
panels. **Not done, and flagged as such in code comments on both controllers**: SME conditionally validates some
of these against each other (e.g. `younger1900` only valid if `format` contains a year), but the exact conditions
weren't independently confirmable from the documentation available in this repo (grepped
`documentation/2606-06-doc/` for `notInDCustomFormat`/`interpretationOfYear`/"younger...1900" — no hits beyond a
single unrelated `interpretationOfYear` mention in the QM filtering docs), so no enable/disable or cross-field
validation logic was guessed at — the controls are always-editable with no gating.

**4. DONE (2026-09-06).** `RequirednessConfig.errorMessage` (custom "this field is required" message) could be
toggled off the default but never authored — `TypeDefinitionPanelController`'s "use default error messages"
checkbox only *cleared* the list when checked, with no text field to type a replacement into. Fixed via a new
`LocalizedTextPanelController.configureRequirednessErrorMessage()`, embedded as an extra row in
`type-definition-panel.fxml`'s existing `defaultErrorMessagesGrid`, visible only while the field is required, not
a multi-select String choice, and the checkbox is unchecked.

**5. DONE (2026-09-06), with a correction.** `ModelConfig.decimalSeparator`/`conditionLanguage` and
`DocumentModelContent.modelInfo` were entirely unexposed. Fixed `decimalSeparator`/`conditionLanguage` via a new
`ModelConfigPanelController`, and `modelInfo.immutable`/`comment` via a new `ModelInfoPanelController`, both wired
into `ModelSettingsDialog`/`document-model-settings-dialog.fxml` (Document Model only), following the same
"model-header, not Element-bound" pattern as the existing `TimezonePanelController`. **`modelInfo.name` was
deliberately NOT exposed as an editable field** — every fixture in this repo has it exactly equal to the model's
own `header.id` (e.g. `Company_DM.json`'s `modelInfo.name` is literally `"Company_DM"`), which turned out to be
because `NewModelFactory` sets it from the same name at creation time, but — unlike `header.id` itself —
`ProjectItem.renameTo()`/`createCopy()` never kept it in sync afterward. That's a latent correctness bug, not a
missing-field gap: exposing `modelInfo.name` as free text would let a user desync it further. Fixed the actual
bug instead — both methods (and `NewModelFactory.createModelFromExisting()`) now sync `modelInfo.name` alongside
`header.id`, with a regression test (`ProjectItemRoundTripTest.renameSyncsHeaderIdAndModelInfoName`).

**6. DONE (2026-09-05).** `GroupConfig.modelAlias` was a dead field — no reader or writer anywhere in
`a12-studio-ui` or `a12-studio-models`/validation. Confirmed obsolete via kernel changelog A12K-4102 (see the
correction in the "Data model" section above) rather than a mis-named `includeConfig` duplicate, so it was
deleted outright rather than wired up.

**Existing document-model validators** (`a12-studio-models-validation`, wired via `DocumentModelValidationService`,
all run together on tree rebuild/save): `SchemaVersionValidator`, `DuplicateIdValidator`,
`NumberFieldValueLimitValidator`, `EnumerationValuesValidator`, `MultiSelectGroupValidator`,
`AttachmentGroupValidator`, `BasicConsistencyValidator`, `MissingReferenceValidator`,
`StringPatternErrorMessageValidator`, plus generic header-level ones (`MissingLocaleValidator`,
`LocaleCodeValidator`, `ModelIdFilenameValidator`, `ModelSuffixValidator`, `UniqueModelIdValidator`,
`NameConventionValidator`, `TimeZoneValidator`). Coverage is already broad and roughly matches SME's custom
structural checks (`DMValidationService.kt`'s `checkMissingErrors` family: dangling Include ref, missing index
field, duplicate names, missing computed-field target, too-few multi-select enum values, missing TypeDef ref) —
the gap is not "missing validators," it's "validators correctly demand data that the UI provides no way to enter"
(see point 1 above). No rule-contradiction/TDG solver exists on either side of this doc's prior analysis, confirmed
still true.

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

## Query Model

*Analyzed 2026-09-05 (rest of this doc last analyzed 2026-07-17 — don't assume the same currency).*

a12-studio's Query Model editor (`a12-studio-ui/.../editors/querymodel/`, data model in
`a12-studio-models/.../querymodel/`) exists but is a thin shell: the tree is read-only, filtering is a single
free-text expression for the whole query rather than per-node, there is no UI to set the target Document Model
after creation, and there are **zero validators**. SME's `queryModel` (`client/src/modules/queryModel/`) is a real
structured-query editor built on a manipulable document-graph tree with per-node constraint authoring, aggregation,
and full reference/rename tracking — see comparison below.

### Data model

| Field | a12-studio (`QueryModelContent`) | SME (`Query.QueryRoot` wire format) |
|---|---|---|
| Target | `targetDocumentModel` (String, DM id only) | `targetDocumentModel` — can also be a Combined Document Model or Transformer Model output |
| Projection | `projectionName` | `projectionName` |
| Field selection | `fields[]` (in-result paths) | `fields[]`, plus a `useAllFields` mode |
| Filter | `filterDefinition` — **one free-text string for the entire query** | `constraint` — recursive `Operator` AST (`and`/`or`/`not`/`exact_match`/`double_range`/`date_range`/`undefined_match`/`simple_search`/`has`), attachable **per graph node** |
| Traversal | none — tree is a fixed mirror of the target DM | `links[]` — nested relationship traversals (`relationshipModel`, `targetRole`, optional `constraint`, optional `maxDepth` for self-reference recursion), plus `has(...)` as a filter-only traversal |
| Sort | `sort[]` (`QuerySort`: optional relationship+role hop, then `QuerySortBy` — field/direction/nullHandling/ignoreCase) | `sort` — same shape (field/direction/nullHandling/ignoreCase) |
| Paging | `paging` (pageNumber, pageSize) | `paging` — same shape |
| Aggregation | `aggregateResults` (Boolean) — **dangling flag, no config behind it** | `aggregation` — `group: {field}[]` + `aggregations: {function: count\|sum\|max\|min\|avg, field}[]`, a distinct result-shape mode |
| Root exclusion | not present | `exclude` — omit the root document itself, return only linked docs |

SME's in-editor representation additionally splits into three independently-validated sub-documents (`settings`
header form, `documentGraph` tree, `postProcessing` sort/paging/aggregation) that get merged back into the flat
wire JSON on save (`transformations/qmTransformer.ts`) — a serialization-layer detail, not something a12-studio
needs to copy architecturally.

### Editor features — gap list

| Feature | SME reference | a12-studio status |
|---|---|---|
| Editable tree / document graph | Add a root DM via an ER-diagram picker (reuses the Model Graph Diagram component); add relationship-traversal nodes (only relationships actually connected to the selected node are offered) | **Missing** — tree is a fixed, read-only mirror of one target DM's fields/groups; no traversal nodes, no add/remove |
| Per-node filter/constraint | Query-language grammar editor (ANTLR-backed, field/relationship autocomplete against the model graph), compiles to the `Operator` AST; semantically validated (field exists, type-correct operator, valid relationship+role) | One whole-query free-text `filterDefinition` via `RichtextEditorController` — confirmed to be a plain `CodeArea` with cosmetic string-literal highlighting only, **no grammar parsing, no autocomplete, no semantic validation** |
| Target Document Model selection | Settings tab, editable at any time | **No UI at all** — `targetDocumentModel`/`projectionName` can only be set by hand-editing the JSON; `ModelSettingsDialog` explicitly hides model-references/supported-characters for QueryModel |
| In-result field toggles | Inline tree checkboxes, tri-state on groups, disabled+forced for non-indexed fields | Present (`QueryModelTreeController`'s In-Result column), roughly at parity |
| Sort | Multi-field, relationship-hop, direction, null-handling, ignore-case | Present (`QuerySort`/`QuerySortBy`/sorting panel), roughly at parity — `QueryTraversalOption.options()` scopes to *every* relationship in the project rather than only ones connected to the target DM |
| Paging | pageNumber/pageSize | Present, roughly at parity |
| Aggregation/grouping | Full group-by + count/sum/max/min/avg mode | **Missing** — `aggregateResults` boolean has no config surface behind it |
| Multi-target-type queries (CDM, Transformer Model as target) | Supported | Not supported — DM only |
| Reference/rename tracking | Target-DM, relationship, sort/aggregation field-path references are all first-class in SME's refactoring graph; renaming a DM/field auto-updates or flags the query (`qmModule.ts` `refactorDocument()`) | **Missing** — `resolveTargetDocumentModel()` silently produces an empty tree if the stored id no longer resolves, no error surfaced |
| Validation | Root-required, per-node schema validation, constraint semantic validity, target-role validity, field-projection sanity, tab-level validation counts | **None** — no `QueryModelValidationService`, nothing wired to `ModelType.QUERY` at all; even the sorting panel's "relationship could not be resolved" indicator is a UI style hint, not a real validation error |

### Feasibility spike: the query-grammar dependency (2026-09-05) — **feasible, not kernel-gated**

SME's per-node filter authoring (`@com.mgmtp.a12.sme/qmm-support`, `moduleSupport/qmm/` in the SME repo) turns out
**not** to require any proprietary a12 kernel/npm-registry access at all — it's a self-contained language toolchain
that happens to target TypeScript today, not something wrapping a closed kernel API:

- **The grammar itself is a plain, standalone ANTLR4 file** (`moduleSupport/qmm/QL.g4`, 140 lines) with no
  SME/kernel-specific runtime dependency at the grammar level: `and`/`or`/`!`, 6 binary comparison operators
  (`== != >= <= ~ !~`), field references (`[/Path/To/Field]`), function-call syntax, and null/boolean/string/number
  literals. `moduleSupport/qmm/build.gradle.kts` generates the TypeScript parser from it via
  `org.antlr.v4.Tool -Dlanguage=TypeScript` — but **Java is ANTLR4's native/default target**, and `org.antlr:antlr4`
  is a plain BSD-licensed artifact on Maven Central, not an a12 kernel dependency. Gradle even ships a built-in
  `antlr` plugin (`id 'antlr'`, generates Java lexer/parser from `.g4` files in `src/main/antlr`) that a12-studio
  isn't using anywhere yet but could adopt trivially — a12-studio's `build.gradle` files use plain `java-library` +
  string coordinates (no version catalog), so adding `antlr4-runtime` is a small, ordinary dependency change.
- **The relationship-traversal "filter" (`has(...)` in the earlier gap-list table) is not a separate grammar
  construct** — it's just one of the ~15 built-in functions (confirmed via `functions.ts`/test names:
  `Has`, date/time/date-range/date-fragment constructors, range and match functions), called through the same
  `callExpression` grammar rule as everything else. This significantly narrows what a Java port needs to cover —
  one grammar, one function registry, not a family of special cases.
- **The compiler pipeline is portable business logic, not UI code**: `parser.ts` (316) → `binder.ts` (285) →
  `checker.ts` (199) → `emitter.ts` (404) → `importer.ts` (411, the reverse direction) → `formatter.ts` (267) →
  `functions.ts` (1229, the function/operator registry) → `base/*.ts` type-system/resolver/visitor (~1500) — about
  4,700 lines total, none of it DOM/React-dependent. This is the real cost of the feature: a bounded, mechanical-ish
  Java port of an existing, well-tested reference implementation (SME ships unit tests per function in
  `moduleSupport/qmm/test/core/checker/*.test.ts`), not a from-scratch design.
- **Only the Monaco-editor integration layer (~1,800 lines: completion/hover/inlay-hint providers, theming) doesn't
  port** — that's genuinely IDE-specific and would need a JavaFX/RichTextFX-based replacement (building on
  `RichtextEditorController`, which already hosts a `CodeArea`), reusing the ported binder/checker for the semantic
  data (field types, valid completions) rather than reimplementing that logic twice.
- **The emitted target shape, `Query.Operator`**, comes from `@com.mgmtp.a12.dataservices/dataservices-access` (a
  real published package, not workspace-local) — but since a12-studio only needs to *author and validate* this JSON
  (not execute queries against live data), the shape can be modeled directly as new a12-studio Java POJOs, the same
  way `QueryModelContent`/`QuerySort`/`QueryPaging` already hand-model JSON shapes today, without needing the actual
  kernel/dataservices JAR as a dependency.

**Conclusion: Phase 3 (per-node filtering) should target a Java port of `QL.g4` + the compiler pipeline, not a
from-scratch structured filter-builder.** This is more work than a simple field/operator/value builder, but it
gets a12-studio to the exact same query language and JSON output SME produces (so files stay
interchangeable/round-trippable) instead of inventing a parallel, incompatible filter representation. The
editor-integration (autocomplete/highlighting) can be scoped down initially — ship the grammar/compiler port with a
plain syntax-highlighted `RichtextEditorController`-style editor first, add autocomplete as a follow-up once the
semantic layer (binder/checker) exists to drive it.

**Status (2026-09-05): grammar/parser step done.** `QL.g4` (byte-identical to SME's, since the grammar itself has
no target-language-specific content) now lives at
`a12-studio-models/src/main/antlr/de/a12/studio/models/querymodel/ql/QL.g4`, wired up via Gradle's built-in `antlr`
plugin (`a12-studio-models/build.gradle`, `org.antlr:antlr4:4.13.2` for codegen + `org.antlr:antlr4-runtime:4.13.2`
as an `api` dependency since generated parser classes are part of this module's public surface). One gotcha worth
recording: the Gradle ANTLR plugin does **not** infer the Java package from the grammar file's directory nesting —
without an explicit `-package` argument the generated classes came out in the *default* (unnamed) package despite
living in the right directory; fixed via `generateGrammarSource { arguments += ['-visitor', '-package',
'de.a12.studio.models.querymodel.ql'] }`. A smoke test
(`a12-studio-models/src/test/java/de/a12/studio/models/querymodel/ql/QueryLanguageGrammarTest.java`) parses the
same sample expressions SME's own `moduleSupport/qmm/test/core/checker/*.test.ts` exercise (field comparisons,
`and`/`or`/`not`, `Has(...)`, nested/range function calls) and confirms both valid and invalid inputs behave as
expected — full grammar/lexer parity confirmed, not just "it compiles."

**Remaining for Phase 3**: the semantic pipeline is not started yet — binder (resolve field refs/relationships
against a model graph), checker (type/overload validation per function), emitter (parse tree → `Query.Operator`
JSON), importer (the reverse direction, JSON → parse tree, needed to load existing files back into editable text),
formatter (pretty-printing), and the function/operator registry (`functions.ts`'s ~15 built-ins) all still need a
Java port — see the line-count breakdown above for relative sizing. None of that is wired into `QueryModelContent`
or the editor UI yet; `filterDefinition` is still the old free-text field.

**Status (2026-09-05): `Query.Operator` JSON model done.** Rather than reverse-engineer the exact wire format from
SME's TypeScript alone, the authoritative source turned out to be the platform's own Data Services API
documentation (`documentation/2606-06-doc/data_services-dataservices-documentation-src.md`, "Query Language"
operator reference) plus a real fixture (`client/resources/input/models/example/models/person/Intern/
QueryModeling/HighExperienceInterns_QeM.json` in the SME repo) — both give literal, unambiguous JSON examples for
every operator, which is more reliable than inferring shapes from `emitter.ts`'s TS types. The result is
`a12-studio-models/src/main/java/de/a12/studio/models/querymodel/operator/` — an `Operator` tagged union (10
concrete subclasses: `And`/`Or`/`Not`/`ExactMatch`/`UndefinedMatch`/`DoubleRange`/`DateRange`/
`DateFragmentRange`/`SimpleSearch`/`Has`) following the exact same `@JsonTypeInfo(use=NAME, property=..., visible=
true)` + write-only discriminator convention as `documentmodel.FieldType`, keyed on `"operator"` instead of
`"type"`. Notably, the model covers the **full** documented API shape (e.g. `exact_match`'s `values`/`caseSensitive`,
`date_range`'s alternate `value`/`reverse` mode for `IDateRangeType` fields), not just the subset SME's Query
Language grammar/emitter currently reaches — since hand-authored or kernel-produced JSON can use the whole surface,
and the model's job is to round-trip whatever's on disk, not just what one compiler emits.

One round-trip bug surfaced and was fixed the same way `overviewmodel.Column.width` was previously (see "Known
issues" above): `double_range`'s `from`/`to` are backed by `JsonNode` (not `Double`) with `@JsonIgnore` `Double`
convenience accessors, because real fixture data mixes plain-integer (`"from": 5`) and decimal (`"from": 5.0`)
formatting for the same field — a `Double`-typed field coerces everything to the latter and silently rewrites the
file on save. Four round-trip tests (`OperatorJsonRoundTripTest`) cover every operator using the real doc/fixture
examples verbatim, including this exact `5` vs `5.0` case. `ExactMatchOperator.value` is a `JsonNode` (not
`String`) for the same class of reason: a Number field's `==` produces a raw JSON number, not a stringified one
(confirmed against `emitter.ts`'s `emitLiteralNode`, which only stringifies booleans, passing numbers through
unconverted) — a `String`-typed field would have silently coerced every numeric equality into text.

**Status (2026-09-05): emitter + formatter done for the full non-aggregation surface.** Added
`QueryLanguageEmitter` (text → `Operator`) and `QueryLanguageFormatter` (`Operator` → text, combining SME's
separate importer+formatter stages into one direct step since QL has no comments/whitespace worth preserving
through an intermediate tree), both in `a12-studio-models/.../querymodel/ql/`. Together they cover the entire
callable-function surface confirmed from `moduleSupport/qmm/src/internal/compiler/base/configuration.ts`'s
`FunctionConfigMap` (the actual ground truth for what's user-typeable — most of SME's ~15 "functions" are
internal-only synthetic dispatch tags for surface *operators* like `==`/`>=`/`and`, not things a user calls by
name): `Has`, `Match`, `InRange` as callable identifiers, plus `Date`/`Time`/`DateTime`/`DateFragment`/`DateRange`
as value constructors, alongside the `and`/`or`/`!`/`==`/`!=`/`>=`/`<=`/`~`/`!~` surface operators. Exact argument
orders and formats (e.g. `Date(day, month, year)`, not `(year, month, day)`; `DateFragment`'s 1-or-2-arg
magnitude-based format detection) were taken directly from `functions.ts`'s param resolvers, not guessed.

Key design decision: **no binder/checker (no field-type resolution) was needed for correct emission.** SME's
checker binds field paths to their Document Model type to disambiguate `double_range`/`date_range`/
`datefragment_range` for `>=`/`<=`/`InRange` — but that disambiguation turns out to be fully determined by the
*value's own syntax* already (a number literal vs. a `Date`/`Time`/`DateTime` call vs. a `DateFragment` call), so
the Java emitter dispatches purely syntactically and gets the same result without needing a Document Model schema
lookup at all. Field/function *validity* (does this field exist, is this target role real) is therefore not
checked here — only syntactic well-formedness is; semantic validation is a separate, later concern (SME's own
`checker.ts`/custom conditions) that would need real schema access and hasn't been ported.

Two shapes the emitter can never produce have no clean QL surface syntax and are formatted with a documented,
lossy fallback in `QueryLanguageFormatter` rather than failing: `exact_match` with a `values` list (expanded to an
`or` of `==` comparisons) and a boolean-sourced `exact_match` value (rendered as a quoted string — indistinguishable
from a genuine string value without field-type context). `DateRangeOperator`'s alternate `value`/`reverse` mode
(for `IDateRangeType` fields) is formatted via `field == DateRange(from, to)`, inferred from the platform doc's
interval-string example rather than confirmed against SME's emitter (which doesn't appear to wire this path at
all in the code actually read) — flagged as the one part of this pass not independently verified against SME.

149 tests pass in `a12-studio-models` (up from 6): `QueryLanguageGrammarTest` (12, grammar-only), 31 new emitter
tests covering every function/operator combination against SME's own test/doc examples, and a 34-case
`QueryLanguageFormatterRoundTripTest` proving `emit → format → emit` is lossless (structurally, not necessarily
byte-identical text) for every construct — the actual correctness bar for an editor's save/reload cycle.

**Status (2026-09-05): Phase 1 (Settings + validators) done, and the compiler is now wired into the UI for the
existing whole-query filter field** — ahead of the original build-order plan below, which had per-node filtering
(originally Phase 3) waiting on the editable graph tree (Phase 2). Since `filterDefinition` already existed as a
plain string field, there was no need to wait: `QueryLanguageEmitter`/`Formatter` validate it today even though
it's still one whole-query expression, not yet a per-node constraint.

- **Settings tab** (`QuerySettingsPanelController`/`query-settings-panel.fxml`, new "Settings" tab in
  `query-model-editor.fxml`, matching SME's own tab layout): reuses `TargetModelPanelController` (promoted to the
  shared `propertyeditors` package per CLAUDE.md's explicit "pick one document model" convention — it already
  anticipated Query Model as a second consumer) to finally give `content.targetDocumentModel` a UI, and syncs the
  header's DOCUMENT-type `ModelReference` on change (mirroring `MappingModelEditorController`). Selecting a target
  DM now also reloads the Model Tree tab so it doesn't keep showing the previous target.
  `content.projectionName` is deliberately **not** exposed as an editable field — every real fixture/example uses
  the constant `"document"` (SME itself treats it as read-only for the same reason), so it's auto-defaulted instead
  of inventing a field for something that isn't meant to vary.
- **Validators** (`a12-studio-models-validation/.../validators/query/`, registered in a new
  `QueryModelValidationService`, wired into `ValidationService` for `ModelType.QUERY`): target-DM required
  (`QueryTargetDocumentModelRequiredValidator`); `fields[]` and non-traversal `sort[].sortBy.field` paths must
  resolve against the target DM (`QueryFieldReferenceValidator`/`QuerySortFieldReferenceValidator`, via a small
  `QueryElementResolution` helper doing linear-scan path lookup — Query Model paths are "/"-separated names, unlike
  Overview's kernel-id-based `elementRef`, so `ElementIndex`'s id resolution doesn't directly apply);
  `sort[].relationshipModel`/`targetRole` must resolve to a real Relationship Model and a role it actually declares
  (`QueryRelationshipTraversalValidator` — turns the sorting panel's previous UI-only styling hint into a real
  error); paging bounds (`QueryPagingBoundsValidator`); and `filterDefinition` syntax
  (`QueryFilterDefinitionSyntaxValidator`, via `QueryLanguageEmitter`). A sort entry that *does* traverse a
  relationship has its field-path validation skipped for now — resolving the hop's own target DM to check the
  field against needs more infrastructure than this pass adds; the traversal itself is still validated.
- **Filter dialog validation**: `RichtextEditorController` (the shared expression-editor panel also used by
  Overview/Form) gained a generic `setValidator(Function<String, String>)` hook — on every change (and once on
  load) it shows the validator's message in its own error container, or clears it. `QueryFilterDefinitionDialogController`
  wires this to `QueryLanguageEmitter`, and binds the OK button's disabled state to the panel's `errorProperty()` —
  a syntax error now blocks saving instead of being silently accepted as opaque text.
- **Scope note recorded live in this session**: the user explicitly decided to skip an ER-diagram element picker
  for the future editable tree (Phase 2) — a simpler Document-Model list/combo picker will do instead, unlike
  SME's diagram-based one.

**Status (2026-09-06): Phase 2 (editable graph tree) done — the tree is no longer limited to one Document
Model.** `content.links: List<QueryLink>` (new class, `a12-studio-models/.../querymodel/QueryLink.java`) is a
recursive relationship-traversal hop: `relationshipModel`/`targetRole`, its own `fields` (scoped "In Result" list,
just like `content.fields` but for that hop's own target Document Model), and nested `links` for multi-hop
traversal. `constraint` (an `Operator`, per-node filtering) and `linkDocumentFields` are mapped so an existing
file round-trips losslessly, but neither has editor UI yet - `constraint` is still Phase 3 (per-node filtering),
and `linkDocumentFields` would need resolving the relationship's own link-document schema
(`RelationshipModelContent.getLinkDocumentModel()`), which nothing in this editor does. The nested-`links`
recursion shape was taken on trust from the original SME inventory pass rather than re-verified against SME's TS
source in this session - flagged the same way the `date_range` `value`/`reverse` mode was earlier.

- **Tree**: `QueryModelTreeController`/`QueryTreeRow` now render relationship-link rows (icon:
  `Icons.PNG_MODEL_RELATIONSHIP`) alongside Document Model field/group rows, at any nesting depth. Each row
  carries a `fieldsScope` - the specific `fields` list ("In Result" toggles read/write against `content.fields`
  for the root and everything under it that isn't itself under a link, or that link's own `fields` otherwise) -
  replacing the old hardcoded `content().getFields()` everywhere. An unresolved relationship/role (relationship
  or role deleted elsewhere) renders as a childless, checkbox-less row instead of breaking the tree.
- **Add/remove**: a row's context menu (right-click) and two new toolbar buttons offer "Add Relationship"
  (target Document Model row or an existing relationship-link row - multi-hop) and "Remove Relationship"
  (link rows only, with a confirmation prompt). `QueryTraversalOption` gained `optionsConnectedTo(projectItem,
  documentModelId)` - scoped to relationships that actually declare that Document Model for some role (including
  the same Document Model twice, for a legitimate self-referencing case like a hierarchy's Parent/Child) - unlike
  the existing unscoped `options()` used by the Sort dialog, which still lists every relationship in the project
  since it only needs *a* valid traversal, not one reachable from a specific node. The picker itself
  (`QueryAddRelationshipDialogController`/`query-add-relationship-dialog.fxml`) is the plain combo box the user
  asked for, not an ER diagram. No `CommandStack`/undo support was added for add/remove - consistent with the
  rest of this editor (the "In Result" toggles have never had undo either), not a gap specific to this feature.
- **Validators**: `QueryLinkValidator` recursively checks every link's relationship/role resolution (reusing the
  same messages as `QueryRelationshipTraversalValidator`) and its `fields[]` paths against the resolved Document
  Model - a broken hop doesn't stop validation of hops nested under a resolved sibling.
- **Not manually verified in-app this session**: this is a real JavaFX desktop app with no browser/Electron
  automation available and no project-specific run skill; `compileJava` succeeded (catches FXML wiring mistakes
  like a bad `fx:id` at load time) and the data-model/validator layers have real tests, but the tree's actual
  on-screen behavior (context menu, toolbar button enablement, nested rendering) has not been clicked through -
  needs manual verification, e.g. against `testing/workspaces/basic` with a Relationship Model connected to a
  Query Model's target Document Model.

**Still remaining**: no per-node constraint slot yet (`filterDefinition` is still one whole-query expression on
the root only, not attachable to a relationship-link node) - that's Phase 3, now unblocked by this tree work.
Real semantic validation of the *filter expression's own* field references (does `[/Foo/Bar]` inside a
`filterDefinition` string actually exist) also isn't checked - only its syntax is. Aggregation and reference/
rename tracking are unchanged from the plan below.

### Proposed build order

1. ~~**Settings + validators**~~ — done, see Status above (2026-09-05).
2. ~~**Editable graph tree**~~ — done, see Status above (2026-09-06).
3. **Per-node filtering**: give each graph node (not just the whole query) its own constraint, authored/validated
   via the already-built `QueryLanguageEmitter`/`Formatter` and stored in `QueryLink.constraint` (already mapped
   in the data model, just not editable yet). Autocomplete remains a separate follow-up once a semantic
   (field-existence-aware) layer exists.
4. **Aggregation** — only once it's confirmed the kernel path a12-studio would use actually supports an
   aggregation-mode query result; otherwise a documented non-goal.
5. **Reference/rename tracking** — hook into whatever a12-studio's existing rename/move refactoring mechanism is
   (see the Document Model gap list above — a12-studio doesn't have one yet either, so this is coupled to that
   broader gap, not query-model-specific work).

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
| 10 | **queryModel / overviewModel** | Search/filter/list-screen configuration; consumer-side (built on top of a DM), not blocking other model types. See dedicated "Query Model" section above for a12-studio's current gap list. |
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
- **queryModel** — reusable structured query (filter/sort/selection tree) over a DM; experimental. See dedicated "Query Model" section above.
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
| Condition/expression language validation & formatting | `ValidationRuleService`, `ComputationRuleService` (Kotlin) | **Missing** (corrected 2026-09-05 — previously claimed present; no such Java services exist, no kernel dependency in this repo). `RuleConfig.errorCondition`/`ComputationAlternative.precondition`/`operation` are edited as plain text with no semantic validation — see the Document Model "Editor features" correction above |
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
