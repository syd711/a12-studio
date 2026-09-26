# SME Reference Comparison

This document exists to help anyone working on a12-studio compare it against **SME (Simple Model Editor)**,
the original/reference implementation at `C:\workspace\sme`, part of the mgm A12 low-code platform. a12-studio is a
from-scratch **Java reimplementation** of the same modeling tool concept — not a port. Use this doc to see what
SME's editors do, what a12-studio currently has, and what's missing.

Last analyzed: 2026-09-20 (TODO #14: the sections that were still dated 2026-07-17 / 2026-09-05 — Document Model
editor-feature table and validator list, Form Model tables/gap lists, Query Model gap list, the "Other model types"
survey and the Backend / kernel capability map — were re-verified against the code; every statement changed on that
pass carries "(2026-09-20)". Sections that were already dated after 2026-09-05 were only touched where the code
contradicted them). SME itself (`C:\workspace\sme`, HEAD `ba2e34687` of 2026-03-09, i.e. unchanged since before the July
analysis) was only spot-checked on that pass: the module list under `client/src/modules`, the `isExperimental()`
flags and the backend service/controller names in the capability map all still match; the SME-side behaviour
descriptions were **not** re-read and stay as in the earlier analyses. SME evolves independently of this repo —
re-verify specifics against `C:\workspace\sme` before relying on them for anything but general orientation.

## Architecture: how the two projects actually relate

SME is a polyglot monorepo: Kotlin/Spring Boot backend (`backend/`) + TypeScript/React/Redux frontend (`client/src`,
split into `core`, `modules` [one per model type], `app`, `a12Extension`, `packages`), packaged as an Electron
desktop app.

**Key insight:** SME's backend is not a distinct architectural layer conceptually — it's a thin REST wrapper around
the A12 kernel libraries (`com.mgmtp.a12.kernel:*`, `com.mgmtp.a12.tdg:*`, `com.mgmtp.a12.print:*`). It has
no database, no file persistence, no session state; every endpoint takes model JSON in and returns a computed
result. File load/save is purely a frontend/Electron filesystem concern.

**a12-studio does not depend on the kernel.** `a12-studio-data-services/build.gradle` has no kernel/print/base
dependencies today — only `project(':a12-studio-models')` plus test libraries — and (2026-09-20) that module is
now small: it holds only the wireframe-preview services/DTOs (`dataservices/preview/`). The `SmmService`,
`SMEMappingModelService`, `PrintService` scaffolding that older versions of this doc (and `CLAUDE.md`) list there was
deleted on 2026-07-20 (commit `6f526548`); model validation lives in `a12-studio-models-validation`, model classes in
`a12-studio-models`. The only vendor-JVM code a12-studio runs is out of process: the Preview App server, the
`WcfCli` conversion tool and (since 2026-09-25) the Simple Model Editor backend `sme.jar` that the Form Engine preview
uses for Document Model expansion and validation code (`a12-studio-ui/.../previewapp/`), each launched as a subprocess. An earlier version of this doc
claimed a12-studio "pulls in the same kernel libraries directly" and listed specific coordinates as already present;
that was never actually true (no such `build.gradle` entries exist in git history, and there is no
`ValidationRuleService.java` in this repo). The kernel's Community-edition artifacts (e.g. `kernel-md-facade`,
`kernel-md-model`) are in fact anonymously downloadable from `artifacts.geta12.com` — dual-licensed EUPL-1.2/commercial,
same as this repo's own `LICENSE` — so an in-process kernel dependency is a legally and technically viable path, not
a blocked one. But taking it is a real architectural decision (large transitive dependency footprint; turns
a12-studio from an independent reimplementation into a kernel wrapper for whatever slice uses it), so it hasn't been
taken. **Spike 2026-09-19** (see "Kernel dependency spike" in the Backend / kernel capability map below) confirmed the
path is technically viable for kernel **31.1.1** — not SME's pinned 30.7.0, which is unpublished and whose 30.8.x
successors cannot even read a12-studio's Document Model version 29.4.0 — for everything except TDG, which is
enterprise-only. The strategy actually in use is a **clean-room, data-driven port**: read the same JSON rule definitions the
kernel/SME ship (e.g. `client/resources/models/documentModel/Domain*.json`) and evaluate them with a
purpose-built interpreter in `a12-studio-data-services`, rather than either reimplementing SME's REST endpoints or
depending on the real kernel jars. See the `SchemaVersionValidator`, `DuplicateIdValidator`,
`NumberFieldValueLimitValidator`, `MultiSelectGroupValidator`, `AttachmentGroupValidator`, and
`BasicConsistencyValidator` classes for the (currently hand-ported, not yet data-driven) document-model-level
rules, and the planned meta-model validation rule engine (below) for the much
larger family of field/group/rule/computation config-validation rules SME's `Domain*.json` files define.

---

## Document Model

The first editor a12-studio got, and still the most complete one (`a12-studio-ui/src/main/java/de/a12/studio/ui/editors/documentmodel/`,
data model in `a12-studio-models/.../documentmodel/` — corrected 2026-09-20: it was never in `a12-studio-data-services`,
and this is no longer the only editor; see "Other model types" for the rest).

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

a12-studio's current editor (2026-09-20): tree/detail split view (`DocumentModelElementsTreeController`), group vs.
field vs. rule vs. computation detail editors, undo/redo via a `CommandStack`, search + filter, drag and drop,
clipboard/bulk actions, move/rename refactoring, insert-from-Document-Model, Settings + Type-Definitions dialogs, and a
read-only "Additive Elements Only" preview for Additive Document Models.

**Correction (2026-09-05):** this section previously claimed condition validation/formatting "already calls the
same kernel APIs as SME" via Java `ValidationRuleService`/`ComputationRuleService` equivalents. Confirmed false
by grep — no such services, no kernel dependency anywhere in `a12-studio-data-services`/`a12-studio-models`. The
"Backend / kernel capability map" table below has the same correction applied to its "Condition/expression
language validation & formatting" row. Practical effect: Rule/Computation condition text (`errorCondition`,
`precondition`, `operation`) is edited as plain text with no semantic validation, the same way
`QueryModelContent.filterDefinition` and `overviewmodel.Column.expression` already work via
`RuleEditorController` — not blocked on porting a condition-language grammar, but also not actually checked
for validity beyond "non-blank".
**Update (2026-09-20):** that last sentence is no longer true. A clean-room, syntax-only ANTLR grammar for the
condition language exists (`RuleLang.g4` + `RuleLanguageSyntaxChecker` in `a12-studio-models`, added 2026-09-14/15,
reproducing the kernel's parser-level messages `MVK_INCOMPLETE_INPUT` / `MVK_EXPECTED_TOKEN_NOT_FOUND` /
`MVK_UNEXPECTED_TOKEN` / `MVK_LEXER_STANDARD_ERROR`) and is wired as the per-keystroke validator of the rule,
computation-alternative and computation-precondition editors (`DocumentModelValidationRuleEditorController`,
`ComputationAlternativesPanelController`, `ComputationOptionsPanelController`,
`ComputationAlternativeDialogController`). Since 2026-09-21 it **also runs as a `ModelValidator`** (`RuleConditionSyntaxValidator`: a rule's `errorCondition`, a
computation's `commonPrecondition` and each alternative's `precondition`/`operation`; blank text is skipped, as in the
editors), so a broken condition shows up in the validation list and on the element's tree row. It does no semantic checking — no field/type resolution, argument
counts or path existence (kernel-side those need the model expanded; see the spike below).

Missing or worth checking against SME (`commonDocumentModel/api/editor/*`):

| Feature | SME reference | a12-studio status | Won't Fix |
|---|---|---|---|
| Rule contradiction / consistency check | TDG constraint solver (`checkRuleContradictions`, endpoint `/api/document-model/check-rule-contradictions`) detects logically unsatisfiable rule sets (e.g. a field required but an error rule fires whenever it's filled) | **Missing, and blocked** (re-verified 2026-09-20: no `tdg` dependency; the 2026-09-19 kernel spike found TDG enterprise-only, see the capability map) | |
| Move/rename refactoring | Auto-rewrites rule/computation condition text referencing a moved/renamed element (`moveElementApi.ts` → backend `/move-element-with-refactoring`) | **Present within one model** (2026-09-19): `DocumentModelRefactoring` (`a12-studio-models-validation/.../refactoring/`) + UI `RefactoringCommand` wrapping `RenameElementCommand`/`MoveNodeCommand`, so inline rename, the General Information panel's Rename and tree drag-and-drop are one undo step each. Clean-room (SME's rewrite lives in the proprietary kernel `MoveSupportDM`). Resolves each path to the *element* in the old tree and re-expresses it against the new one, so a moved *rule* (whose relative paths are measured from its own position) works as well as a moved field; only a reference that no longer resolves to the same element is rewritten, keeping its style (absolute vs relative, `..Group` turning-group name, `*` markers). Covers Rule `errorEntityRelPath`/`errorCondition`/`errorMessage` `$path$` parameters, Computation `computedFieldRelPath`/`commonPrecondition`/alternative `precondition`+`operation`/`errorMessage`, Group `indexFieldName`, `documentUniquenessCriteria[].fields[].fullName`. References by element id (Form/Overview/Tree/RelationshipUI `elementRef`/`fieldId`/`groupRef`, `ModelConfig` uniqueness criteria) cannot break — ids don't change on rename/move. **Other models' references (2026-09-19, `ProjectReferenceRefactoring`):** the same rename/move now also updates, in the same undo step, the references the project's other models hold on the changed Document Model — Print `FieldRef.path` (where `model` is the changed DM), Query field paths wherever they sit (2026-09-20, `QueryReferenceRefactoring`, scoped by which Document Model each is evaluated against: root `fields`/`sort`/`constraint`/`filterDefinition` = the target DM; a relationship hop's `fields`/`constraint`/`filterDefinition`, a sort entry through a relationship and `has`/`Has(...)` constraints = the role's DM; `linkDocumentFields` and link constraints = the relationship's link DM), Mapping `SortField.sortFieldFullName` (per `Source.dmId`), Structural Mapping `*FullName`s and Selection `Selected`/`Unselected` paths (both have no DM reference of their own, so they follow only when every Mapping/Combination Model using them agrees the changed DM is that side's/base model), and rules/computations of *other Document Models that include this one* (SME `calculateIncludedNameChanges`/`calculateIncludedPathChanges`; an Include mounts the children of the included model's root group, so the path tail after the Include group is re-derived from the included model's old/new tree). The affected models are edited in place (editors share the project tree's model instance and save on every edit, so there is no dirty state to clash with), saved, and announced via `ModelSaveEvent`; undo restores, saves and announces them again, and skips a model that was reloaded from disk meanwhile. Verified against every element of every DM in `testing/workspaces` (renames + moves; undo restores every model byte-for-byte; relative paths of including models still reach the same element). **Print calculation steps** (2026-09-21): the `[<DM id>/<path>]` field references in a step's `operation` that read the changed model follow it. **Open editors** (2026-09-21): a rewritten model is announced with a `ModelRefactoredEvent` and the tab showing it is rebuilt in place (`TabPaneController#modelRefactored`, like after a revert; the editor's selection is reset). **Chains of Includes and Additive bases** (2026-09-21): a Document Model that reaches the changed one through a *chain* of Includes (A includes B includes C) follows a rename/move in C - `IncludedModelChange` carries a model lookup and the path tail is followed through B's own Include groups until it reaches C (cycle-safe) - and an Additive Document Model whose base (`AdditiveDocumentModelResolver`) is the changed model follows for the paths that leave its own groups into the base (verified on `PersonEmployee_Ad`'s `../Type` / `FirstName`). Limits: a mirrored group that the base renames or moves is not followed by the overlay (it matches its base by name, so the overlay's own group would have to move as well), and an overlay whose base reaches the change only through its own Include is not followed. **Cut/Paste** (2026-09-21): Cut is now a *pending move* like SME's (see the multi-select row) and is refactored like a drag-and-drop. **Still missing:** Form `hostDocumentModelPath` (include provenance: the path of the host DM's Include group the include was bound to; followed since 2026-09-20 for a form bound to the changed DM, `FormIncludeProvenanceValidator` reports one that still goes stale, e.g. a path that runs into a DM that is merely included), paths through a *chain* of Includes or an Additive base model; conditions that don't parse are skipped (logged). | |
| Ad hoc testing / live preview | Select elements (Alt+T, or bulk from the Ctrl+M panel), backend `AdHocTestService` calls kernel `DocumentModelService.createReducedDocumentModel(selected, partiallySelected)` + `generateValidationCode`, the client generates a Form Model from the reduced DM (`fmm-support` `FormModelGeneratorAPI`) and renders both in the preview window | **Present (2026-09-25)**, built on the installed SME instead of the kernel or the private `fmm-support`: "Ad Hoc Testing" (toolbar button, context menu of an element and of the root, Alt+T; `DocumentModelActions#startAdHocTest`) opens `AdHocTestPreviewSession` in the browser chosen in the Preview settings. The session expands the Document Model and its includes with the installed backend (`SmeBackend`, POST `/api/document-model/expand`), lets `/generate-ad-hoc-test-input` cut out the selection (an element and its descendants; selecting an Include selects its whole expanded subtree; no selection = the whole model), generates the Form Model with `FormScreenGenerator` (the studio port of the "build screens from fields" generator, so screens/labels can differ in detail from what SME's `form-model-generator` emits) and renders it with the real Form Engine (see "Form Engine preview" below). It follows edits to the Document Model like SME (polling, see the Preview settings), dropping selected elements that no longer exist. **Not present:** ad hoc testing of an Additive Document Model (the button is disabled; SME tests it through its Combination Model), and an element inside an Include/Attachment/Multi-Select is tested as that whole group. Pinned by `FormEnginePreviewTest` and `DocumentModelTreeFxTest` | |
| Copy elements from another Document Model | "Copy Document Model" (`event_copy_dm`, Ctrl+Shift+C; `copyDocumentModelSagas.ts`): pick a standalone DM (same TD mode as the open one), the backend expands it, every Include is turned into a plain Group, the locales are synced to the open model, and its content is appended under the selected node (root if none). Type definitions: a source that owns TDs gets them copied as new TDs (usages re-pointed); a source that only imports TDMs gets those imports added to the open model's references | **Present (2026-09-20)**: "Insert from Document Model..." in the tree's Add menu, the context menus and on Ctrl+Shift+C (`DocumentModelActions#insertFromModel`, picker `InsertFromModelDialogController`, one undo step via `InsertModelContentCommand`). The work is done by `DocumentModelInsertion` (`a12-studio-models-validation`, `documentinsertion`, `DocumentModelInsertionTest` incl. a sweep of every fixture Document Model): deep copies of the source's root groups; every Include (also inside included models, recursively) becomes a plain group holding the included root group's children; a missing or cyclic Include stays an Include and is reported; type definitions follow SME's all-or-nothing rule (`TypeDefinitionMode`, shared with `IncludeTypeDefinitionModeValidator`) - any used type definition owned by a regular Document Model means all used ones are copied as new local type definitions (fresh id, unique name, fields re-pointed), otherwise the owning Type Definition Models are imported (skipping ones the target imports already, or that another needed one imports) - and a target of the other mode gets an explanation instead of a plan; labels in locales the target does not declare are dropped. Fresh element ids and sibling-unique names come from `DocumentModelElementFactory`. Picker: standalone Document Models only (no Type Definition or Additive models), other than the open one, with a compatible type-definition mode (SME's `hasSameTDMode`); a source that includes the open model is refused. **Differences from SME:** only type definitions a copied field uses are carried over (SME copies every one the source sees); a missing translation for a target locale is not padded with an empty text (the validators report it); a source with an unresolvable Include is still inserted, that Include kept and reported, where SME's expansion fails. Like SME, paths in copied rules/computations are copied verbatim - relative ones keep working inside the copied subtree, an absolute one starting at the source's root group does not. | |
| Model diff / compare | `hasModelDiffEditor`, full settings/tree/typedef diff | **Won't do** (decided 2026-09-19) | |
| Drag & drop reorder/reparent in tree | Per-element `dnd` metadata (`draggable`/`droppable`/`reorderable`) | **Present** (confirmed 2026-09-05) — `DocumentModelElementsTreeController.setupRowDragAndDrop`/`resolveDropPosition`: reorder above/below, reparent into, root-end drop, with fixed-children/attachment-adjacency vetoes | |
| Tree filtering (by type, category, annotated-only, etc.) | Filter panel (`tree/filter/filters.ts`, `useData.ts`): search by **name / id / label**; *Annotated* only; per element type (Validation Rules, Computation Rules, Attachments, Multi-Selects, Includes, Fields); per field type (String, Number, Date, Date-Time, Time, Date Fragment, Date Range, Confirm, Boolean, Custom, Enumeration - by *effective* type, i.e. through type definitions); *Always Required*; *Required If Parent Group Filled*. A group stays visible while any descendant matches | **Present (2026-09-20)**: `DocumentModelTreeFilter` (pure state + predicates, `DocumentModelTreeFilterTest`) applied by `DocumentModelElementsTreeController#toFilteredTreeItem`, edited in a popup behind the filter button next to the search field (`DocumentModelTreeFilterController` / `document-model-tree-filter.fxml`; `DocumentModelTreeFxTest` drives it). Same rules as SME: a hidden element type takes its subtree with it, a group stays while something below it (or, by search, itself) matches, the annotation filter applies to every leaf, the field-type filter sees a type-definition field as the base type it resolves to (through `ElementIndex#effectiveFieldType`; one whose type definition is missing is never hidden), the search matches name, id or label (all locales; only groups/fields have labels). With nothing set the tree is shown unfiltered, empty groups included; the filter button turns filled while a narrowing filter is on and the empty tree says "No element matches". **Differences from SME, on purpose:** the two requiredness checkboxes are a12-studio's two requiredness modes (always / only if the parent group is filled) joined as alternatives - SME ANDs two flags one of which implies the other - and switching one on hides every non-field element (SME keeps all rules and computations listed next to the required fields); the search compares the element name, not the tree label ("Group [5]"); the synthetic Base Model node of an additive model is not an Include the filter could hide. The filter state is per open tree and not persisted. | |
| AI-assisted model generation | `documentModel/ai/*` — generates a DM from a prompt/PDF via `@com.mgmtp.ai.generation` | **Won't do** (decided 2026-09-19) | |
| Additive Document Model (overlay/inherit/overwrite editing) | Separate module (`additiveDocumentModel`), full editing mode | **Partly present** (corrected 2026-09-20; the old "`kernel-md-join` dependency present" was wrong, there is no kernel dependency). An Additive Document Model is a plain Document Model whose header carries the `additive-document` annotation; `ModelFactory` instantiates the marker subclass `AdditiveDocumentModel` (icon, tree toolbar). The editor is the normal Document Model editor plus a read-only **"Additive Elements Only"** tree toggle that previews the base model (`AdditiveDocumentModelResolver`: reverse lookup over the project's Combined Document Models' `Addition` steps). **Updated 2026-09-22:** when more than one Combination Model references the same Additive Document Model, this used to silently pick the first hit; it now matches SME's own `SelectModelWithContextView` behavior instead - silent when there is exactly one candidate, and otherwise a "Select Combination Model" picker (`AdditiveContextDialogController`) shown once when the tab is opened, its choice held only for that editor session and never persisted (`DocumentModelElementsTreeController#resolveContext`). Validation/refactoring (`ElementIndex`, `ProjectReferenceRefactoring`), which have no user to ask, keep the silent first-hit resolution via `AdditiveDocumentModelResolver#findBaseModel`. **Not present:** SME's overlay editing mode (mark elements included/overwritten/purely-additive, add/remove against the base) and a real join (kernel `DocumentModelJoiningService`, feasible per the spike but not adopted). | |
| Composed Document Model (graph composition via Element Picker) | Separate module (`composedDocumentModel`); authored by dragging "Relationship Element"/"Link Relationship Element" diagram nodes, which auto-stamp `cdm.relationship`/`cdm.sourceRole`/`cdm.targetRole`/`cdm.targetDocumentModel` | **Present (2026-09-23)**, without the diagram: a Document Model whose header carries `cdm.queryRoot` (root Document Model id) loads as the marker subclass `ComposedDocumentModel` (mirrors `AdditiveDocumentModel`'s pattern - see the row above); `ComposedDocumentModelResolver` reads/writes `cdm.queryRoot` and the relationship-chain annotations (a12-studio-side numeric-suffix convention for more than one step - SME writes one unsuffixed set per diagram node; a12-studio has no diagram canvas, so it represents the chain as an ordered list instead, same annotation names/values). `CdmQueryRootPanelController` (Document Model Settings dialog) edits both, replacing the raw annotations panel for this key family (`RolesEditorPanelController`'s pattern). Two validators: `CdmQueryRootReferenceValidator` (root resolves to a real Document Model), `CdmRelationshipStepValidator` (each step's relationship/roles/target resolve consistently). **Not present:** the diagram-driven authoring UX itself (see the "Other model types" priority table - Model Graph Diagram is unbuilt) and DM-expansion-backed semantic checks (Open Decision #1). | |
| Multi-select bulk actions | Ctrl+M toggles a checkbox multi-selection panel (Ctrl+Space toggles all, Space toggles one); bulk Delete / Ctrl+X / Ctrl+C on the selection, bulk "Ad hoc Test" (Alt+T). SME also distinguishes copy-node from copy-node-and-children | **Present (2026-09-20)**, with different mechanics: the tree is a `TreeTableView` in `SelectionMode.MULTIPLE` (Ctrl/Shift-click, no checkbox mode, and none is planned); `DocumentModelActions#cutSelection`/`copySelection`/`confirmAndDeleteSelection` act on the *top-level* part of the selection (descendants of a selected node are not handled twice; elements inside Include/Attachment/Multi-Select groups and the synthetic additive base node are skipped), **Ctrl+X only marks** the elements (dimmed, they stay in the tree - SME's tree engine does the same with its `CUT` clipboard action) and the next paste **moves** them (2026-09-21): same elements, same ids, one undo step, path references rewritten like for a drag-and-drop (`RefactoringCommand`), a name taken at the destination made unique in the same step, a paste into the cut group's own subtree refused; pasted into *another* model they are copied there (new ids) and removed from the model they were cut in; a Copy, another Cut, or elements that were deleted in the meantime give the pending cut up (the paste is then a copy again). A paste of copied elements inserts every clipboard entry as a fresh clone (new ids, unique names - now unique among the pasted copies too) after the selected leaf / as last child of the selected group, and the context menu's "Create Overview Model from Selection" is a bulk action SME does not have. **Closed 2026-09-20:** Ctrl+X / Ctrl+C / Ctrl+V (Cmd on a Mac) work on the tree like SME's, each only where its toolbar button is enabled (the buttons' tooltips and the context menu show the keys); a bulk delete, cut or paste is **one undo step** (`CompositeCommand`, before: one per element); pasting several elements no longer risks two clones drawing the same id (`DocumentModelElementFactory.regenerateIds` now tracks the ids it hands out). Still different: the bulk "Ad hoc Test" (see above), copy always includes the children. | |
| Markdown report generation per element | `createMarkdownReport`, used for AI/export tooling | Missing — **parked** (2026-09-20: not started, do not start unless asked; re-verified no code) | |

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
per-language error text. `errorCondition`/`precondition`/`operation` are plain-text `RuleEditorController`
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
`NameConventionValidator`, `TimeZoneValidator`). Added since this analysis (2026-09-20 check of
`DocumentModelValidationService`, 22 validators in all): the per-type config validators `StringTypeConfigValidator`,
`NumberTypeConfigValidator`, `EnumerationTypeConfigValidator`, `CustomFieldTypeConfigValidator`,
`DateFormatConfigValidator`, and `IncludeTypeDefinitionModeValidator` (local vs. imported type definitions must not
mix, shared with the insert-from-model feature). Coverage is already broad and roughly matches SME's custom
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

## Combined Document Model

*Built 2026-09-08.*

a12-studio's Combined Document Model editor (`a12-studio-ui/.../editors/combineddocumentmodel/`, data model in
`a12-studio-models/.../combineddocumentmodel/`) now edits both fields SME defines in
`resources/models/combinationModel/DomainCombination.json`: an optional **Base Model** (`content.baseModelId`,
a Document Model reference reused via `TargetModelPanelController`, now made optional via `setRequired(false)`
since — unlike Mapping's Target — SME has no "missing" rule for it) and the ordered **Combination Steps**
(`content.CombinationSteps[]`, `CombinationStepsPanelController` + an add/edit dialog), each an `Addition` /
`Selection` / `DecorationForFields` / `DecorationForGroups` step referencing an Additive/Selection/Decoration
model. The dialog's Type-driven field enabling mirrors SME's `CombinationEditor.json` `dependentField` rules, so
a step built through the UI can't violate them.

Validation (`de.a12.studio.modelsvalidation.validators.combination`, wired into `CombinationModelValidationService`)
ports all 7 structural `Rule`s from `DomainCombination.json`'s `CombinationSteps` group (missing/not-allowed per
step type, duplicate additive model) plus the generic `HeaderModelReferenceValidator` for invalid references —
**not** ported: the full DM-expansion + SMT rule-contradiction pass and the "Validate model up to this step" row
action, neither of which has any backing implementation in a12-studio (see the Backend/kernel capability map
above — `CombinationModelExpansionService` does not exist in this repo).

**Loop detection ported 2026-09-20** (it needs the reference graph only, not DM expansion — SME's
`CombModelReferenceHelper.modelCausesOrHasLoop` just collects the transitively reachable model ids and checks whether
the candidate itself or the open model is among them). `CombinationBaseModelLoopValidator` (error on
`content/baseModelId`) and `CombinationAdditiveModelLoopValidator` (error on `content/combinationSteps/<i>` for an
`Addition` step), both on top of `CombinationReferenceGraph`, registered in `CombinationModelValidationService`;
messages `validation.combinationBaseModelLoop` / `validation.combinationAdditiveModelLoop` name the model and the
cycle (`A -> B -> A`). Ported semantics, read from the TS rather than assumed: the walk starts at the candidate;
a Combined Document Model contributes its base model and, per step, the additive model (of an `Addition` step),
selection and decoration models; a Document Model contributes its header `modelReferences`, **only those with
purpose `include`** — except below the additive model of an `Addition` step of a Combined Document Model reached on
the walk, where every purpose counts (SME passes `undefined` there, apparently unintentionally, but it is the
behaviour). Selection and decoration models are recorded but **not** followed (SME does not either; the walk into a
decoration model's own references is therefore not checked). A loop is reported when the candidate is the model
itself, leads back to it, or holds a cycle of its own (as SME's `modelCausesOrHasLoop`). Differences to SME: the walk
tracks (model, purpose filter) pairs so a model reached first on an include-only path is still expanded on an
all-purposes path, and a model missing from the project is a leaf (SME throws; the invalid-reference rules report
it). Two things SME does that are **not** ported: the Base Model picker still lists candidates that would loop
(SME's `baseModelReferenceProvider` filters them out; here the loop is reported after the selection), and SME's
`createsIncludeLoop` hook (a Document Model's Include picker asking the combination module about loops) has no
counterpart. Note also that SME registers the conditions `BaseModelMustBeValid`/`AdmShouldNotCauseLoop` but no rule
in the shipped `DomainCombination.json` references them (only `InvalidReference`), so in SME itself the loop
protection is essentially the picker filtering. Tests: `CombinationLoopValidatorsTest` (self-reference, two-model
cycle, cycle the model is not part of, include-vs-untagged purposes, leaf semantics, 3-model chain without a loop),
`FixtureWorkspacesCombinationLoopTest` (every real Combined Document Model of every fixture workspace is loop-free).

A Combination Step's Selection Model reference (`SelectionModel.smId`) needed a real, project-aware picker, but
a12-studio had no `ModelType.SELECTION` at all. Added a minimal stub (`ModelType.SELECTION`, an empty
`SelectionModel`/`SelectionModelContent`, `model-versions.json` entry with `enabled: false`) — same shape as the
existing `PrintModel` precedent (loadable/referenceable project-wide, but opens "not supported yet" until a real
editor exists. **Superseded 2026-09-13** — see the dedicated "Selection Model" section below, `enabled` is now
`true`).

---

## Selection Model

*Built 2026-09-13, superseding the stub described just above.*

SME's own Selection Model shape turned out to be much flatter than the stub-era placeholder assumed: `content`
has exactly three identically-shaped sections — `Data`, `Computation`, `Validation` (field order confirmed
against every real fixture, both in this repo and in SME's own test/example fixtures) — each a `SelectionContent`
(`Default: "Selected" | "Unselected"`, optional `Selected`/`Unselected` arrays of `{path}`). Ported to
`a12-studio-models/.../selectionmodel/`: `SelectionModelContent` (Data/Computation/Validation), `SelectionCategory`
(`defaultValue`/`selected`/`unselected`), `SelectionDefault` (enum), `PathSpecification` (`path`).
`selected`/`unselected` are `@JsonInclude(NON_NULL)` over a nullable (not pre-initialized) `List`, not `NON_EMPTY`
over an eagerly-initialized one — the same absent-vs-explicit-empty fidelity fix this doc's "Known Issues" section
already describes for `A12Model.Header.labels`/`overviewmodel.Column.width`, needed here because real fixtures use
both shapes for the same field (e.g. `PersonSkills_NumberConversion_Se.json`'s `Data.Selected: []` vs. its
`Computation`/`Validation`, which omit `Selected`/`Unselected` entirely).

**No persisted reference Document Model, confirmed by design, not by omission.** Every real `"modelType":
"selection"` fixture found (in this repo and in SME's own repo, including SME's `client/src/modules/selectionModel`
source) has an empty `header.modelReferences` — SME's own standalone Selection Model editor
(`selectionFrameDataProvider.ts`) receives its reference Document Model transiently at open/create time
(`InitialSelectionData.referenceModel`, supplied by whatever the Selection Model was opened from - typically a
Combination Model's base model) and never writes it back to the file; `SelectionModelEntry.isAddable` is even
`false` in SME, meaning a Selection Model can't be created standalone at all, only in that referencing context.
a12-studio's editor therefore has no live Document Model tree/checkbox view (SME's "Selected Elements" tab) - there
is nothing durable in the file to resolve one against, and building a transient "pick a DM just for this editing
session" flow was judged out of scope for a first editor. `Selected`/`Unselected` path specifications are instead
edited as plain, pattern-validated text — the same tier of fidelity `overviewmodel.Column.expression`/
`QueryModelContent.filterDefinition` already get via `RuleEditorController`, just with plain `TextField` rows
here since paths are single-line.

**Editor** (`a12-studio-ui/.../editors/selectionmodel/`): `SelectionModelEditorController`, a single-tab
settings+content layout (no tree) following `CombinedDocumentModelEditorController`'s precedent, embedding three
instances of one shared `SelectionCategoryPanelController`/`selection-category-panel.fxml` (Default combo +
Selected/Unselected repeatable path lists, dynamic-row pattern per `AnnotationsPanelController`) — reused
runtime-retitled per section rather than three near-duplicate FXML/controller pairs, the same trick
`DataTypeDateConfigurationPanelController` uses for Date/DateTime/Time. Not bound to a single `Element` (no element
graph exists), so every field is wired with a plain listener + a local `updatingFromModel` guard and saved via
`commitHeaderChange()`, per `LayoutPanelController`'s documented reasoning — the inherited `bindTextField`/
`bindComboBox` helpers always end up calling the *element-bound* `commitChange()`, which unconditionally hides a
header-style panel's own error container on every debounced commit (there is no bound `Element` to validate), which
would otherwise clobber the validation message this panel deliberately shows.

**Validators** (`a12-studio-models-validation/.../validators/selection/`, wired into a new
`SelectionModelValidationService`) port all 7 structural rules from the kernel's `MM_SelectionModel_2` domain model
(cross-checked against SME's own `DomainSelectionSpecification.json`, which encodes the identical rule set) —
applied uniformly across Data/Computation/Validation via a small `SelectionCategories` helper so each validator is
written once, not three times: `DEFAULT_MISSING`, `ONLY_WILDCARD`/`INVALID_PATTERN` (a single path-specification
grammar regex, ported verbatim), `SELECTED_DUPLICATE`/`UNSELECTED_DUPLICATE`, `SELECTED_IN_UNSELECTED`, and
`DEFAULT_SELECTED_BUT_NO_UNSELECTED`/`DEFAULT_UNSELECTED_BUT_NO_SELECTED`. **Not ported**: the header-level
`HEADER_ID_STARTS_WITH_XML` and `mustNotHaveARolesAnnotation` rules from the kernel's Selection-Model-specific
header meta-model (`DomainSelectionSettings.json`) — the former has no equivalent anywhere else in a12-studio today
(every other model type's header could use it too; adding it only for Selection Model would be inconsistent scope
creep) and the latter is already soft-mitigated by `AnnotationsPanelController` hiding any header annotation named
`"roles"` from view for every model type. Selection Model's join/consistency check against a live Document Model
(SME's `SelectionModelController`, see the Backend/kernel capability map below) remains unimplemented, same as
every other kernel-join capability in this repo.

**Header round-trip gap — resolved (verified 2026-09-19)**: enabling `ModelType.SELECTION` (`model-versions.json`) made
`AdvancedNewProjectModelsRoundTripTest` exercise `PersonSkills_NumberConversion_Se.json` for the first time; its header
has none of `locales`/`labels`/`modelReferences`, and `A12Model.Header` used to re-serialize them as `[]`. `A12Model`
now distinguishes an absent key from an explicit `[]` (null-backed header DTO + `*Explicit` flags, see CLAUDE.md
"Known issues"), so the file — and every other model type's header — round-trips unchanged. Pinned by
`A12ModelHeaderRoundTripTest`; the Basic, Advanced-new and Commerce round-trip suites all pass (Advanced-new: 96 tests,
0 failures, 3 skipped for model types with `enabled=false`).

---

## Print Typesetting Model

*Built 2026-09-25. `modelType` `typesetting`, suffix `TSM` (SME's own fixtures use `_TSM`).*

**Where SME's behavior actually lives.** SME's `client/src/modules/printTypesettingModel` is a ~450-line shell (module
registration, data provider, reference provider for roles): the editor UI, the marshaller and the validation are all
in the external `@com.mgmtp.a12.print/print-typesetting` npm package, and the server-side check in
`com.mgmtp.a12.print:print-typesetting` (`TypesettingModelValidator` = the kernel run over the
`DomainTypesettingMetaModel`). Both are downloadable anonymously from the community repo
(`https://artifacts.geta12.com/artifactory/a12-community-maven/com/mgmtp/a12/print/print-typesetting/<v>/print-typesetting-<v>-sources.jar`
and `.../api/npm/a12-community-npm/@com.mgmtp.a12.print/print-typesetting/-/print-typesetting-<v>.tgz`; 3.2.3-4.0.1
in the Maven repo, SME pins 3.2.1 which is not published). The npm package is the source of truth for the editor; the Java jar carries
`models/DomainTypesettingMetaModel.json`, the kernel meta-model with the field limits and the roles rules.

**Model.** `header` has only `id`, `modelType`, `modelVersion` and `annotations` (the `roles` annotation) — no locales,
labels or model references. `content` is `customHyphenationExclusions` (`[{word, index[]}]`), `preventLineBreakRules`
(`[{pattern}]`), `internal` (the generated hyphenation dictionary, `{}` in every model SME creates), `orphan` and `widow`
(0-10, default 2). Ported to `a12-studio-models/.../typesettingmodel/`: `TypesettingModel`, `TypesettingModelContent`
(the two hyphenation containers are raw `JsonNode`s so whatever a file carries survives a load/save cycle and an absent
key stays absent), `PreventLineBreakRule`, and `PreventLineBreakRules`/`PreventLineBreakRuleType`/`SpecialPattern`.

**One list, three tables.** Every rule is a regex in `preventLineBreakRules`; the editor tells them apart by shape
(`rule-conversion.ts`): one of two curated **special patterns** (`§\d+ Abs\. \d+`, `\d+\(\d+\)\([a-zA-Z]\)`), else a
**number unit** if it starts with `[+-]?(\d{1,3}(?:[.,]\d{3})+|\d+)(?:[.,]\d+)? `, else a **character sequence**; the
latter two are stored regex-escaped. A row added but not yet filled in is stored as `{{character}}`/`{{unit}}`/`{{special}}`
so it stays in its table. `PreventLineBreakRules.classify/toValue/toPattern` port this, pinned by `PreventLineBreakRulesTest`.

**Editor** (`a12-studio-ui/.../editors/typesettingmodel/`), one extracted property editor per SME section:
`CharacterSequenceRulesPanelController`, `NumberUnitRulesPanelController`, `SpecialPatternRulesPanelController` (a shared
`AbstractRulesPanelController` holds the row/add/delete/validation behavior) and `OrphanWidowPanelController`. Roles are
not in the editor: the Model Settings dialog shows only the General information (name, description) and Roles panels
for this model type (every other panel is hidden and left unbound so none can disable Save), and the New Model dialog neither asks for nor writes locales.

**Validation** (`TypesettingModelValidationService`; no locale validators, the header has no locales):
- Character sequence: required, letters and hyphens only (`^[\p{L}-]+$`), at most 20 characters.
- Number unit: required, no digits, whitespace or special characters, at most 20 characters.
- Special pattern: must be chosen. Duplicate rules are reported on every occurrence, across all three tables.
- Orphan/widow: 0-10 (the meta-model's `minValue`/`maxValue`). The spinner clamps typed values, so only a file can carry
  an out-of-range one; it is shown as is and reported.
- Roles (`HeaderRolesValidator`, reusable for any model type): valid role names, no duplicates, no blank role (errors);
  role missing from `auth/roles.yaml`, a roles file present but no roles on the model, roles but no roles file
  (warnings). Role problems also show on the settings-button badge (`ValidationService#getSettingsIssueMessages`).
  SME's own role regex has a character range (`,-_`) looser than its message; the validator checks what the message says.

**Not done / known limits.** A Print Model's Text Styles cannot pick a Typesetting Model yet: the Print Model editor is
disabled and has no typesetting-reference UI (SME's schema tab does). Renaming a Typesetting Model already rewrites the
header references of the models pointing at it (generic id-based rewriting). No `customHyphenationExclusions` editing,
matching SME. Not checked against the real print engine: the regexes are only compared with SME's editor's output.

---

## Form Model

*Analyzed 2026-09-06 (the previous version of this section, "Form Model — not started", was written before this
module existed in a12-studio and is factually wrong — do not trust anything from before this date about Form Model).*

a12-studio's Form Model editor is **substantial, not empty**: 76 data-model classes
(`a12-studio-models/.../formmodel/`), 92 UI classes (`a12-studio-ui/.../editors/formmodel/`, tree +
per-node-type editors + dialogs, plus the preview server in `.../ui/preview/`), and 30 form-specific validators
(`a12-studio-models-validation/.../validators/form/`; counts as of 2026-09-20, the section was written against 55 /
~35 / 6). It covers the core Screen/Section/ControlGrid/Row/Control
tree, field/group configuration with dependent-field/group hide-and-readonly rules, responsive (lg/md/sm) layout,
repeats (Inline/Embedded/Detached), buttons, and a lightweight live preview. The gaps below are real but are gaps
*within* a mature editor, not a from-scratch build.

### Element types & structural coverage

| SME element/concept | a12-studio | Status |
|---|---|---|
| `Screen` | `Screen` | Has it, including `initiallyFocusedElementId` (2026-09-19): `InitiallyFocusedElementPanelController` in the Screen tab offers the editable Controls of the first screen outside of repeats (SME's `focusableElementsEnum`, shared as `InitiallyFocusedElementSupport`), is hidden on every other screen unless a stale value is set (so it can be cleared), and `FormInitiallyFocusedElementValidator` ports both SME rules (`InitialFocusedElementOnlyOnFirstScreen`, `InvalidReference`) |
| `Section` | `Section` | Has it (collapsible/initiallyCollapsed) |
| `MultiColumnSection` | `MultiColumnSection` | Has it. Layout: `FlexLayoutPanelController` edits `lg`, and since 2026-09-19 the same `ResponsiveLayoutPanelController` as the Control Grid edits `md`/`sm` (it now takes a getter/setter pair instead of a `ControlGrid`); the md/sm column-count check already existed in `FormLayoutColumnSumValidator`. SME's `mustHaveLayout` (`layout/lg` required) is validated since 2026-09-21 by `FormMultiColumnSectionLayoutValidator` |
| `ControlGrid` | `ControlGrid` | Has it (layout, verticalAlignment, readonly/readonlyPresentation) |
| `Row` | `Row` | Has it |
| `Control` | `Control` | Has the fields. `index` (`ControlIndex`: `type` SEMANTIC/NUMERIC + `value`) since 2026-09-19 - despite the "search-indexing" reading it is SME's *Control Index*: which repetition of a repeatable group a Control shows when it is placed outside that group's repeat (numeric = row number, semantic = value of the group's index field). `ControlIndexPanelController` is shown when `ControlIndexSupport.isIndexable` says so (SME's `isIndexableControl`: granularity distance from the enclosing Embedded/Detached Repeat to the field is positive, needs `ElementIndex.granularity`) or an index is already set. No real fixture carries an `index` (the wire shape is SME's mapping rule + the editor's `Index` group), and SME applies no checks to the value. Not done: SME's backend consistency error for an indexable Control *without* index (belongs with the form-vs-DM drift check, TODO #7b) and the heterogeneous-to-many initial-value rule. `nameForTree` is deliberately **not** modeled: SME derives it from the Document Model element's name for the editor tree (`transformWithDocumentModel.ts`), it is not in the wire mapping (`mappingRules/control.ts`) and appears in no fixture. `datePickerConfig` (year range of the date picker) has a panel since 2026-09-19 (third pass), shown for date, date-time and `YYYY-MM-DD` date-range fields - `DatePickerConfigPanelController`, shared with the column editor; an empty config is dropped like SME's `removeEmptyDatePickerConfig`, and `FormDatePickerConfigValidator` ports the three range rules of `I_DatePickerConfig.json` |
| `TextCell` | `TextCell` | Has it, with an editor panel (`FormNodeEditorTextCellPanelController`, step 2 below; corrected 2026-09-20, this row said "no editor panel") |
| `ExpressionCell` | `ExpressionCell` | Has it, with an editor panel (`formnode-editor-expression-cell-panel.fxml`; corrected 2026-09-20). Its expression is still edited in a plain `TextArea`, not the rule editor - see the CLAUDE.md convention on expression fields |
| `CustomCell` (a named custom-component cell inside a grid row) | `CustomCell` | Has it, with an editor panel (`FormNodeEditorCustomCellPanelController`, step 6 below; corrected 2026-09-20, this row said "Absent") |
| `CustomScreenElement` | `CustomScreenElement` | Has it, including `height` (2026-09-19): `CustomScreenElementHeightPanelController` edits it as whole pixels (blank = component's own height) and `FormCustomScreenElementHeightValidator` ports SME's `zeroNotAllowed`; all 16 `height` values in the fixture workspaces are on this element type |
| `ButtonPanel` (a button bar addable as its own node *inside* the screen tree) | `ButtonPanel` | Has it, addable inline in the tree with its own editor (`FormNodeEditorButtonPanelPanelController`, step 6 below; corrected 2026-09-20, this row said "Absent" and that buttons only live in the header/footer boxes) |
| `DetachedRepeat` / `EmbeddedRepeat` / `InlineRepeat` | same | Has it structurally; see "Repeats" below for field-level gaps |
| `Binding` / `BindingRepeat` (CDM relationship-driven selector/repeat) | `Binding` and `BindingRepeat` | **Both present (2026-09-23)**: `Binding`/`BindingContent`/`BindingDetails`/`BindingMetaInformation`, created by dragging a relationship from the Relationships panel onto the tree (`RelationshipModelPanelController` → `FormModelTreeController#dropRelationshipModel`), which creates a `BindingRepeat` instead when the target role is to-many (`FormModelElementFactory.isToManyTargetRole`). `BindingDetails` now also models the UI-component configuration: `mainComponent`/`editModalComponent` (`BindingComponent`: `name` — Drop Down/Dual Pane/Table List — `modelsSME`, page sizes, `propsExtensions.dualPaneProps`/`tableListProps`), `isFixedRelationship`, `cdmChildActivitiesEnabled`, `modificationConfiguration`; anything still unmodeled stays in each class's `extras` map. Edited in `FormNodeEditorBindingPanelController` (a plain `Binding`) or `FormNodeEditorBindingRepeatPanelController`/`BindingRepeatRelationshipPanelController` (a `BindingRepeat`, which also reuses the groupRef-independent repeat sub-panels — Column Settings, Alignment, Additional Settings, Row Actions, Hide Condition, Styles, Header Styles, Annotations), both embedding the shared `BindingComponentPanelController` for the component config. Checked by `FormBindingRelationshipReferenceValidator`/`FormBindingTargetRoleValidator` (extended to cover `BindingRepeat`), the new `FormBindingComponentReferenceValidator`/`FormBindingComponentRequiredFieldsValidator`, and `FormBindingRepeatCdmRequiredValidator` (a `BindingRepeat` needs its Form Model bound to a `ComposedDocumentModel`). |
| `FieldBasedRepeatOverviewColumn` | `FieldBasedRepeatOverviewColumn` | Present, with "Add Column" and an editor panel (label/width/sortable/filterable/preferred sorting, readonly, message position) plus, since 2026-09-19, panels for **display** (hide label, fixed width, filter exposition - only enabled for a filterable column), **pin direction**, **icon**, **alignment** (independent horizontal/vertical overrides for header and content), the per-column **hide condition** (`ConditionallyHidden`; master fields scoped to the column's own field, like a Control), **header styles**, annotations and, for a column bound to a date field, the **date picker** year range. The width field takes decimals (SME: at least 0.3, one decimal place; `FormColumnWidthValidator`), which the editor used to truncate to an integer - `RepeatOverviewColumn.getWidth()` is now a `Double` and still writes an integral width as an integer, like the fixtures |
| `ExpressionRepeatOverviewColumn` (compute a column via expression instead of a field) | `ExpressionRepeatOverviewColumn` | Present (step 2 below); its expression is edited with the rule editor (`RuleEditorController`), and it shares the pin direction / icon / hide-condition panels above (hide-condition master fields scoped to the enclosing repeat's group, as in SME's `resolveDmElementForFmElement`) |

### Field/group configuration & the dependency system

This is the area with the most concrete, well-defined gaps:

- **`FieldConfigEntry`** (`a12-studio-models/.../formmodel/FieldConfigEntry.java`) has `label`, `placeholder`, `hint`,
  `initialValue`, `suffix`, `exposition`, `readonly`, `dependentField`, `elementRef`. SME's `FieldConfigurationEntry`
  additionally has: `enableSelectAll` (multi-select "select all" toggle), `formatting`, `secret` (password masking),
  `annotations`, and — the two biggest ones — **`dependentEnumeration`** and **`externalEnumeration`**, each an
  entire feature a12-studio had no representation of at all. **All of these were added on 2026-09-06 (step 1 and
  step 3 in the build order below; re-checked 2026-09-20) - the description that follows is the baseline that
  motivated that work:**
  - `dependentEnumeration` (`DependentEnumeration`: `masterField` + `constraint[]`, each `{masterValue,
    constraintValues[], valueForMasterChange}`) — constrains *which enum values* a dependent field may offer based
    on a master field's value (distinct from `dependentField`'s readonly/notRelevant, which only affects visibility).
  - `externalEnumeration` (`ExternalEnumeration`: `src`, `customValuesAllowed`, `caseSensitive`) — sources a field's
    enum options from an external URL instead of the Document Model's own enum definition.
  - `attachmentConfig` (`placeholderIcon`, `accept` MIME filter, `defaultAction` replace/download) — upload-field
    presentation config. **Closed** (2026-09-19, second pass): the model had `placeholderIcon` only and no UI; it now has
    all three fields and `AttachmentSettingsPanelController`, shown for a Control bound to an attachment group and for
    the attachment group in the Data Configuration tab (the entry is keyed by the *group's* id, as in SME). Absent means
    the default; an `attachmentConfig` with nothing set is removed again.
- **`GroupConfigEntry`** is roughly at parity (`dependentGroup`, `groupRef`, `numberOfInitialRows`); a12-studio even
  adds `label`/`hint`/`placeholder` fields SME keeps elsewhere — not a gap, just a modeling difference.
- **Hide condition — closed 2026-09-06 (step 1), text below is the baseline.** (`ScreenElement.hideConditionField`/`hideConditionValue`,
  `a12-studio-models/.../formmodel/ScreenElement.java`, both since replaced by `HideCondition`/`HideConditionCase`): a12-studio modeled exactly **one** trigger value per master
  field. SME's `HideCondition` (`fmElements/types/hideCondition.ts`) is `{masterField, cases: HideConditionCase[]}`
  — a list of trigger values. This matters concretely for Enum-typed master fields: SME can hide an element when the
  master is any of several enum values, a12-studio can only match one. This is a structural data-model gap (not
  just missing UI) — fixing it means changing `ScreenElement` from two scalar fields to a
  `masterField`/`cases[]` shape, which is also what would let a12-studio implement SME's two hide-condition
  validators (see below).
- **`DependentField`**: present and matches closely (`masterField` + cases of `masterValue`/`notRelevant`/`readonly`),
  and `DependentCase` also carries `value`/`fieldRef` - force a specific value, or copy one from another field, when
  the master changes (the Dependent Field panel's Value-Type picker, step 3 below; corrected 2026-09-20, this bullet
  said a12-studio had no equivalent).
- **`DependentGroup`**: at parity (`masterValue`/`notRelevant`/`readonly`).
- **Dependent controls** (a control/group hiding *other* screen-tree nodes, SME's `dependentControls`/
  `ScreenElementRef`): SME stores them on the master `Control` itself (`dependentControls.screenElement[]` of
  `{idref, masterValue}`); a12-studio models exactly that (`Control.dependentControls`, lossless round-trip, validated
  since 2026-09-19). **Closed 2026-09-20 (TODO #7):** the Dependencies tab (`DependentControlsPanelController`) now
  edits it for Boolean, Confirm *and* Enumeration masters (SME's `isPossibleDependentControlMaster`) - the earlier
  Confirm-only tab was an unaddressed gap, not a scoping decision, and wrote an a12-studio-only shape
  (`notRelevantNodes` inside the bound field's `dependentField.case[]`, unknown to SME and the Form Engine). That shape
  is now legacy: still loaded and shown, moved into `dependentControls` on the first change. Candidate rules are
  ported in `DependentControlSupport` (allowed type, not a container of the master, compatible data context). **Delete
  and Cut** now take the ids of the removed elements out of every master's `dependentControls` in the same undo step
  (2026-09-21, `RemoveDependentControlEntriesCommand`; a block left without an entry is dropped, undo restores it);
  the validator and the tab's warning stay as the safety net. Not ported: the T/D flags on the tree rows.

### Repeats

SME's shared `RepeatBase` (`fmElements/types/detachedRepeat.ts`) has several fields `AbstractRepeat.java` doesn't:
- **`filterExpression`** — a per-repeat filter expression. **Present since 2026-09-06 (step 4)** on `AbstractRepeat`, with UI.
- **`initialSorting`** — which overview column the repeat is initially sorted by. **Present since 2026-09-06 (step 4)**
  on `AbstractRepeat`, with UI. Its matching SME validator ("a repeat's `initialSorting` column must itself be
  sortable", `SortableColumnCustomCondition`) is ported since 2026-09-21 as `FormInitialSortingColumnSortableValidator` (an
  initial-sorting column whose `sortable` is not `true` is an error on the column, for both column types, repeats in a
  detail screen included; the Overview Model has its own `OverviewInitialSortingReferenceValidator`).
- **`rowActionGroup`** — a list of custom row actions, each with its own `buttonStyling`, `event`, `confirmation`/
  `confirmationDialogTitle`, and `scope`. **Closed** (step 4 below, completed 2026-09-19): the "Row Actions" table edits
  event/scope inline and an Edit dialog (`RowActionDialogController`, mirroring SME's `I_SectionRowAction-form.json`
  and the Button dialog) edits everything else — functions, confirmation title/message, visual settings, label,
  description, styles, annotations. **The default row action** (2026-09-19, second pass) has its own panel on
  Detached/Embedded repeats (`RepeatDefaultRowActionPanelController`: Edit/View, Download with multi file upload, any
  custom row action without a confirmation, plus hide-button) and SME's synchronisation is ported as
  `DefaultRowActionSupport` (validation module): the default is cleared when its row action is deleted or gains a
  confirmation, follows a rename, and a "Download" default is cleared when multi file upload is switched off.
  `FormDefaultRowActionValidator` reports a default the editor could not have offered. Rename tracking assumes
  the row-action list keeps its length (an inline event edit or the Edit dialog) - the same assumption as SME's
  index-based lookup in the form-engine backup.
- **`titleHidden`** — present since 2026-09-06 (step 4).
- **`confirmationTexts`** per-repeat override — present since 2026-09-06 (step 4), next to the model-level default
  (`Defaults.confirmationTexts`).
- **`MultiFileUploadOptions`** (attachment-repeat config: download toggle, upload description/button/helper text) —
  **Closed** (2026-09-19): `RepeatMultiFileUploadPanelController` on Inline/Embedded repeats (hidden for Detached). As
  the SME docs require, enabling picks the repeated group's single non-repeatable attachment group automatically
  (`MultiFileUploadSupport`) and is refused with an error naming the group if there is none/several. The
  `attachmentConfig` gap above (`accept`, `placeholderIcon`, `defaultAction`) is closed too, see there.
- **`TableStyle`**: SME's has `cardHeight`/`actionColumnWidth` in addition to `tableHeight`/`rowHeight`; both were
  added to a12-studio's `TableStyle` on 2026-09-06 (step 4).
- **No repeat-type conversion** (Detached⇄Embedded⇄Inline⇄Binding) exists in the UI — converting requires
  delete-and-recreate, confirmed by reading `FormModelActions`/`FormModelNodeTypes`.
- **`BindingRepeat`** (2026-09-23): a new `AbstractRepeat` subclass wrapping a `BindingContent` (see the
  `Binding`/`BindingRepeat` row in "Element types" above) instead of a `groupRef`-bound Document Model group.
  Its editor pane deliberately reuses only the repeat sub-panels that don't depend on a `groupRef` (Column
  Settings, Alignment, Additional Settings, Row Actions, Hide Condition, Styles, Header Styles, Annotations);
  Field Information/Label/Hint/Placeholder (all `GroupConfigEntry`/Document-Model-group-dependent) and Default
  Row Action/Multi File Upload (Inline/Embedded-specific, not obviously applicable to a relationship-driven
  repeat) are not shown for it — revisit if a real need for any of them on a `BindingRepeat` surfaces.

### Includes / transclusion — present (2026-09-20, TODO #8)

**What SME does.** SME's editor has no include-insertion code: `ScreenElementBase` only carries the `Included` mixin
(`includeId`/`formModelRef`/`hostDocumentModelPath`) and shows a "link" icon (`isIncluded()`). The expansion is a
build-time batch, `FormModelExpansionBatchCLI` from `com.mgmtp.a12.formengine:formengine-model` (SME's Gradle task
`resolveFormIncludes`, commit "A12SME-2367 Form Model Dev-Includes"), which rewrites the model files in place.
**The library is published with sources** in the community repo (`artifacts.geta12.com/artifactory/a12-community-maven`,
`com/mgmtp/a12/formengine/formengine-model/<version>/...-sources.jar`, 38.4.0–39.0.1 at the time; SME's own 38.3.0 is not
there; EUPL-1.2/commercial), so the exact semantics of `IncludeExpansion`/`IncludeMapper` were read, not guessed. What it does:

- The unit is the **first screen** of the referenced Form Model, not an arbitrary subtree: all its screen elements
  replace the include element. Only Section, ControlGrid, ButtonPanel and the three repeats can carry an include; the
  parent must be a Screen, a Section or (single ControlGrid) an EmbeddedRepeat.
- Every copied id gets `<includeId>_` in front (Screen, Section, repeats, ControlGrid, Row, Control, TextCell, columns,
  ButtonPanel, Button, header/footer). `includeId` is not an id of the source form.
- `elementRef`/`groupRef` (Control, field column, the three repeats) are rebound **by path**, not by prefix: id ->
  path in the source Document Model, source root group name dropped, appended to `hostDocumentModelPath`, resolved in
  the host Document Model (a `/` path keeps the source path). With `hostDocumentModelPath` = an Include group of the
  source's Document Model this yields `<includeGroupId>_<sourceId>`.
- Provenance goes on each copied top-level element only. A single element takes the include element's name, several get
  `<includeId>-<name>`.
- The source's field/group configuration entries for what the copy binds to are re-keyed and merged (host attribute
  wins, only unset attributes are filled). Expression cells make it fail.
- Re-running the expansion on a model that already contains it re-expands it (neighbors with the same `includeId` count as
  one include), so an include is refreshable.
- The Form Engine leaves hide-condition/dependency masters, dependent controls and everything inside expression
  columns untouched.

**What a12-studio does.** `FormIncludeExpander` (`a12-studio-models-validation`, package `formincludes`) ports that
behavior (clean-room, written against the behavior above) and additionally rebinds hide-condition masters, dependency
masters/cases and `dependentControls` (an id outside the copied screen is dropped with a warning), and refuses
expression cells/columns and Bindings. `ElementIndex.resolveIdByPath` is the new path -> form-id lookup. UI: tree "Add"
menu (context menu and toolbar) -> *Include Form Model...* on a Screen, Section or Multi-Column Section
(`IncludeFormModelDialogController`: source form, Document Model path - candidates are the host DM's Include groups of the
source's DM, but any path is accepted -, optional name; every change is a dry run, so OK is only enabled for an include that
works); *Refresh Include* on an included element (replaces the whole run, undo brings the old one back);
`ExpandIncludeCommand` makes either one undo step (config entries added or merged, undo puts the original instances back);
included elements show a link badge; duplicating/pasting an included element drops the provenance of the copy (two
neighbors with one `includeId` would be refreshed as one). `FormIncludeProvenanceValidator` checks the three fields are
complete, the form exists and the path still exists in the host's DM. Tests: `FormIncludeExpanderTest` (a golden test:
SME's `HostModel_expanded.json` with the address section emptied is expanded and must equal SME's own output),
`FormIncludeProvenanceValidatorTest`, `ExpandIncludeCommandTest`, `IncludeFormModelDialogControllerTest`,
`FormModelActionsIncludeMenuTest`, `FormModelActionsIncludeProvenanceTest`; fixtures are SME's four files
(`formincludes/`, the Document Models converted to `includeConfig` - SME's copy still uses `modelAlias`, plus a second
Include group `billing`).

**Follow-ups, done the same day.**
- *Embedded Repeat grid slot.* Include Form Model is also offered on an Embedded Repeat (the Form Engine's third parent
  kind): the included form's first screen must be exactly one Control Grid (`Expansion.isSingleControlGrid()`; the dialog
  says so by naming the form and disables OK otherwise), it replaces the repeat's grid (asking first if the current grid has
  cells), and such a grid can be refreshed although it has no siblings. `ExpandIncludeCommand` takes a list range or the slot.
- *Rename/move.* `ProjectReferenceRefactoring` now rewrites the `hostDocumentModelPath` of the includes of every Form Model
  bound to the changed Document Model (data-binding reference, else the first Document Model reference), through the same
  `PathRewriter` as the other absolute paths, in the same undo step. The element references of the included elements are ids
  (`<includeGroupId>_<id>`) and don't change. Not covered: a form bound to a Document Model that merely *includes* the
  changed one, with a path that runs into the included model.
- *Combined Document Model as the source's DM.* A form bound to a Combination Model can be included: the UI hands the
  expander `ProjectDocumentModels.getOtherDocumentModelsWithCombinations`, i.e. the plain Document Models plus the stand-in
  `CombinedDocumentModelElements` makes of every combination (it carries the combination's id). Base fields keep their ids,
  additive ones stay `md5(additiveId)_<id>` below the Include group (tested on the real `PersonEmployee_Fm`, 32 references).
  It inherits that class's approximation: Selection and Decoration steps are not applied, and below an Include group the
  elements of *all* root groups (base and additive) are one name space, so equal names in two of them resolve to the first.

**Not done:** a Form Model that is *bound to* a combination as the include's **host**: the form editor does not resolve a
combination for its own tree at all (`documentModel` is null there, so the Include action is not offered) - a limit of the
editor, not of the include.

### Model-level / editor-structure gaps

- **"Data Configuration" tab — added 2026-09-06 (step 3 below; the bullet is the baseline).** SME centralizes all dependent-enum/field/group and hide-condition authoring in one
  tab with a fields tree, candidate-value pickers, and refactor-safe editing
  (`editor.elements/dependencies/`, ~35 files). a12-studio spreads the equivalent across per-node panels only
  (Hide Condition panel per node, Confirm Dependencies tab for Confirm controls) — there's no centralized place to
  see/manage every dependent-enum or dependent-field rule in the model, and no authoring path at all for
  `dependentEnumeration`/`externalEnumeration` since those don't exist yet.
- **"Cleanup" tab — added 2026-09-06 (step 3 below; the bullet is the baseline).** SME's `editor.cleanup/` flags `FieldConfigEntry`/`GroupConfigEntry` rows that no longer have
  a live referencing screen node or a resolvable DM field/group (`isInconsistentEntry`), and offers a one-click
  "Clean All". a12-studio's `FormFieldReferenceValidator` catches the same dangling-reference case but only as a
  validation *error* — there is no UI action to prune it, so the user has to hand-edit JSON or delete-and-recreate.
- **Structural consistency check** between the form model and its (possibly since-changed) document model. SME's
  is a categorized `Problem[]` (INFO/WARNING/ERROR) background check done by the kernel's `ConsistencyValidator`
  (`FormModelConsistencyCheck.kt`; its `FormModelCategory` list is not visible from the sources available here).
  **Partly closed 2026-09-20 (TODO #7):** a12-studio has no server-side architecture and doesn't need one - the drift
  cases are ordinary validators over the Document Models the project already holds: `FormDependencyDriftValidator`
  (dependent field/group/enumeration, hide condition and control dependency masters: gone, wrong type, a case value
  the field no longer has; dependent enumeration values; dependent field forced value / copy source),
  `FormDependentControlContextValidator`, `FormReferenceTypeDriftValidator` (Control/column on a group, Repeat on a
  field or a no longer repeatable group) and `FormControlIndexRequiredValidator`. All ERROR: unlike SME's three
  severities a12-studio's validation only distinguishes what makes the form wrong. Missing field/group references were
  already `FormFieldReferenceValidator`/`FormGroupReferenceValidator`, hide-condition values
  `HideConditionSupportedValuesValidator`. Not detected (no SME category known / no fixture): field-type changes that
  invalidate per-type Control settings other than the date picker range, and a `Control` whose field type no longer
  suits its Control type.
- **Style presets.** `FormModelContent.styles` (a model-level named style-class list) has a panel in the Model
  Settings dialog (2026-09-19; the shared `StylesPanelController`, restored on Cancel via `ModelSnapshot`). Second
  pass, same day: like SME, the list is now the source of the per-element `style`/`headerStyle` entries. Inside a
  Form Model the Styles panel offers a combo of the defined styles (typing is only for the model-level list itself),
  and Save in the Model Settings dialog carries renames and deletions over to every element that uses the style
  (`FormStyleReferences`, a reflective walk over all `List<Style>` fields - checked against the JSON of every fixture
  form model - so a new element type with a style list is covered automatically). `FormStyleReferenceValidator`
  reports an undefined or nameless style. Deliberately at Save, not while editing, so Cancel can't orphan
  references. Not covered: the Button dialog and Row Action dialog only see the presets through the selected
  project item (fine in the app, where it is always the form).
- **Form Engine preview (2026-09-25) - the Form Model editor's Preview button now renders with the real Form Engine.**
  The earlier wireframe (`PreviewServer` + `preview.html`, boxes and field names only) is kept as the fallback when the
  A12 installation has no Simple Model Editor and for the Application Model preview. The real preview does not embed or
  copy any Form Engine code: SME's in-editor preview is a workspace package of the SME repo (`moduleSupport/fmm`, not
  private as an older note here claimed) that boots the SME client bundle in "preview window" mode (`window.name ===
  "preview-window"`) and talks to its opener over `postMessage`. a12-studio serves the **installed** SME client
  (`<installation>/bin/simple-model-editor/<v>/static`, read at runtime like the Preview App client, see `SmeInstallation`)
  under `/sme/` with `form-engine-bootstrap.js` injected, which plays the opener: it answers `request-initial-data` with
  `set-initial-data` and pushes edits as `update-formModel`/`update-models`, polling `/fe/{session}/data` on the Preview
  settings' refresh interval (revisions keep unchanged parts from being resent). The data (`FormModelPreviewSession`):
  the live Form Model, the Document Model (or the Combination Model's merged Document Model) expanded by the installed
  SME **backend** (`SmeBackend`: `sme.jar` started on demand with the bundled JRE, POST `/api/document-model/expand`,
  `/api/combination-model/expand`, `/api/document-model/generate-validation-code`; stopped with the studio), and its
  generated JavaScript validation code. The page opens in the browser selected in Preferences > Preview (the old Form
  button ignored that setting and used Edge/Chrome autodetection). Learned on the way: the Form Engine only accepts a
  Form Model whose content has `subHeaderBox` and `footerBox` (the studio omits them while empty, so a fresh Form Model
  failed with "Json is no valid FormModel!"; the preview adds them to its copy); and the installed backend's request
  shapes are what counts - the SME source checkout differs (its combination expansion takes `modelId`/`documentModels`/
  `selectionModels`/`combinationModels`, the installed 13.0.2 takes `combinationModel`/`referencedModels`; check with
  `javap` on `BOOT-INF/classes` in `sme.jar`). **Not wired:** the preview's Theme and Data menus (workspace `.theme`
  files and sample documents are not offered, edits made in the preview are not saved), Form Models bound to a
  Relationship UI Model's relationships render without them (as in SME, the preview only knows the Document Model).
  **Added 2026-09-20 for completeness:** a12-studio also has SME's *other* preview concept, "Deploy → Preview App":
  `PreviewAppProcess` / `PreviewAppDeployer` (`a12-studio-ui/.../previewapp/`) run the real `preview-app-server` from an
  A12 installation for the open project, after `ModelConversionService` (the vendor's `WcfCli`, separate JVM) has
  converted the Document Models to runtime form. That is a real render of a *deployed* project, not the instant
  in-editor preview.
- **Content Model preview (2026-09-25) - the Content Model editor's center is a `WebView` running the real Content
  Engine.** SME resolves it like the Form Engine preview: the SME client booted as the "content preview window"
  (`window.name` contains `content-model-preview=true`, `ContentModelPreviewWindow` in
  `@com.mgmtp.a12.contentengine/contentengine-editor`, started from `createContentEnginePreview` in
  `modules/contentModel/preview`) asks its opener over `postMessage` (`request-data`, `ping` every 500 ms, later
  `request-document`/`request-theme`) and gets `send-data {contentModel, documentModel, themeNames, documentIds}`.
  a12-studio serves the installed client under `/sme/?content=<session>` with `content-model-bootstrap.js` injected
  (same mechanism as `form-engine-bootstrap.js`, one `PreviewServer`), polling `/cm/{session}/data` on the Preview
  settings' refresh interval; `ContentModelPreviewSession` supplies the live Content Model and, if its header has a
  `document-model-for-content-model` reference, the Document Model expanded by `SmeBackend`. The editor embeds that URL
  in a JavaFX `WebView` (`ContentModelEditorController`, layout tree | preview | properties like SME's tree | canvas |
  settings); without a Simple Model Editor in the A12 installation the center shows why instead. Learned on the way:
  (1) JavaFX 21's WebKit has **no IndexedDB**, which the client uses while booting - the bootstrap installs a tiny
  in-memory stand-in with `Object.defineProperty` (a plain assignment throws, `indexedDB` is a getter-only property);
  (2) the preview wants the Document Model as the kernel's own deserialized object plus the serialized one, while the
  server only has JSON: the bootstrap gets the kernel's `DocumentServiceFactory` out of the client bundle (chunk
  registration -> webpack's require -> the module whose source defines `getDocumentModelSerializer(){return new`; export
  names are minified, `webpackRequire.c` is not exposed) and deserializes the model without its `__meta` groups, as SME
  does; (3) the client fetches `api/document-model/generate-validation-code` itself, so `PreviewServer` answers that
  one path under `/sme/` through `SmeBackend`; (4) the client re-renders on every `send-data`, so the bootstrap only
  sends when something changed. Measured in the WebView: ~4 s to render a plain model, ~9 s for one bound to a Document
  Model (includes starting the backend). **Not wired (as for the Form Model preview):** themes and sample documents
  (`themeNames`/`documentIds` are empty, so the Theme/Data menus are empty), and edits made inside the preview.
- **Content Model property column (2026-09-25) - mirrors SME's setting panel per element type.** SME's panel is not
  in the SME repo: it is the `settingsRenderer` of each module in `@com.mgmtp.a12.contentengine/contentengine-editor`
  (`internal/core/default-editor-elements/*/*.settings.tsx` + `*.controllers.ts`; the community npm ships `src/`; SME
  pins 0.10.0 which is not published, 0.11.0 was read) and `@com.mgmtp.a12.formengine/formengine-content-elements-editor`
  (form elements). The studio replaces the old id/type/namespace/raw-JSON column by a stack of `ContentSettingsPanel`s
  (`editors/contentmodel`), each shown only for the types it has settings for, in SME's section order: Element,
  Configuration (group/field reference), Source, Events, Content, Variant, Appearance, Icons, Queries, Columns, Layout,
  Responsive behavior, Row, Display Options, Dimensions, Color, Background Image, Border, Shadow, Accessibility,
  Advanced, Event, Raw properties. Most panels are pure FXML: rows (`editors/contentmodel/fields`: `ToggleRow`,
  `LengthRow`, `SpacingRow`, `ColorRow`, `SwitchRow`, `TextRow`, `SliderRow`, `IconRow`, `ShadowRow`, `ClickEventRow`)
  declare a props `path` and the element `types` they apply to, and one `ContentSettingsPanelController` shows/edits them
  (adding a setting means adding a row). Custom controllers: element, raw props,
  responsive (Grid row/column), table columns, media query. Values are written the way SME's formatters do (`ContentProps`:
  `false` flags and unspecified keywords are omitted, `enableColumnsResizing`-style default-true flags get an explicit
  `false`, padding/margin/radius as CSS shorthand with a "Mixed" 4-value mode, colors as `rgb()`/`rgba()`, background
  images as `url('...')`, URLs checked against DOMPurify's scheme allow-list). Table column insert/delete/move/pin go
  through `ContentTableColumns`, which keeps head/body/foot cells index-aligned like SME's middleware. New children and
  retyped elements get SME's default props (`ContentElementDefaults`, additive). **Deliberate deviations:** the Grid switch
  SME labels "Gutter" (it stores `noGutter`, so switching it on removes the gutter) is labeled "No gutter"; the type stays
  editable (SME fixes it when the element is created); a "Raw properties (JSON)" panel stays for what has no typed panel,
  notably the rich formatting of Paragraph/Heading (SME edits it inline on the canvas; the words themselves have a plain
  text field in the Content panel, `LexicalText`/`LexicalTextRow`: one line per Lexical block, formatting of the runs
  around an edit kept, `html` regenerated, read-only with a hint when the tree holds links or field references);
  "Group Reference", "Field",
  "Field reference" and "Screen Reader Column" are plain text fields (SME offers a picker over the Document Model / the
  table's columns). **Still missing:** the form-content elements (Text Line, Checkbox, ... with elementId, localized
  label/hint/placeholder, annotations), the Conditional element's condition editor, and the pickers above.

### Validators — gap list

a12-studio's form validators (`FormModelValidationService`; **2026-09-20: 30 form-specific ones plus the 6 generic header validators, the table below was written against 6 - rows are marked where they have since been closed**) map onto SME's validation surface. SME combines
declarative kernel meta-model rules (structural/type checks, not hand-written) with 21 hand-written "custom
conditions" (`validation/customConditions/index.ts`) — a12-studio has no declarative meta-model layer, so its
validators are the sole source of truth here.

| a12-studio validator | Closest SME equivalent | Notes |
|---|---|---|
| `FormDocumentModelReferenceValidator` | `ValidDocumentModelReference` / `ValidDocumentModelMustBeSelected` | Roughly at parity |
| `FormFieldReferenceValidator` | field-config-entry path validation in `validateFieldConfigEntries`/kernel rules | Roughly at parity for existence-checking; SME's version also validates against the DM's *current* type, not just presence |
| `FormButtonScreenReferenceValidator` | (likely a declarative kernel rule, not a custom condition) | **Done** (2026-09-19) — test added (`FormValidatorsTest`, fixture `FormButtonScreenReferenceValidator_invalid`) covering an existing screen, a special token (`#next`) and stale targets in the model-level and a per-screen footer |
| `FormLayoutColumnSumValidator` | `LayoutLgSumIsGreaterTwelveCustomCondition` | Matches (sum ≤ 12) |
| `FormSiblingNameUniquenessValidator` | (likely a declarative kernel uniqueness rule) | No custom-condition equivalent found; probably fine as a12-studio-side logic since there's no kernel layer to duplicate |
| `ControlGridLayoutValidator` | `InconsistentNumberOfColumnsCustomCondition` + kernel per-cell layout rules | a12-studio's version was reverse-engineered from a real fixture since the per-cell offset/span check isn't a custom condition in SME (kernel-declarative) |
| `DependentEnumerationMasterRequiredValidator`, `DependentFieldMasterRequiredValidator`, `DependentGroupMasterRequiredValidator` | `DependentEnumerationMasterRequired` / `DependentFieldMasterRequired` / `DependentGroupMasterRequired` | **Done** (2026-09-06, step 1; row corrected 2026-09-20, it said "Gap") |
| `ExternalEnumerationSourceRequiredValidator` | `ExternalEnumerationSourceRequired` | **Done** (2026-09-06, step 1; row corrected 2026-09-20, it said "N/A") |
| `DependentControlOptionsMustExistValidator` | `DependentControlOptionsMustExistInFormModel(Editor)` | **Done** (2026-09-19) — checks `Control.dependentControls` (exists / same top-level screen / allowed type, one message per reason) and the ids in the Confirm tab's `notRelevantNodes`. The `Editor` variant is SME-internal (form open in editor vs. workspace validation) and has no a12-studio counterpart |
| `DependentControlsAtLeastOneOptionValidator` | `DependentControlsAtLeastOneOptionMustBeSelected(Editor)` | **Done** (2026-09-19) — a `dependentControls` block with no `screenElement` |
| `FormDependencyDriftValidator`, `FormDependentControlContextValidator`, `FormReferenceTypeDriftValidator`, `FormControlIndexRequiredValidator` | kernel `ConsistencyValidator` (backend `checkConsistency`), `determineDependentEnumState`, `areControlAndScreenElementCompatible` | **Done** (2026-09-20, TODO #7) — form-vs-Document-Model drift, see "Structural consistency check" above |
| `DependentFieldAtLeastOneActionValidator` | `DependentFieldAtLeastOneActionPerCaseMustBeSelectedCustomCondition` / `CaseValueIsUndefined` | **Done** (2026-09-19) — every case of a `dependentField` with a master field needs `notRelevant`/`readonly`/`value` (`""` counts)/`fieldRef` (or, a12-studio only, `notRelevantNodes`); see `DependentCase.hasAction()` |
| — | `InitialFocusedElementOnlyOnFirstScreen` (+ the screen's `InvalidReference` on `initiallyFocusedElementId`) | **Done** (2026-09-19) — `FormInitiallyFocusedElementValidator` |
| `HideConditionAtLeastOneCaseValidator`, `HideConditionSupportedValuesValidator` | `AtLeastOneHideConditionCaseFilled` / `OnlySupportedHideConditionValuesPresent` | **Done** (2026-09-06, step 1; row corrected 2026-09-20, it said "Gap") |
| `FormInitialSortingColumnSortableValidator` | `SortableColumnCustomCondition` (`columnMustBeSortableWhenSetAsInitialSorting`) | **Done** (2026-09-21) — the column a repeat is initially sorted by must be sortable |
| `FormBindingRelationshipReferenceValidator`, `FormBindingTargetRoleValidator` | Binding relationship/role reference rules | **Done** (2026-09-18, extended 2026-09-23 to also cover `BindingRepeat`) for the linkage fields `Binding`/`BindingRepeat` models |
| `FormBindingComponentReferenceValidator`, `FormBindingComponentRequiredFieldsValidator` | `availableItemsOverviewMustHaveAValidReference`/`selectedItemsOverviewMustHaveAValidReference`/`additionalFieldsFormMustHaveAValidReference`, `availableItemIsRequired`/`selectedItemIsRequired` (`I_BindingComponent.json`) | **Done** (2026-09-23) — `Binding`/`BindingRepeat`'s `mainComponent`/`editModalComponent` reference and required-field checks |
| `FormBindingRepeatCdmRequiredValidator` | (the structural precondition `DescendantOfHeterogeneous(ToMany)Relationship`/`InvalidBindingRepeatRepetitionAndMultiplicity` share) | **Done** (2026-09-23) — flags a `BindingRepeat` whose Form Model isn't bound to a `ComposedDocumentModel` |
| `FormGroupReferenceValidator`, `FormUnusedConfigEntryValidator`, `FormStyleReferenceValidator`, `FormDefaultRowActionValidator`, `FormDatePickerConfigValidator`, `FormColumnWidthValidator`, `FormCustomScreenElementHeightValidator`, `FormIncludeProvenanceValidator` | (kernel-declarative / editor rules, see the per-feature sections) | Added 2026-09-06 to 2026-09-20 along with the features they guard; each is described where its feature is |
| — | `DescendantOfHeterogeneous(ToMany)Relationship`, `InitialValueAndDescendantOfHeterogeneousToManyRelationship`, `InvalidBindingRepeatRepetitionAndMultiplicity` | Structural precondition done (see `FormBindingRepeatCdmRequiredValidator` above); the deeper heterogeneous-descendant/repetition-vs-multiplicity reasoning inside CDM's precomputed schema stays N/A, blocked on kernel-backed DM expansion (Open Decision #1 in TODO.md) |

### Proposed build order

1. **Hide-condition multi-value + dependent enumeration/external enumeration.** Highest-value cluster: fixes the
   structural hide-condition gap (`masterField`+`cases[]`), and adds `dependentEnumeration`/`externalEnumeration` to
   `FieldConfigEntry`. Unlocks 5 of the "gap" validators above in one pass (`DependentEnumerationMasterRequired`,
   `ExternalEnumerationSourceRequired`, `AtLeastOneHideConditionCaseFilled`, `OnlySupportedHideConditionValuesPresent`,
   plus makes `DependentFieldMasterRequired`/`DependentGroupMasterRequired` worth adding at the same time since
   they're the same shape of rule).
2. **Fill the three "can add but can't edit" holes**: `TextCell`/`ExpressionCell` editor panels, and
   `RepeatOverviewColumn` "Add Column" + editor panel (including the missing `filterExposition`/`pinDirection`/
   `icon`/`labelHidden`/`headerStyle`/`fixedWidth`/hide-condition fields, and a new `ExpressionRepeatOverviewColumn`
   type). These are nodes users can already put in a form but can't configure — a correctness trap, not just a
   missing nice-to-have.
3. **Data Configuration tab + Cleanup tab.** Centralize dependent-field/group/enum and hide-condition authoring
   (currently scattered per-node); add the one-click orphaned-config-entry cleanup SME has. Natural follow-on to
   step 1 since it's the same underlying data this tab surfaces.
4. **Repeat feature completion**: `filterExpression`, `initialSorting` (+ its validator), real `rowActionGroup`
   (replacing the currently-dead `defaultRowAction`), `titleHidden`, per-repeat `confirmationTexts`,
   `MultiFileUploadOptions`/`attachmentConfig`. Group these together since several share the attachment-field theme.
5. **Includes/transclusion.** Done 2026-09-20 - see "Includes / transclusion" above.
6. **`ButtonPanel` screen element + `CustomCell` type.** Smaller, self-contained additions once the tree/editor
   infrastructure changes above have landed.
7. ~~**`Binding`/`BindingRepeat` (CDM relationship-driven selector/repeat).**~~ Done 2026-09-23 — see the
   "Fixed 2026-09-23" entry in TODO.md and the Composed Document Model row above; both blockers (Relationship
   Model, Composed Document Model support) are resolved.

**Status (2026-09-06): steps 1-6 done, all wire shapes verified against real SME fixtures, all data-model changes
covered by round-trip tests.**

- **Step 1**: `HideCondition`/`HideConditionCase` (`masterField`+`cases[]`) replaced the old single-value
  `hideConditionField`/`hideConditionValue` on `ScreenElement`/`Row`/`Control`; `HideConditionPanelController`
  reworked into a checklist whose value choices adapt to the master field's type (Boolean/Confirm/Enumeration).
  `DependentEnumeration`/`ExternalEnumeration` (+ `formatting`/`secret`/`enableSelectAll`/`annotations`) added to
  `FieldConfigEntry`, with new editor panels. All 5 "gap" validators from step 1 added, plus
  `DependentFieldMasterRequired`/`DependentGroupMasterRequired`, all with fixtures/tests.
- **Step 2**: `TextCell`/`ExpressionCell` editor panels added. `RepeatOverviewColumn` fields consolidated onto a
  shared base (`label`, `filterExposition`, `pinDirection`, `icon`, `labelHidden`, `headerStyle`, `fixedWidth`,
  `hideCondition`, `annotations`); new `ExpressionRepeatOverviewColumn` type (verified against a real fixture,
  including its distinctive `name` field that `FieldBasedRepeatOverviewColumn` lacks). "Add Column" wired through
  the tree's generic add/attach/detach/reorder commands (which needed two latent-bug fixes: they previously assumed
  only `ControlGrid`/`Screen` could ever occupy a Repeat's single child slot). New column editor panel covers
  label/width/sortable/filterable/preferred-sorting + type-specific fields; icon/pin-direction/per-column hide
  condition were deferred here and added 2026-09-19 (see the column table rows above).
- **Step 3**: new **Data Configuration tab** — a flat table of every field/group config entry with reusable detail
  editors (External Enumeration, Dependent Enumeration, and two new panels, **Dependent Field** and **Dependent
  Group**, closing a gap bigger than originally scoped: `dependentField`/`dependentGroup` had **zero** editor UI
  anywhere before this, not even a partial one). "Add Field"/"Add Group" let a field be configured before any
  Control references it. `GroupConfigEntry.numberOfInitialRows` also exposed (previously unedited anywhere). New
  **Cleanup tab** flags both dangling (field/group no longer exists in the DM) and orphaned (exists but unreferenced
  by the tree) entries with "Clean All", backed by a new `FormReferences` utility.
  `ExternalEnumerationPanelController`/`DependentEnumerationPanelController` were refactored to take a
  `FieldConfigEntry` directly (not a `Control`) so the same panels serve both the per-node editor and this tab.
  **Dependent Field**/**Dependent Group** were initially a raw, free-editing table over whatever `case` entries
  already existed in the file (no way to pick a trigger value from the master field's own declared values) - a
  materially weaker editor than SME's grid (`DependentFieldGroupTable` in `dependencyTable.tsx`), which
  auto-populates one row per value the master field can actually take. Rebuilt to match: both panels now derive
  their rows from the master field's own type (`DependentCaseSupport.masterValueOptions`, mirroring SME's
  `createCaseMap`) - one checkbox row per enum value / true-false / true, plus a synthetic "-No Selection-" row -
  with a Display picker (Default/Not Relevant/Read Only, Read Only hidden for a field the DM already computes)
  and, for Dependent Field only, a Value-Type picker (Unchanged/Value/Field Value) whose Value column becomes a
  picker of the dependent field's own enum/boolean/confirm values, free text, or a dropdown of type-compatible
  sibling fields, depending on the choice. Unchecked rows are simply not written; the whole `case` list is
  rebuilt from checked rows on every edit, matching SME's `mergeCaseMap`.
  **Dependent Enumeration** got the same treatment, mirroring SME's `DependentEnumerationTable`: the panel was a
  plain three-column free-text table (`masterValue`, comma-separated `constraintValues`, and a `valueForMasterChange`
  text box), not a grid backed by either field's own declared values. Rebuilt into a 2-D grid - one row per literal
  value the master field's own Enumeration declares (via `DependentCaseSupport.enumerationLiterals`; unlike Dependent
  Field/Group there's no synthetic "no selection" row, matching SME's `getValuesFromEnumerationLike`), one column per
  literal value the *dependent* field (the entry's own `elementRef`) declares, each cell a Hidden/Visible/Default
  3-state picker. "Default" (at most one per row, enforced by clearing any other Default in the same row on
  selection) is what becomes `valueForMasterChange`; every row always yields a constraint entry (there's no per-row
  checkbox - unlike Dependent Field/Group, SME's own model has no way to "disable" a single master-value row) unless
  the whole grid is neutral (every cell Visible), in which case `constraint` is cleared entirely, matching the
  reference's "useless to have the dependent enum" check in `mergeConstraintsMap`.
- **Step 4**: `filterExpression`, `initialSorting`, `titleHidden` added to `AbstractRepeat` with UI. Real
  `RowAction`/`RowActionGroup` added (rich, multi-action); the old single-slot type was renamed
  `DefaultRowAction` to stop colliding with it and now correctly matches SME's `DefaultRowAction` shape
  (`event`/`custom`/`hideButton`) instead of the richer one. New "Row Actions" table editor (event + scope; the full
  per-action `buttonStyling`/confirmation editing was deferred here and added 2026-09-19 as the Edit dialog).
  Per-repeat `confirmationTexts` override and `TableStyle.cardHeight`/`actionColumnWidth` added, both with UI.
  `MultiFileUploadOptions` added to `InlineRepeat`/`EmbeddedRepeat` only (matching SME's actual restriction) — UI
  added 2026-09-19.
- **Step 5** (originally planned last, done ahead of step 6 once its actual shape was found): **not** attempted as
  a live resolve-and-expand feature — a real fixture (`client/resources/input/models/fmm/workspace/HostModel.json`)
  showed SME expands an include at *author* time (copies the referenced subtree into this model's own `screens`
  with rewritten ids) and keeps `includeId`/`formModelRef`/`hostDocumentModelPath` purely as provenance metadata,
  not a live reference resolved at render time. So the scoped, correct fix was just adding those three fields to
  `ScreenElement` for round-trip fidelity (verified against that fixture's exact shape) — a12-studio still has no
  UI action that performs the copy-and-rewrite itself, but the round-trip hazard (silently dropping this data on
  load-then-save) is closed. The copy-and-rewrite action followed on 2026-09-20 (see "Includes / transclusion").
- **Step 6**: `ButtonPanel` (new `ScreenElement`, addable inline in the screen tree, own button list reusing
  `ToolbarButtonsPanelController`) and `CustomCell` (new `Cell` type) added, both with editor panels. While in this
  area, also closed a pre-existing gap found by inspection: `CustomScreenElement` had **no editor pane at all**
  (addable via the tree, but none of its inherited `ScreenElement` fields — name, label, hide condition, styles,
  annotations — were editable afterward); it now has one.
- **Found and fixed along the way**: a pre-existing round-trip bug unrelated to this work —
  `Row.cell` lacked `@JsonInclude(NON_EMPTY)`, so an empty row's `[]` cell list was written back as an explicit
  `"cell": []` instead of matching source files that omit it.
- **Step 7 done 2026-09-23**: a minimal `Binding` (relationship + target role only, created by dragging a
  relationship onto the tree) existed since 2026-09-18 — see the `Binding` row in "Element types". Composed
  Document Model support (real `ComposedDocumentModel` type + `cdm.*` annotation editor/validators, see the
  Composed Document Model row above) landed 2026-09-23, and with it `Binding.details.mainComponent`/
  `editModalComponent`/`isFixedRelationship`/`cdmChildActivitiesEnabled`/`modificationConfiguration` (the
  UI-component configuration) and `BindingRepeat` (created instead of a plain `Binding` when the dragged
  relationship's target role is to-many). See TODO.md's "Fixed 2026-09-23" entry for the full file list.

---

## Query Model

*Analyzed 2026-09-05 (rest of this doc last analyzed 2026-07-17 — don't assume the same currency).*

a12-studio's Query Model editor (`a12-studio-ui/.../editors/querymodel/`, data model in
`a12-studio-models/.../querymodel/`) has an editable, split-view tree (`QueryModelTreeController`: the graph on
the left, a per-selected-"document node" property panel — `QueryDocumentNodePanelController` — on the right,
mirroring `document-model-editor.fxml`'s own tree+editorContainer split) with per-node filter/field-projection
authoring, validators, a target-DM Settings tab, reference/rename tracking (2026-09-20) and aggregation
(2026-09-20) — see the comparison below. SME's `queryModel` (`client/src/modules/queryModel/`) remains the
reference for how each of them behaves.

### Data model

| Field | a12-studio (`QueryModelContent`) | SME (`Query.QueryRoot` wire format) |
|---|---|---|
| Target | `targetDocumentModel` (String, DM id only) | `targetDocumentModel` — can also be a Combined Document Model or Transformer Model output |
| Projection | `projectionName` | `projectionName` |
| Field selection | `fields[]` (in-result paths), plus a `useAllFields` (Boolean) mode — same on `QueryLink` for a relationship-hop's own target | `fields[]`, plus a `useAllFields` mode |
| Filter | `filterDefinition` (free-text Query Language, `QueryLanguageEmitter`/`Formatter`) — **per graph node**: `QueryModelContent.filterDefinition` for the root, `QueryLink.filterDefinition` for a relationship hop's own target | `constraint` — recursive `Operator` AST (`and`/`or`/`not`/`exact_match`/`double_range`/`date_range`/`undefined_match`/`simple_search`/`has`), attachable **per graph node** |
| Traversal | `links[]` — nested relationship traversals (`relationshipModel`, `targetRole`, optional `maxDepth` for self-reference recursion); `constraint` (structured `Operator`)/`linkDocumentFields` map for lossless round-tripping only, no editor UI | `links[]` — nested relationship traversals (`relationshipModel`, `targetRole`, optional `constraint`, optional `maxDepth` for self-reference recursion), plus `has(...)` as a filter-only traversal |
| Sort | `sort[]` (`QuerySort`: optional relationship+role hop, then `QuerySortBy` — field/direction/nullHandling/ignoreCase) | `sort` — same shape (field/direction/nullHandling/ignoreCase) |
| Paging | `paging` (pageNumber, pageSize) | `paging` — same shape |
| Aggregation | `aggregation` (`QueryAggregation`, 2026-09-20) — `group: {field}[]` + `aggregations: {function: count\|sum\|max\|min\|avg, field, alias?}[]`; the block's presence is the "Aggregate Results" switch. Replaces the former dangling `aggregateResults` boolean, which was never on the wire | `aggregation` — the same; SME's `technical_useAggregation` switch is editor-only (`qmTransformer.ts` writes `aggregation` only when it is on) |
| Root exclusion | `exclude` (Boolean, root only) — "Only Links" checkbox (`QueryOnlyLinksPanelController`) | `exclude` — omit the root document itself, return only linked docs |

SME's in-editor representation additionally splits into three independently-validated sub-documents (`settings`
header form, `documentGraph` tree, `postProcessing` sort/paging/aggregation) that get merged back into the flat
wire JSON on save (`transformations/qmTransformer.ts`) — a serialization-layer detail, not something a12-studio
needs to copy architecturally.

### Editor features — gap list

| Feature | SME reference | a12-studio status |
|---|---|---|
| Editable tree / document graph | Add a root DM via an ER-diagram picker (reuses the Model Graph Diagram component); add relationship-traversal nodes (only relationships actually connected to the selected node are offered) | **Present since 2026-09-06** (corrected 2026-09-20; this row said "Missing — fixed, read-only mirror"): `content.links[]` (`QueryLink`) traversal hops, nested to any depth, added/removed in `QueryModelTreeController`; see "Status (2026-09-06): Phase 2". Still not like SME: the root DM is chosen in the Settings tab, not through an ER-diagram picker |
| Per-node filter/constraint | Query-language grammar editor (ANTLR-backed, field/relationship autocomplete against the model graph), compiles to the `Operator` AST; semantically validated (field exists, type-correct operator, valid relationship+role) | Present, per graph node (`QueryDocumentNodePanelController`'s embedded `RuleEditorController`, `QueryLanguageEmitter`-validated, bracketed-path autocomplete via `BracketedPathSuggestionProvider`) — free-text QL grammar rather than SME's structured-AST editor, and only syntax is checked (field existence inside the expression isn't) |
| Target Document Model selection | Settings tab, editable at any time | Present (Settings tab, `QuerySettingsPanelController`), roughly at parity |
| In-result field toggles | Inline tree checkboxes, tri-state on groups, disabled+forced for non-indexed fields | Present (`QueryModelTreeController`'s In-Result column, tri-state on groups) **and** the right panel's "Fields included in Result Set" list (add/remove + "All Fields of the Document Model") — non-indexed fields are still not specially disabled or forced in the tree (corrected 2026-09-20: the `indexed = false` annotation *is* known since the aggregation work, and the filter, aggregation and sort validators use it; the *field projection* check `QueryFieldReferenceValidator` and the tree checkbox do not) |
| Sort | Multi-field, relationship-hop, direction, null-handling, ignore-case | Present (`QuerySort`/`QuerySortBy`/sorting panel), roughly at parity — `QueryTraversalOption.options()` scopes to *every* relationship in the project rather than only ones connected to the target DM |
| Paging | pageNumber/pageSize | Present, roughly at parity |
| Aggregation/grouping | Full group-by + count/sum/max/min/avg mode | **Present** (2026-09-20) — `QueryAggregationPanelController` on the Post Processing tab: the switch, group fields, aggregations (function, field, alias); offers only eligible fields (non-repeatable, not `indexed = false`) and, per aggregation, only fields the chosen function fits; `QueryAggregationValidator` (see the Validation row and "Status (2026-09-20): aggregation done") |
| Multi-target-type queries (CDM, Transformer Model as target) | Supported | Not supported — DM only (**parked**, 2026-09-20; also blocked: there is no Composed Document Model or Transformer Model here) |
| Reference/rename tracking | Target-DM, relationship, sort/aggregation field-path references are all first-class in SME's refactoring graph; renaming a DM/field auto-updates or flags the query (`qmModule.ts` `refactorDocument()`) | **Present** (2026-09-20; was partly present 2026-09-19) — renaming/moving an element of a Document Model rewrites every query field path evaluated against it, at any depth: root and hop `fields`, `sort` (also through a relationship), `constraint` (also below `has`), `filterDefinition` text (`[/Path]` refs, also inside `Has(...)` constraints), a hop's `linkDocumentFields` (`ProjectReferenceRefactoring` → `QueryReferenceRefactoring`, one undo step with the DM edit). Renaming a Document Model or Relationship Model *file* (id) rewrites `targetDocumentModel`, `relationshipModel` of hops/sorts/`has` operators (nested included, `ModelReferenceRewriter`) and, new, the relationship named in `Has("<rel>", ...)` inside `filterDefinition` text; SME's own `refactorDocument()` only handles `targetDocumentModel`. An unresolvable target is now an explicit error (validator + banner in the Model Tree tab + message in the Settings tab's target combo), see the Validation row. **Still missing:** a *role* rename in a Relationship Model (the role field commits per keystroke and nothing — Query, Form bindings, Relationship UI — reacts to it; the Query validators flag the dangling `targetRole`), an aggregation's paths are covered since 2026-09-20 (`aggregation.group[].field`, `aggregation.aggregations[].field`, target DM only) |
| Validation | Root-required, per-node schema validation, constraint semantic validity, target-role validity, field-projection sanity, tab-level validation counts | Present (`QueryModelValidationService`, `a12-studio-models-validation/.../validators/query/`): target-DM required **and must exist in the project** (2026-09-20; before, a dangling target was only caught when the header's DOCUMENT reference still named it, otherwise the tree was just empty), `fields[]`/sort field-path resolution (root and per-link, recursive), relationship+role resolution (sort traversal and graph links, recursive), paging bounds, and `filterDefinition` QL syntax (root and per-link, recursive) — field-projection sanity is reachability-only (the "Add" combo only offers real field paths, so an invalid path isn't reachable through the UI at all); refs *inside* a filter expression's text are resolved too since 2026-09-20 (see "Status (2026-09-20): semantic filter validation done"); `content.aggregation` (`QueryAggregationValidator`, 2026-09-20): every group/aggregation field must resolve to a non-repeatable field that is not `indexed = false`, function/field-type compatibility, no links, `document` projection (see "Status (2026-09-20): aggregation done") |

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
  `RuleEditorController`, which already hosts a `CodeArea`), reusing the ported binder/checker for the semantic
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
plain syntax-highlighted `RuleEditorController`-style editor first, add autocomplete as a follow-up once the
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
`checker.ts`/custom conditions) that would need real schema access and hasn't been ported. *(2026-09-20: the
existence half is now a separate pass over the parse tree, `QueryFilterReferenceChecker` - see "Status
(2026-09-20)" in the Query Model section below; the emitter itself is unchanged and still type-blind.)*

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
- **Filter dialog validation**: `RuleEditorController` (the shared expression-editor panel also used by
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

**Status (2026-09-14): Phase 3 (per-node filtering) UI done — closes the gap flagged above.** Every "document
node" in the graph (the target Document Model row, and any relationship-link row that resolved to one) now gets
its own Filter Definition and Fields-in-Result-Set editor, not just the root:

- **Split-view tree**: `query-model-tree.fxml`'s `<center>` is now a `SplitPane` (tree left, a `BorderPane
  fx:id="nodeEditorContainer"` right — mirrors `document-model-editor.fxml`'s tree+editorContainer split
  exactly), driven by a new `elementsTreeTable` selection listener in `QueryModelTreeController`. The old
  "Filter Definition" tree column and its click-to-open-a-dialog cell (`FilterDefinitionCell`,
  `QueryFilterDefinitionDialogController`, `query-filter-definition-dialog.fxml`) are removed — editing moved
  entirely into the always-visible panel, so there is no longer a second, redundant editing surface for the
  same field. The "In Result" checkbox column is unchanged (still useful as a quick per-field toggle while
  browsing) and stays in sync both ways with the new panel's field list, since both read/write the exact same
  `fields` `List` instance (`QueryTreeRow#getFieldsScope()`) — a plain `elementsTreeTable.refresh()`/panel
  `refresh()` call after either side's edit is enough, no tree rebuild needed.
- **Data model**: `QueryLink` gained `filterDefinition` (String) and `useAllFields` (Boolean), mirroring the
  same-named fields already on `QueryModelContent` — a relationship hop's resolved target Document Model is
  exactly as filterable/projectable a node as the query's own root. `QueryModelContent` gained `useAllFields`
  too; `exclude` already existed (round-trip-only) and now has UI.
- **`QueryDocumentNodePanelController`** (`query-document-node-panel.fxml`) is the panel shown in
  `nodeEditorContainer`: a read-only "Target/Linked Document Model: `<id>`" label, the embedded `RuleEditorController`
  (Filter Definition, same `QueryLanguageEmitter` syntax validation and `BracketedPathSuggestionProvider`
  autocomplete the old dialog used), `QueryFieldsProjectionPanelController` ("Fields included in Result Set" -
  the "All Fields of the Document Model" checkbox, and, when off, an add/remove-able list of field paths sourced
  from `ElementIndex(targetDocumentModel).allElements()` filtered to `FieldElement`), and - root only -
  `QueryOnlyLinksPanelController` ("Only Links", `content.exclude`). `QueryFilterableNode` is a small adapter
  interface (`QueryFilterableNode.of(QueryModelContent)` / `.of(QueryLink)`) so the panels work against one
  shape regardless of which backing type is bound - the two don't share a common supertype.
- **Event-firing subtlety**: `RuleEditorController`'s own inherited `commitChange()` only fires
  `StudioEventManager.fireModelSavedEvent` when bound to a real `Element` (`this.element != null`) - true for
  every *other* embedded use of it in this codebase (each one also calls `setElement(...)` purely for this
  side effect, e.g. `DocumentModelValidationRuleEditorController`), but there is no `Element` backing a
  `QueryFilterableNode`. `QueryDocumentNodePanelController`'s `setCustom` writer does the full commit itself
  (save + fire event + notify `QueryModelTreeController` to refresh) instead, accepting one harmless redundant
  `ProjectItem#save()` from the inherited call that runs afterward - flagged here in case a future "model-header
  + embedded RuleEditorController" case wants a cleaner shared solution (e.g. a small `RuleEditorController`
  subclass overriding `commitChange()` to call `commitHeaderChange()`) instead of repeating this workaround.
- **Validators**: `QueryFilterDefinitionSyntaxValidator` now also recursively checks every `QueryLink.filterDefinition`
  (not just `content.filterDefinition`), the same recursion shape `QueryLinkValidator` already uses for
  `fields`/target-role.
- **Not ported**: SME's field-projection custom condition (`QmInvalidFieldProjection`) also rejects a field with
  an `indexed = false` annotation - a12-studio's `DocumentModel` has no `indexed`-annotation concept at all, so
  there is nothing to port that check against; not a gap this pass could close.

**Status (2026-09-20): semantic filter validation done** (was: "does `[/Foo/Bar]` inside a `filterDefinition`
actually exist isn't checked - only its syntax is"). A port of the *existence* half of SME's binder/`Resolver`
(`moduleSupport/qmm` `binder.ts`, `base/resolver.ts`), deliberately without the checker's type/overload/enum-value
diagnostics (the emitter still does no type checking either):
- **`QueryLanguageReferences`** (`a12-studio-models`, `querymodel.ql`) walks the same parse tree as the emitter
  (both now share `QueryLanguageSyntax.parse`) and returns, in source order, the bracketed field paths of a scope
  and every `Has(relationship, role, constraint, linkConstraint)` call with its nested scopes. No project access.
- **`QueryFilterReferenceChecker`** (`a12-studio-models-validation`, `validators.query`) resolves those against the
  project: each `[/Path]` in the Document Model the filter runs against (the root filter: the target DM; a hop's
  filter: the DM its role plays), following Includes/Additive bases (new `ElementIndex.resolveAbsolutePath`, unlike
  `QueryElementResolution.resolveByPath`'s plain scan of the model's own elements); "unknown field" (SME 2024),
  "not a field" (2023, e.g. `[/Root]`) and "annotated `indexed = false`" (2036); `Has`: unknown relationship (2025),
  role not in the relationship, role not reachable from the current DM (all roles for a self-referencing
  relationship, otherwise the roles of the *other* DM - `getExpectedTargetRoles`, 2026/2027), target DM missing
  (2029), and for a link constraint the relationship's link DM missing/unset (2030/2031). The `constraint` is then
  checked in the role's DM and the `linkConstraint` in the link DM, recursively. A scope whose DM cannot be
  determined skips its field paths (the broken hop is reported once, by the link validator) but its nested `Has`
  calls are still checked. `/__meta/...` paths are skipped like in the sibling validators. No "Did you mean ..."
  candidates (SME 2024/2033) - the editor's autocomplete covers that.
- **`QueryFilterDefinitionReferenceValidator`** runs it for the root and every hop (recursively) from
  `QueryModelValidationService`, error ids `content/filterDefinition` / `content/links` like the syntax validator;
  a syntactically invalid filter is left to `QueryFilterDefinitionSyntaxValidator`. **Editor**:
  `QueryDocumentNodePanelController`'s per-keystroke validator now returns the syntax error, else one line per
  unresolved reference (the checker is built once per `load`, from a snapshot of the project's models, like the
  suggestion provider's index - a DM edited in another tab is picked up on the next selection, not per keystroke).
- Tests: `QueryFilterReferenceCheckerTest` (16). No real SME fixture has a `filterDefinition` with references
  (`grep` over `testing/` and SME's `client/resources`, `integrationTest`, `moduleSupport/qmm/resources` finds only
  the two synthetic invalid-syntax fixtures), so nothing could be swept for false positives; the messages and
  scoping rules come from SME's `checker.test.ts` and the sources above.
- Correction to the "Not ported" bullet above: a12-studio's `Element` carries generic `annotations`, so an
  `indexed = false` check *is* possible - it is done for the filter expression now. The **field projection**
  (`fields[]`, `QueryFieldReferenceValidator`) still does not reject non-indexed fields; not changed here.

**Still remaining**: type checking of a filter expression (operator/value vs. field type, enumeration values -
SME 2007-2019/2035) and "Did you mean" suggestions. Aggregation is unchanged from the plan below.

**Status (2026-09-20): reference/rename tracking done** (was: only the root's `fields`/`sort`/`constraint`/filter were
followed, and a dangling target was an empty tree). Three parts:
- **Element rename/move** - `QueryReferenceRefactoring` (`a12-studio-models-validation`, `refactoring`), called from
  `ProjectReferenceRefactoring`. A query holds paths against several Document Models, so each site is rewritten only
  when the model it is *evaluated against* is the changed one (SME's binder/resolver scoping, the same one
  `QueryFilterReferenceChecker` resolves with): root = `targetDocumentModel`; a hop (`QueryLink`, nested hops each with
  their own) and a sort entry through a relationship = the DM the hop's `targetRole` plays in its Relationship Model;
  `linkDocumentFields` and a `linkDocumentConstraint`/link constraint = the relationship's link DM; a `has` operator or
  `Has(...)` call switches scope for its nested constraints (the old code skipped everything below `has`). A hop/`has`
  whose relationship, role or DM doesn't resolve is left alone. Filter text is rewritten through the parse tree (new
  positions on `QueryLanguageReferences.FieldReference`/`HasCall`, `QueryLanguageReferences.replace`) so a `Has`
  constraint is attributed to the role's DM, not the enclosing one; text that doesn't parse falls back to the lexer
  tokens (as before) unless it contains a `Has` call, where the scope would be a guess.
- **Model id rename** - the JSON-tree `ModelReferenceRewriter` already covered `targetDocumentModel`/`relationshipModel`
  at any depth (verified now, `ModelReferenceRewriterQueryTest`); added the relationship id inside `Has("<rel>", ...)`
  in `filterDefinition` text, which a JSON walk cannot see. SME's `refactorDocument()` only handles the target.
- **Unresolvable target** - `QueryTargetDocumentModelRequiredValidator` now reports "target Document Model "X" does not
  exist" (any model kind that can stand in as a document reference - a Combined Document Model - is accepted);
  `QueryModelTreeController` shows an error banner instead of a silently empty tree (unset vs. not found), and the
  shared `TargetModelPanelController` (Settings tab, Mapping, Combination) flags a stored id its combo box has no item
  for, which it used to display as if it were a valid selection.
- Tests: `ProjectReferenceRefactoringTest` (+10: hops, nesting, sorts, `has` operator and text, unresolved scopes,
  invalid text, non-BMP offsets, undo), `ModelReferenceRewriterQueryTest` (3), `QueryValidatorsTest` (+3),
  `ProjectReferenceRefactoringFixturesTest` (+2, real hop), `FixtureWorkspacesQueryValidatorsTest` (every real
  query's target resolves in its workspace),
  `QueryModelTreeTargetProblemTest`/`TargetModelPanelControllerTest` (JavaFX thread, skip without a display).
- **Not done, on purpose:** role rename tracking (see the comparison table row above). Aggregation paths were added with
  the aggregation itself, see "Status (2026-09-20): aggregation done".
  Verified on the real `advanced_new` workspace (`ProjectReferenceRefactoringFixturesTest`: a rename in the role's DM
  moves a hop's `fields`, a rename in the relationship's link DM moves its `linkDocumentFields`, nothing else changes;
  the every-element sweep still undoes byte for byte). Not verified on a real file: a `Has(...)` in filter *text* -
  no fixture has any `filterDefinition` - and a `has` operator whose constraint holds a real field path (the fixtures'
  `has` constraints only test `/__meta/docRef`); those come from the model classes and SME's checker tests.

**Status (2026-09-20): aggregation done** (was: `aggregateResults`, a boolean that appears in no wire format).

- **Gate — does the runtime support an aggregation-mode result? Yes, so it was built rather than recorded as a
  non-goal.** The runtime that executes a Query Model is Data Services' Query API, not the kernel the #3 spike looked
  at, and nothing in it is enterprise-gated: `POST /api/aggregation` (and the `QUERY` JSON-RPC operation with an
  `aggregation` block) takes `aggregation: {aggregations: [{function, field}], group: [{field}]}` and returns one
  generated document per group (`C:\workspace\a12\2606-06-doc\data_services-dataservices-documentation-src.md`,
  "Aggregations"; tutorial `overall-dev_tutorial_query_discovering_queries.md`, "Aggregation Example 1/2"). SME's
  backend has no aggregation code at all - it is purely the editor's wire format.
- **Wire shape:** `content.aggregation` (`QueryAggregation` > `QueryAggregationGroup`, `QueryAggregationEntry`). It
  replaces the old `aggregateResults` boolean, which SME and Data Services never had; no fixture or real file contains
  it, so nothing is lost, and because the model classes ignore unknown keys a file that did carry it simply loses it
  on the next save. SME's switch (`technical_useAggregation`) is editor-only, so here too the presence of the block
  *is* the switch. `group` is optional on the wire (no group = aggregate the whole result set) and is kept absent-vs-
  explicit-`[]` like `sort`/`fields`. `alias` is only in SME's meta model (`QMPostProcessingMetaModel`), not in the
  Data Services docs, and is round-tripped as optional.
- **Editor:** `QueryAggregationPanelController` + `aggregation-panel.fxml`, embedded in the Post Processing tab (the
  switch moved out of the Paging panel). Inline rows, no dialog: group fields, and aggregations as function / field /
  alias. Like SME, switching the switch off keeps the configuration for the editing session. The field combos offer
  only what is eligible (`QueryAggregationSupport`) and an aggregation's field combo only what its function fits; a
  stored value that no longer fits stays visible, is marked and is listed with the validator's messages.
- **Rules (`QueryAggregationValidator`, `QueryAggregationSupport`):** group/aggregation fields must be non-repeatable
  fields without `indexed = false` (SME Query Model docs, "Validation"); function availability per type is Data
  Services': `count` any, `sum`/`avg` numbers, `min`/`max` numbers, dates, date-times, times; the query must have no
  `links` and the `document` projection (errors: an aggregation result has no roots to hang links on). Warnings: no
  aggregation entry (it only groups) and a `sort` (ignored in aggregation mode). A target that does not resolve
  skips the field checks - `QueryTargetDocumentModelRequiredValidator` reports that.
- **Refactoring:** `QueryReferenceRefactoring` rewrites the group and aggregation field paths when an element of the
  target Document Model is renamed or moved (one undo step with the rest).
- **Tests:** `QueryAggregationTest` (wire shape, absent-vs-empty `group`, the switch), `QueryAggregationValidatorTest`
  (18, against `Aggregation_DM.json`), `ProjectReferenceRefactoringTest` +2, JavaFX-thread `QueryAggregationPanelTest`
  (11, against the real `advanced_new` query/`Person_Dc`, including what reaches the file).
- **Not verified on a real file:** no fixture anywhere has an `aggregation` block, so the wire shape comes from the Data
  Services documentation and SME's meta model/transformer, not from an SME-authored file. Also open: for a field
  reached through an Include or Additive base the repeatability check cannot see the enclosing repeatable group (the
  same limitation `ElementIndex.granularity` has), so that case gets no error rather than a wrong one.

### Proposed build order

1. ~~**Settings + validators**~~ — done, see Status above (2026-09-05).
2. ~~**Editable graph tree**~~ — done, see Status above (2026-09-06).
3. ~~**Per-node filtering**~~ — UI done, see Status above (2026-09-14). Autocomplete already existed (reused
   from the old whole-query dialog); the semantic (existence-aware) layer for the filter expression's own
   references was added 2026-09-20, see "Status (2026-09-20)" above; type checking remains open.
4. ~~**Aggregation**~~ — done 2026-09-20, see "Status (2026-09-20): aggregation done" below (the gate held: Data
   Services executes aggregation-mode queries).
5. ~~**Reference/rename tracking**~~ — done 2026-09-20, see "Status (2026-09-20): reference/rename tracking done"
   above (element rename/move, model-id rename, explicit error for an unresolvable target; role rename not covered).

---

## Other model types — survey and priority

Every SME module implements `SMEModule`/`DefaultSMEModule` and (if it's a standalone file type) registers an
`ExplorerEntry`. Load/save nearly always follows the same pattern: parse raw workspace files into an in-memory
document, serialize back to JSON (occasionally YAML) on save.

### Status and priority (re-verified 2026-09-20)

The original priority column ("Why this priority", written 2026-07-17) is kept because it still explains the
ordering, but its claims about existing kernel libraries and `a12-studio-data-services` scaffolding were wrong (see the
architecture section: there is no kernel dependency, and that scaffolding was deleted on 2026-07-20). The "a12-studio
today" column is what the code shows on 2026-09-20. **Editor enablement** comes from `model-versions.json`: a type
with `"enabled": false` has classes and possibly an editor, but opening a file shows "not supported yet"
(`EditorFactory`), so treat it as not shipped. SME itself marks additive, combination, composed, content, mapping,
query, selection, structural mapping and transformer as `isExperimental()` (checked 2026-09-20).

| # | Module | Why this priority (2026-07-17) | a12-studio today (2026-09-20) |
|---|---|---|---|
| 1 | **structuralMappingModel** | Foundational — referenced by mappingModel and combinationModel. SME's editor: source-tree/target-tree drag&drop field mapper, resolution-strategy editor for conflicts. | **Data model only, disabled.** `ModelType.STRUCTURALMAPPING` + full content classes (`FieldMapping`, `Slice`, `ResolutionStrategy`, `GroupToClearOnFirstFill`); `StructuralMappingModelEditorController` is a 24-line stub; no validation service, no kernel dependency (the old "`kernel-md-structuralmapping-tool` present, `SmmService` scaffolding exists" was wrong). Rename/move refactoring already rewrites its `*FullName` paths. |
| 2 | **mappingModel** | Depends on structuralMappingModel + additiveDocumentModel. ETL-style: source DM(s) + target DM + optional precomputation, driven by a referenced SMM. | **Started, disabled.** Content classes (`MappingSource`, `MappingTarget`, `SortField`, `PreComputationFragmentRef`, `StructuralMappingModelRef`, ...) and a 141-line editor with the Target Model panel and an editable Sources list (`SourceModelsPanelController` + dialog); no validation service. The precomputation fragment and the Structural Mapping Model link are not editable yet (the controller's own comment says "added later"). |
| 3 | **combinationModel** | Built on additive + structural mapping. | **Present, enabled.** Structural editor + 7 structural rules (2026-09-08), base/additive loop detection (2026-09-20, reference graph only); DM expansion/SMT validation is not, see the dedicated section. |
| 4 | **additiveDocumentModel** | Hard dependency of both mappingModel and combinationModel. Overlay editing mode: elements are included/overwritten/purely-additive relative to a base DM. | **Partly present, enabled** (a Document Model with an annotation): read-only "Additive Elements Only" preview and base-aware path resolution; no overlay editing mode, no join. See the Document Model gap-list row. |
| 5 | **relationshipModel** | Foundational — link, masterDetailModel, treeModel, modelGraphDiagram and formModel's `Binding`/`BindingRepeat` all reference it. | **Present, enabled** (the old "no current scaffolding" is obsolete): `RelationshipModelEditorController` (8 files) + `RelationshipModelValidationService` (6 relationship-specific validators). The **Relationship UI Model** (`relationship-ui`, `Ru`) also has an editor (8 files) and 3 validators. A role rename does not propagate to Query/Form/Relationship UI references (TODO decision). |
| 6 | **selectionModel** | Reusable selection spec. | **Present, enabled** (2026-09-13), see the dedicated section. |
| 7 | **printModel** | Most editor-complex of the print family — relies on an external print-engine component library for the layout canvas. Backend renders PDF only. | **Large editor, disabled.** 28 content classes, an 811-line `PrintModelEditorController`, 7 print validators; no print-engine dependency and no PDF rendering (the old `PrintService`/`DocumentModelResolver`/`PrintParameters` scaffolding was deleted 2026-07-20). Rename/move refactoring rewrites `FieldRef.path`. |
| 8 | **printSettingModel / printTypesettingModel** | Small, no cross-model references — cheap wins once printModel work begins. | **Typesetting: present, enabled** (2026-09-25): `ModelType.TYPESETTING` (the `model-versions.json` key was `printtypesettings`, which never matched the header's `typesetting`), `TypesettingModel`, an editor with four extracted panels, 4 validators plus the reusable roles validator; see the dedicated "Print Typesetting Model" section. **Print Setting: not present** (deprecated in the platform). |
| 9 | **link / document** | Record-editing modules depending on relationshipModel/documentModel. `document` = data *instances* of a Document Model. | **Not present.** |
| 10 | **queryModel / overviewModel** | Search/filter/list-screen configuration; consumer-side, not blocking other model types. | **Both present, enabled.** Query: see the dedicated section. Overview: `OverviewModelEditorController` + 21 editor files (columns, filter items with per-type options, sub-header slots (every element type - button, search, filter, multi-selection - is editable through one dialog since 2026-09-21, the last three without the button-only Event/Confirmation/Priority/Icon block), initial sorting, styles, query-model link) and 16 overview validators; no dedicated section in this doc yet. |
| — | **appModel** | Standalone. | **Present, enabled** (`ApplicationModelEditorController` + module/scene/region editors, 3 application validators, wireframe preview via `ApplicationModelPreviewService`, real Preview App deploy). |
| — | **masterDetailModel** | Standalone. | **Present, enabled** (`MainDetailModelEditorController`, 2 validators; `MasterDetailModuleGenerator` is used by the Preview App deploy). |
| — | **treeModel** | Standalone. | **Editor present, disabled** (`TreeModelEditorController` wiring 3 tabs, 2026-09-25: *Columns* = `TreeRootPanelController` (SME "Root", `configuration.rootRef`: picks one of the nodes' `childRelationshipConfigurations`, shown as "DM → relationship" like SME; a dangling ref is kept and reported), `TreeColumnsPanelController` (row-based columns editor + Hierarchical Column, matching SME's placement) and `TreeNodeTypesPanelController` (one draggable/movable row per node type, click or Edit opens `TreeNodeDialogController` for Document Model / drag & drop / per-column field mapping, Add button below the rows; replaces the old list+detail *Nodes* tab); *Configuration* = `TreeConfigurationPanelController`; *Layout* = `TreeAccessibilityPanelController` (Hide Label, `configuration.labelHidden`) and the Overview editor's `StylesPanelController` bound to `content.styles` - 6 validators). **Still missing vs. SME's Node Types:** editing a node's Child Relationship Configurations (so Root can only offer configurations already in the JSON), actions, context menu, default row action, row title, icon, inherit-from-supertype, styles. |
| — | **contentModel** | Experimental in SME itself. | **Editor present, disabled** (`ContentModelEditorController`, 2 validators; the center renders the model with the real Content Engine in a `WebView`, see "Content Model preview"; the right column mirrors SME's per-type setting panel, see "Content Model property column"). |
| — | **typeDefinitionModel** | Reuses the whole DM editor infrastructure. | **Present, enabled** (`TypeDefintionModelEditorController`; the type-definition mode rules are validated, see the Document Model section). |
| — | **umModule** | User-management config: two YAML file types, "roles" and "users". | **Present** as `RolesEditorController` / `UsersEditorController` over `RolesDocument` / `UsersDocument` (`editors/auth`, `AuthFileFactory`). |
| — | **transformerModel, modelGraphDiagram** | Lower cross-reference count / experimental in SME. | **Not present** (no `ModelType`, no classes; only a transformer icon). |
| — | **settingsModule, filesModule, attachment, data** | Workspace-level resources, no structured model editing. | **Not assessed on 2026-09-20.** a12-studio has its own project settings (`ProjectSettings`); whether SME's `settings.yaml` is read is unconfirmed (no reference to it in the code). |
| n/a | **common, preview** | Not model types. `common` = shared editor UI building blocks used across modules. `preview` = pure runtime capability (opens a browser window running the live app), no persistence. | a12-studio's equivalents are the shared `propertyeditors` package, and two previews: the wireframe `PreviewServer` and the real Preview App deploy (see the Form Model "Live preview" note). |

The parked / rejected list for the whole tool lives in `TODO.md` ("Won't do" and "Parked").

### One-line descriptions of every other module (for orientation)

- **appModel** — overall application structure: navigation, module registration, entry screens.
- **attachment** — binary/file attachments (images, PDFs); opaque content, type inferred from directory location.
- **combinationModel** — see dedicated section above / priority table.
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
- **selectionModel** — reusable selection/filter spec over DM data; experimental. See dedicated "Selection Model" section above.
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
| Rule contradiction / consistency (constraint solver) | `RuleContradictionCheckService` → `com.mgmtp.a12.tdg.lib.TestDataGenerator.checkModel(...)` | **Missing, and blocked** (spike 2026-09-19): `com.mgmtp.a12.tdg:tdg-lib` is not in any community Maven repo (404) and the enterprise repos answer 401, so it cannot be pulled anonymously. Needs mgm credentials/commercial licence, or a clean-room solver |
| Document model structural/consistency validation | `DMValidationService` (kernel `getElementProblems` + custom checks) | Hand-ported clean-room validators only (`BasicConsistencyValidator` etc.) — corrected 2026-09-19: the `ValidationRuleService`/`ComputationRuleService` previously listed here never existed in this repo. **Feasible on kernel 31.1.1 through the public API**: `IDocumentModelService.checkConsistency` flags corrupted conditions, unexpanded includes and invalid entity paths (message only, no line/column) |
| Condition/expression language validation & formatting | `ValidationRuleService`, `ComputationRuleService` (Kotlin) | **Missing** (corrected 2026-09-05 — previously claimed present; no such Java services exist, no kernel dependency in this repo). `RuleConfig.errorCondition`/`ComputationAlternative.precondition`/`operation` are edited as text with a **syntax-only** check (clean-room `RuleLang.g4` / `RuleLanguageSyntaxChecker`, per-keystroke in the editors only, since 2026-09-14/15; verified 2026-09-20) and no semantic validation — see the Document Model "Editor features" correction above. **Spike 2026-09-19: works in-process on kernel 31.1.1** (`DocumentModelService.hasValidConditionText` / `isValidComputation` / `formatComputationOperation`, all `a12internal`), with line/column positions; needs the model expanded first |
| Print rendering (PDF) | `PrintService` — PDFBox or legacy engine via `a12.print.engine.runtime` | **Missing** (corrected 2026-09-20): the `PrintService`/`DocumentModelResolver` scaffolding was deleted 2026-07-20 and no print-engine dependency exists. A Print Model editor and validators exist but the type is disabled |
| Document model expansion (includes/imports) | `ExpansionService` | **Missing** (confirmed 2026-09-19: no include/import expansion anywhere in this repo). **Feasible on kernel 31.1.1** (`DocumentModelExpandService.expand`, `internal`): `Invoice_DM` 6 → 135 elements in 23 ms |
| Combination Model expansion | `CombinationModelExpansionService` | **Missing** (corrected 2026-09-08 — previously claimed present; no such service, or `services/combinationmodel/` directory, exists in this repo). The Combined Document Model editor built 2026-09-08 only validates the structural rules from `DomainCombination.json` (missing/not-allowed/duplicate references per step); DM expansion, rule-contradiction/SMT solving and the "Validate model up to this step" action all still depend on this (loop detection does not: it needs only the reference graph and was ported 2026-09-20) |
| Additive Model join | `AdditiveModelController` (`kernel-md-join`) | **Missing** — corrected 2026-09-19: no kernel dependency is present (this row previously said it was). **Feasible on kernel 31.1.1**: `kernel-md-join` no longer exists there, the join moved into `kernel-md-facade` (`DocumentModelJoiningService.join`, `internal`); `Person_Dc` + `PersonEmployee_Ad` joined to 42 elements |
| Selection Model join/validate | `SelectionModelController` | **Missing** (2026-09-20). Only the clean-room structural rules of `DomainSelection.json` are ported (`validators/selection/`, 5 validators); no join/apply of a selection against a Document Model |
| Structural Mapping Model consistency | `StructuralMappingModelService` (`SmmService`) | **Missing** (corrected 2026-09-20: the `services/structuralmappingmodel/` scaffolding listed here was deleted 2026-07-20). No validation service for this type at all; the editor is a stub and the type is disabled |
| Mapping Model consistency/generation | `SMEMappingModelService` | **Missing** (corrected 2026-09-20: `services/mappingmodel/` was deleted 2026-07-20). No validation service; the editor covers target + sources only and the type is disabled |
| Test data generation | `TestDataService` (same TDG lib, generative mode) | Missing, and blocked on the same TDG availability problem as rule contradiction |
| Formula/computation execution over content documents | `DocumentValidationService` (`docRtService.compute(...)`) | **Missing** (confirmed 2026-09-20: nothing in any module evaluates computations or validates document instances; there is no `document` model type either). Not covered by the kernel spike, which only exercised model-level calls |
| XSD → Document Model transformation | `TransformerService` | Not present (re-verified 2026-09-20: no XSD code, no Transformer Model type; the create-from-Access/-Excel plugins are unrelated converters) |
| Move/rename refactoring (rewrite condition text) | `MoveRefactoringService` | Present in-process (clean-room `DocumentModelRefactoring` + `ProjectReferenceRefactoring`, no kernel call), within the model and across the project's Print/Query/Mapping/Structural Mapping/Selection models and including Document Models; remaining gaps listed in the Document Model gap list above |
| File load/save persistence | None on SME's backend either — frontend/Electron-owned | a12-studio owns this directly (single JVM app) |

`a12-studio-server` currently contains only `A12StudioServer.java` and `A12StudioServerTest.java` (2026-09-20; the
`SystemResource.java` this note used to list is gone) — it is not where kernel calls happen, and neither is
`a12-studio-data-services` (preview services only, see the architecture section): today no module calls the kernel.
The one place vendor JVM code runs is out of process, in the Preview App deploy (`a12-studio-ui/.../previewapp/`).

### Kernel dependency spike (2026-09-19) — decision: **hybrid, kernel pinned to 31.1.1**

Time-boxed spike, no production code. A scratch Gradle project (outside the repo) resolved the kernel against the
same repository a12-studio already configures in `settings.gradle`
(`https://artifacts.geta12.com/artifactory/a12-community-maven/`, no credentials) and ran the four capabilities
in-process against the JSON fixtures under `testing/workspaces/{basic,advanced_new,e-commerce}` (53 Document
Models with a `modelRoot`). Everything below was observed, not inferred from documentation, unless marked otherwise.

**Which kernel version.** SME pins kernel `30.7.0`; that version is not published. Published: `30.8.1/5/6`,
`31.1.0/1/3`. The version has to match the Document Model version of the files, and a12-studio's files are
DM `29.4.0`, which is the kernel **31.1.x** generation (kernel changelog A12K-4102/3995: `includeConfig`,
`documentUniquenessCriteria`):

| Kernel | Reads a12-studio's DM 29.4.0 fixtures? |
|---|---|
| 30.8.6 | 52 of 53. `Person_Dc` fails hard on `documentUniquenessCriteria`. **Worse:** the serializer only knows the old `modelAlias`, so `includeConfig` is silently dropped and expanding `Invoice_DM` does nothing (resolver never called, 0 problems, 6 elements before and after). A silent no-op, not an error |
| 31.1.1 | 53 of 53 |

`31.1.3` is unusable today: `kernel-md-facade:31.1.3` requires `kernel-internal-*:31.1.3`, which are only published
up to `31.1.1`. Pin `31.1.1` and re-check when `31.1.3`'s internals appear. In 31.x the modules were reshuffled:
`kernel-md-model` and `kernel-md-join` no longer exist past 30.8.6 (the model classes are in
`kernel-internal-md-model`, the join is inside `kernel-md-facade`), and `kernel-md-facade` alone pulls in the rest.

**Footprint and conflicts (kernel 31.1.1, `kernel-md-facade` + `-documentmodel` + `-serializer`).** 72 jars,
20.5 MB: 56 `com.mgmtp.a12.kernel` artifacts (including the `mm*` model-typing/validator ones), 2 `com.mgmtp.a12.base`, and
four third-party artifacts new to the studio: Groovy 3.0.25, commons-cli, commons-text, jakarta.validation-api. The studio's `a12-studio-ui` runtime
classpath has 50 artifacts, 11 of which overlap; none needs a code change, all resolve upward:

| Artifact | Studio | Kernel | Resolves to |
|---|---|---|---|
| `tools.jackson.core:jackson-databind`/`-core` | 3.1.4 | 3.2.1 | 3.2.1 |
| `jackson-annotations` | 2.21 | 2.22 | 2.22 |
| `commons-lang3` | 3.16.0 | 3.20.0 | 3.20.0 |
| `commons-collections4` | 4.4 | 4.5.0 | 4.5.0 |
| `commons-io`, `slf4j-api` | 2.22.0 / 2.0.18 | 2.21.0 / 2.0.17 | studio's (higher) |
| `antlr4-runtime`, `antlr-runtime`, `ST4` | 4.13.2 / 3.5.3 / 4.3.4 | identical | unchanged |

Kernel 31.x uses **Jackson 3** (`tools.jackson`), the same generation as the studio, so there is no second Jackson
on the classpath (kernel 30.8.x used Jackson 2 / `com.fasterxml`). The one real bump, Jackson 3.1.4 → 3.2.1, was
tested by forcing it through a Gradle init script: `:a12-studio-models:test` 821 tests, 0 failures, 6 skipped. The
studio has no `module-info.java`, so there are no JPMS split-package problems. **Not verified:** a full
`a12-studio-ui` build/run or `shadowJar` with the kernel on the classpath (check `mergeServiceFiles()` and
JavaFX co-existence when this is first integrated), and installer size impact. Avoid `kernel-rewrite`: it pulls
OpenRewrite → Groovy 4 (`org.apache.groovy`) and clashes with the kernel's Groovy 3 on the same Gradle capability.

**Licence.** `base-model-consistency` publishes a `-license.txt`; `kernel-md-facade` and `kernel-internal-md-model` 31.1.1
ship `META-INF/LICENSE`, `NOTICE`, `THIRD_PARTY_NOTICES`, and the facade sources carry
`SPDX-License-Identifier: EUPL-1.2 OR LicenseRef-commercial` (read from `DocumentModelExpandService.java`; I did not
open every artifact). That is the same dual licence
as this repo's `LICENSE` (EUPL-1.2), so linking is compatible if the EUPL option is chosen, and the notices must be
shipped. Metadata is thin: the kernel POMs I checked have no `<licenses>` block and the `kernel-md-facade` CycloneDX SBOM declares a licence for only
3 of 58 `com.mgmtp` components, which will show up as "undeclared" in `generateLicenses` output. **TDG is different:**
no `com.mgmtp.a12.tdg` group exists in `a12-community-maven`, `a12-2026-06-community-maven` or
`a12-2025-06-community-maven` (404), and `a12-enterprise-maven` / `a12-enterprise-plus-maven` return 401. TDG is a
licensed product; use needs mgm credentials (`a12-license@mgm-tp.com`).

**Capabilities, in-process on kernel 31.1.1 against a12-studio JSON:**

| # | Capability | Result |
|---|---|---|
| a | Validate a rule `errorCondition` / computation `operation` | **Works.** Company_DM 4 rules OK, Person_Dc 4 rules + 1 computation OK, Country_Dc 4 + 1 OK, Invoice_DM (expanded) 30 rules + 21 computations OK, 0 false positives. A deliberately corrupted condition gives `L1:4-12 ... Unexpected found 'nonsense'. [MVK_UNEXPECTED_TOKEN]`, so line/column positions are available. The kernel **refuses** to build its validation service for a model with unexpanded includes (`Unexpanded include at '/Invoice/Addresses'`), so the pipeline is expand, then validate. First service creation 35 ms cold, 110 ms for the 135-element Invoice_DM |
| b | Expand a Document Model's includes | **Works.** `Invoice_DM` 6 → 135 elements, 23 ms, 0 notifications; the resolver is fed by `UnexpandedModelResolverImpl(Function<String, Reader>)`, i.e. from in-memory or on-disk JSON without a directory layout |
| c | TDG `checkModel` rule contradiction | **Not testable.** Library not obtainable (see Licence) |
| d | Additive join | **Works mechanically.** `Person_Dc` (21 elements) + `PersonEmployee_Ad` (23) → 42 elements, 2 expected warnings (`roles` and `additive-document` annotations are not joined). Not diffed against SME's output, so semantic parity is unchecked |

**API stability, the main risk.** Everything used above except `checkConsistency` sits in an `internal` or `a12internal`
package (`DocumentModelService`, `DocumentModelSerializer`, `DocumentModelExpandService`,
`DocumentModelJoiningService`, `IMVK_Service`). The kernel's own policy (`kernel-kernel-documentation-dev.md`,
"Architecture") says changes there "are documented neither in the changelog nor in the migration instructions" and can
land in minor and patch releases; other A12 components are informed in advance, an outside consumer is not. SME is such a
component. The evidence that this bites: between SME's kernel 30.x code and 31.1.1 the expansion moved from
`DocumentModelService.expand(dm, resolver, reporter)` to `DocumentModelExpandService.expand(dm, ExpandingDmResolver, consumer)`,
the join changed from `join(ref, adm, locale, ..., AdditionOrigins)` to `join(ref, List<JoiningItem>, locale, JoiningProblemReporter)`
with `JoiningType.ADDITIVE_MODEL`, `getMvkServiceForModel(dm, String)` lost its second argument, and Jackson went 2 → 3.
SME's Kotlin helpers therefore cannot be ported line by line. The **public** API (`DocumentModelServiceFactory`) is
small: `IDocumentModelSerializer` (de)serialize, `IDocumentModelService` (`getPath`, `checkConsistency`,
`generateValidationCode`, `getAllAnnotatedElements`) and `IDocumentModelMigrator` (forward and backward migration).
`checkConsistency` alone already reports corrupted conditions, unexpanded includes and invalid entity paths, but with
messages only.

**Recommendation: hybrid per capability.**

- **Adopt the kernel, pinned to exactly `31.1.1`,** for whole-model consistency (public API), per-rule and computation
  validation and formatting, DM expansion, additive join and model migration.
- **Confine it to one new Gradle module** exposing a12-studio-typed interfaces (no kernel types leaking out), so the `internal`
  surface, the Jackson/commons bumps and any future kernel upgrade touch one place. Guard it with contract tests over
  the existing `testing/workspaces` fixtures (the same ones the round-trip tests use), so a kernel bump that changes
  behaviour fails a test instead of shipping silently.
- **Stay clean-room** for everything already built (move/rename refactoring, the hand-ported structural validators) and
  for anything TDG-dependent until mgm access is sorted out. Rule contradiction and test-data generation are blocked,
  not merely unbuilt.
- **Before committing:** ask mgm (`a12-license@mgm-tp.com`) whether depending on `a12internal`/`internal` classes from
  a non-A12 component is acceptable and whether they will announce changes; that is a business decision and it decides
  how much of the `internal` surface is safe to rely on.

**What it would unlock, ranked by value per effort:**

1. **Semantic validation and formatting of rule/computation conditions**, with positions. Plugs into
   `RuleEditorController.setValidator(...)`, which already exists and currently has nothing semantic to call.
2. **DM expansion.** Prerequisite for the kernel to validate any model with includes at all, for the Combined Document
   Model's "validate model up to this step", for Form Model includes, and for any view of the effective element tree.
3. **Additive join.** Unblocks the Additive Document Model editor and is a hard dependency of the Mapping and
   Combination models (see "Other model types").
4. **Kernel consistency check** as the authoritative layer next to the hand-ported validators (check for duplicate
   messages before switching any of them off).
5. **Model migration** through `IDocumentModelMigrator`, the only piece here that is public API.
6. *Blocked:* rule contradiction, test-data generation (TDG).

**Reproducing.** Repository above; coordinates `com.mgmtp.a12.kernel:kernel-md-facade:31.1.1`,
`kernel-md-documentmodel:31.1.1`, `kernel-md-serializer:31.1.1`; entry points
`DocumentModelSerializer().deserialize(Reader)`, `new DocumentModelService().getMvkServiceForModel(dm)`,
`hasValidConditionText(mvk, rule, reporter)`, `isValidComputation(path, dm, reporter)`,
`DocumentModelExpandService.expand(dm, ExpandingDmResolver.of(new UnexpandedModelResolverImpl(id -> reader), locale, sink), sink)`,
`DocumentModelJoiningService.join(ref, List.of(new DocumentModelJoiningItem(adm, JoiningType.ADDITIVE_MODEL)), locale, new JoiningProblemReporter(reporter))`.
The scratch project is not in the repo; it was ~200 lines of Java over the calls above.

---

## How to keep this doc useful

This is a snapshot (2026-09-20 for the sections re-verified on that day, see the header; older dates on individual
sections and rows still apply where a section says so). SME modules marked "experimental" here may graduate or change;
kernel dependency versions will drift. When picking up work in an area covered here, spot-check the relevant SME module/backend
service still looks the way this doc describes before trusting the gap list — SME is under active, independent
development.
