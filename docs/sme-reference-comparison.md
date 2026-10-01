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

**Correction (2026-09-27):** "which a12-studio already correctly implements" above overstated it — `includeLevel`
specifically was never added to `IncludeConfig.java` (confirmed by reading the class: only `reference`,
`excludeRules`, `excludeComputations`, plus `@JsonIgnoreProperties(ignoreUnknown = true)`, so a real file carrying
an `includeLevel` key would silently lose it on the next save). Checked against SME's own source
(`commonDocumentModel/api/elements/{include,group}.ts`): the field is `"SINGLE_RG" | "MODEL_ROOT"`, and it is
**not** in SME's own Include-authoring form either (`DomainInclude.json` has no such field) — `MODEL_ROOT` is an
internal marker SME's Additive Document Model uses to mount its base model as a synthetic root-level Include with
the single-root-group constraint (see the next paragraph) waived; a12-studio's Additive Document Model doesn't
represent its base model as an Include at all (`AdditiveDocumentModelResolver`), so it has no use for the value.
Net effect: not an editor-feature gap, but a real lossless-round-trip gap — add the field to `IncludeConfig` (no
UI, no new validator) the next time round-trip fixtures are touched.

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

**Correction (2026-09-27):** `roundingMode?` above describes SME's own shape only — `ComputationConfig`/
`ComputationAlternative` (`a12-studio-models`) has no such field at all, confirmed by reading both classes. This
is lower-priority than the `includeLevel` gap above: SME's own `DomainComputation.json` carries an explicit
comment on this exact field ("the field is not shown in the SME, but needs to be in the model, so that it will
not be overwritten") — it is a round-trip-only value (an `Exact`/`RoundUp`/… enum) with **no editor UI on the SME
side either**, same bucket as the already-noted `toleranceRangeOp`. No fixture in either repo carries it today, so
add it as a lossless pass-through field (no UI, no validator) only once a real file turns up needing it.

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
| Ad hoc testing / live preview | Select elements (Alt+T, or bulk from the Ctrl+M panel), backend `AdHocTestService` calls kernel `DocumentModelService.createReducedDocumentModel(selected, partiallySelected)` + `generateValidationCode`, the client generates a Form Model from the reduced DM (`fmm-support` `FormModelGeneratorAPI`) and renders both in the preview window | **Present (2026-09-25)**, built on the installed SME instead of the kernel or the private `fmm-support`: "Ad Hoc Testing" (toolbar button, context menu of an element and of the root, Alt+T; `DocumentModelActions#startAdHocTest`) opens `AdHocTestPreviewSession` in the browser chosen in the Preview settings. The session expands the Document Model and its includes with the installed backend (`SmeBackend`, POST `/api/document-model/expand`), lets `/generate-ad-hoc-test-input` cut out the selection (an element and its descendants; selecting an Include selects its whole expanded subtree; no selection = the whole model), generates the Form Model with `FormScreenGenerator` (the studio port of the "build screens from fields" generator, so screens/labels can differ in detail from what SME's `form-model-generator` emits) and renders it with the real Form Engine (see "Form Engine preview" below). It follows edits to the Document Model like SME (polling, see the Preview settings), dropping selected elements that no longer exist. **Ad hoc testing of an Additive Document Model, done 2026-09-30**: the button now enables whenever a Combination Model references it, resolved the same way the "Additive Elements Only" preview already resolves that ambiguity; `AdHocTestPreviewSession` expands that Combination Model as context (SME's `contextData`) instead of the additive model alone, mapping selected element ids onto their kernel-rewritten ids first (`md5Hex(additiveModelId) + "_" + originalId`, independently re-confirmed against the real `PersonEmployee_Ov.json` fixture). Could not be verified end-to-end (no A12 installation in this sandbox to run `SmeBackend` against) - only the pure id-mapping is covered by an automated test; verify the full flow against a real installation. An element inside an Include/Attachment/Multi-Select is still tested as that whole group. Pinned by `FormEnginePreviewTest`, `DocumentModelTreeFxTest`, `AdHocTestPreviewSessionAdditiveIdMappingTest`, `AdditiveAdHocTestAvailabilityTest` | |
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

**Resolved (2026-09-27):** the exact conditions are now known, read directly from SME's `DomainField.json`
meta-model rules: `younger1900`/`youngerThan1900Check` is only valid when `format` contains a year component
(`OPTIONAL_DATE_TYPE_INVALID`'s sibling rule `YOUNGER1900_CHECK_INVALID`); `interpretationOfYear` is only
relevant for `DateRange` and is specifically *required* (not just "valid") when `formatDateRange ==
"DD.MM-DD.MM"` **and** the model is year-based (`INTERPRETATION_OF_YEAR_INVALID`/`_MISSING`); plain
Date/DateTime/Time's `optionalDateType` is only valid for the three formats `DD.MM.YYYY`/`YYYYMMDD`/`YYYY-MM-DD`,
while DateRange's own `optionalDateType` is restricted to `FULL` only (stricter — a different rule,
`OPTIONAL_DATE_RANGE_INVALID`). **Update (2026-09-30):** these SME conditions are correct, but re-checking them
against a12-studio's own (already-diverged) data model before implementing found only `YOUNGER1900_CHECK_INVALID`
actually ports - `optionalDateType` has no a12-studio equivalent at all (a12-studio's similarly-named
`DateFragmentFieldType` is a real, distinct kernel type, `IDateFragmentType`, not a port of this SME concept) and
`interpretationOfYear`'s gating needs a "model is year-based" input (`ModelInfo.baseYear`) a12-studio's `ModelInfo`
doesn't carry. See "Document Model: gap review (2026-09-27)" below, gap 10, for the full corrected write-up.

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
still true. **Update (2026-09-27):** the count is now 25 (`RuleConditionSyntaxValidator` added 2026-09-21,
`CdmQueryRootReferenceValidator`/`CdmRelationshipStepValidator` added 2026-09-23) — see the full numbered list in
"Document Model: gap review (2026-09-27)" below, which also found several validator **correctness** gaps this
paragraph's "roughly matches" didn't catch (an over-strict Enumeration rule, and under-strict String/Include/name
rules).

### Document Model: gap review (2026-09-27)

**Status (2026-09-30): gaps 1-6, 9, 10 (partial) and 11 closed, plus the separately-tracked Include-loop picker
item ("Features" in TODO.md); 7, 8, 10 (remainder) still open (7-8 no UI needed/low priority, 10's remainder
turned out not to be a re-read-and-port job after all - see below).**
`EnumerationTypeConfigValidator.checkLabels` now only runs once at least one enum value already has at least
one non-blank label (gap 1); `StringTypeConfigValidator` now requires `linebreaksPermitted` to be explicitly
set whenever `noValueValidation` is on and rejects `pattern`/`minLength`/`hintList` alongside it (gap 2); new
`IncludeStructureValidator` ports SME's remaining two Include checks - included model must declare every
locale this model has, included model must have exactly one non-repeatable root group (gap 3); new
`SupportedCharactersValidator` requires every `ModelConfig.supportedCharacters` entry to be exactly one
character with no stray whitespace (gap 4); gap 5 (duplicate element *name* within a group) turned out to
already be implemented - `MissingReferenceValidator.getElementsWithDuplicatedNames` was already wired into
`validate()`'s group branch, just without a pinning test (added: `missingReferenceValidatorReportsDuplicateElementName`
in `DocumentModelValidatorsTest`) - this review's initial read of the validator list missed that the existing
`validation.missingReference.duplicatePath` check *is* SME's `A12_DUPLICATE_NAME_WITHIN_GROUP` rule, not
something separate; `BasicConsistencyValidator` gained the name-pattern check (leading digit, leading "xml"
case-insensitive, `..`/`::`/`.:`/`:.`) for every Group/Field/Rule/Computation name (gap 6). Separately, the
"Base Model / Include pickers should not offer loop-creating candidates" item (SME's `createsIncludeLoop`) is
now fixed too: `TransitiveTypeDefinitions.includedModelIds` (new, the Include-only twin of the pre-existing
`importedModelIds` used the same way for the Type Definition Import picker) walks a candidate's own Include
chain, and both `IncludeDialogController.includableModels` (new-Include dialog) and
`IncludePropertiesPanelController.includableModelIds` (existing-Include's reference combo) now exclude any
candidate whose Include chain already reaches back to the model being edited.

Full field-by-field and validator-by-validator review, prompted by the same treatment already done for Tree/
Overview/Application Model. Method: read every rule in SME's Document-Model-editor meta-model
(`client/resources/models/documentModel/Domain{Field,Group,Rule,Computation,Typedef,ModelConfig,ModelSettings,
Multiselect,MultiselectField,Attachment,Include,HiddenRoot,AddDocumentModel,CopyDocumentModel,
EditSupportedCharacters}.json` — these files are themselves Document Models and are SME's actual ground truth for
every field/validation rule its self-hosting Form-Engine editor generates, not just `.tsx` source) plus the
backend's hand-coded structural layer (`backend/.../documentModel/features/validation/DMValidationService.kt`),
against a field/panel/validator inventory of a12-studio's current `documentmodel` package, model classes and
`DocumentModelValidationService`. SME checkout: `C:\workspace\sme` HEAD `ba2e34687`/2026-03-09 (unchanged since
the March analysis), kernel schema `modelVersion` 28.4.0 as pinned in the Domain files — a slightly older schema
than the 29.x+ shape a12-studio's own data model targets (see the `includeConfig`/`modelAlias` correction above),
so a few of SME's own fields here (`Boolean.trueValue`/`falseValue`, see "Checked, not a gap" below) may themselves
already be dead weight SME carries only for backward migration, not real gaps to close.

Several strong candidate gaps surfaced by the meta-model read turned out, on checking a12-studio's actual Java
model and panels directly, to already be built or not applicable — listed first so the numbered list below isn't
cluttered with false leads:

- **Element `Descriptions` (Internal/External, per-language) already exist and are already wired up.** SME's
  meta-model gives every element type a `Descriptions` group alongside `Labels`/`HelperText`/`Annotations`; a
  first pass over SME's side alone suggested this was unmodeled in a12-studio. It isn't: `Element.java` has had
  `internalDescription`/`externalDescription` (`List<Label>`) from the start, `LocalizedTextPanelController`
  already has generic `configureInternal()`/`configureExternal()` methods, and every Document Model element editor
  (`DocumentModel{Field,Group,Include,Attachment,ComputationRule,ValidationRule}EditorController`) already calls
  them. No action needed.
- **`BooleanFieldType` correctly has zero options.** SME's `DomainField.json` (this checkout's older schema) shows
  a `trueValue`/`falseValue` pair with real validation rules (`TRUE_FALSE_EQUALS`/`TRUE_FALSE_INVALID`), which
  looked like a gap against a12-studio's genuinely empty `BooleanFieldType`. But every occurrence of `type:
  "ConfirmType"` found in that file is meta-model-internal (a checkbox-shaped meta-field reused on *other*
  options, e.g. `zeroAllowed`), not the real Boolean field type's own config group — there is no real
  `BooleanType` options block in this file at all. Confirms the existing correction in "Field-level & validator
  gap analysis" point 2 above; not a gap.
- **Type Definition name uniqueness (model-wide) is already checked.** `MissingReferenceValidator`'s
  `duplicateTypeDefinitionName` check already covers SME's `TYPE_DEF_NAME_DUPLICATED` rule.
- **Number field's `trait` (Amount/Percent/Permille) is already exposed** on `DataTypeNumberConfigurationPanelController`.

| # | Gap in Studio | What SME does | Effect today |
|---|---|---|---|
| **Validator correctness (highest priority — these are wrong answers, not missing ones)** | | | |
| 1 | **Enumeration per-locale label check is *stricter* than SME (false positives).** `EnumerationTypeConfigValidator.checkLabels` unconditionally requires every enum value to have a label in every model locale | SME's rule is conditional/all-or-nothing (`A12_COUNT_OF_LABELS_INVALID`/`A12_LABEL_FOR_LANGUAGE_MISSING`): labels are optional, but once *any* value has a label in *any* language, *every* value needs one in *every* language | A perfectly valid, unlabeled Enumeration field (legal in SME — labels are opt-in) is flagged as an error in a12-studio purely for existing. Fix: only run the per-locale-coverage check once at least one value already has at least one non-blank label |
| 2 | **String `noValueValidation` has no cross-validation at all.** `StringTypeConfigValidator` only checks min>max length and the two line-break conflicts | SME additionally requires `linebreaksPermitted` to be explicitly set whenever `noValueValidation` is on, and forbids `pattern`/`minLength`/`hintList` from being set at the same time as `noValueValidation` (only `maxLength` may remain) | A String field can have `noValueValidation=true` together with a `pattern` or `minLength` that then does nothing (silently ignored by the kernel), with no warning the combination is meaningless |
| **Missing validators** | | | |
| 3 | **Include validation is missing two of SME's checks.** Only reference-resolves (`MissingReferenceValidator`) and TD-mode (`IncludeTypeDefinitionModeValidator`) exist | SME also requires: (a) the included model declares every locale the including model has ("invalid locales"), (b) the included model has exactly one non-repeatable root group | An Include of a model missing a locale, or of a model with 2+ root groups (or a repeatable one), is accepted with no warning — the third SME Include check, "creates an include loop", is a pre-existing TODO item ("only reported after selection") |
| 4 | **`ModelConfig.supportedCharacters` has a raw-text panel but no real validator.** `SupportedCharactersPanelController` only reports a JSON-parse error | SME additionally validates each array entry is a single quoted character with no stray whitespace | `["ab", " ", ""]` parses as valid JSON and passes silently, even though none of those entries is a usable single supported character |
| 5 | **Duplicate element *name* within a group is not checked** (only duplicate *id*, via `DuplicateIdValidator`) | SME's `A12_DUPLICATE_NAME_WITHIN_GROUP` family flags two siblings sharing a display `name` even when their ids differ | Two fields/groups/rules in the same group can have identical names; since relative-path rule/computation expressions resolve by name, this is a real ambiguity risk, not just cosmetic |
| 6 | **No name-pattern validation on Group/Field/Rule/Computation names** (only blank-check, via `BasicConsistencyValidator`) | SME rejects a leading digit, a leading "xml" (case-insensitive), and dot/colon/mixed sequences (`..`, `::`, `.:`, `:.`) in names | A name like `1Field` or `xmlNote` is accepted; likely low-severity in practice since such names are unusual, but cheap to close alongside gap 5 in the same validator pass |
| **Round-trip data loss (no UI needed — SME doesn't have UI for these either)** | | | |
| 7 | *Fixed 2026-10-01: pass-through `IncludeConfig.includeLevel` (`NON_NULL`), pinned by `IncludeLevelRoundingModeRoundTripTest`.* `IncludeConfig.includeLevel` is entirely unmodeled and `@JsonIgnoreProperties(ignoreUnknown = true)` silently drops it on load | Real field in SME's persisted shape (`"SINGLE_RG" \| "MODEL_ROOT"`), part of the same kernel A12K-4102 migration that produced `includeConfig` itself — but not present in SME's own Include-authoring form (`DomainInclude.json`) either; `MODEL_ROOT` is an internal marker SME's Additive Document Model uses to mount its base model as a synthetic root-level Include, a mechanism a12-studio's `AdditiveDocumentModelResolver` doesn't need | A file with an explicit `includeLevel` on a regular Include loses it on next save. Add the field for lossless round-trip only — no UI, no validator (see the correction in "Data model" above) |
| 8 | *Fixed 2026-10-01: pass-through `ComputationConfig.roundingMode` (`NON_NULL`, raw string), same test.* `ComputationConfig`/`ComputationAlternative.roundingMode` is entirely unmodeled | Real field in SME's shape, but SME's own meta-model comment says it "is not shown in the SME" either — round-trip-only, same bucket as the already-known `toleranceRangeOp` | No fixture in either repo carries it today; lowest priority of the round-trip items, add only if one turns up |
| **Fixed 2026-09-27** | | | |
| 9 | ~~`DocumentModelContent.documentUniquenessCriteria` (`ContentUniquenessCriterion`, addresses fields by full path string) has zero editor UI and zero validator~~ — distinct from the fully-built `ModelConfig.uniquenessCriteria` (addresses fields by element id) | Not independently re-verified against SME's own form for this; `ContentUniquenessCriteriaPanelController`/`ContentUniquenessCriterionDialogController` follow `DocumentUniquenessCriteriaPanelController`'s shape exactly, just keying Fields by full path (`ElementIndex.getPath`/`resolveAbsolutePath`) instead of element id, and `ContentUniquenessCriteriaValidator` checks name required/unique, ≥1 field, and every field path resolves — no SME source confirms the Required/non-repeatable *eligibility* restriction the other dialog has, so that restriction was deliberately not copied over | The real `advanced_new/models/10_People/Person_Dc.json` fixture (`PersonIDMustBeUnique`, fields `/Person/Type`/`/Person/PersonID`) is now editable and validated, not just silently round-tripped |
| **Lower confidence — re-verify against `DomainField.json` before implementing** | | | |
| 10 | ~~`DataTypeDateFragmentConfigurationPanelController`/`DataTypeDateRangeConfigurationPanelController`'s "expert" fields have no cross-field gating~~ - `YOUNGER1900_CHECK_INVALID` closed 2026-09-30, the other two SME rules don't port (see below) | SME gates `younger1900`/`interpretationOfYear`/`optionalDateType` validity on `format`/`formatDateRange` | **`YOUNGER1900_CHECK_INVALID` done:** new `DateYounger1900ConfigValidator` requires a format containing a year whenever `youngerThan1900Check` is set, on `DateFragmentFieldType`/`DateRangeFieldType` (the only two a12-studio types with this field). **`OPTIONAL_DATE_TYPE_INVALID`/`OPTIONAL_DATE_RANGE_INVALID` don't port**: both key off SME's `optionalDateType` field (`DAY_OPTIONAL`/`MONTH_OPTIONAL`/`YEAR_OPTIONAL` - a partial-date-with-placeholder-zeros concept, e.g. `00.12.0000`), which has no equivalent in a12-studio at all; `DateFragmentFieldType`/`DateFragmentTypeOptions.formatOfFragment` looked like a plausible match by name but is a different concept on inspection - its own presets (`yyyy`, `MM`, `yyyy-MM`, `MM-dd`) are "capture only this fragment", not "full format with some fragments zeroed", and don't overlap SME's three optionalDateType-eligible formats (`DD.MM.YYYY`/`YYYYMMDD`/`YYYY-MM-DD`) at all - confirmed this is a real, distinct A12 kernel type (`IDateFragmentType`, per `C:\workspace\a12\2606-06-doc\data_services-dataservices-documentation-src.md`'s `datefragment_range` operator), not an SME concept a12-studio ported, so there is nothing to gate here. **`INTERPRETATION_OF_YEAR_INVALID`/`_MISSING` don't port** either, for a different reason: both need a "is this model year-based" input - SME's `ModelInfo.baseYear` (`serializedDocumentModel.ts`) - which a12-studio's `ModelInfo.java` doesn't model at all (only `name`/`immutable`/`comment`/`joinedModelsInfo`); adding `baseYear` (round-trip field + a `ModelInfoPanelController` UI field) is a real, separate, small gap worth its own pass before `interpretationOfYear` gating can be built - not guessed at here |
| 11 | ~~`NumberTypeConfigValidator` may be missing a `trait=Amount ⇒ maxFractionalDigits==2` rule...~~ - closed 2026-09-30 | `DomainField.json`'s `A12_AMOUNT_AND_INVALID_FRACT_DIGITS`/`MIN_LENGTH_BIGGER_MAX_LENGTH` (Custom section) | **Done:** `NumberTypeConfigValidator` now checks `trait=amount ⇒ minFractionalDigits==maxFractionalDigits∈{0,2}` (a12-studio's `trait` wire value is lowercase, confirmed against real fixtures - a translation detail, not a gap); `CustomFieldTypeConfigValidator` now checks `minLength>maxLength`. Re-verified directly against `DomainField.json`: the doc's prior `positivesOnly`-vs-negative-`minValue` and `maxIntegerDigits`-vs-`maxValue`-digit-count items don't correspond to any real SME rule - dropped as speculative, not implemented. Number's own `minLength`/`maxLength` (string-representation length) has no a12-studio field to hang a check on, same as gap 10's `optionalDateType` - not a gap. `MIN_FRACT_DIGITS_MISSING`/`MAX_FRACT_DIGITS_MISSING` (required-ness) checked and confirmed **not portable**: `DataTypeNumberConfigurationPanelController` deliberately makes these an optional pair behind a "has decimal places" checkbox (a real a12-studio divergence from SME, not an oversight) - 29 of 68 real `NumberType` occurrences in `testing/workspaces` have no fractional digits at all, so porting this rule would flag ~43% of real, valid fields |

**Also checked and found to be a non-issue:** `SchemaVersionValidator`'s errors are model-sourced (`elementId ==
null`) and `ValidatorRunner` drops model-sourced errors entirely per its own doc comment — this makes the
validator dead in practice (it can never surface an error to the UI), which may be intentional parity with SME
(whose own UI only ever surfaces element-level problems) or an accidental dead validator; worth a one-line
confirmation the next time `ValidatorRunner` is touched, not urgent on its own. **Cross-cutting annotation
duplicate-name checking, fixed 2026-09-29:** SME's `annotationNamesNotUnique` (`core/ModelHeader.json`,
`RepetitionNotUnique(annotations/name)`) had no equivalent for any model type, not just Document Model, and was
structurally similar to the already-tracked cross-cutting `HeaderRolesValidator` gap - folded into that same
treatment rather than a Document-Model-only fix. New `AnnotationDuplicateValidator` (header annotations only;
SME's `I_Annotated` mixin is also composed into element-level `annotations` lists - a Document Model `Element`,
a Form Model `ScreenElement`/`Row`/`Cell` - each of which could independently have a duplicate name, but that is
a separate per-model-type element-walk gap; Document Model elements were added 2026-10-01 as `ElementAnnotationDuplicateValidator`, Form Model screens/elements/rows/controls/overview columns as `FormAnnotationDuplicateValidator`; buttons, config entries and row actions are not walked) is now registered in all 14
model-type validation services, matching `HeaderRolesValidator`'s own rollout. Pinned by
`AnnotationDuplicateValidatorTest`; full `a12-studio-models-validation` suite green, no real fixture trips a new
finding. Labels/HelperText/Descriptions cannot have SME's "duplicate language row" problem at all
— `LocalizedTextPanelController` renders one control per declared model locale (`Map<String, TextInputControl>`
keyed by locale), so the shape that would produce a duplicate simply doesn't exist in this editor's UI.

**Suggested order.** 1 and 2 first (validator *correctness* bugs — one over-strict causing false positives on
every unlabeled Enumeration field, one silently accepting a meaningless String configuration); 3–6 next (missing
validators, all cheap, no design work — 5 and 6 are natural to land together as one pass over
`BasicConsistencyValidator`/name checks); 9 done (real fixture surfaced needing it); 10's `YOUNGER1900_CHECK_INVALID`
slice and 11 done; 7, 8 whenever round-trip fixtures are next touched (no UI, no urgency); 10's `optionalDateType`
half needs no further work (no a12-studio equivalent exists to gate); its `interpretationOfYear` half needs
`ModelInfo.baseYear` added first (small, separate task) before it's worth attempting.

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

## Type Definition Model

*Gap review 2026-09-27, against SME's dedicated `client/src/modules/typeDefinitionModel/` module, the type-definition
editing code it shares with a plain Document Model's own "Type Definitions" tab, and the BA doc's Type Definition
Model section.*

**What it actually is.** A Type Definition Model (TDM) is not a separate document format on either side — it's a
plain Document Model whose header carries an annotation marking it TD-only (SME: `{"name": "tdonly", "value":
"true"}`, read by `hasTypeDefinitionModelRootAnnotation`) and whose `modelRoot`/groups stay empty; only
`content.typeDefinitions` is used. SME's module is thin: an explorer entry, a module wrapper that reuses the entire
Document Model editor frame minus the Model Tree tab, and one genuinely TDM-specific behavior described in the next
paragraph. Server-side, TDM resolution is a thin DTO (`ResolveTypeDefinitionDTO.kt`) around the same kernel-backed
expansion regular Document Models use — nothing TDM-specific happens on the backend beyond that. a12-studio mirrors
this closely: `TypeDefinitionTableController` (`a12-studio-ui/.../editors/typedefinitionmodel/`) is a single, shared,
well-developed component (transitive Include/Import resolution via `TransitiveTypeDefinitions`, "included"/
"included-imported" row styling, locale-compatible/cycle-safe Import candidates, deferred-save mode) used both by
`TypeDefintionModelEditorController` (the standalone TDM tab) and `TypeDefinitionSettingsDialog` (a regular Document
Model's own Type Definitions dialog) — exactly SME's shared-component design.

**Fixed 2026-09-27 (gaps 1-3):** `TypeDefinitionTableController.updateAddImportAvailability()` now has a `model
instanceof TypeDefinitionModel` escape hatch that never disables Add or Import for a TDM itself (gap 1), pinned by
`TypeDefinitionTableControllerTest` (new - a12-studio-ui had no test for this controller before). `TypeDefinitionPanelController.collectAvailableTypeDefinitionLabels()` now takes a `multiSelectParent` flag and,
when set, returns only the model's own local type definitions — no transitively included/imported ones — matching
the BA doc's "for consistency reasons it is not possible to use imported Type Definitions or Type Definitions from
includes"; `MultiSelectGroupValidator` gained the matching `checkTypeDefinitionIsLocal` check (gap 2, pinned by
`multiSelectGroupValidatorReportsNonLocalTypeDefinition` in `DocumentModelValidatorsTest`). `BasicConsistencyValidator`'s
type-definition pass now also checks blank id/name, matching every other element kind (gap 3, pinned by
`basicConsistencyValidatorReportsBlankNameOnATypeDefinition`). **Gap 4 re-checked, left as is:** `MissingReferenceValidator.getDuplicateNamedTypeDefinitions` still only compares a model's own local type
definitions against each other; no SME source confirms a rule for the same-named local-vs-imported case in a TDM's
now-real combined set, so this stays unchanged per the "verify rather than assume" guidance rather than guessing at
new behavior.

**Gap 1, the real architectural gap: a TDM can't hold local and imported type definitions at the same time.**
`TypeDefinitionTableController.updateAddImportAvailability()` disables Add once any Import exists and disables
Import once any local type definition exists, unconditionally for every `DocumentModel` the table serves — including
a `TypeDefinitionModel` itself. SME's `selectTypeDefinitionMode()` (`typeDefOverviewWithImport.tsx`) special-cases
exactly this: it returns `"combined"` whenever the model being edited *is* a TDM
(`hasTypeDefinitionModelRootAnnotation`), and `AddButton`/`ImportButton` only disable on `"import"`/`"local"`
respectively, never on `"combined"` — because this component is shared verbatim between the plain-DM editor and the
TDM editor. So a **plain Document Model** must pick one mode (local-only or imported-only, which a12-studio already
gets right), but a **TDM itself** may own local type definitions *and* import other TDMs at once, working as a hub
that re-exports imported ones alongside its own — exactly the BA doc's stated purpose ("maintain all Type
Definitions that are needed in the different Document Models of a project in a central place... It is also possible
to import another Type Definition Model"). Fix: a `model instanceof TypeDefinitionModel` escape hatch in
`updateAddImportAvailability()` that never disables either button for a TDM. No fixture on disk exercises this
combination today; add one alongside the fix.

**Gap 2: a multi-select group's enumeration value field can be pointed at an imported/included type definition,
which SME explicitly forbids.** The BA doc: *"For consistency reasons it is not possible to use imported Type
Definitions or Type Definitions from includes"* for a multi-select's value field. a12-studio's
`TypeDefinitionPanelController.collectAvailableTypeDefinitionLabels()` offers the model's own type definitions plus
every transitively included/imported one with no `isMultiSelectParent()` special case, and `MultiSelectGroupValidator`
only checks that the effective type resolves to Enumeration/String, not that a referenced type definition is local.
Fix: filter the combo to local-only type definitions when `isMultiSelectParent()`, and add the matching validator
check.

**Gap 3: `BasicConsistencyValidator`'s type-definition pass never checks blank id/name, only Enumeration duplicate
values.** Every other element kind gets a blank-id/blank-name check through `allElements()`; type definitions aren't
reachable through that walk and their dedicated second pass only calls the Enumeration-duplicate check. Reachable
today only via a hand-edited/imported file (the UI's own Add/rename paths already validate non-blank) — one-line
addition once touched.

**Gap 4 (re-check once Gap 1 lands, lower confidence):** `MissingReferenceValidator.getDuplicateNamedTypeDefinitions`
only compares a model's own, non-included, non-imported type definitions against each other — correctly matching
SME's current, narrower scope. Once Gap 1 makes a TDM's local-plus-imported set real, a same-named local and imported
type definition would sit unflagged side by side in the same combined table; no SME source found that confirms a rule
for this specific combined case, so verify rather than assume when implementing Gap 1.

**Fixed 2026-09-28: per-row "invalid" indicator.** SME's `TypedefOverview` marks an invalid row via a dedicated
`invalidTypeDefs`-driven icon column (`CustomTableBodyCell`, `DMValidation.select("invalidTypeDefs", ...)`);
a12-studio previously only surfaced an error once a row was opened. `TypeDefinitionTableController` now computes
`errorMessagesByTypeDefinitionId` (this model's own `Studio.getValidationService().validate(model)`, plus - for
every distinct owning model among the included/imported rows - that model's own `validate(...)` too, so a problem
on an inherited type definition, e.g. a duplicate name inside the model that actually owns it, still marks the
row here) and renders it via a `nameColumn` cell factory: a red name plus a tooltip listing the actual message(s),
matching this codebase's existing per-row validation convention (`ElementNameTreeCell`/`FormModelTreeCell`) rather
than introducing a separate icon column. Recomputed wherever the table already refreshes itself (`load()`/Add/
Delete/Import/Delete Import), so no new refresh trigger was needed. Pinned by two new cases in
`TypeDefinitionTableControllerTest` (an own type definition with an invalid `StringType` config, and an
imported type definition invalid in its owning model).
*Renamed 2026-10-01 to `CommonFieldDefinitions_TDM.json` (id, `modelInfo.name` and the three referencing fixtures
updated).* `testing/workspaces/advanced_new/models/CommonFieldDefinitions_Td.json` used a non-conforming `_Td` suffix instead
of the official `_TDM` (the a12 naming-convention doc and `model-versions.json` both say `_TDM`; the e-commerce
fixture `CommonTypes_TDM.json` is correctly named) — invisible today because that workspace disables suffix
enforcement, and not worth a standalone rename since three other fixtures (`PersonEmployee_Ad.json`, `Team_Dc.json`,
`PersonSkills_LinkFields_Base_Dc.json`) reference it by id.

**Checked, not a gap.** Every field-type-config validator (`EnumerationValuesValidator`, `StringTypeConfigValidator`,
`NumberTypeConfigValidator`, `EnumerationTypeConfigValidator`, `CustomFieldTypeConfigValidator`,
`DateFormatConfigValidator`, `NumberFieldValueLimitValidator`, `StringPatternErrorMessageValidator`) already walks a
model's own `content.typeDefinitions` in addition to `allElements()`. Type Definition name uniqueness (own list,
model-wide) matches SME's `TYPE_DEF_NAME_DUPLICATED` scope exactly. Missing/invalid type-definition references
(`MissingReferenceValidator`'s `NOT_SPECIFIED`/`DOES_NOT_EXIST`) and broken transitive import chains
(`TransitiveTypeDefinitions.hasUnresolvedImportChain`) both correctly mirror SME's kernel rules. Import candidate
filtering (locale-direction, already-imported exclusion, cycle prevention) matches `importTypeDefsView.tsx` exactly.
**Delete/Remove-Import confirmation dialogs are stricter than SME, not weaker**: SME's own BA doc says explicitly
that neither action has any check or confirmation dialog at all ("There is no check if a Type Definition is used in
a field definition before deleting it and thus no confirmation dialogue", and the identical statement for Remove
Import) — a12-studio shows a confirmation dialog with an explanatory message before either, so it's already ahead
here, not behind. `IncludeTypeDefinitionModeValidator`/`TypeDefinitionMode` (Include-compatibility between two
*regular* Document Models) is orthogonal to Gap 1 — a TDM is never Included, only Imported via a header
`ModelReference`. The "TD" tree badge for a `TypeDefFieldType` field, the round-trip shape of `TypeDefinition`
(`id`/`name`/`fieldType` only — no additional persisted fields found anywhere), the `_TDM` suffix convention, and the
Import picker offering only `TypeDefinitionModel` instances (never plain/Additive Document Models) are all already
correct.

**Suggested order.** 1 (the real gap, add a combined-mode fixture) → 2 (small, BA-doc-backed) → 3 (one line, land
with 2) → 4 (re-check once 1 lands) → the invalid-indicator column (done 2026-09-28) → the `_Td` suffix rename
whenever that fixture is next touched anyway.

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

### Gap review (2026-09-27)

Re-checked against current SME `combinationModel` source (`client/src/modules/combinationModel/`, its meta-model `DomainCombination.json`/editor Form Model, and the BA doc's `04_editor.adoc`) and current a12-studio source.

**Fixed 2026-09-27 (gaps 1-4, 7):** `CombinedDocumentModelEditorController.documentModelOptions()` (Base Model picker) now calls `ProjectDocumentModels.getOtherDocumentModelsWithCombinations()`; `CombinationStepsPanelController`'s single `documentModelIds()` is now two methods - `decorationModelIds()` (same combinations-inclusive helper, feeds the Base Model and Decoration pickers) and `additiveModelIds()` (filtered to `AdditiveDocumentModel` instances only, feeds the Addition step's picker) - `CombinationStepDialogController.initDialog`/`Dialogs.showCombinationStepForAdd`/`showCombinationStepForEdit` all took the extra parameter (gaps 1, 2). New `CombinationInvalidReferenceValidator` ports the 4 "Invalid Reference" rules directly: a dangling base-model reference reports under `content/baseModelId`, and a dangling additive/selection/decoration reference reports under the offending step's own `content/combinationSteps/<i>` - so `CombinationStepsPanelController.refreshValidation()`'s existing prefix filter now picks them up on the step row itself, with the generic `HeaderModelReferenceValidator` staying as the overall-model backstop it already was (gaps 3, 4). `HeaderRolesValidator` is now wired into `CombinationModelValidationService` too (gap 7, batched with Relationship Model's identical gap - see above). Pinned by new tests in `CombinationValidatorsTest`. **Gap 5 (Preview tab, Addition-only slice) and gap 6 (99-step cap) both done as of 2026-09-30** - see their table rows below.

| # | Gap in Studio | What SME does | Effect today |
|---|---|---|---|
| 1 | **The Base Model picker, and the Additive/Decoration model pickers on a Combination Step, only offer plain Document Models — a Combination Model can't be nested inside another one.** `CombinedDocumentModelEditorController.documentModelOptions()` and `CombinationStepsPanelController.documentModelIds()` both call the plain `ProjectDocumentModels.getOtherDocumentModels(projectItem)` | SME's `04_editor.adoc` is explicit: *"Base Document Model:: Select a **(Combined or Transformed)** Document Model from the dropdown list"* - `combModelReferenceProviders.ts`'s `getBaseModelReferenceCalculator` resolves against `resolveStandaloneDocumentModelTypes()`, which includes Combination Model. Same underlying reference calculator backs the Addition/Decoration step pickers | A Combination Model cannot be layered on top of another Combination Model at all - the picker never lists one, even though a12-studio already has the exact helper this needs (`ProjectDocumentModels.getOtherDocumentModelsWithCombinations`, already used by Form Model, Content Model and `ModelSettingsDialog` for this same "treat a Combination Model as document-model-shaped" purpose) sitting unused here |
| 2 | **The Additive Model picker on an Addition step is not filtered to Additive Document Models.** `CombinationStepsPanelController.documentModelIds()` offers every plain Document Model | SME's `additiveModelReferenceProvider` resolves specifically against `additiveDocumentModelProto.type` - an Addition step's Document Model dropdown only ever offers real Additive Document Models | Any Document Model, additive or not, can be picked as an Addition step's target through the UI; a12-studio already has `AdditiveDocumentModel`/`AdditiveDocumentModelResolver` (used elsewhere for the Document Model editor's own "Additive Elements Only" preview) that could power the same filter here but doesn't |
| 3 | **A dangling additive/selection/decoration/base-model reference is never shown inline on the offending Combination Step row.** `CombinationStepsPanelController.refreshValidation()` only displays an error whose `elementId` starts with `content/combinationSteps/` (`STEP_ELEMENT_ID_PREFIX`); the only check covering these references is the generic `HeaderModelReferenceValidator`, whose flat `elementId` is always `header/modelReferences` - it never matches that prefix, so it's caught in overall model validation but never surfaces on the step itself | `DomainCombination.json` has 4 dedicated "Invalid Reference" rules not mentioned in this doc before now - `A12_ADDITIVE_MODEL_ID_INVALID_REFERENCE`/`A12_SELECTION_MODEL_ID_INVALID_REFERENCE`/`A12_DECORATION_MODEL_ID_INVALID_REFERENCE` (`errorEntityRelPath: "../dmId"`/`"../smId"`) and `A12_BASE_MODEL_ID_INVALID_REFERENCE` - each shown right on the offending field | A user editing a Combination Step sees no error on the row itself when its Document/Selection Model reference is broken - only a generic, differently-located validation error elsewhere, unlike SME which points at the exact field |
| 4 | *Precision correction, not a new gap:* **the "7 structural Rules ported" framing undercounts SME's actual rule set.** `DomainCombination.json` declares 11 rules: the 7 already ported (3 `*_MISSING` + 3 `*_NOT_ALLOWED` + `ADDITIVE_MODEL_DUPLICATE`) **plus** the 4 invalid-reference rules in gap 3 (not ported as dedicated validators - misplaced under the generic header validator instead) - `BaseModelMustBeValid`/`AdmShouldNotCauseLoop` remain genuinely unused custom-condition-only rules on SME's own side, so that part of the existing text is still correct | — | Documentation precision only; folded into gap 3's fix rather than tracked separately |
| 5 | *Partly fixed 2026-09-30.* ~~No user-facing Preview of the expanded/merged Document Model.~~ SME has a dedicated read-only editor screen for this (`editor/preview/combinationPreviewView.tsx`, a `ConfigurableDmTree` over the real kernel-expanded result) | a12-studio's only expansion logic (`CombinedDocumentModelElements.resolveForFieldReferences`, deliberately conservative - merges Addition steps only, skips Selection/Decoration) is used silently by Overview/Form field-reference pickers; there is no visible tree/preview built on it anywhere | New "Preview" tab (`CombinationPreviewPanelController`) shows a read-only tree built on the same existing helper - the Addition-only-merge slice this row called for, not the kernel-backed real one. Full parity (Selection/Decoration steps, real semantic join) still needs the kernel dependency this doc already tracks as blocked (Open Decision #1) |
| 6 | *Fixed 2026-09-29.* **No cap on the number of Combination Steps.** SME's `CombinationSteps` group declares `"repeatability": 99`; a12-studio's `combinationSteps` is an unbounded list with no size check | — | New `CombinationStepsMaxCountValidator` (registered in `CombinationModelValidationService`) now reports an error once a model has more than 99 Combination Steps, pinned by `CombinationValidatorsTest.stepsMaxCountValidatorReportsMoreThan99Steps`/`stepsMaxCountValidatorAllows99Steps` |
| 7 | **`HeaderRolesValidator` is not registered in `CombinationModelValidationService`** | Same shared kernel `ModelHeader` roles rules every other model type gets | Same cross-cutting gap already flagged for Application/Master Detail/Relationship/Query Model - fold into the same follow-up |

**Not gaps (checked).** `CombModelReferenceHelper.modelCausesOrHasLoop`'s exact walk semantics (already documented above) were re-read directly against current `combinationHelper.ts` and still match with no drift. SME's real semantic validation (`combModelValidation.ts::validateCombinationModel`) turns out to be broader than "DM expansion + SMT rule-contradiction pass" suggests - it expands via the kernel into a real Document Model and then runs the *entire* Document Model validator suite against it (every field/group/type-definition rule, not just contradiction-solving); doesn't change the kernel-blocked status, just the precision of what's blocked. SME's `SELECTION_MODEL_MISSING` rule (requiring a Selection Model reference for Selection **and both** Decoration step types) is already correctly implemented three-ways by `CombinationSelectionModelMissingValidator`. SME's `invalidCombinationModal.tsx` "Save As Invalid" escape hatch is tied to SME's own save-blocking-on-validation-error UX, which a12-studio's architecture never has anywhere (badges only, no editor blocks saving) - a pre-existing, consistent, project-wide difference, not specific to this model type. Loop-avoidance in the Base Model picker (SME filters loop-causing candidates at picker time; a12-studio reports the loop after selection) was already tracked above and is unchanged.

**Not investigated (flag for follow-up if ever prioritized).** `diff/combiCalculateDiff.ts` (purpose unclear, likely a version-diff viewer, not read); `addModel/addCombinationModelForm.tsx` (an inline "or create a new model" convenience on the pickers - low-priority UX parity); `expansion/combModelExpansion.ts`'s exact algorithm vs. `CombinedDocumentModelElements`'s approximation (known to differ in scope - Selection/Decoration skipped - but not diffed line-by-line).

**Suggested order.**
1. **Gaps 1, 2** (picker type-filtering): reuse `ProjectDocumentModels.getOtherDocumentModelsWithCombinations` for the Base Model/Additive/Decoration pickers (gap 1), and filter the Additive step picker specifically to `AdditiveDocumentModel`s via the existing resolver (gap 2) - both are call-site swaps to already-existing helpers, no new mechanism.
2. **Gap 3** (inline invalid-reference errors): add 4 small validators (or one parametrized one) emitting `content/combinationSteps/<i>` / `content/baseModelId`-prefixed errors for a dangling additive/selection/decoration/base-model reference, so `CombinationStepsPanelController.refreshValidation()`'s existing prefix filter picks them up - the generic `HeaderModelReferenceValidator` stays as a backstop, this just gives the step row its own inline error too. Folds in gap 4's precision fix automatically.
3. **Gap 7** (`HeaderRolesValidator`): batch into the cross-cutting follow-up already tracked for the other model types.
4. **Gap 5** (Preview tab) and **gap 6** (99-step cap): lower priority, opportunistic.

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
not in the editor: the Model Settings dialog should show **just** the Roles panel for this model type (every other
panel, including General information/name/description, hidden and left unbound so none can disable Save) — SME's real
dialog (`TypesettingModelContainer.tsx` in the npm package) has exactly three sections (Prevent Line Break Rules,
Orphan/Widow, Roles) and no name/description UI at all, and its own header deserializer is configured with
`hasDescription: false` — a TSM's header schema has no description field to edit in the first place. The New Model
dialog neither asks for nor writes locales.

**Known regression, fixed 2026-09-27 (see `TODO.md`'s "Open issues"):** a same-day follow-up commit had regressed
`ModelSettingsDialog.java`'s roles-only guard (renamed `rolesOnly` to `generalAndRolesOnly`, moved the two
`modelSettingsNameController.setModel/.focusNameField()` calls outside the guard and dropped its
`setVisible(false)` call), so the dialog showed Name/Description for a Typesetting Model too. Both calls are
back inside `if (!generalAndRolesOnly)`/`if (generalAndRolesOnly)` respectively;
`TypesettingModelEditorTest.theModelSettingsDialogOffersNothingButTheRoles` is green again (re-verified
2026-09-29).

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
matching SME (re-verified 2026-09-27 against the full npm v3.2.3 and v4.0.1 UI source — no such component exists in
either version; the two versions are otherwise functionally identical, differing only in an internal
`internal`→`a12internal` package rename). Not checked against the real print engine: the regexes are only compared
with SME's editor's output. Re-verified 2026-09-27, nothing else missing: rule classification/escaping, per-rule
value validators (character set down to the exact punctuation list), duplicate detection, orphan/widow range and
default, delete-confirmation flow, `HeaderRolesValidator`'s rules, `ModelSuffixValidator`/`model-versions.json`, and
`NewModelFactory.buildTypesettingModel()`'s defaults all match SME's real package field-for-field — the one open item
is the Model Settings dialog regression above.

---

## Form Model

*Analyzed 2026-09-06 (the previous version of this section, "Form Model — not started", was written before this
module existed in a12-studio and is factually wrong — do not trust anything from before this date about Form Model).
Re-reviewed 2026-09-27 against SME's meta-model `Rule`s directly (not just fixtures/`.tsx`) — see "Form Model: gap
review (2026-09-27)" near the end of this section for the 9 gaps that surfaced.*

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
  files and sample documents are not offered, edits made in the preview are not saved) - re-checked 2026-09-30:
  a12-studio has no existing code reading either a project's `.theme` files or its `data/documents/*.json` sample
  instances, and no real `.theme` fixture exists anywhere in `testing/workspaces` to verify a wire format
  against, so this is left open rather than guessed at (see TODO.md); Form Models bound to a Relationship UI
  Model's relationships render without them (as in SME, the preview only knows the Document Model).
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
  through `ContentTableColumns`, which keeps head/body/foot cells index-aligned like SME's middleware. The Columns panel (2026-09-26) is a `module-row` list like Modules/Tree columns (`RowFactory`: drag handle, move up/down, edit, delete; "Add column" appends before the right-pinned ones; insert above/below in the row's context menu): one label row per column (head-cell text or `<id>`), moves and drops only among equally pinned columns as in SME (`RowFactory` got a `canDrop` predicate and per-direction move enabling for that). The edit dialog (`TableColumnDialogController`) holds everything SME shows inline or in its expandable row: pin direction (re-sorts like SME), action column, default width, min width (only with "Enable resizing"), fixed width and the horizontal/vertical alignment of general/head/body/foot; widths follow SME's rule (blank clears, otherwise non-negative, rounded down to one decimal) and an invalid one blocks OK with a message naming the field. New children and
  retyped elements get SME's default props (`ContentElementDefaults`, additive). **Deliberate deviations:** the Grid switch
  SME labels "Gutter" (it stores `noGutter`, so switching it on removes the gutter) is labeled "No gutter"; the type stays
  editable (SME fixes it when the element is created); a "Raw properties (JSON)" panel stays for what has no typed panel,
  notably the rich formatting of Paragraph/Heading (SME edits it inline on the canvas; the words themselves have a plain
  text field in the Content panel, `LexicalText`/`LexicalTextRow`: one line per Lexical block, formatting of the runs
  around an edit kept, `html` regenerated, read-only with a hint when the tree holds links or field references);
  "Group Reference", "Field"
  and "Field reference" are plain text fields (SME offers a picker over the Document Model); "Screen Reader Column" is a
  combo box over the table's columns (`ColumnRow`) with SME's hint as a tooltip. **Still missing:** the form-content elements (Text Line, Checkbox, ... with elementId, localized
  label/hint/placeholder, annotations), the Conditional element's condition editor, and the pickers above.
- **Content Model "Add child" (2026-09-26) - which element may go where.** SME's insert panel is not a free choice: it
  lists only the element types whose parent rule accepts the target and whose result the target's child rule accepts
  (`ModelStateSelector.insertableNodeTypes` in `contentengine-editor`, rules declared per element in each
  `*.module.tsx`: `parentRules`/`childRules` as anyOf/noneOf/sequence with min/max instances and a nested rule for a
  child's own children). The studio ports that as `ContentElementLibrary` (52 element types: 40 default Content
  Engine elements + the 12 form elements of `formengine-content-elements-editor`, with SME's labels, categories
  "Layout / Content / General / Form Elements" and `orderingConfigurations`), `ContentRuleEvaluator` (the rule
  engine, including the sequence pairing and the instance limits) and `ContentInsertion` (simulates the child list
  after the insert and evaluates it; Repeatable Group and Conditional are looked through, except in the table body,
  as in SME; supports child/above/below). The "Add child" toolbar button / context-menu entry opens the "Add Element"
  dialog (`editors/contentmodel/dialogs`, tiles per category, double click or Add confirms) with exactly those types
  and is disabled when the list is empty (e.g. on a Paragraph or a complete Table). The new element is built by
  `ContentElementFactory` like SME's `propertiesCreator`s: `Type-<uid>` id, default props, and the parts a type is
  useless without (Table with head/body/foot + 5 sample columns/rows, Grid with a row, Expandable with both states,
  lists with three items, Button Group Container with two groups, ...); a table row gets one cell per column of its
  table. Save/Cancel/Commit/Add Row/Delete Row are never offered (SME excludes them too). **Not ported:** SME's paste
  rules (cut/copy/paste/drag still accept any target), "Insert above/below" (the rule engine supports both positions,
  only the menu entries are missing), the experimental-elements switch (SME ships it off), plugin-contributed
  element libraries (an element of an unknown type takes no children).

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

### Form Model: gap review (2026-09-27)

Full field-by-field and validator-by-validator re-review, prompted by the same treatment already done for
Document/Overview/Application/Tree Model. Method: read SME's Form-Model-editor meta-model ground truth
(`client/resources/models/formModel/FormModelFrame.json` — the graph-level `Rule`s it declares — plus the shared
mixins it composes, `I_ScreenElementBase.json`, `I_Label.json`, `I_FieldBasedInput.json`,
`I_RepeatOverviewColumnBase.json`, `I_ButtonStyling.json`, `FieldConfigurationEntry.json`), the 21 hand-written
`FMCustomConditions` (`client/src/modules/formModel/validation/customConditions/index.ts`), and the BA
documentation (`docs/modules/formModel/asciidoc/chapter02/02.02_form-model-view.adoc`,
`chapter03/03.03_common_editor_features.adoc`, `chapter03/03.09_conditionally_hidden.adoc`,
`chapter04/04_refactoring.adoc`) — against a12-studio's `FormModelValidationService` (30 form validators),
`FormModelContent`/`FieldConfigEntry`/`LocalizedText` and the `modelsettings`/`formtree` editor packages. Unlike
the 2026-09-06 build (which worked mostly from fixtures and `.tsx` reads), this pass starts from the meta-model's
own declared `Rule`s, which is how gaps 1-6 below surfaced — none of them have a real fixture on disk exercising
them yet, matching the pattern of SME's own rule set (most of these conditions never fire on the sample
workspaces either).

**Status (2026-09-27): gaps 1-7 closed; 8 (only when needed) and 9 (pure UX) still open.** `HeaderRolesValidator`
is now registered in `FormModelValidationService` (gap 1); `FormAmountSuffixFieldRefValidator`,
`FormPlaceholderExpositionConflictValidator`, `FormExternalEnumerationExpositionValidator`, a duplicate-name
check added to `FormStyleReferenceValidator`, and (briefly) `FormReservedAnnotationNameValidator` close gaps 2-6;
new `FormLabelExpressionValidator` (backed by a new `FormLabelExpressions` reflective-walk helper in
`a12-studio-models`, mirroring `FormStyleReferences`'s own reflective walk over `List<Style>` fields but for
`LocalizedText`-typed fields instead) closes gap 7, reusing the `ExpressionLang` syntax checker built for
Overview expression columns - no new grammar work needed, confirming the "single well-contained validator"
framing below. Pinned by 9 new tests in `FormValidatorsTest`.

**`FormReservedAnnotationNameValidator` removed (2026-09-28):** it unconditionally flagged the header
annotation `bindingConfiguration` as an error whenever present, on the assumption (per SME's own
`editor_annotationNameMustNotBeReserved` rule) that nothing legitimate would ever produce it. That's false in
practice: `bindingConfiguration` is the real payload the relationship-binding feature writes for a Form
Model's bound sections (see the `OverviewBindingPurpose` note above) and is present as valid, correct data in
`testing/workspaces/basic/models/{Company_FM,Person_FM}.json` and multiple `e-commerce` fixtures - the
validator was reporting every one of them as broken. `AnnotationsPanelController` already does the actual
protection SME's rule is for (hides `bindingConfiguration` from the Annotations panel so a user can't type or
rename an annotation to it), so the model-level validator was redundant on top of being wrong; deleted rather
than rescoped, since a model-level check can't distinguish "system-written" from "hand-typed" once the
annotation is just sitting in `header.annotations`.

**Checked, not a gap** (candidates that turned out to already be built): the "General Detached/Inline Repeat
Settings" and "Rule Confirmation Settings" model-settings panels the BA doc describes all exist
(`GeneralDetachedRepeatSettingsPanelController`, `GeneralInlineRepeatSettingsPanelController`,
`RuleConfirmationSettingsPanelController`); `FormModelContent.detachedRepeatCommitButtonEnablement`/
`inlineRepeatReadonlyPresentation`/`disableRuleConfirmation`/`hideConfirmationSummary` are all wired to a panel.
`FormButtonScreenReferenceValidator` already checks all four button boxes (`subHeaderBox`/`footerBox` ×
major/minor, model-level and per-screen) — actually broader than SME's three separate `Rule`s (SME has no
`subHeaderBox/minorButtons` target check at all; a12-studio's does). Repeat Default Button Labels
(`Defaults.buttonLabels`, SME's `I_SectionDefaultRepeatButtonLabels-form.json` list) are modeled and wired
(`FormModelEditorController.loadRepeatDefaultButtonLabels`). `AmountSuffix` itself (static/dynamic, `fieldRef`) is
fully modeled with a settings panel — only its reference validator is missing (gap 2 below).

| # | Gap in Studio | What SME does | Effect today |
|---|---|---|---|
| **Cross-cutting (cheapest, land first)** | | | |
| 1 | **`HeaderRolesValidator` is not registered in `FormModelValidationService`** (same cross-cutting gap already flagged for Application Model in TODO.md) | SME's `roles` header annotation gets `mustHaveValidRoleValues`/`rolesNotUnique`/`shouldNotHaveEmptyRoles`/`roleIsNotPartOfRolesModel`/etc. on every meta-model including `FormModelFrame.json` (`UniqueRoleCustomCondition` is the Form-specific wiring of the shared rule) | A Form Model's `roles` annotation (duplicate roles, invalid characters, roles absent from the workspace's `access-rights.yaml`) is completely unchecked - one line to close (`FormModelValidationService` already imports the sibling validators; add `new HeaderRolesValidator()` like `TreeModelValidationService`/`ContentModelValidationService` do) |
| **Missing validators (cheap, same shape as existing ones, land together)** | | | |
| 2 | **`AmountSuffix.fieldRef` has no reference validator** | `amountSuffixFieldRefMustBeValidReference`: the dynamic Amount Suffix's field reference must resolve to a real, non-repeatable Enumeration field | A dynamic Amount Suffix pointing at a deleted/renamed/wrong-type field is silently accepted |
| 3 | **`FieldConfigEntry.placeholder` + `exposition` conflict is unchecked** | `graph_mustNotHavePlaceholderWhenFullOrInlineExposition`: a placeholder is meaningless (and rejected) when `exposition` is `FULL` or `INLINE` | A Control can have both set at once with no warning, even though the Form Engine won't show the placeholder |
| 4 | **`externalEnumeration` + `exposition` conflict is unchecked** | `graph_externalEnumerationExpositionMustBeValid`: once `externalEnumeration.src` is set, `exposition` must be one of `FULL`/`INLINE`/`COMPACT`/`AUTOCOMPLETE` | An External Enumeration field can be left on an incompatible exposition (e.g. the default) with no warning |
| 5 | **Model-level `styles` list allows duplicate names** | `styleNamesNotUnique` (`RepetitionNotUnique(content/styles/name)`) | `FormStyleReferenceValidator` already checks "no name"/"undefined reference" but not "two style entries with the same name" - the second silently shadows the first wherever it's picked from the combo |
| 6 | **The header annotation name `bindingConfiguration` is not reserved** | `editor_annotationNameMustNotBeReserved`: SME refuses to let you hand-author an annotation with that name because the Overview Model's binding-purpose resolution reads it as structured JSON (see the `OverviewBindingPurpose`/`bindingConfiguration` note in TODO.md's Overview Model section) | A user typing a `bindingConfiguration` annotation by hand in the raw Annotations panel would silently corrupt the binding-overview resolution for any Overview Model reading it, with no warning at authoring time |
| **Missing feature (broad blast radius, but a single well-contained validator)** | | | |
| 7 | **Label-as-Expression has zero validation.** `LocalizedText`/`ExpressionText` (`type: "Multilingual" \| "Expression"`) is already modeled and already editable in the UI for every labeled element - `ControlLabelPanelController`, `RepeatLabelPanelController`, `FormNodeEditorScreenPanelController`/`...SectionPanelController`/`...RowPanelController`, button styling, `FieldConfigEntry`, `RepeatOverviewColumnBase` all reference `LocalizedTextType`/`ExpressionText` - but no validator ever checks the `expressionText` it carries | SME's `I_Label.json` mixin (composed into `I_ScreenElementBase`, `I_FieldBasedInput`, `I_RepeatOverviewColumnBase`, `I_ButtonStyling`, `FieldConfigurationEntry`, `ExpressionCell`, `Row`, `Screen` - i.e. nearly every named element) has two rules: `expressionMustNotBeEmpty` and `expressionMustBeValid` (`ExpressionValidationCustomCondition`, the *same* text-templating "Expression" language a12-studio already built `ExpressionLang.g4` for to check Overview expression columns - see the Overview Model gap review, gap 8) | An element whose Label Type is switched to "Expression" can be left with a blank or syntactically broken expression and shows no error anywhere; a new `FormLabelExpressionValidator` walking every `LocalizedText` field on the tree and reusing the existing `ExpressionLang` syntax checker closes this in one pass across every element type at once |
| **Missing UI (fields exist for round-trip, no way to author them yet)** | | | |
| 8 | **"Preprocessing Settings" has no editor panel at all.** `FormModelContent.openNewDocumentPreProcessing`/`openExistingDocumentPreProcessing` exist on the Java model (presumably added for round-trip) but nothing in `a12-studio-ui` ever reads or writes them - no panel, no FXML, no `ModelSettingsDialog` wiring | The BA doc's "Preprocessing Settings" (`chapter02/02.02_form-model-view.adoc`) is a real Model-Settings sub-panel: two 3-way enums (`no preprocessing` / `evaluate computations` / `evaluate computations and dependencies`) controlling whether computations/dependencies run when the Form Engine opens a new vs. an existing document, plus a CDM-specific default note | The setting can never be authored in the studio at all; a value already present in an imported file round-trips silently but is invisible and unreachable in the UI. No real fixture on either side currently sets a non-default value, so this is lower priority than 1-7 - add the panel (mirroring `GeneralDetachedRepeatSettingsPanelController`'s pattern) whenever a fixture needs it, or proactively since the shape is already fully known from the meta-model |
| **Missing editor convenience (small, purely UX)** | | | |
| 9 | *Fixed 2026-09-29.* **No "Copy Hide Condition" / "Paste Hide Condition" context actions.** SME has dedicated context-menu entries and keyboard shortcuts (`Ctrl+H`/`Ctrl+B`) to copy a Hide Condition (master field + selected cases) from one element and paste it onto another, with a replace-confirmation and no compatibility check on paste (`docs/modules/formModel/asciidoc/chapter03/03.09_conditionally_hidden.adoc`, "Copying the Hide Condition") | `HideConditionPanelController` gained two icon buttons instead of tree context-menu items/shortcuts - a12-studio already funnels every node type's Hide Condition editing through this one shared panel (SME can also do it purely from the tree, without opening an editor), so buttons on the panel are the architecture-fitting translation rather than new tree/keyboard-shortcut plumbing. A static `copiedHideConditionSource` (the source panel's own getter) re-resolves the source's *current* value at paste time exactly like SME does, clones it so the target gets an independent copy, and shows the same overwrite confirmation when the target already has one. Pinned by 5 `HideConditionPanelControllerTest` cases |

**Also confirmed still open, not re-litigated here:** the interactive "Commit / Edit / Delete" refactoring dialog
SME shows when deleting a Screen/Control that's referenced elsewhere (`chapter04/04_refactoring.adoc`, "Example:
Deleting a Screen") is the same cross-cutting missing feature already tracked as Application Model gap 6 in
TODO.md ("no within-model refactoring... dialog... for any model type") - Form Model's `FormButtonScreenReferenceValidator`/
`FormDependentControlContextValidator`/etc. only catch the resulting dangling reference after the fact, as a
validation error, not interactively at delete time. Not re-listed as a separate numbered gap since the fix belongs
with that cross-model-type follow-up, not a Form-Model-only patch.

**Suggested order.** 1 first (one line, closes a whole rule family). 2-6 next - all cheap, single-purpose
validators following the exact shape of existing ones (`FormDatePickerConfigValidator`,
`FormBindingComponentReferenceValidator`) - land together as one pass. 7 is the highest-value item despite not
being first: broad blast radius (every labeled element in the model) but genuinely a single new validator class
reusing the already-built `ExpressionLang` checker, no new grammar or UI work needed. 8 only when a fixture or a
real workflow needs to author it (the shape is fully known from the meta-model, so there's no discovery risk left,
only build cost). 9 last - pure editor convenience, no correctness impact.

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
| Multi-target-type queries (CDM, Transformer Model as target) | Supported | **Corrected 2026-09-27 - this row was stale/wrong.** A Composed Document Model target already works today: CDM is a marker subclass of `DocumentModel` (`cdm.queryRoot` header annotation), not a separate `ModelType`, so `QuerySettingsPanelController.documentModelOptions()`'s plain `ProjectDocumentModels.getOtherDocumentModels(...)` already lists CDM files. A **Combination Model** target genuinely does not work, but not because the type is missing (it's existed since 2026-09-08) - it's a picker/resolver wiring gap, see the "Gap review" below. Transformer Model as target remains a real blocker (the type doesn't exist in a12-studio at all) |
| Reference/rename tracking | Target-DM, relationship, sort/aggregation field-path references are all first-class in SME's refactoring graph; renaming a DM/field auto-updates or flags the query (`qmModule.ts` `refactorDocument()`) | **Present** (2026-09-20; was partly present 2026-09-19) — renaming/moving an element of a Document Model rewrites every query field path evaluated against it, at any depth: root and hop `fields`, `sort` (also through a relationship), `constraint` (also below `has`), `filterDefinition` text (`[/Path]` refs, also inside `Has(...)` constraints), a hop's `linkDocumentFields` (`ProjectReferenceRefactoring` → `QueryReferenceRefactoring`, one undo step with the DM edit). Renaming a Document Model or Relationship Model *file* (id) rewrites `targetDocumentModel`, `relationshipModel` of hops/sorts/`has` operators (nested included, `ModelReferenceRewriter`) and, new, the relationship named in `Has("<rel>", ...)` inside `filterDefinition` text; SME's own `refactorDocument()` only handles `targetDocumentModel`. An unresolvable target is now an explicit error (validator + banner in the Model Tree tab + message in the Settings tab's target combo), see the Validation row. **Corrected 2026-09-27**: a role rename in a Relationship Model *is* propagated to Query Model since 2026-09-22 (`RoleRenameRefactoring`: `QuerySort.targetRole`, every `QueryLink.targetRole` recursively, every `HasOperator.targetRole` recursively through And/Or/Not, incl. `linkDocumentConstraint`) - the narrower gap that's actually still open is below. An aggregation's paths are covered since 2026-09-20 (`aggregation.group[].field`, `aggregation.aggregations[].field`, target DM only) |
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
  calls are still checked. `/__meta/...` paths are skipped like in the sibling validators. **"Did you mean ..."
  candidates (SME 2024) added 2026-09-30** (`bestMatchFieldPaths`, ported from `moduleSupport/qmm`'s
  `resolver.ts`'s `getBestMatchFields`: up to 2 indexable field paths, plain Levenshtein distance, no threshold)
  - the "the editor's autocomplete covers that" reasoning this line used to give turned out not to hold up:
  autocomplete only helps while actively typing, not for an already-saved invalid reference (a rename elsewhere,
  a hand-edited/imported file), and SME's own product has both mechanisms rather than treating one as
  redundant. See "Query Model" in TODO.md.
- **`QueryFilterDefinitionReferenceValidator`** runs it for the root and every hop (recursively) from
  `QueryModelValidationService`, error ids `content/filterDefinition` / `content/links` like the syntax validator;
  a syntactically invalid filter is left to `QueryFilterDefinitionSyntaxValidator`. **Editor**:
  `QueryDocumentNodePanelController`'s per-keystroke validator now returns the syntax error, else one line per
  unresolved reference (the checker is built once per `load`, from a snapshot of the project's models, like the
  suggestion provider's index - a DM edited in another tab is picked up on the next selection, not per keystroke).
- Tests: `QueryFilterReferenceCheckerTest` (18, 2 added 2026-09-30 for the "did you mean" suggestions). No real SME fixture has a `filterDefinition` with references
  (`grep` over `testing/` and SME's `client/resources`, `integrationTest`, `moduleSupport/qmm/resources` finds only
  the two synthetic invalid-syntax fixtures), so nothing could be swept for false positives; the messages and
  scoping rules come from SME's `checker.test.ts` and the sources above.
- Correction to the "Not ported" bullet above: a12-studio's `Element` carries generic `annotations`, so an
  `indexed = false` check *is* possible - it is done for the filter expression now. The **field projection**
  (`fields[]`, `QueryFieldReferenceValidator`) still does not reject non-indexed fields; not changed here.

**Still remaining**: type checking of a filter expression (operator/value vs. field type, enumeration values -
SME 2007-2019/2035). Confirmed real and portable (re-investigated 2026-09-30, corrected an initial wrong
"no SME recipe" finding from searching the wrong directory) but large: SME's own implementation is a genuine
type-checking compiler pass (`moduleSupport/qmm/src/internal/compiler/{checker,binder,functions}.ts` +
`base/type-system.ts`, ~4,300 lines across the compiler+utils subset, with real function-signature overload
resolution) - needs its own dedicated, multi-file pass, not a quick addition; see "Query Model" in TODO.md.
"Did you mean" suggestions are done, see above. Aggregation is unchanged from the plan below.

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
   above (element rename/move, model-id rename, explicit error for an unresolvable target; role rename covered since
   2026-09-22, see the gap review below for the one narrower piece that isn't).

### Gap review (2026-09-27)

Re-checked against current SME `queryModel` source (`client/src/modules/queryModel/`) and current a12-studio source, specifically to correct two rows above that had gone stale since they were written (2026-09-05 through 2026-09-20) and to look for anything new.

| # | Gap in Studio | What SME does | Effect today |
|---|---|---|---|
| 1 | **A Combination Model cannot be picked as a Query target, and even a hand-authored reference to one wouldn't resolve.** `QuerySettingsPanelController.documentModelOptions()` calls the plain `ProjectDocumentModels.getOtherDocumentModels(projectItem)` instead of `getOtherDocumentModelsWithCombinations(projectItem)` (the helper that adds, per Combination Model, the synthetic Document Model `CombinedDocumentModelElements.resolveForFieldReferences` builds for it — already used the same way by Form Model, Content Model and the generic `ModelSettingsDialog`). Even past the picker, `QueryElementResolution.targetDocumentModel()` resolves via plain `context.findOtherDocumentModel(...)`, so field/sort/filter validation against a Combination Model target would still fail to resolve, unlike `OverviewElementResolution`, which already follows a Combination Model stand-in the same way Overview's own field-reference resolution does | SME allows a Combination Model (and Transformer Model, N/A here) as a query target, per the (now-corrected) "Multi-target-type queries" row above | A Query Model cannot be built against a Combination Model at all today - not because the type doesn't exist (it has since 2026-09-08) but because two call sites default to the Document-Model-only helper instead of the Combination-aware one every other consumer already uses. Cheapest fix in this whole review: swap both call sites |
| 2 | *Fixed 2026-09-29.* **`RoleRenameRefactoring` rewrites the structured `constraint`/`QueryLink`/`QuerySort` tree on a role rename, but not a `Has("<relationship>", "<role>", ...)` call written as free-text `filterDefinition`.** Verified directly: `RoleRenameRefactoring.queryEdits` only walks `query.getContent().getConstraint()` (the structured mirror, kept for lossless round-tripping) and `QueryLink`/`QuerySort`, never `filterDefinition` text. Compare `ModelReferenceRewriter`'s own `Has(...)` handling (triggered by renaming the *Relationship Model file*, i.e. its id): it rewrites `has.relationshipModel()` (the call's first argument) via `idMap.get(...)`, but that rewriter maps *ids*, not role names, so it was never going to cover this either - the role argument (the call's second argument) has no rewriter on either rename path | Not a gap relative to SME (SME's own `filterDefinition`-equivalent structured `Operator` AST has no separate free-text form to go stale in the first place, so there's nothing on SME's side to diff this against) - this is a purely internal a12-studio inconsistency between its two parallel representations of the same filter | `QueryLanguageReferences.HasCall` gained `targetRoleStart`/`targetRoleStop` (the role argument's source position, alongside the existing `relationshipStart`/`relationshipStop`), and `RoleRenameRefactoring.queryEdits`/`links` now rewrite the role argument of every matching `Has(...)` call (root `filterDefinition`, every relationship hop's, nested ones inside a `constraint`/`linkConstraint`) via the same parse-tree-based approach `ModelReferenceRewriter` already used for the relationship-id argument - invalid Query Language is left alone, matching that same behavior. Pinned by 4 new `RoleRenameRefactoringTest` cases |
| 3 | **`HeaderRolesValidator` is not registered in `QueryModelValidationService`** (confirmed: 9 query validators + 7 generic header validators, no `HeaderRolesValidator`) | Same shared kernel `ModelHeader` roles rules every other model type gets | Same cross-cutting gap already flagged for Application/Master Detail/Relationship/Combined Document Model - fold into the same follow-up rather than fixing per model type |

**Not gaps / already accurate (re-confirmed).** `QueryFilterReferenceChecker` (the 2026-09-20 semantic filter work) already checks field-path existence, rejects `indexed = false` fields, and validates `Has(...)` relationship/role existence and role-reachability, root and every link recursively - broader than the "Editor features" table's own field-projection row gives it credit for in isolation; the two rows should be read together, not as a contradiction. No type/enum-value checking exists anywhere in the QL toolchain on either side of a `Has(...)`/comparison, confirmed by `QueryLanguageEmitter`'s and `QueryFilterReferenceChecker`'s own doc comments - `TODO.md`'s existing "type and enum-value checking is not done" item is still accurate and still open. `QueryFieldReferenceValidator` (the `fields[]` projection) still doesn't special-case `indexed = false` - still accurate, still open (existing TODO item). `QueryTraversalOption.options()` being unscoped ("every role in the project") is specific to the **Sort** dialog and is a documented, intentional choice there (the separate "Add Relationship" graph picker is correctly scoped to reachable relationships) - worth reading as a deliberate simplification, not an oversight.

**Suggested order.**
1. **Gap 1** (Combination Model target): swap `QuerySettingsPanelController.documentModelOptions()` to `getOtherDocumentModelsWithCombinations(...)` and give `QueryElementResolution.targetDocumentModel()` the same Combination-aware resolution `OverviewElementResolution` already has - two well-precedented call-site changes, no new mechanism to design.
2. **Gap 2** (`Has(...)` role text): extend `RoleRenameRefactoring.queryEdits` (and its Form/RelationshipUI/Overview siblings if they have the same text/structured split - Query Model is the only one with a free-text mirror today, so likely Query-only) to also rewrite the role argument of every `Has("<relationship>", "<role>", ...)` occurrence in `filterDefinition`/link `filterDefinition` text, using the same text-rewriting approach `ModelReferenceRewriter` already uses for the relationship-id argument.
3. **Gap 3** (`HeaderRolesValidator`): batch into the cross-cutting follow-up already tracked for the other model types.

**Cross-cutting `HeaderRolesValidator` follow-up, actually closed 2026-09-29.** Every prior pass in this doc/TODO.md
that declared this gap "closed for every model type" (2026-09-27) was wrong - it had only checked the model types
each individual gap review happened to cover, not grepped every `*ValidationService` class. A direct check of all
15 found `DocumentModelValidationService`, `OverviewModelValidationService`, `SelectionModelValidationService` and
`PrintModelValidationService` still missing it. All four now register `HeaderRolesValidator`, pinned by four new
tests mirroring `MasterDetailModelValidationServiceTest`'s shape
(`DocumentModelValidationServiceTest`/`OverviewModelValidationServiceTest`/`SelectionModelValidationServiceTest`/
`PrintModelValidationServiceTest`). The full `a12-studio-models-validation` suite and the
documentmodel/overviewmodel/selectionmodel/printmodel slices of `a12-studio-ui`'s suite are green - no real
fixture's `roles` annotation trips a new finding. This time verified by grepping every `*ValidationService` file
for the registration, not by trusting the prior pass's summary.

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
**Update 2026-09-26:** `model-versions.json` has every type `enabled: true` except Print (the 0.0.14 commit of 2026-09-25 switched on tree, content, mapping, structural mapping and query), so the "disabled" wording in the rows below is stale for those; what each editor still lacks is what the rows and the per-type reviews say.

| # | Module | Why this priority (2026-07-17) | a12-studio today (2026-09-20) |
|---|---|---|---|
| 1 | **structuralMappingModel** | Foundational — referenced by mappingModel and combinationModel. SME's editor: source-tree/target-tree drag&drop field mapper, resolution-strategy editor for conflicts. | **Data model only, disabled.** `ModelType.STRUCTURALMAPPING` + full content classes (`FieldMapping`, `Slice`, `ResolutionStrategy`, `GroupToClearOnFirstFill`); `StructuralMappingModelEditorController` is a 24-line stub; no validation service, no kernel dependency (the old "`kernel-md-structuralmapping-tool` present, `SmmService` scaffolding exists" was wrong). Rename/move refactoring already rewrites its `*FullName` paths. |
| 2 | **mappingModel** | Depends on structuralMappingModel + additiveDocumentModel. ETL-style: source DM(s) + target DM + optional precomputation, driven by a referenced SMM. | **Started, disabled.** Content classes (`MappingSource`, `MappingTarget`, `SortField`, `PreComputationFragmentRef`, `StructuralMappingModelRef`, ...) and a 141-line editor with the Target Model panel and an editable Sources list (`SourceModelsPanelController` + dialog); no validation service. The precomputation fragment and the Structural Mapping Model link are not editable yet (the controller's own comment says "added later"). |
| 3 | **combinationModel** | Built on additive + structural mapping. | **Present, enabled.** Structural editor + 7 structural rules (2026-09-08), base/additive loop detection (2026-09-20, reference graph only); DM expansion/SMT validation is not, see the dedicated section. **Gap review 2026-09-27**: see "Gap review (2026-09-27)" under "Combined Document Model" above - Base/Additive/Decoration pickers don't offer Combination Models, the Additive picker isn't type-filtered, invalid step references don't surface inline, and `HeaderRolesValidator` isn't wired in. |
| 4 | **additiveDocumentModel** | Hard dependency of both mappingModel and combinationModel. Overlay editing mode: elements are included/overwritten/purely-additive relative to a base DM. | **Partly present, enabled** (a Document Model with an annotation): read-only "Additive Elements Only" preview and base-aware path resolution; no overlay editing mode, no join. See the Document Model gap-list row. |
| 5 | **relationshipModel** | Foundational — link, masterDetailModel, treeModel, modelGraphDiagram and formModel's `Binding`/`BindingRepeat` all reference it. | **Present, enabled**: `RelationshipModelEditorController` (8 files) + `RelationshipModelValidationService` (9 relationship-specific validators). The **Relationship UI Model** (`relationship-ui`, `Ru` — an a12-studio-original design with no SME equivalent) also has an editor (8 files) and 3 validators. Role rename now propagates to Query/Form/Relationship UI/Overview references (`RoleRenameRefactoring`, fixed 2026-09-22) but not yet to the Tree Model. **Gap review 2026-09-27**: see "Relationship Model: gap review" below - a Combination Model picker gap plus its own validator bug, a missing role-pattern validator, `HeaderRolesValidator` not wired in, the Tree Model rename gap just mentioned, and two low-priority round-trip holes (`storage`/`embeddedGroupPath` etc.). |
| 6 | **selectionModel** | Reusable selection spec. | **Present, enabled** (2026-09-13), see the dedicated section. |
| 7 | **printModel** | Most editor-complex of the print family — relies on an external print-engine component library for the layout canvas. Backend renders PDF only. | **Large editor, disabled.** 28 content classes, an 811-line `PrintModelEditorController`, 7 print validators; no print-engine dependency and no PDF rendering (the old `PrintService`/`DocumentModelResolver`/`PrintParameters` scaffolding was deleted 2026-07-20). Rename/move refactoring rewrites `FieldRef.path`. |
| 8 | **printSettingModel / printTypesettingModel** | Small, no cross-model references — cheap wins once printModel work begins. | **Typesetting: present, enabled** (2026-09-25): `ModelType.TYPESETTING` (the `model-versions.json` key was `printtypesettings`, which never matched the header's `typesetting`), `TypesettingModel`, an editor with four extracted panels, 4 validators plus the reusable roles validator; see the dedicated "Print Typesetting Model" section. **Print Setting: not present** (deprecated in the platform). |
| 9 | **link / document** | Record-editing modules depending on relationshipModel/documentModel. `document` = data *instances* of a Document Model. | **Not present.** |
| 10 | **queryModel / overviewModel** | Search/filter/list-screen configuration; consumer-side, not blocking other model types. | **Both present, enabled.** Query: see the dedicated section (**gap review 2026-09-27**: a Combination Model can't be a query target, `Has(...)` role text isn't rewritten on role rename, `HeaderRolesValidator` not wired in - also corrected two stale claims about CDM-as-target and role-rename coverage). Overview: `OverviewModelEditorController` + 21 editor files (columns, filter items with per-type options, sub-header slots (every element type - button, search, filter, multi-selection - is editable through one dialog since 2026-09-21, the last three without the button-only Event/Confirmation/Priority/Icon block), initial sorting, styles, query-model link) and 23 overview validators (16 as of the 2026-09-26 review, +7 new classes since - see below). **Gap review 2026-09-26, updated 2026-09-27**: see "Overview Model: gap review" below - gaps 1, 2, 3, 4, 5, 6, 7, 8, 9 (partial), 10, 11, 12, 13, 14, 15 are closed; open: 16 (partial, picker candidates), 17 (partial, structural refactoring - filter-field deletion and event/model references still not cascaded). |
| — | **appModel** | Standalone. | **Present, enabled** (`ApplicationModelEditorController` + module/scene/region editors, 4 application validators, wireframe preview via `ApplicationModelPreviewService`, real Preview App deploy). **Gap review 2026-09-27, fixed same day (gaps 1-8):** see "Application Model: gap review" below - `Constraints` round-trips arbitrary keys, the region-uniqueness/view-add-model-type validator bugs are fixed, `HeaderRolesValidator` is wired in, region fields are breadcrumb-picker `ComboBox`es, and Region/Scene/Case rename/delete now auto-rewrites within-model references (`ApplicationModelStructuralRefactoring`). Gap 9 (nested subregion editing) is intentionally left - no real fixture needs it. |
| — | **masterDetailModel** | Standalone. | **Present, enabled** (`MainDetailModelEditorController`, 2 validators; `MasterDetailModuleGenerator` is used by the Preview App deploy). **Gap review 2026-09-27**: see "Master Detail Model: gap review" below (5 numbered gaps; headline is no heterogeneous/CDM expansion in the Form Mapping candidate lists, which a12-studio already has the building blocks for elsewhere). |
| — | **treeModel** | Standalone. | **Editor present, enabled** (since 2026-09-25), matching SME 13.0.2/tree model 11.0.0 as of 2026-09-27 - see "Tree Model: full gap review against SME 13.0.2" below for the full write-up. Five tabs (Tree, Node Types, Configuration, Layout, Custom Actions), 17 validators, Row Activation (not the pre-11.0.0 `defaultRowAction`), the Virtual Root, and the column editor's Label/Icon/Alignment/Styles/pin-direction. |
| — | **contentModel** | Experimental in SME itself. | **Editor present, disabled** (`ContentModelEditorController`, 2 validators; the center renders the model with the real Content Engine in a `WebView`, see "Content Model preview"; the right column mirrors SME's per-type setting panel, see "Content Model property column"). **Gap review 2026-09-26** ("Content Model: gap review"): no Document Model / Base Group setting, new models are seeded without `namespaceVersions`/root props, only 2 structural validators (none of SME's reference, form-element or setting checks), move/duplicate/cut/paste ignore the structure rules, and the editor shows no validation result. |
| — | **typeDefinitionModel** | Reuses the whole DM editor infrastructure. | **Present, enabled** (`TypeDefintionModelEditorController`; the type-definition mode rules are validated, see the Document Model section). **Gap review 2026-09-27**: see "Type Definition Model" below — close to parity already; the one real gap is that a TDM can't hold local and imported type definitions at once, unlike SME. |
| — | **umModule** | User-management config: two YAML file types, "roles" and "users". | **Present** as `RolesEditorController` / `UsersEditorController` over `RolesDocument` / `UsersDocument` (`editors/auth`, `AuthFileFactory`). |
| — | **transformerModel, modelGraphDiagram** | Lower cross-reference count / experimental in SME. | **Not present** (no `ModelType`, no classes; only a transformer icon). |
| — | **settingsModule, filesModule, attachment, data** | Workspace-level resources, no structured model editing. | **Not assessed on 2026-09-20.** a12-studio has its own project settings (`ProjectSettings`); whether SME's `settings.yaml` is read is unconfirmed (no reference to it in the code). |
| n/a | **common, preview** | Not model types. `common` = shared editor UI building blocks used across modules. `preview` = pure runtime capability (opens a browser window running the live app), no persistence. | a12-studio's equivalents are the shared `propertyeditors` package, and two previews: the wireframe `PreviewServer` and the real Preview App deploy (see the Form Model "Live preview" note). |

The parked / rejected list for the whole tool lives in `TODO.md` ("Won't do" and "Parked").

### Overview Model: gap review (2026-09-26)

**Status (2026-09-30): gaps 1, 2, 3, 4, 5, 6, 7, 8, 9 (partial), 10, 11, 12, 13, 14, 15, 16 (partial) closed; 16
(remainder), 17 (partial) open.** New/changed: `FilterStringFieldsMultiSelectPanelController` gained the
`enumeratedStringFilter.fields` list editor (String fields only, reusing `CustomSelectionOfFieldsPanelController`'s
row pattern) plus `OverviewEnumeratedStringFilterValidator` (gap 1). `OverviewConfiguration.actionColumnWidth`
is now a decimal (`JsonNode`-backed, like `Column.width`) with a real UI field; `Column.MIN_WIDTH`/`OverviewConfiguration.MIN_ACTION_COLUMN_WIDTH`
(both 0.3) are enforced by `OverviewColumnValidator`/`OverviewInfiniteScrollingValidator` (gap 2). `NewModelFactory.buildOverviewModel`
now seeds the Subheader (Multi-Selection left, Search+Filter right - bare markers, no `confirmation`/`priority`,
see the class javadoc for why) and an empty `rowActionGroup` exactly like SME's real `OverviewModelModule.initializeNewOverviewModel`
(checked directly against `C:\workspace\sme`); `rowActionGroup`/`subHeaderBox`/`footerBox`/`enableFilter`/`showFullTextSearch`
no longer serialize as literal `null` (gap 3). `OverviewInfiniteScrollingValidator` covers Paging Size/Row
Height/Action Column Width required-and-minimum; `PagingBehaviourPanelController` seeds Row Height to 49 on
switching to Infinite Scrolling (gap 10). Initial Sorting duplicates (`OverviewInitialSortingReferenceValidator`)
and a sortable-reference-columns-only picker (gap 11); Filter Section label-required-while-filter-button-shown
(`OverviewFilterSectionsValidator`, gap 12); Context Menu group-without-action and Footer `export_excel`
"Composed Document Models only" warning - not the deeper non-repeatable-CDM half (`OverviewContextMenuValidator`/
`OverviewFooterExportExcelValidator`, gap 13). `OverviewColumnValidator` covers Width minimum, Preferred-Sorting-
required, Dynamic-Suffix reference/indexed (gated on `useDynamicSuffix`, not just presence) and column header/
content Style reference validity+uniqueness; `attachmentDisplayModeIsRequiredForAttachment`/
`multiSelectDisplayModeIsRequiredForMultiSelect` are explicitly **not** ported - checked directly against the
SME source, `elementType` (the field these two rules key off) is stripped from every exported file and
reconstructed on import *from whichever display-mode field is already present*, not by resolving the reference
against the Document Model, so the semantically live-resolved check Studio would otherwise use is stricter than
SME's own, real, session-history-dependent rule (proven by 5 real fixtures, e.g. `Company_OM.json`'s "Logo"
column, that a first cut of this rule flagged and SME itself does not) (gap 9, partial). Gap 8 (expression
columns): the Column dialog's OK button now requires Name + a syntactically valid Expression for an expression
column (blank `elementRef`), and `OverviewExpressionColumnValidator` + the header-label-or-icon check
(`OverviewColumnHeaderLabelOrIconValidator`, extended) enforce the same server-side. The syntax checker is
**not** `RuleLanguageSyntaxChecker` as this section originally assumed - checked directly against the SME
source and against all 13 real expression columns in `testing/workspaces` (`RuleLanguageSyntaxChecker` rejects
every one of them): Overview expression columns use the platform's separate text-templating "Expression"
language (`kontext`/`case`/multilingual-value constructs, `documentation/2606-06-doc/expression-expression-docs.md`),
now ported as its own ANTLR grammar (`a12-studio-models/src/main/antlr/de/a12/studio/models/expressionlang/ExpressionLang.g4`,
`ExpressionLanguageSyntaxChecker`), transcribed directly from that doc's own grammar declaration and verified
against all 13 real columns. `OverviewBindingPurpose` (gap 7) mirrors SME's `omParser.isBindingOverviewModel`,
scanning both Form Model Bindings (`modelsSME.availableItemsOverview`/`selectedItemsOverview`) and - a12-studio's
own addition, since it models this as a distinct Relationship UI Model type rather than folding it into
`formModel` - `DualPaneSelectionComponent`/`TableListComponent`/`EditConfiguration`. It now backs `OverviewSubHeaderElementValidator`
(gap 6): the six "element not allowed while the feature is off" rules (Filter/Search/Multi-Selection x major/
minor slot) plus Filter's own "no element added"/"only one allowed" pair (new - Search's and Multi-Selection's
existing `OverviewSearchElementValidator`/`OverviewMultiSelectionElementValidator` gained the same `purpose`
gate instead of a new pair), transcribed rule-by-rule from `OverviewMetaModel.json` rather than the simplified
"just skip while off" a first reading suggests - Search's off-rule and its own missing/duplicate pair are
skipped entirely for *either* Binding purpose (replaced, for `available_item` only, by a WARNING with SME's own
different message, "not supported in Available Items Binding Overview"); Multi-Selection's off-rule and its
missing/duplicate pair are likewise skipped for either purpose; Filter's off-rule and its missing/duplicate pair
apply to every purpose *except* `selected_item`. A Custom Filter exemption the literal meta model has no
equivalent for (SME's own TS types have no `newFilterConfiguration` concept at all, see "wire-shape differences"
above) was necessary too: a literal port flagged 5 real fixtures (`Person_Ov`, `PersonEmployee_Ov`,
`PersonFreelancer_Ov`, `All_Skills_Ov`, `Available_Skills_Ov`) whose Filter element the BA doc says a Custom
Filter overview doesn't need at all. Verified against the whole fixture corpus (every overview file under
`testing/workspaces`, with real sibling Form/Relationship UI Models in context) before and after: zero findings
either way. Gaps 4/14 (Query Model handling): `OverviewReferencePanelController.syncModelReferences` now writes
only `query-model-for-overview` in Query Model mode (every field-reference picker already fell back to the Query
Model's own `targetDocumentModel` when there was no explicit reference, so nothing else needed to change to stop
writing the redundant, staleness-prone `document-model-for-overview` one); a new "Add" button next to the Query
Model picker opens `CreateQueryModelDialogController` (name/location/locales/roles/Target Document Model/a
"Generate Fields, Paging and Sorting from this Overview Model" checkbox) and `OverviewModelEditorController`
creates the model and seeds it from the Overview's own reference columns/Paging Size/Initial Sorting -
`OM_NotValid` is still not ported, per the existing decision. Gap 5 (Subtype): `FieldRef.subModel` is typed now;
`DocumentModelHeterogeneity.recursiveSubTypes` (new) backs the Custom Selection Of Fields row's Subtype combo,
which re-points that row's Field picker at the sub-type's own elements and clears a stale `fieldId` when it
changes; `OverviewFilterCustomFieldsValidator` validates `subModel` itself and resolves `fieldId` through it;
rename-rewrite is via `ModelReferenceRewriter.REFERENCE_FIELD_NAMES` gaining `"subModel"` rather than a header
reference (no fixture ever showed what a `sub-document-model-for-overview` reference should look like, and it
isn't needed for rename-safety - the content-field rewrite already covers that). Gap 16, repeatable-field and
multi-select/filter-field/Section Data items done: `OverviewElementOptions.columnElementIds` excludes repeatable
fields from the Column dialog's Element Reference picker (matching `OverviewFieldReferenceValidator`'s own
"repeatable" error). New `OverviewElementResolution.isEnumerationMultiSelect(ElementIndex, Element)` mirrors
SME's `DocumentModelApi.isEnumerationMultiSelect` (a multi-select group whose single value field is
Enumeration-typed); `columnElementIds()` now excludes a multi-select group unless it's an enumeration multi-select
(`isMultiSelect(element) ? isEnumerationMultiSelect(element) : true`, matching SME) and new
`customSelectionFieldIds()` (Fields plus enumeration multi-select groups, `isFieldLike(element) ||
isEnumerationMultiSelect(element, documentModel)`) replaces the unrestricted `elementIds()` at both its prior
call site (`CustomSelectionOfFieldsPanelController`'s Field picker) and the Section Data field picker
(`FieldReferencesPanelController` - SME's own candidates for both are the same function,
`getFilterValuesForSectionData` delegates to `getFilterValuesByFilterMode`). Verified against every real
multi-select group in `testing/workspaces` (all Enumeration-typed, so nothing currently valid is excluded) and
`ProductMovie_OM.json`'s `custom_list` field referencing its enum multi-select group directly by id (still
resolves); `OverviewElementOptionsMultiSelectTest` (4 cases) pins the new restriction. Still open from gap 16:
the "already-used fields excluded"/"dynamic-suffix fields excluded" sub-clauses of SME's filter-field candidate
rule (found in SME source, not ported this pass) and the Screen Reader Column candidate rule (no SME source
recipe found yet).

**Gap 15 (metadata fields), closed:** `OverviewElementResolution.META_FIELDS` (new, 7 entries covering the
kernel's `__meta` group - docRef/modelReference/modelVersion/creator/createdAt/modifier/modifiedAt) plus
`metaFieldDisplayName`/`isMetaFieldId`; 4 of the 7 ids were confirmed directly against a real fixture
(`testing/workspaces/advanced_new/models/10_People/Person_Ov.json`'s "Creator"/"Created At"/"Modifier"/"Modified
At" labels under its "Meta Data" section), the remaining 3 inferred from the same fixed ordering elsewhere
(`PersonSkills_LinkFields_Fm.json`). `OverviewElementOptions.columnElementIds`/new `customSelectionFieldIds`
append the seven meta field ids to their existing candidate lists (Column dialog's Element Reference picker and
`CustomSelectionOfFieldsPanelController`'s Field picker); `displayPath`/`isResolved` recognize a meta field id so
it renders as `__meta/<name>` instead of the raw kernel id and is never flagged unresolved. Not done: a
dedicated "all\_with\_meta shows this, all does not" distinction in the pickers themselves (Studio's pickers
don't yet key off `filterMode` at all - the meta fields are offered everywhere) - no real fixture uses one, so
this was left for whenever gap 16's "filter mode-aware candidates" is actually built, rather than guessed at now.

**Gap 17 (structural refactoring), partially closed** - 3 of the 5 SME refactoring-dialog behaviors are done,
the other 2 are not:
- *Deleting a column updates Default Sorting* - done. `OverviewColumnsPanelController.openEditDialog` now always
  calls `notifyChanged()` after a confirmed edit (previously only when `pinDirection` changed, so a "Sortable"
  toggle-off never reached the Sorting panel); `OverviewSortingPanelController.refresh()` (called from that
  notification) prunes an Initial Sorting entry whose column was deleted, made non-sortable, or turned into an
  expression column, before rebuilding its rows.
- *Disabling Search/Filter/Multi-Selection removes its sub-header element* - done, new `OverviewSubHeaderPruning`
  (`a12-studio-models-validation`, pure logic, no UI dependency) reuses `OverviewSubHeaderElementValidator`'s own
  `isFilterAllowed`/`isSearchAllowedForPlainOverview`/`isMultiSelectionAllowedForPlainOverview` predicates
  (extracted package-visible for this), so pruning and validation can never disagree on what counts as "not
  allowed" - includes the same purpose-gating (Filter skipped for `selected_item`; Search/Multi-Selection never
  pruned for either Binding purpose, matching that validator's own gating). Wired into
  `OverviewModelEditorController.pruneSubHeader` via two new callbacks -
  `OverviewSearchAndFiltersPanelController.setOnFeatureSwitchChange` (fires on a user toggle of Show Full Text
  Search/Enable Filter/Show Filter Button, never from `setModel` itself) and
  `OverviewMultiSelectionPanelController.setOnEnabledChange` (fires from the existing `onEnabledChanged` hook,
  same "never on load" property) - so opening an already-inconsistent file is left alone (matching how gap 17's
  column-sorting pruning above also only prunes on a subsequent user edit, not on load) and only an actual
  feature-off toggle prunes-and-saves. `SubheaderSlotPanelController` gained a public `refresh()` (re-renders
  from its `rows` list without re-running `initAddMenu()`) so the two Subheader panels reflect the removal
  immediately. Verified against the whole fixture corpus (zero findings before/after) and by
  `OverviewSubHeaderPruningTest` (5 cases) plus `OverviewModelEditorControllerSubHeaderPruningTest` (2 cases,
  full editor wiring).
- *Style rename/delete cascades to column references* - done. `StylesPanelController`'s style name `TextField`
  listener now calls `renameColumnStyleReferences(oldValue, newValue)` on every keystroke (each keystroke's
  rename converges every column's header/content style references to match) and its delete button calls
  `removeColumnStyleReferences(removedStyle)`; both guarded to only run in `setModel` mode (a model-level Styles
  list), not the `setColumn`/`setCustom` single-target modes. Verified by
  `StylesPanelControllerColumnCascadeTest`.
- *Deleting a filter field* and *event and model references* - **not done**. Re-checked 2026-09-29: SME's own
  `overviewRefactoring.ts` (this module's only refactoring-transforms file) has no bespoke code for either
  behavior - it only handles cross-model rename (a `documentModelReference`/`subModel` id swap), not any
  within-model delete cascade - so there is no concrete SME source to port from, unlike gap 1's clean
  `resolveAndFilterAbstractDocuments` recipe. Left open rather than guessed at; a session with the BA doc's
  screenshots of the actual refactoring dialog behavior in hand should attempt this next.

**Re-checked with `OverviewBindingPurpose` in hand, found 2026-09-27 - corrected 2026-09-29 after building a
real fixture regression test (`FixtureWorkspacesOverviewValidatorsTest`, new):** this note originally claimed
two `OverviewFieldReferenceValidator`/`OverviewColumnHeaderLabelOrIconValidator` false positives on binding
overviews. Only the first is real:
- **Real, still open:** four of sixteen (`ProductMovie_OM`, three `e-commerce/99_BindingOverviewModels/*`
  expression columns) have `purpose == null` - those Form Models (`ProductBook_FM` and siblings) use a *third*
  wire shape for their bindings that neither `OverviewBindingPurpose` nor the rest of this review accounted
  for: a header `bindingConfiguration` annotation (a JSON-encoded string,
  `{"type":"relationship","details":{"components":[{"name":"DropDownSelection","models":[{"name":"...",
  "use":"candidate"|"link"}]}]}}` - `candidate`/`link` presumably map to available/selected) plus a plain
  `modelReferences` entry per referenced overview (`purpose: "bindingReference"`, indistinguishable from each
  other without the annotation). Re-verified this causes no actual manifest validator misbehavior on these
  fixtures today (the new fixture test finds no ERROR on any of them) - the gap is real (`OverviewBindingPurpose`
  can't classify this third shape) but currently latent, not an observed bug.
- **Not a bug, retracted:** the claimed "`PersonSkills_Person_Ru_SelectedItems_Ov` and 3 siblings still report
  their field missing despite a `linkReferences` entry" was an artifact of how *this doc's own investigation*
  built its test context, not a real defect - `OverviewElementResolution.indexFor`/`ColumnLinkReference.resolveDocumentModelId`
  already route `TYPE_LINK` resolution through the relationship's `linkDocumentModel` correctly (here, a
  Combination Model, `PersonSkills_LinkFields_Cm`) via `CombinedDocumentModelElements.resolveForFieldReferences`
  - but that helper needs a real, tree-connected `ProjectItem.findByModelId` to find the Combination Model's
  sibling file, which only a real `Project.load()` provides. Confirmed by rebuilding the check with a real
  loaded `Project` (`FixtureWorkspacesOverviewValidatorsTest`): zero `OverviewFieldReferenceValidator` errors on
  this fixture or its siblings.

Reviewed against SME's `overviewModel` module (`document/omDocument.ts`, `transformations/import|exportTransformations.ts`, `converter/omParser.ts`,
`omModule.ts`, `references/omReferenceProvider.ts` + `omPaths.ts`, `customConditions/*`, `transformations/overviewRefactoring.ts`), its resource models
(`OverviewMetaModel.json` for every rule, message and severity, `OverviewModelEditor.json`), `docs/modules/overviewModel/*.adoc`, and the newer BA doc
`C:\workspace\a12\2606-06-doc\sme-sme-om-ba-docs.md` (which also documents the A12 2026.06 features SME's own TypeScript types do not have). Fixtures: the 84 overview
files in `testing/workspaces/*` and the A12 sample workspace (`A12 Tools - 2026.06/bin/workspace-advanced`). The only server-side call in SME's overview module is the shared
expression check (`fetchExpressionProblems`, kernel-backed); everything else is client-side, so almost all of the gaps below can be closed without the kernel.

**What is covered.** The editor (`OverviewModelEditorController` + 21 panel/dialog classes) covers the whole SME "Overview" and "Custom Actions" tabs: Overview Reference
(Document or Query Model, fields restricted to the Query Model's projection), Columns (reference and expression columns with every common and type-specific setting, pin
re-ordering, label auto-fill from the field, relationship `linkReferences`), Initial Sorting, Styles, Multi-Selection (all options, actions, clear confirmation), Search
and Filters (all five filter modes, Section Data, the "Custom Filter" mode with groups, per-type item options, filter definitions and selector configuration), Paging
Behaviour, Row Height / Action Column Width, Columns Resize, Number of Entries, Skip Initial Load, Screen Reader Column, Row Actions, Context Menu, Row Activation, Title for
Interactive Rows, Subtitle, Subheader and Footer. "Create Overview Model from Selection" in the Document Model editor is a Studio addition on top.

**Wire-shape differences that are not gaps.**
- SME 38.2.0 still writes `minorElements`/`majorElements` and keeps `documentModelReference`/`queryModelReference`/`purpose` in `content` while editing (its export moves the references into
  the header and drops them from `content`). Studio reads the legacy slot keys, writes `leftSlot`/`rightSlot`, and keeps the references in the header only, like every real file.
- Studio is *ahead* of SME's TypeScript types on `newFilterConfiguration` (Custom Filter), `skipInitialLoad`, `screenReaderColumn`, `defaultRowAction` without an event ("Non Interactive")
  and the Expand-All-Popup element. SME's `custom` + `event`-required rule (`eventIsRequiredForCustomRowAction`) is therefore deliberately *not* ported. The open questions for
  those features are in `TODO.md` ("Blocked").
- SME normalizes on import/export (`preferredSorting` defaults to ASC, attachment/multi-select display mode defaults, `filterConfiguration` dropped while the filter is disabled, `sectionData`
  only with the filter button, `pagingSize` dropped for infinite scrolling, type-specific column keys dropped when the field type does not match, `showRowCount` dropped with a hidden label).
  Studio keeps what is on disk; that is only listed below where it causes a visible problem.
- Older-version migration (`OverviewMigrationTool`) is not planned, see "Won't do" in `TODO.md`.

**SME rule coverage, as reviewed 2026-09-26** (all rules of `OverviewMetaModel.json`; the "Ported" ones were the 16 overview validators of `OverviewModelValidationService` at the time - 23 as of 2026-09-27, see the Status line above for what closed since). The table below is the original review; not updated row-by-row.

| SME rule(s) | Studio |
|---|---|
| `mustHaveAtLeastOneColumn`, `columns/mustHaveValidReference`, `invalidElementRefIsRepeatable`, `indexedAnnotationShouldBeNotFalseForColumnRef`, `notAllowSortableForMultiSelectColumn`, `referenceColumnHeaderShouldHaveLabelOrIcon` | **Ported** |
| `initialSorting/mustHaveValidReference`, `filterModeIsRequiredWhenShowFilter`, `filedIDsMustHaveItemWhenFilterModeEqualCustom`, `fieldIdsNotUnique`, custom-list `mustHaveValidReference` + `indexedAnnotationShouldBeNotFalse`, `validateIndexedAnnotationForFilterMode`, section `fieldIDsMustHaveItem`/`sectionDataFieldIdsNotUnique`/`uniqueSectionDataFieldId`/field reference, `noMultiSelectionIsAdded`/`onlyOneMultiSelectionIsAllowed`, `noSearchIsAdd`/`onlyOneSearchIsAllowed`, `stylesNotUnique` (only the "empty value" half), `pagingSizeIsRequired` (only "must be at least 1") | **Ported** or ported in part (marked) |
| `initialSortingIDRefNotUnique` | Missing (gap 11) |
| `pagingSizeIsRequired` (pagination without a size), `rowHeightIsRequired`, `actionColumnWidthIsRequired` (infinite scrolling) | Missing (gap 10) |
| `filterTypeSubHeader{Major,Minor}IsNotAllowedWhenNotShowFilter`, `searchTypeSubHeader{Major,Minor}IsNotAllowedWhenNotShowFullTextSearch`, `multiSelectionTypeSubHeader{Major,Minor}IsNotAllowedWhenNotEnableMultiSelection`, `noFilterIsAdded`, `onlyOneFilterIsAllowed`, the two "Search element is not supported in Available Items Binding Overview" warnings | Missing (gap 6, needs gap 7) |
| `expressionNameIsRequired`, `expressionIsRequired`, `expressionMustBeValid`, `expressionColumnHeaderShouldHaveLabelOrIcon` | Missing (gap 8) |
| `suffixRefMustHaveValidReference`, `indexedAnnotationShouldBeNotFalseForDynamicSuffix`, `preferSortingIsRequiredForSortableField`, `attachmentDisplayModeIsRequiredForAttachment`, `multiSelectDisplayModeIsRequiredForMultiSelect`, column `styles/header` and `styles/content` `mustHaveValidReference` + `headerStylesNotUnique`/`contentStylesNotUnique` | Missing (gap 9) |
| `enumeratedStringFilter`: `fieldIdsMustBeFilled`, `fieldIdsNotUnique`, `pagingSizeIsRequired`, field `mustHaveValidReference` | Missing (gap 1) |
| custom-list `mustHaveValidSubModelReference` | Missing (gap 5) |
| `sectionData/label/labelIsRequired` | Missing (gap 12) |
| `contextMenu/groups/groupMustHaveAction`, `footerBox/*EventExcelExportWorksWith(NonRepeatable)CDMOnly` (WARNING) | Missing (gap 13) |
| `queryModelReference/mustHaveValidReference` (`OM_NotValid`), `queryModelReference/requiredWhenEnable`, `documentModelReference/mustHaveValidReference`, `noDocumentModelForTargetRole` | Reference resolution and "reference required" are covered by `HeaderModelReferenceValidator` + `OverviewDocumentModelRequiredValidator`; `OM_NotValid` missing (gap 14); the target-role rule needs gap 7 |
| `mustHaveUniqueName` (binding overviews) | Covered by the generic `UniqueModelIdValidator` |

Studio-only validators (not in SME's meta model): filter groups (`OverviewFilterGroupsValidator`), filter definition syntax (`OverviewFilterDefinitionSyntaxValidator`).

| # | Gap in Studio | What SME does | Effect today |
|---|---|---|---|
| 1 | **Enumerated string filter has no field list.** `FilterStringFieldsMultiSelectPanelController` only toggles the feature and edits Paging Size; `EnumeratedStringFilter.fields` has no editor and no validator | Panel "Filter String Fields with Multi-Select" with a repeat of String fields (candidates: String fields only, repeatable groups allowed); rules: list must not be empty when enabled, entries unique, valid reference, page size required | `ProductBook_OM`, `ProductFood_OM`, `ProductMovie_OM` and `ProductSingle_OM` (e-commerce) carry 1-2 fields that cannot be seen or edited. Switching the feature on in a new model writes an object without `fields`, which SME reports as an error, and nothing in Studio says so. **Highest priority: it is the only overview setting that is silently uneditable.** |
| 2 | **Action Column Width is an integer.** `RowHeightActionColumnWidthPanelController` uses `Integer.valueOf` and a numeric-only field; `OverviewConfiguration.getActionColumnWidth()` casts the stored value with `(int)` | `NumberType` with `minValue` 0.3 and one decimal, same unit as a column `width` (1.0 = 150 px). Five of the six real files use 0.3 / 0.4 / 0.5 (`ProductBook_OM`, `ProductFood_OM`, `ProductMovie_OM`, `ProductSingle_OM`, `CollectionOffer_OM`) | The field shows `0` for those files and the first keystroke rewrites the value as an integer; a fractional width cannot be typed at all. A column's own `width` field has no lower bound either (SME: min 0.3, one decimal; Studio accepts any parsable number) |
| 3 | **Defaults are displayed but not written, and a new model contains `null`s.** A model built by `NewModelFactory` serializes `enableFilter: null`, `showFullTextSearch: null`, `rowActionGroup: null`, `subHeaderBox: null`, `footerBox: null` (measured 2026-09-26 with a throw-away test; `OverviewConfiguration` has no `NON_NULL` on those fields and the mapper writes nulls; the editor materializes the empty boxes and the paging size on open, but not the two booleans). From reading the code (not run): enabling Filter alone leaves `filterConfiguration` absent while the panel shows "All columns" with both check boxes unchecked, and `OverviewFilterModeRequiredValidator` then reports the mode as missing although the combo shows one; once another filter field is touched `showFilterBar`/`showFilterButton`/`filterMode` are written as `null`. Row Height shows 32 but stays unset. Paging Size defaults to 10 | SME's `afterNew` seeds the sub-header (Multi-Selection left; Search + Filter right, each with `confirmation`/`priority`), an empty `rowActionGroup`, and its export writes `showFullTextSearch`/`enableFilter` as booleans and never writes `null`. Import defaults: `showFilterBar` = `showFilterButton` = true, `filterMode` = `all_columns`, enumerated-string `pagingSize` 10; switching to infinite scrolling pre-fills Row Height with 49; the BA doc gives 50 as the Paging Size default (51 of the 80 files that set one use 50, 27 use 10) | A freshly created Overview Model is neither what SME creates nor what it accepts without an error (no sub-header elements although a feature is later switched on; explicit `null`s in the file). Fix in one place: `NewModelFactory.buildOverviewModel` (defaults + `NON_NULL`) and "persist what the panel displays" in the filter / row height panels, as `PagingBehaviourPanelController` already does for the paging size |
| 4 | **A Query-bound overview writes a second header reference.** `OverviewReferencePanelController.syncModelReferences` adds a `document-model-for-overview` reference next to the `query-model-for-overview` one | SME's `handleModelReferences` writes only the Query Model reference (all 17 Query-bound overviews on disk, including the one in the A12 sample workspace and SME's own `IntegrationTestModelQmRef.json`, look like that); the Document Model is derived from the Query Model's target on every load | The extra reference is redundant (`OverviewModelEditorController.currentDocumentModelId` already falls back to the query's target) and is not re-synced when the Query Model's target Document Model changes; `OverviewElementResolution.referencedDocumentModel` prefers the explicit reference, so validators then resolve fields against the wrong model. The file also differs from what SME writes |
| 5 | **No Subtype (`filterConfiguration.fields[].subModel`).** `FieldRef` has only `fieldId` (and `ignoreUnknown`, so a `subModel` is dropped on the next save); the Custom Selection of Fields panel has an empty placeholder cell where the Subtype belongs; the header reference `sub-document-model-for-overview` is not maintained; rename/move refactoring does not know it | Subtype picker = the recursive sub-types of the referenced Document Model (`resolveSubTypesRecursively`); the field picker then offers the chosen sub-type's own fields; only for `custom_list`; hidden (`excludeSubModels`) when the model has no sub-types; `refactorOverviewModel` rewrites `subModel` on a rename; rule `mustHaveValidSubModelReference` | No fixture has a `subModel` (checked all 84 files), so today nothing is lost; the first real model that uses one loses it silently on save |
| 6 | **Sub-header elements are not tied to their feature switch.** Only "Search/Multi-Selection enabled -> exactly one element" is checked, and the Add menu offers every element type regardless of the switches | Six "not allowed when the feature is off" rules (Filter needs Enable Filter + Show Filter Button; Search needs Show Full Text Search; Multi-Selection needs Enable Multi-Selection; major and minor), "No filter element is added", "Only one filter"; refactoring removes the element when the feature is switched off | Switching a feature off leaves a dead element without any message; a missing/duplicate Filter element is never reported. **Two traps when porting:** (a) do gap 7 first, otherwise the rules produce false positives on binding overviews; (b) SME's filter rules test `filterConfiguration.showFilterButton`, which a **Custom Filter** model does not have - five fixtures (`Person_Ov`, `PersonEmployee_Ov`, `PersonFreelancer_Ov`, `All_Skills_Ov`, `Available_Skills_Ov`) have `enableFilter: true`, a Filter element and only `newFilterConfiguration`, so a literal port would flag all of them (the BA doc: a Custom Filter overview does not need the sub-header element at all, so decide what the rule should say for that mode) |
| 7 | **No binding purpose.** Studio does not know whether an overview is the Available Items or the Selected Items overview of a Form `Binding`/`BindingRepeat` component (55 of the 84 files are named like one, e.g. `*_AvailableItems_OM`, `*_SelectedItems_Ov`) | `omParser.isBindingOverviewModel` scans the Form Models' binding components (`availableItemsOverview`/`selectedItemsOverview`) and sets `purpose` (`available_item`/`selected_item`), plus four explorer types (Overview, for Binding, for Composed Document Model, both) with their own icon and label. With a purpose: the sub-header rules above are skipped (`FieldNotFilled(purpose)`), `showFullTextSearch` is not defaulted, the enumerated-string reference rule is skipped, the Search element gets the "not supported in Available Items Binding Overview" warning, and `selected_item` needs a target-role Document Model (`noDocumentModelForTargetRole`) | No visible effect yet, because the two implemented element rules happen not to fire on the real files; it becomes a false-positive source as soon as gap 6 lands. The explorer shows every overview the same way |
| 8 | **Expression columns are not validated at all.** The column dialog does not require a Name or an Expression (OK is enabled with both blank) and its `RuleEditorController` has no `setValidator`; `OverviewColumnHeaderLabelOrIconValidator` only looks at reference columns; the column type is inferred from "has an expression or a name", so a blank expression column is written without either and reloads as an empty reference column | `expressionNameIsRequired`, `expressionIsRequired`, `expressionMustBeValid` (the module's one server-side call: `fetchExpressionProblems` with the referenced Document Model), `expressionColumnHeaderShouldHaveLabelOrIcon` (WARNING), and `type` is stored explicitly in the editor model | Syntax errors and missing names surface only in SME. The syntax half needs no kernel: `RuleLanguageSyntaxChecker` already validates the same language for Document Model rules (`ComputationOptionsPanelController` wires it with `setValidator(RuleLanguageSyntaxChecker::validate)`); the reference/type half is kernel-backed, Open Decision #1 |
| 9 | **Column-level rules missing**: `suffixRef` (must resolve to an Enumeration field; `indexed` annotation), `preferredSorting` required for a sortable column, display mode required for attachment / multi-select columns, column header/content **style references** (must exist in `content.styles`, unique per list) and `content.styles` uniqueness. The Styles panels are free-text lists, not pickers of the styles defined in the model | Style fields are references into `content.styles` (`getStyles` provider, `SupportedReferenceTypes.silent`); the Overview docs say "styles must first be set in Styles in order to be available here"; refactoring updates them on rename/delete | A typo in a column style, or two identical styles, is accepted. The dialog itself defaults sorting/display modes, so the three "required" rules only matter for hand-edited files |
| 10 | **Paging / infinite scrolling required fields not validated**: Paging Size for pagination (Studio only checks `>= 1` when present), Row Height and Action Column Width for infinite scrolling. Switching to infinite scrolling does not set Row Height | `pagingSizeIsRequired`, `rowHeightIsRequired`, `actionColumnWidthIsRequired`; Row Height is pre-filled with 49 | An infinite-scrolling overview without Row Height/Action Column Width is accepted (the Overview Engine needs both, see the SME docs "Paging Behavior") |
| 11 | **Initial Sorting**: duplicates are not reported (the picker only avoids them on Add), and any column can be picked (also expression columns and non-sortable ones) | Picker lists sortable reference columns only; `initialSortingIDRefNotUnique`; refactoring removes a column from the default sorting when it is deleted **or made non-sortable** | A non-sortable column in the default sorting is silently accepted; `OverviewInitialSortingReferenceValidator` only catches a deleted column |
| 12 | **Filter section label** not required (`OverviewFilterSectionsValidator` checks id, fields and duplicates only) | `labelIsRequired`: a locale row with an empty text is an error while the filter button is shown | Sections without a visible label are accepted |
| 13 | **Context Menu group without an action** is accepted; **`export_excel` events** get no warning and are not suggested | `groupMustHaveAction` ("Group $name.value$ in context menu should have at least one action"); footer event `export_excel` warns unless the Document Model is a Composed Document Model ("works for CDM models only") and warns again when that CDM has repeatable elements; the sub-header/footer event list is `add`, plus `export_excel` for a CDM | Studio suggests a generic `add`/`edit`/`delete`/`copy` list for every button (only Multi-Selection has its own, `delete_selected`) |
| 14 | **Query Model reference**: no "Add" button next to the picker (only Edit), and no `OM_NotValid` rule | Add opens a new Query Model with the Document Model as Target; switch "Create Query based on Overview Model" fills the result fields from the columns, the paging and the sorting from the Initial Sorting; a referenced Query Model with `exclude` set is an error ("Exclude All Root Documents" cannot be digested by the Overview Engine) | Creating the Query Model is manual. **Check before porting the rule:** two Selected-Items Query Models in `advanced_new` (`PersonTeamAssignment_Ru_SelectedItems_Ov_Qe`, `Teammembers_Ru_SelectedItems_Qe`) are bound to overviews and set `exclude: true`, so either the flag means something else in the 2026.06 wire format or SME flags them too |
| 15 | **Metadata fields cannot be picked** (`__meta/createdAt`, ...): `ElementIndex` only knows the model's own tree; "all" and "all_with_meta" filter modes and the "Generate ... + metadata" filter-group action behave identically | `DocumentModelApi.getDmReferenceCandidates` includes the kernel `__meta` group; `all` filters `/__meta/` out, `all_with_meta` keeps it; the default sorting at runtime is `__meta/createdAt` desc | No real file references one (checked), but a metadata column or filter cannot be authored. `OverviewElementResolution.META_GROUP_ID_PREFIX` already knows the fixed ids the kernel injects |
| 16 | **Pickers offer every element** and rely on the validators afterwards (`OverviewElementOptions.elementIds`) | Column Element Reference: non-repeatable only, multi-select only when it is an enumeration multi-select; filter fields: fields and enumeration multi-selects, repeatable allowed, already-used fields and dynamic-suffix fields excluded; enumerated-string fields: String fields only; Section Data fields: only fields the active filter mode offers; Screen Reader Column guidance: avoid boolean/expression columns | Usability only (invalid picks are reported by the validators that exist); listed so the picker rules are not forgotten when gaps 1 and 5 are built |
| 17 | **No structural refactoring** when the model changes | SME's refactoring dialog (Commit / Edit): deleting a column updates Default Sorting, disabling Search/Filter/Multi-Selection removes its sub-header element, deleting a filter field, style rename/delete, event and model references | Studio only reports (dangling `initialSorting`, `screenReaderColumn` is cleared); see "Move/rename refactoring" for the cross-model part that exists |

**Test coverage.** `OverviewValidatorsTest` has 26 tests for the 16 validators; the UI has two test classes (`OverviewMultiSelectionPanelControllerTest`, `OverviewModelEditorControllerQueryModelLinkColumnsTest`) for about twenty panel and dialog controllers, so nothing pins the behavior described in gaps 1-3.

**Suggested order.** (1) with its four validators first, then (2), (3), (10) and the Row Height persistence, because they are small and change what is written. Then (7) before (6), together with the sub-header validators. Then (8) (syntax part), (9), (11), (12), (13) as plain `ModelValidator`s with fixtures (`*_invalid.json`, like the existing ones). (4) and (14) belong together (Query Model handling), (5) is its own task, (15)-(17) last.

### Tree Model: Expansion Strategy gap review (2026-09-26)

**Status (2026-09-26, same day): gaps 1-4 and 6 are closed.** The Configuration tab now has `TreeConfigurationPanelController` (strategy type; switching calls `ExpansionStrategy.switchTypeTo`, which drops the other strategy's keys and writes `expansionDepths: []` for "tree"), `TreeInitialExpansionPanelController` (Enable, Type, Number Of Levels, unique Node Types To Apply rows; "level by level" only), `TreeExpansionDepthsPanelController` ("tree" only), `TreeWholeTreeExpansionPanelController` (`configuration.wholeTreeExpansion`) and `TreePaginationPanelController` ("level by level" only). Typed model fields: `InitialExpansion` (`type`, `level`, `affectedNodeRefs`), `ExpansionStrategy.pageSize`, `TreeConfiguration.wholeTreeExpansion`; "Enable Initial Expansion"/"Enable Pagination" are not stored (presence of the key), an empty node type list is written as absent. Deleting a node type removes it from `affectedNodeRefs`. **Gaps 5, 7, 8 and 9 closed later the same day.** (5) `TreeExpansionStrategyValidator` (tree: depths not empty, each relationship one a child relationship configuration uses, none twice; level by level: initial expansion has a type, `level_limit` a level of at least 1, node types exist and are unique; page size at least 1) and `TreeWholeTreeExpansionValidator` (the four Expand All PopUp rules: element without the flag is an error per element; flag needs exactly one, in either slot); all five fixture trees satisfy them. (7) The depths panel only offers relationships without a depth (the edited depth keeps its own), disables Add when none are left and follows changes to the node types and their relationships; the depth dialog got the Max Depth help icon. (8) A tree without a strategy shows "Level by level" without writing it; the first edit of Initial Expansion/Pagination writes the type. (9) What could really break was `TreeChildRelationshipConfiguration.relationshipModelRef`: `ModelReferenceRewriter.REFERENCE_FIELD_NAMES` (used by the project-tree rename and the Application Groups plugin) lacked it, so renaming a Relationship Model updated the header and the expansion depths but left the child relationship configurations dangling; it is in the set now (`ModelReferenceRewriterTreeTest`). Tree references by element/column/node id (column mappings, `rootRef`, `affectedNodeRefs`) cannot break by a rename, and delete cleanup exists for columns and node types. Nothing of the review is open. The table below is the original review.

Reviewed against SME's `TreeMetaModel.json` (rules), `TreeModelEditor.json` (layout, `fieldConfiguration`), `tmDocument.ts` +
`import/exportTransformations.ts` (wire shape) and `docs/modules/treeModel/0203_tree_features.adoc` / `0400_refactoring.adoc`. Fixtures that
exercise it: `advanced_new/70_Countries/Country_Tr.json` and `e-commerce/01_Products/Bundle_TM.json` (`level_by_level` with
`initialExpansion` + `affectedNodeRefs` + `pageSize`), `advanced_new/20_Teams/Team_Tr.json` / `Product_TM.json` (`tree` with depths); all five also set `wholeTreeExpansion`.

**Wire shape (SME editor model <-> file).** `level_by_level`: `{type, initialExpansion?: {type: "all_levels"|"level_limit", level?, affectedNodeRefs?: [nodeId]}, pageSize?}`.
The editor's `enableInitialExpansion` / `enablePagination` checkboxes are not stored: on load they are `!!initialExpansion` / `!!pageSize`, on save an unchecked box drops
`initialExpansion` / `pageSize`. `tree`: `{type, expansionDepths: [{relationshipModel, maxDepth}]}` and *nothing else* - SME's export writes `expansionDepths: []` even when empty and
drops `initialExpansion`, `pageSize`, `enable*` when the strategy is `tree` (and `expansionDepths` when it is `level_by_level`). Defaults: type `level_by_level`, initial type `all_levels`,
`pageSize` 10, `maxDepth` 1. Separate flag `configuration.wholeTreeExpansion` ("Enable Expand/Collapse The Whole Tree", `true` or absent).

| # | Gap in Studio | What SME does | Effect today |
|---|---|---|---|
| 1 | **No Initial Expansion editor** (Enable checkbox, Type "Expand by a pre-defined level"/"Expand all possible nodes", Number Of Levels, Node Types To Apply) | Section shown for `level_by_level`; Number Of Levels only for `level_limit`; node types list may be empty = all nodes, entries unique, must reference an existing node type | `Country_Tr`'s `initialExpansion` is invisible (kept only in `ExpansionStrategy.extras` as a raw map); a new one cannot be authored |
| 2 | **No Pagination editor** (Enable Pagination, Page Size) | Checkbox + spinner (default 10), page size required when enabled, `level_by_level` only | `pageSize: 5` in `Country_Tr` invisible/uneditable |
| 3 | **No "Enable Expand/Collapse The Whole Tree" checkbox** (`configuration.wholeTreeExpansion`; no code reads or writes it, it survives only via `TreeConfiguration.extras`) | Four rules: an *Expand All PopUp* subheader element is an error while the flag is off (major and minor), and with the flag on there must be exactly one such element (error at 0 and at >1) | `SubheaderSlotPanelController.TREE_TYPES` already offers *Expand All PopUp*, so a user can add the element but has no way to switch the flag - SME then reports an error on a model Studio calls fine |
| 4 | **Switching the strategy only rewrites `type`** (`TreeConfigurationPanelController`) | Export keeps only the keys of the active strategy | Tree -> level_by_level keeps stale `expansionDepths`; level_by_level -> tree keeps `initialExpansion`/`pageSize`; SME drops them on its next save, so files churn between the tools. A `tree` strategy also never gets `expansionDepths: []` (`TreeModelLoadTest` pins `null`) |
| 5 | **No validators at all for the strategy** (6 tree validators exist, none touches it) | Rules in the meta model: `tree` with no expansion depth ("Expansion Depths must not be empty"); depth relationship not one of the tree's relationships (Invalid Reference) or used twice; `level_limit` without Number Of Levels; initial expansion enabled without a Type; `affectedNodeRefs` dangling or duplicated; pagination enabled without Page Size | None of these can be reported; a dangling reference is silently kept |
| 6 | **Dangling `affectedNodeRefs` after deleting a node type** (`TreeNodeTypesPanelController.removeNode` only clears `rootRef`) | Refactoring (`0400_refactoring.adoc`) lists initial-expansion node refs among the references to update/remove | `Country_Tr` -> delete `node-ecae3` leaves a broken `initialExpansion.affectedNodeRefs` |
| 7 | **Expansion Depths dialog offers already-used relationships** and has no hint for the non-recursive case | Relationship must be unique across depths; docs: a non-recursive relationship is always depth 1, and Max Depth must not exceed Data Services' `query.maxQueryDepth` | Duplicates can be created (validator #5 would flag them); no guidance |
| 8 | **Strategy combo can be empty** (a tree with no `expansionStrategy` shows a blank combo) | `type` always has a value (default `level_by_level`; SME's import maps an unknown shape to level_by_level with initial expansion off) | Only reachable through hand-written files; `NewModelFactory` already writes `level_by_level` |
| 9 | **Rename/move refactoring does not cover tree models** (no tree code in `refactoring/`) | Renaming a relationship or Document Model, or a node type / column, updates `expansionDepths[].relationshipModel`, `childRelationshipConfigurations`, `affectedNodeRefs`, ... | Cross-cutting, not only the expansion strategy; listed here because depths and initial expansion are two of its reference kinds |

**Suggested order.** (4)+(1)+(2)+(3) belong together: replace `TreeConfigurationPanelController`'s single combo by an *Expansion Strategy* panel group (combo; below it, for `level_by_level`, the Initial
Expansion and Pagination panels; for `tree` the existing depths panel), backed by a small model helper that maps `enable*` <-> presence of `initialExpansion`/`pageSize` and prunes the inactive strategy's keys on save
(typed `InitialExpansion`/`NodeRef` fields on `ExpansionStrategy` instead of `extras`, keeping the absent-vs-explicit round-trip rule). The Whole Tree checkbox is a plain `TreeConfiguration` boolean next to it.
Then (5)+(6) as `TreeExpansionStrategyValidator` (+ the four whole-tree rules) and the node-delete cleanup, then (7). (9) is its own task.

### Tree Model: full gap review against SME 13.0.2 (2026-09-26)

**Status (2026-09-27): the whole plan (steps 1-7) is built.** `RowActivation` (type event/insert/non_interactive) replaces the old `defaultRowAction` (`TreeNode.rowActivation`, `TreeNodeInheritance.Part.ROW_ACTIVATION`; a legacy `defaultRowAction` key survives unmigrated in `extras`, per the won't-do); `TreeColumn.width` is a `JsonNode` like the Overview's (whole numbers as `1`, decimals as `1.5`, minimum `TreeColumn.MIN_WIDTH` = 0.3); `TreeEvents` gives every context (row, header, multi-selection, row activation) its real event list, hiding copy/paste while a relationship has a link Document Model (`TreeProjectModels.hasLinkDocumentModel`); node type and insert-action Document Model pickers offer Combination Models too and, for inserts, only SME's real per-position candidates (`TreeHeterogeneity.insertCandidates`/`rootInsertCandidates`, not "every Document Model in the project"). All 8 gap-B validators are built as `TreeRootRefValidator`, `TreeVirtualScrollingValidator`, `TreeMultiSelectionElementValidator`, `TreeStylesValidator`, `TreeColumnValidator`, `TreeNodeStructureValidator` (columns-needed/inheritance/circular/child-column-conflict), `TreeChildRelationshipValidator` (relationship+role validity, missing/no node type) and `TreeActionsValidator` (every action/button/context-menu-group/row-activation rule, shared across row actions, context menus, the Virtual Root and Subheader/Footer/multi-selection buttons via `TreeValidationSupport`), plus `HeaderRolesValidator` and `TreeVirtualRootValidator` (label text) - 17 validators total, wired into `TreeModelValidationService`. Column editor gaps closed: `TreeColumnDialogController` now edits Label/Hide Label/Icon/Alignment/Styles/decimal Width, columns are kept sorted and guarded by pin direction (`TreeColumns`), and `TreeColumnValidator.isHeaderEmpty` flags an empty header live on the row. Small items: `screenReaderColumnRef` (Accessibility panel), tree Subtitles (Model Settings), attachment/multi-select column display mode (`ColumnMappingEditor.displayModeCombo`), and `TreeNodeActions.newDeleteAction` seeding every new node type. Virtual Root is built as `TreeVirtualRootPanelController` (enable checkbox, Label, reused `TreeNodeActionsPanelController`/`TreeNodeContextMenuPanelController`). New shared model helpers: `TreeHeterogeneity` (SME's sub type graph plus relationship-child-role/circular/insert-candidate logic, a heterogeneity superset of `DocumentModelHeterogeneity` that also takes Combination Models), `TreeEvents`, `TreeColumns`, `TreeNodeActions`, `TreeValidationSupport` (UI-independent) and `TreeActionContext`/`TreeProjectModels` (UI-side, project-aware). Two model-layer fixes found only by writing round-trip tests first (as planned in step 1): `TreeNode.configuration` needed `@JsonInclude(NON_NULL)` (a node without one gained an explicit `null`), and `overviewmodel.Alignment.horizontal` needed the same (a tree column with only `vertical` set gained an explicit `null` `horizontal` on save) - both caught by `AdvancedNewProjectModelsRoundTripTest`/`ProjectsFolderModelsRoundTripTest` on `Country_Tr.json`. Tests: `TreeModelVersion11Test`, `TreeHeterogeneityTest`, `TreeRulesTest` (one group per new validator), `FixtureWorkspacesTreeValidatorsTest` (no validator fires on any real sample-workspace tree); the whole models suite, the whole models-validation suite and every tree-related UI test (185 tests: `TreeModelEditorPanelsTest`, `TreeExpansionStrategyPanelsTest`, `TreeNodeConfigurationPanelTest`, `TreeConfigurationDialogsTest`, dialog tests) are green, plus a direct check of every other test that exercises the shared classes touched along the way (`EventButtonDialogController`/`EventButtonsPanelController`/`SubheaderSlotPanelController`/`AbstractMultiSelectionPanelController`/`MultiSelectionActionDialogController`/`ModelSettingsDialog`/`StylesPanelController`, now all taking an optional per-context event-suggestion list) - no regression, the only two failures seen anywhere are the pre-existing, unrelated `FixtureWorkspacesFormValidatorsTest` (City_Fm drift) and `TypesettingModelEditorTest.theModelSettingsDialogOffersNothingButTheRoles`. Running the *entire* `a12-studio-ui` suite in one process hit a Java heap OOM in this environment even before this change (a resource limit, not a regression); per-package runs are green. Not done: no dedicated FX test was written for `TreeVirtualRootPanelController` or for the column dialog's new Alignment/Styles/Icon fields beyond what the existing dialog tests already exercise indirectly - a good next addition, not a gap in the port itself.

**Baseline - read this first.** The authority for the tree format is the *installed* SME (`<installation>/bin/simple-model-editor/13.0.2/static/models/`: `TreeMetaModel.json`, `TreeModelEditor.json`,
`TM_NodeAction*.json`, `OMTM_*.json`, with `TreeMetaModel.validation.js` next to them; tree model version **11.0.0**), not the `C:\workspace\sme` checkout, whose `TREE_MODEL_VERSION` is `10.1.0`: the checkout still has
`defaultRowAction`, `majorElements`/`minorElements` and no `screenReaderColumnRef`. Studio's fixtures and `model-versions.json` are on 11.0.0, and the platform docs
(`documentation/2606-06-doc/tree_engine-treeengine-dev-docs.md`, "Row Activation" and "Migration Instructions" 11.0.0) say what changed. The installed meta model has the included models expanded (137 rules against 64 in the
checkout). Method: dump every `Rule`/`Field` of the installed meta model with a script, map the editor's `Control`s (`elementRef`) to meta model paths to get the real editor outline, and read the checkout's
`customConditions/*.ts`, `references/tmReferenceProvider.ts`, `middlewares/*` and `transformations/*` for what the TM-specific rules and defaults mean (still valid); event lists come from the installed client bundle.
Not gaps, on purpose: migration of older tree models (decided won't-do in `TODO.md`, so a pre-11.0.0 `defaultRowAction` gets no fallback), and SME's Subheader labels (in 13.0.2 `leftSlot` is captioned "Major" and `rightSlot`
"Minor", a leftover of the rename; the migration docs map `majorElements` to `rightSlot`, which is what Studio does).

**What the Studio editor has today:** five tabs - Tree (Root, Columns), Node Types (node type list + the selected node's configuration), Configuration (strategy, initial expansion, depths, whole tree, pagination, multi-selection,
drag & drop), Layout (virtual scrolling, row height / action column width, columns resize, Hide Label, styles), Custom Actions (Subheader, Footer). Eight validators: nodes/columns not empty, unique node Document Model, Document Model
reference exists, column mapping (known column, field exists, not `indexed = false`), hierarchical column, expansion strategy (8 rules), whole-tree flag vs. Expand All PopUp (4 rules).

**A. Editor / format gaps** (all checked against code; "S" small, "M" medium, "L" large)

| # | Gap | SME 13.0.2 | Effect in Studio today | Size |
|---|---|---|---|---|
| 1 | **Row Activation is not modeled; Studio edits the pre-11.0.0 `defaultRowAction`** | `nodes[].rowActivation` = `{type: non_interactive \| event \| insert, event, position (as_child default \| above \| below), documentModelRef}`; absent = view/edit. `configuration.inherit.rowActivation` replaces `inherit.defaultRowAction`. Events: `event_open_node`, `event_toggle_expansion`, the row events, or a custom name. Four rules (event required for `event`; position required and Document Model valid for `insert`; no copy/paste event when a relationship has a link Document Model) | `TreeNode.defaultRowAction`, `TreeNodeRowActivationPanelController` (checkbox + free event) and `TreeNodeInheritance.Part.DEFAULT_ROW_ACTION` all use the old shape. A real 11.0.0 `rowActivation` sits in `TreeNode.extras`: round-trips, invisible, and an `inherit.rowActivation` flag does not clear it. What Studio writes is unknown to the 11.0.0 tools. The only fixture with `defaultRowAction` is the synthetic `TreeModelNodeConfiguration.json` | M |
| 2 | **Column header cannot be authored**: no Label, Icon, Hide Label, Alignment, Styles | Column detail screen: Name, Width, Pin Direction, Fixed Width, Icon (name + theme), Label (multilingual) + Hide Label, Alignment (header/content x horizontal/vertical), Styles (header/content) | `TreeColumnDialogController` edits Name, Width, Fixed Width, Pin Direction only, a new column gets `label: []`: every column made in Studio has no header text and would draw SME's warning "Empty column header!". `alignment` exists only in `extras`, `styles` is an untyped `Map`. The Overview column dialog already has these panels | M |
| 3 | **Pin direction is not enforced** | Columns are kept sorted left-pinned, unpinned, right-pinned (`sortColumns`, on leaving a row and on import), and moving a column past one with another pin direction is refused ("You may not switch the position of columns with different pin directions.") | `TreeColumnsPanelController` neither sorts nor guards (the Overview has `resortByPinDirection`) | S |
| 4 | **Column width is an `Integer`** | Number, minimum 0.3, one fractional digit, default 1.0 (docs: 0.3, steps of 0.1) | `TreeColumn.width` is `Integer` and the dialog parses with `Integer.valueOf`, so "1.5" cannot be typed; a file with `"width": 1.5` goes through Jackson's default float-to-int coercion, expected to be read as 1 and saved as 1 (**not run yet** - the first task is a failing round-trip test; all fixtures happen to use whole numbers). The Overview solved the same with a `JsonNode`-backed `Column.width` | S |
| 5 | **`screenReaderColumnRef`** (Tree tab, "Accessibility": one of the columns) | New in the 13.0.2 meta model and editor; no explicit rule. The tree engine docs do not describe it; the Content Engine docs describe the same property on its table ("column with descriptive text that identifies the row for screen readers") | Not typed (only `extras`), no UI; the Layout tab's Accessibility panel has Hide Label only | S |
| 6 | **Subtitles** (`configuration.subtitle`, Header screen next to Labels and Hide Label) | Multilingual text | `ModelSettingsDialog` shows subtitles for the Overview Model only; `TreeConfiguration` has no field (survives in `extras`) | S |
| 7 | **Display mode of a mapped attachment / multi-select field** (node and child relationship column mapping) | Attachment: preview \| icon with file name \| icon \| file name; multi-select: default \| comma separated. Picking such a field sets the default, changing the field resets it, `displayModeIsRequired` | `TreeNodeColumn.Configuration` holds the two keys and `ColumnMappingEditor` copies them, but there is no editor ("No editor UI yet" in the class) | M |
| 8 | **A new node type gets no default Delete action** | `initializeNewNodeMiddleware`: `[{type: event, event: event_delete_node, secondary, destructive, icon delete_forever/filled, labelHidden, label/description/confirmation title+message in every header locale}]` (`middlewares/utils/actionDelete.ts`) | `TreeNodeDialogController` builds a bare `TreeNode`; deleting nodes at runtime needs a hand-built action | S |
| 9 | **Virtual Root** | `configuration.virtualRoot` = `{label (required), actions (insert always as_child), contextMenu}`; `enableVirtualRoot` is not stored (key present = on); it needs an "add" group to create the first node | Not modeled, no UI (only `extras`) | L |
| 10 | **Event candidates per context** | Row actions and context menus: `event_add_link, event_delete_link, event_delete_node, event_expand_sub_tree, event_collapse_sub_tree, event_open_node`, plus `event_copy_node, event_copy_node_and_children, event_cut_node, event_paste, event_paste_above, event_paste_below` only while no relationship has a link Document Model. Subheader / footer / virtual root: `event_add_root_node, event_expand_whole_tree, event_collapse_whole_tree` (+ `event_paste` likewise). Multi-selection buttons: `event_delete_nodes` (+ `event_copy_nodes, event_cut_nodes, event_paste`). Row activation: `event_open_node, event_toggle_expansion` | One static list in `TreeNodeActionDialogController` (no `event_open_node`, copy/paste offered unconditionally); Subheader, Footer and multi-selection buttons have no candidates (`event_add_root_node` occurs nowhere in the UI) | S |
| 11 | **Document Model candidates** | Node type: Document, Combination and Transformer Models not already used by another node type. Insert action: as child -> the Document Models of the node's child relationships (non-abstract); above/below -> the node's and its siblings' | The node picker lists `ModelType.DOCUMENT` only, so a Combination Model cannot be chosen (yet `TreeDocumentModelReferenceValidator` also reports one as "not found", see B); the action picker lists every Document Model | S |

**B. Validators** - SME's tree rules (meta model + custom conditions) against Studio. Present: see the list above. **Missing** (every rule is an ERROR unless marked):

| Area | Missing rule (SME message) | Notes |
|---|---|---|
| Configuration | `rootRef` must reference an existing child relationship configuration ("Invalid Reference") | only shown in the Root panel today |
| Configuration | Virtual scrolling on: Row Height and Action Column Width are required ("This field is required."); Row Height >= 1, Action Column Width >= 0.3 | |
| Configuration | Multi-selection: a Multi-Selection Subheader element while multi-selection is off (2 rules, one per slot); enabled without exactly one such element (2 rules) | `OverviewMultiSelectionElementValidator` is the model to copy, it only handles the Overview |
| Configuration | Virtual Root enabled: label required; its actions and context menu follow the action rules below | with gap 9 |
| Styles | `content.styles` values unique; every style reference (node, column header/content, action, button) exists in `content.styles`; a list has no style twice | |
| Columns | WARNING "Empty column header! The column header doesn't have an icon or a visible label."; Name required, Width >= 0.3 | pairs with gap 2 |
| Nodes | Document Model reference: a Combination/Transformer Model is valid in SME, Studio reports it as not found | `TreeDocumentModelReferenceValidator` uses `findOtherDocumentModel`; `hasOtherDocumentOrCombinedModel` exists |
| Nodes | "Node type must have at least 1 column" - unless it inherits its columns, or a parent node's child relationship configuration maps columns (`TMNoColumnRefInParentRelationshipConfig`) | |
| Nodes | A column mapped twice in one node ("The values of the field must be unique."); mapped field repeatable ("The reference is invalid. The referenced field is repeatable."); display mode missing for an attachment / multi-select field | the existing `TreeColumnFieldValidator` only knows "unknown column", "missing field", "indexed = false" |
| Nodes | "This node type has no parent to inherit." (an `inherit.*` flag on a node that is no sub type of another node type) | `TreeNodeInheritance` already has the sub type test |
| Nodes | WARNING "There are circular relationships between this node type and its child node type" | needs the child Document Model of a relationship and its sub types |
| Child relationships | Relationship Model and parent role must exist and fit the node's Document Model or its super type ("Invalid Reference"); relationship unique within a node ("This relationship already exists in current node type", not when inherited); column mapping: column reference valid and unique, field in the link Document Model valid, not repeatable, not `indexed = false` | |
| Child relationships | "At least one node type should be added for child role of relationship X"; WARNING "Missing node type for child role of relationship X" (heterogeneity: super type or every sub type needed); "This column is already defined in column mapping of a child node" | the SME docs' section on the warning explains the runtime error it prevents |
| Row activation | 4 rules, see gap 1 | |
| Actions (node rows, node context menu, virtual root) | Event required for an event action; position required for an insert action (not for the virtual root); event not copy/paste while a relationship has a link Document Model; Document Model exists; Priority required for row actions (not context menu); styles and annotation names unique; context menu group needs at least one action, an "add" group only insert actions | the action dialog enforces only the required event today |
| Buttons (Subheader, Footer, multi-selection) | Event required for a Subheader button; Priority required for a button; "Invalid Event." for a Footer / multi-selection button; no copy/paste event with a link Document Model; styles and annotation names unique | WARNING "Event export_excel works for CDM models only" (Subheader) is a kernel-backed check on the Overview's button list, skip |
| Header | `HeaderRolesValidator` (roles format/unique/roles file) and unique annotation names | the validator exists and is used by the Content, Relationship and Typesetting services, not the Tree service |

**C. Plan** (each step ends with tests: a `*_invalid.json` fixture per validator under `a12-studio-models-validation/src/test/resources/treemodel/`, panel tests through `FxTestSupport`; new panels follow the "Extract property editors" rule, messages name the field)

1. **Pin the risks with failing tests first** (S): `"width": 1.5` round trip (gap 4), a Combination Model as node Document Model (gap 11/B), a real 11.0.0 `rowActivation` node with `inherit.rowActivation` (gap 1). Add one fixture with all three.
2. **Format correctness** (M): gap 1 (model `RowActivation` + panel rework: type combo Default / Non interactive / Event / Insert with the event/position/Document Model fields, inherit key renamed, drop `defaultRowAction`), gap 4 (`JsonNode`-backed width, dialog accepts 0.3 to 1 decimal), gap 10 (event lists by context, copy/paste hidden while a relationship has a link Document Model - one small `TreeEvents` helper shared by all pickers), gap 11 (Combination Models offered and accepted).
3. **Validators without UI** (M, can run in parallel with 2): register in `TreeModelValidationService` in this order - references (`rootRef`, style references and uniqueness, child relationship, node column mapping), structure (columns needed, inheritance, circular, missing node type, virtual scrolling, multi-selection element rules), buttons and actions (one shared action rule set for row/context-menu/virtual-root/row-activation/buttons), header (`HeaderRolesValidator`). The relationship/heterogeneity helpers (child role's Document Model, sub types, "missing node type") are the only new logic; reuse `ElementIndex` and the sub type lookup behind `TreeNodeInheritance`.
4. **Column editor** (M): gaps 2 and 3 - reuse the Overview column dialog's Label/Icon/Alignment/Styles panels, type `TreeColumn.alignment`/`styles`, sort by pin direction and guard moves, then the "Empty column header" warning (also flagged live on the row like the Overview does).
5. **Small editor items** (S each): gaps 5, 6, 7, 8.
6. **Virtual Root** (L, last): gap 9 with its validators; it reuses the node action panels and the context menu group dialog.
7. **Docs and memory**: update this section and the tree memory, fix the stale "four tabs" javadoc of `TreeModelEditorController`.

Order rationale: 1 and 2 first because they are silent format problems in what is already shipped (the editor is enabled); 3 is the largest user-visible gain per effort since Studio reports about a third of SME's tree problems; 4 next because a column without a header is what every newly created tree gets; 9 last, it is the only sizeable new feature.

### Content Model: gap review (2026-09-26) - real gaps only (usability differences deliberately left out)

**Status (2026-09-26, same day): step 1 of the suggested order is done - gaps 1, 2 and 3 are closed.** `ContentModel.getDocumentModelId()/setDocumentModelId()` implement SME's
import/export transform (first `document` reference whatever its purpose; writing produces one canonical reference first - purpose `document-model-for-content-model`, alias `DM` -, drops other document references, keeps
other types, and unbinding clears `baseGroupId`; switching to another Document Model deliberately leaves the base group, which is then reported by name); `ContentConfiguration.baseGroupId` is a typed
field (`NON_NULL`); `ContentElementLibrary.NAMESPACE_VERSION` (0.9.0) holds the engine version; `NewModelFactory.buildContentModel` seeds `namespaceVersions` and builds the root Box with `ContentElementFactory`
(SME-style id and default props, so the node satisfies the schema); `ContentModelPreviewSession` uses the shared lookup. In the Model Settings dialog a Content Model now shows `ContentDocumentModelPanelController`
(combo, open, remove) and `ContentBaseGroupPanelController` (combo over the Document Model's groups shown by path, remove; only while a Document Model is bound), and no longer the generic Model References panel;
`ModelSnapshot` restores the binding and base group on Cancel. **Deviations from the plan above:** the Document Model panel does *not* reuse `TargetModelPanelController` (it has no deferred-save/dialog support, cannot clear a
selection, and words its errors for "Target Model") - it follows `GeneralSettingsPanelController`'s header-panel pattern instead; the group choice comes from `ElementIndex.allElements()`, i.e. the model's own groups, so groups
that only exist inside an *included* model (SME sees them, expanded) are not offered yet; the candidate Document Models are the project's Document Models plus the stand-ins for Combination Models (SME also lists transformer models,
which the studio has no type for). Not done and still open: 4 and everything from step 2 on. Tests: `ContentModelDocumentBindingTest`, `NewContentModelTest` (models), `ContentModelSettingsDialogTest` (dialog through its FXML, incl. Cancel).
Known unrelated failures seen while running the neighbouring suites: `FixtureWorkspacesFormValidatorsTest` (`City_Fm.json` drift), 5 tests of `ContentModelEditorPanelsTest` (the Element panel include is commented out in
`content-model-editor.fxml` since `a03beb89`, and one heading-text test), `TypesettingModelEditorTest.theModelSettingsDialogOffersNothingButTheRoles` (expects the name panel hidden, the dialog shows it).

**Status (2026-09-26, later): steps 2 and 3 are done - gaps 5, 7, 8, 10, 11 and 12 are closed; 6 (form elements) and 9 (setting values) only partly (6 is validated but still has no editor); 17 (roles validator for Content Models) was not touched.**
**Finding that changes the plan's premise: SME does not validate the whole tree against the parent/child rules, it applies them to editing operations only.** SME's own library declares the
three table row types childless (`childRules: C0`, the same as a Heading) and its editor fills them with cells itself, so every real table has "rows with children". A whole-tree structure check
therefore has to exempt the children of `TableHeadRow`/`TableBodyRow`/`TableFootRow` (`ContentStructure.ROWS`); the fixture-workspace sweep (`FixtureWorkspacesContentValidatorsTest`, all real Content
Models must give no error) found this on its first run. **Models (`a12-studio-models`):** `ContentStructure` is the rule engine behind everything (`violations`, `canRemove`, `canMove`, `canDuplicate`, `canPaste`,
`canRelocate`; an edit is allowed if it adds no violation that was not there before, so a hand-edited tree stays editable elsewhere - a limit of that: an element already in violation cannot be detected getting
worse), `ContentNodes` (event nodes of `onClick`/`onRowClick`, the `ce-field-reference` nodes inside Lexical text), `ContentUrls` (the URL allow-list, moved out of `TextRow`), `ContentModelContent` keeps unknown
keys (`extras`). **Validation (`validators/content`, 12 validators registered in `ContentModelValidationService`):** the two existing ones (duplicate ids now reported on the element instead of `content/root`) plus
`ContentNodeShapeValidator` (type, namespace, props required; Warning for a missing/other engine version), `ContentStructureValidator`, `ContentDocumentModelTypeValidator`, `ContentBaseGroupValidator`,
`ContentGroupReferenceValidator` (Repeatable Group, Add Row Action - repeatable only -, Image dynamic source), `ContentFieldReferenceValidator` (Field Output, Conditional conditions, references inside texts),
`ContentFormElementValidator` (elementId, existing, data type per element, data-context compatibility, children Warning), `ContentEventNodeValidator`, `ContentSettingsValidator`, `ContentWarningsValidator`. The
reference checks run on `DocumentStructure`, the Document Model with Includes expanded (`<include id>_<id>`), and on SME's data-context rules read from the bundle: the context of an element is the base group or the
closest enclosing Repeatable Group; a group is a candidate below its context; a field is a candidate when it can be reached from the context or a group above it without going through a repeated group; a form
element may not be more repeated than its context (`granularity`). `ValidationContext.cached` shares the expanded Document Model between the validators of one call. All findings are reported against the element's id
(events: against the element that holds them) and name the element, the setting or the reference. **Editor:** `updateActionState` asks `ContentStructure` for Delete, Cut, Move, Duplicate and Paste (and the handlers
check again, since shortcuts do not look at the buttons); the context menu got Add above/below and Paste above/below; drag and drop moves an element as one undoable step (`RelocateElementCommand`), the drop zones
(middle = last child, top/bottom quarter = above/below) accept only what `canRelocate` allows; the existing `tree-row-drop-*` styles show where it lands.
**Deviations from the plan above:** one validator per concern but not per plan item (e.g. no separate class for the Add Row Action, it is part of the group reference check); the walker does not stop at the first
group it cannot resolve like SME (it skips only the subtree below it, so the rest is still checked); reference findings are Errors although SME's own filter would drop them (see "What SME reports"); Delete is
guarded like Cut (SME's guard for Delete was not verified); `ContentSettingsValidator` covers only what could be derived with certainty - required texts/icons, Image source, unsafe URLs; SME's numeric controllers
(lengths, spacing, shadow, colors: "Invalid numeric value") are not ported, so an invalid length in a file is still accepted (the rest of gap 9). **Not done:** step 4 (nothing in the editor shows these findings yet:
no tree markers, issue count or settings badge for the new checks - gap 16), 5 (editors for form elements, Add Row Action group, Conditional) and 6 (migration). **Unverified by a running UI:** the drag handlers
(`setupDragAndDrop`) - the drop position and the move are tested through the controller's methods, the JavaFX drag events themselves were not exercised.

**Status (2026-09-26, later still): step 4 is done - gap 16 (the editor shows no validation result) and, for Content Models, gap 17 are closed.** `ContentModelEditorController.refreshIssues` asks the validation service for
everything wrong with the model (on load, after every save of the model - which covers panel edits, structural commands, undo/redo and the Model Settings dialog - and when another Document Model is saved) and shows it in
four places: the row of each element the findings are about (`validation-error`, or the new `validation-warning` in the warning color; the messages as tooltip), a summary under the tree ("Errors: n, Warnings: m", click goes to
the next element with a problem and wraps around), a message box above the selected element's settings (the `error-container` component, errors before warnings), and the badge/tooltip of the settings button, which now also
lists what belongs to a Content Model's own settings (`ValidationService.getSettingsIssueMessages`: a Document Model that is missing or of another kind, a base group the Document Model does not have). Findings about the
root without an element of its own (`content/root`) are shown on the root. `ContentModelValidationService` now also runs `HeaderRolesValidator` (SME's `ModelHeader` include applies the roles rules to Content Models), so a
Content Model with a `roles` annotation and no roles file in the workspace gets the same warning ("specifying roles needs a roles file") as a Document Model does; the other model types that lack the roles validator (gap 17 said "together with") were
not touched. **Limits:** the validation runs on the whole project model on every save (there is no incremental path; it is debounced by the editor's 300 ms save delay for typing); the count only includes findings about elements
of the tree, not the model-level ones (those are on the settings button); there is no marker on the ancestors of a flagged element, so a problem in a collapsed subtree is only visible through the summary.

**Status (2026-09-26, later still): step 5 is done - gaps 6, 13 and 14 are closed (the four items left over here were done afterwards, see the paragraph below) (15 stays out of scope, the validator covers it).** *One source of truth for what may be
referenced:* `DocumentStructure` (Includes expanded), `ContentFormElementTypes` (which Document Model elements each form element can show + the data-context rule) and `ContentDataContext` (the group an element is relative
to) are public in `a12-studio-models-validation`; the validators and the editor's pickers both use them, so the editor cannot offer what the validators reject. `ContentReferences`/`ContentDocumentReferences` (UI) put
that behind the property column's `Context` (`references()`, `locales()`); the Document Model with Includes is built on first use and rebuilt on every `refreshIssues`. **New rows:** `ReferenceRow` (group, repeated
group, field, form-element pickers; shown by path, stored by id; a stored id that is not available stays selected and marked; without a Document Model it says to bind one), `PairListRow` (localized texts `[{locale,text}]`,
annotations `[{name,value}]`; the key is removed when the list is empty), `ConditionsRow` (field picker, Equal / Not equal, value editor by data type: Yes/No/No data, Yes/No data, the enumeration's values, number or text;
choosing another field clears the value; a non-number is not stored), `ToggleRow.omitInitial` (SME omits default values), and the Add Row Action's group picker in `ClickEventRow` (repeated groups only). **New panels**
(read from SME's bundle: `contentengine-editor`'s form element settings): *Form Element* (element picker, and the Message Group Container/Display switches), *Localization* (label, hide label, hint, placeholder, suffix,
truncate suffix, unchecked/checked label - per element type as in SME), *Additional Settings* (readonly, message exposition Default/Tooltip + tooltips on top, show asterisk, auto complete, secret, auto expand, inline,
enable select all), *Annotations*, and *Conditions*; the Configuration panel's Group and Field rows are pickers now (they were text fields). **Not done at that point (done afterwards):** the date picker configuration (min/max/preselection year, absolute),
the field/group lists of the Message Group Container, and pickers for the Image's dynamic source and the inline references of a text. Group and field ids can no longer be typed into a Content Model that is not bound to a Document
Model (SME offers nothing then either); the raw JSON panel still allows it. Tests: `ContentReferenceCandidatesTest` (models-validation), `ContentModelEditorReferencesTest` (7, on the real Product model).

**Status (2026-09-26, after step 6): the leftovers of step 5 are done - date picker config, Message Group Container lists, Image dynamic source, references inside a text.** All four were read out of the installed bundle again. *Image:* `src.dynamic` is a `ReferenceRow` (`Kind.ATTACHMENT_GROUP`, `ContentReferences.attachmentGroups`: the groups below the data context whose usage type is `attachment`) instead of a text field; SME hides the row without a Document Model, this one stays visible but disabled so a stored id is still shown. *Date Picker Config:* new panel `form-element-date-picker-panel.fxml` with `DatePickerConfigPanelController` - Min Year, Max Year, Absolute, Preselection Year in `props.datePickerConfig` (`NumberRow`, a new whole-number row: stored as a JSON number, an invalid text is refused and marked, an emptied field removes the key and, when nothing is left, the whole object). Like SME it shows only while the picked Document Model element is a field of `DateType`, `DateTimeType` or `DateRangeType` (`ContentReferences.isDateField`), so the panel *follows* the Form Element panel: `ContentSettingsPanel.followsOtherPanels()`, and the editor shows the element again in such panels after an edit in another one. *Message Group Container:* new panel `message-group-container-panel.fxml` (Ignore formal errors moved here from the Form Element panel, where SME does not have it; the two info texts; "Automatically collect fields and groups" with the read-only lists of what is collected - fields and groups of the form elements below, without those inside a nested container or display, `ContentReferences.autoCollected`; Fields, Groups and Rules lists). `ListRow` edits a list of strings (pick a field / a group by path, or type the rule; the array stays, empty, like SME's creator makes it). The pickers offer `candidateFieldsThroughRepeatedGroups` (every field below the topmost group, repeated groups included - SME's `traverseRepeatableGroups`) and the groups below the data context. New `ContentMessageGroupValidator` (SME's checks: listing anything needs the Document Model, every entry must exist and be a field/a group; not restricted to the data context) and a warning in `ContentWarningsValidator` for a container without a Message Group Display below it (SME's `Nt`). *References inside a text:* `LexicalText` now edits texts that contain field and group references: a reference shows in the text area as SME labels it (`[path]`, `IndexOf(path)`) and is one unit - the words around it are edited stretch by stretch with the references as anchors, deleting all of the label removes it, changing part of it turns it into plain text; `LexicalText.insertReference(element, offset, id, path, group)` inserts one at the caret (splitting the run, taking the formatting of what precedes it, writing `displayOption: "value-only"` like SME's insert), and `LexicalTextRow` has two pickers under the text (field / group) plus, per field reference, SME's element settings: display (value only / label and value) and the default text (`missingValueText`), through `LexicalText.fieldReferenceOptions/setReferenceOptions`; the HTML export writes the `data-ce-field-ref-*` attributes. Only texts with links stay read-only (the hint now says only links). **A correction to the validators found on the way:** the group references inside a text are index references (`IndexOf`), and SME's candidates for them (the lexical module's `mp`) are the group the element is relative to and the *repeatable groups above it*, not the groups below it - `ContentFieldReferenceValidator` checked the wrong set and now uses `DocumentStructure.candidateIndexGroups` (new message `validation.contentGroupReference.notIndexable`); the picker offers the same set. **Limits:** the pickers are plain combo boxes (no type-ahead like SME's autocomplete, and the `[/` shortcut in the text does not exist); the caret is where the text area's caret is (at the end until it was focused); the auto-collected lists are refreshed when the container is selected or edited, not when a form element inside is changed; a group reference has no display options in SME either. Tests: `ContentModelEditorLeftoversTest` (8, on the real Product model), `LexicalTextTest` (+11), `ContentReferenceValidatorsTest` (+5), `ContentReferenceCandidatesTest` (+1).

**Status (2026-09-26, last): step 6 (migration) is dropped by decision - gap 4 stays open on purpose.** The owner decided not to support model migration: no load-time normalization of the Image `src` string, no `namespaceVersions` update, no migrate action; `Studio.checkModelVersions` stays header-based, and an older Content Model only gets the validator's version warning (recorded in `TODO.md`, "Won't do").

Scope: `ContentModelEditorController` + `content-model-editor.fxml` and everything it wires, against SME's `modules/contentModel` (frame, settings tab,
validator, transformers, reference providers), `resources/models/contentModel/{ContentMetaModel,ContentModelHeaderEditor}.json` and the platform docs
(`content_engine-contentengine-dev-docs.md`, `sme-sme-content-ba-docs.md`). **Important source note:** SME's *validation* is not in the SME repo. `validateContentModel`
only calls `createValidator` from `@com.mgmtp.a12.contentengine/contentengine-core` plus one `validator` per element module from `contentengine-editor` /
`formengine-content-elements-editor`. The rules below were therefore read out of the **installed** client bundle (`<A12 Tools>/bin/simple-model-editor/13.0.2/static/vendors.*.js`, minified:
search for `nodeValidators`, `groupReferenceValidator`, `extendedNodeValidatorByModule`, and the JSON-schema list starting at `version:"0.8.0"`). The installed migrator's latest
namespace version is **0.9.0** (SME 30.7.0's unpublished 0.10.0 mentioned above is not what the installed 13.0.2 ships) - re-read the bundle when the installation changes.

**What SME reports (all `Error` unless noted; SME's `areDocumentsValid` and the tab's issue count keep only `severity === "Error"`).**
`createValidator`: (a) `baseGroupId` set but no Document Model referenced -> "Base group ID is set but no document model is referenced"; (b) `baseGroupId` not found in the Document Model ->
"Can not find the group with id ..."; (c) a Repeatable Group without a Document Model -> "Missing document model" (traversal stops), or whose `groupId` is not found; while walking, every Repeatable Group
pushes its path as the *data context* of its descendants. Per element module: field-reference elements (Field Output `fieldId`, Conditional `conditions[].fieldId`, field/group nodes *inside the Lexical
tree* of Paragraph/Heading/..., `Image.src.dynamic`) must resolve in the Document Model and be a candidate *from the current data context* ("Invalid field reference", "No fields are available", "Invalid group
reference. Only the following group(s) are permitted: ..."); Repeatable Group / Add Row Action `groupId` (must be a Group; Add Row Action only repeatable ones: "Only references to repeatable groups are allowed.");
Save/Commit/Cancel/Delete Row actions need a Document Model; the 12 form elements: `elementId` -> "This element requires a Document Model.", "No Document Model element found for id ...", "The Document Model
element at path ... is not compatible with the current data context ...", plus a per-type field-type rule (Checkbox/Switch: Boolean or Confirm; Radio/Select: Boolean or Enumeration; Text Area/Text Line: String
or CustomField (Text Line also Number); Date types; Auto Complete: Enumeration or String with hint list; Multi Select/Checkbox Group: multi-select groups) and a *Warning* "Child elements are not supported for
this element."; every element with `controllers` (its typed settings): `Invalid setting for property "<path>".` when a value fails its converter/`errorExtractor`. *Warnings* (not blocking): Box without children,
Media Query whose root is not a Box, Table screen-reader column that is an action column, Button `vertical` without label and icon, Message Group Display outside a Message Group Container.
Reference validators return `{message}` without a severity, so SME's own `Error` filter drops them from the validity flag (they still show inline in its settings panel) - whether that is intended is unverified;
**a12-studio should report them as Error** (the engine cannot render such a model).
Model level: `technicalFields/documentModel` and `content/configuration/baseGroupId` carry an "Invalid Reference" rule (`ContentMetaModel.json`); the header include adds the shared header rules.
Wire rules: JSON schema `ContentModel` 0.8.0 - `additionalProperties: false` on header, content and configuration, and a node **requires `id`, `namespace`, `props`, `type`** and allows only those plus `name`, `children`.

Fixtures that exercise it: `e-commerce/01_Products/Product_OfBundle_CM.json` (Document Model bound, Repeatable Group, Conditional, Field Output), `basic/models/WelcomePage_CM.json`, `advanced_new/00_Welcome/WelcomePage_Ct.json` (unbound).
None of them carries a `baseGroupId` or a form element, so nothing in the test data exercises those paths.

| # | Gap in Studio | What SME does | Effect today |
|---|---|---|---|
| **Model settings / binding** | | | |
| 1 | **No Document Model setting.** The only way to bind is a hand-typed row in the generic *Model References* panel (`ModelSettingsDialog`, free-text purpose/alias). `ContentModelPreviewSession.documentModelId` accepts only `purpose = document-model-for-content-model` | Settings tab has a Document Model picker (candidates: document, composed document, combination and transformer models, `cmReferenceProvider`). Import takes the **first `document` reference whatever its purpose** (`ImportTransformations`); export writes exactly one `{purpose: "document-model-for-content-model", modelType: "document", alias: "DM"}` as the first reference and drops all other document-type references | A model SME calls bound (other purpose) is unbound in the studio preview and every reference check below; several document references can be added and SME collapses them on its next save. `HeaderModelReferenceValidator` only checks that *some* model with that id exists, so a Form Model as "Document Model" passes |
| 2 | **No Base Group setting** (`content.configuration.baseGroupId` is not a typed field; it only survives in `ContentConfiguration.extras`) | Picker over `DocumentModelUtils.getCandidateBaseGroups(dm)` (shows the model path, stores the group id), not relevant while no Document Model is set; **export removes `baseGroupId` when there is no Document Model** (`ExportTransformations.transformConfiguration`) | Cannot be authored; a stale `baseGroupId` stays after the Document Model reference is removed, which SME then reports as an error |
| 3 | **A new Content Model is not valid for the engine/SME.** `NewModelFactory.buildContentModel` writes `namespaceVersions: {}` and a root Box with **no `props`** (key omitted, `@JsonInclude(NON_NULL)`) | New models get `namespaceVersions: {"com.mgmtp.a12.contentengine": <latest>}` (0.9.0 in 13.0.2); every node created by SME has default props (Box: `style{display:flex,width:100%,...}`) | The installed migrator treats a model whose namespace version differs from the latest as migratable (`ns !== latest`), which is what SME's `handleNamespaceMigration` turns into "migration required" (`postMigrationConfig.isRequired` itself was not read); the engine logs `Namespace version mismatch ... model version is undefined, runtime version is 0.9.0`; the schema requires `props` on every node. `ContentElementDefaults.defaultProps("Box")` already exists |
| 4 | **No migration / version handling for content.** Studio reads the header `modelVersion` only (`Studio.checkModelVersions`, refuses the whole project on mismatch) | `DefaultMigrator` steps 0.9.0-pre.1 -> pre.2 (Image `src: "..."` -> `src: {static: "..."}`) -> 0.9.0; models below 0.7.0 are refused; old ones are flagged and migrated | A pre-0.9.0 model (Image `src` as string) opens with an empty Source panel. Low priority: all fixtures are 0.9.0 |
| **Validation (models-validation module: only `ContentRootElementValidator` + `ContentElementIdUniqueValidator` exist besides the generic header ones)** | | | |
| 5 | **No Document Model reference rules at all** (the whole contentmodel package never resolves a Document Model except the preview session) | Rules (a)-(c) above, plus every reference check of the Field/Group/Image/Conditional/Rich-text elements, resolved against the data context | A Content Model pointing at a deleted/renamed group or field, or a base group without Document Model, shows no problem in the studio and breaks in SME/the engine |
| 6 | **Form elements cannot be validated or even completed.** All 12 are insertable (`ContentElementLibrary`), get `elementId: ""` (`ContentElementDefaults.FORM_ELEMENT`) and have no editor for it (only the Raw JSON panel) | `elementId` required + resolvable + type-compatible + in the data context; Warning for children | Every freshly added Text Line/Checkbox/... is an SME error the studio cannot show, let alone fix without JSON |
| 7 | **Structure rules are only enforced when *inserting*** (`ContentInsertion`); nothing checks the finished tree | The editor keeps the tree valid on every operation (see 9-11); the engine and SME's tree assume it | A hand-edited file, a paste, a duplicate or a move (9-11) can leave e.g. a Table without foot, a Grid holding a Box, a Row holding a Row, and no validator says so. The rule engine (`ContentRuleEvaluator.matches`) already exists, it is just package-private |
| 8 | **No schema/shape validation.** `ContentModelContent` is `@JsonIgnoreProperties(ignoreUnknown = true)` (unknown `content.*` keys are silently dropped on save); nothing checks a node's required `id`/`namespace`/`props`/`type` or `namespaceVersions` completeness | Schema above; engine warns for every namespace in use without a matching `namespaceVersions` entry | Missing `props`/`namespace` on a hand-written node loads fine and is saved as is |
| 9 | *Mostly closed 2026-09-29/30.* **Per-property setting validation is not re-run on load/raw edit** (panels validate what is typed - width rules, URL scheme - but the model validator never does) | `Invalid setting for property "..."` for any typed setting whose value fails its converter | New `ContentSettingValueValidator`/`ContentPropertyFormatRules` cover the CSS length (`LengthRow`), spacing shorthand (`SpacingRow`) and whole-number (`NumberRow`) settings across all 9 `*-panel.fxml` files that use them, transcribed from each row's own FXML `path`/`types`/`keywords`/`units` attributes (a port, not a guess). Still open: `ColorRow`/`ShadowRow` settings - the CSS color/shadow grammar needs the installed-bundle treatment the rest of this review used, not a guess. Since 2026-10-01 the keywords/units live once in `ContentPropertyFormats` (`a12-studio-models`), read by both the rows (by `path`) and the rule table. An invalid length loaded from disk or typed in *Raw properties (JSON)* is now caught; an invalid color/shadow is still accepted silently |
| 10 | **Both existing validators report against the constant id `content/root`** | Issues belong to the node (`nodePath`) | `validateElement(model, nodeId)` (what every other editor uses) can never find a Content Model issue; there is no way to mark the offending node |
| **Editing operations that produce invalid trees** | | | |
| 11 | **Move up/down, Cut, Duplicate, Paste ignore the rules** (`MoveElementCommand` is a plain swap; Duplicate/Cut/Paste are enabled for every non-root element; paste already listed under "Not ported") | `isMovable`, `isCuttable` (removal must keep the parent's rule valid), `isDuplicable` (same module insertable as next sibling), `isPastable` all evaluate the rule on the simulated child list. The doc states "the editor ensures elements can only be moved to valid locations" | Table head/body/foot can be reordered or duplicated, a Grid Row deleted from under a Grid that requires one, an Expandable's Collapsed/Expanded copied. Delete is unguarded as well (whether SME guards delete was not verified) |
| 12 | **No drag and drop, no paste above/below, no insert above/below** (the rule engine already supports both positions, `ContentInsertion.Position`) | SME offers all three | Feature gap (listed for completeness, was already noted under "Not ported") |
| **Missing editors (only where there is no UI path at all; plain-text pickers for group/field ids are a usability difference and excluded)** | | | |
| 13 | **Add Row Action's group cannot be set.** `ClickEventRow.newEventNode` writes `props.groupId = ""` and only shows the type name | Event section with a group picker restricted to repeatable groups | An Add Row Action created in the studio can never become valid without raw JSON |
| 14 | **Conditional has no condition editor** (`conditions: []`, no panel) - already listed above | Field, operator, value per condition (AND logic) | A Conditional is always empty/always shown; conditions only via JSON |
| 15 | **Inline field/group references in Paragraph/Heading/... text** (`LexicalTextRow` is read-only when the tree holds them) | Authored inline on the canvas | Not authorable in the studio; also not validated (SME checks each of them, see 5) |
| **Editor feedback** | | | |
| 16 | **The editor shows no validation result at all.** No tree markers, no issue count, no per-element message; the settings badge only lists time zone / locale / roles (`getSettingsIssueMessages`) | Issue counts on the *Model Tree* and *Settings* tabs, per-element messages in the settings panel, `saveInvalid`, an invalid model is flagged in the explorer | Errors only appear as a marker on the model in the project tree, without saying where. `FormModelTreeController.errorMessagesByElementId` + `TabErrorBadge` are the pattern to copy |
| 17 | *Cross-cutting, not Content-specific:* `HeaderRolesValidator` is registered for Document and Typesetting models only, but SME's `ModelHeader` include applies the roles rules to the Content Model too | Roles rules on every model | No roles problems reported for Content Models |

Not gaps (checked): element library (all 40 default + 12 form modules present, categories/order as SME), parent/child insertion rules and Add-child filtering, default props per type, table column handling, click event
types, the model header (id equals filename, suffix, locales, name convention), preview (see "Content Model preview" for its own not-wired list: themes, sample documents, edits inside the preview).

**Suggested order.**
1. **Model settings first (1, 2, 3)** - everything else needs a resolvable Document Model. In `a12-studio-models`: `ContentModel.getDocumentModelId()/setDocumentModelId(...)` implementing SME's import/export transform (first `document` ref whatever its purpose,
   export = one canonical reference first, other document refs dropped, `baseGroupId` cleared without Document Model), used by `ContentModelPreviewSession` instead of its own lookup; a typed `ContentConfiguration.baseGroupId` (`@JsonInclude(NON_NULL)`).
   In `a12-studio-ui`: two extracted property editors following the CLAUDE.md rules, shown in `ModelSettingsDialog` for `ContentModel` (Document Model: reuse `TargetModelPanelController`'s plain combobox via a sibling FXML, candidates from
   `ProjectDocumentModels.getOtherDocumentModelsWithCombinations` - SME also lists composed and transformer models; Base Group: combo over the Document Model's groups (path shown, id stored), hidden without a Document Model). Fix
   `NewModelFactory.buildContentModel` (seed `namespaceVersions` from one engine-version constant next to `ContentElementLibrary.NAMESPACE`, give the root Box `ContentElementDefaults.defaultProps`). Tests: a `ContentModelTest` for the
   transform (incl. round trip of a model with two document refs), a `NewModelFactoryTest` case asserting the schema-required node keys.
2. **Validators (5, 6, 8, 10, and the warnings)** in `a12-studio-models-validation/.../validators/content`, one class + one `*_invalid.json` fixture + one test each, like `ContentValidatorsTest`; register in `ContentModelValidationService`;
   report against the node id (10); messages name the element/property per the CLAUDE.md rule. Suggested set: `ContentBaseGroupValidator` (a, b), `ContentGroupReferenceValidator` (c + Add Row Action, data-context aware),
   `ContentFieldReferenceValidator` (Field Output, Conditional, Image dynamic, Lexical inline refs), `ContentFormElementValidator` (per-type field-type table above; Warning for children), `ContentEventNodeValidator`,
   `ContentNodeShapeValidator` (required keys, namespace vs `namespaceVersions`, Warning), `ContentStructureValidator` (whole-tree rule check, see 3.), `ContentSettingsValueValidator` (9; needs the per-property converters that
   `ContentProps`/the rows already encode - extract them from the UI rows into `a12-studio-models` first), and the Warning-level checks. Field/group resolution: `ElementIndex` (`resolveElement`, `isInRepeatableGroup`, `resolveDisplayPath`) over
   `context.findOtherDocumentModel`; the data-context rule needs a small helper (base group + enclosing Repeatable Groups) that both this and the future field pickers use. Tighten `HeaderModelReferenceValidator`/a content-specific check so the
   Document Model reference must be a document-type model. Drop `@JsonIgnoreProperties(ignoreUnknown = true)` on `ContentModelContent` in favour of an extras map (same trick as `ContentConfiguration`), pinned by a round-trip test.
3. **Make the rules operational (7, 11)**: make `ContentRuleEvaluator` reachable (public facade on `ContentInsertion`): `validateTree(root)`, `canRemove`, `canMove(direction)`, `canDuplicate`, `canPaste(position)` - all "simulate the child list, evaluate the parent's rule"
   exactly like `insertableModules` already does. Use them to disable the toolbar/context-menu entries in `ContentModelEditorController.updateActionState`, and as the structure validator's engine. Then paste above/below and, last, drag and drop (12) on top of `canPaste`.
4. **Show it (16)**: `errorMessagesByElementId()` in `ContentModelEditorController`, an error/warning marker + tooltip in the tree cell, refreshed after every command (`afterCommand`/`onPanelChanged`), an issue count next to the tree, and the Document Model / Base Group messages in the settings badge
   (extend `getSettingsIssueMessages` with the two content element ids). Also add the roles validator to the content service (17) together with the other model types that lack it.
5. **Editors (6, 13, 14, 15)** once 1-2 give them a candidate-field service: form element panel(s) (`elementId` picker restricted to the fields the per-type table allows, label/hint/placeholder/annotations), the Add Row Action group inside `ClickEventRow`
   (repeatable groups only), the Conditional condition list. Inline references in rich text (15) stay out of scope like the rest of the canvas editing; the validator (5) covers them.
6. **Migration (4)** - dropped 2026-09-26 (not supported by decision, see `TODO.md`); was: last and only if older files show up: port `DefaultMigrator`'s one real step (Image `src` string) as a load-time normalization plus the `namespaceVersions` update, and let `Studio.checkModelVersions` stay header-based.

**Open questions.** (i) Severity of the reference checks - see above; the studio should still treat them as errors. (ii) Whether SME guards *Delete* like Cut (only `isCuttable`/`isMovable`/`isDuplicable`/`isPastable` were read). (iii) The exact candidate rule for "fields available from a data
context" lives in `contentengine-editor` (`candidateFields`/`candidateGroups`); port it from the bundle's behavior against a Document Model with nested repeatable groups rather than guessing (a fixture with base group + Repeatable Group + Conditional is needed - none exists today).

### Application Model: gap review (2026-09-27)

Reviewed against SME's `appModel` module (`document/amDocument.ts`, `amModule.ts`, `transformation/{appModelTransformer,appModelRefactoring,import|exportTransformations}.ts`,
`references/{amReferenceProviders,amPaths,regionReferenceProvider,sceneProvider,caseProvider}.ts`, `middlewares/onConstraintTypeChangeMiddleware.ts`,
`customConditions/validJsonCheck.ts`), its docs (`docs/modules/appModel/{01_introduction,02_editor,03_subeditors,04_glossary,05_refactoring}_app_model.adoc`), and every
`*AppModel*.json`/`*AM.json`/`*AppModelModule.json` fixture under `client/resources` (used to check what actually occurs in practice, not just what the schema
allows - see the Prior Scene point below). SME's Application Model validation is not a hand-written TypeScript validator set (unlike most other modules) - the
editor relies on JSON-schema shape plus the reference providers listed above to catch broken references, so there is no `*.ts` validator file to diff against; the
comparison below is against the kernel meta-model constraints as documented and as observed in fixtures.

**The editor already has a counterpart for every screen in SME's structure diagram** (`02_editor_app_model.adoc`): Region/Subregion/Layout/Default Region
(`RegionPanelController`/`SubregionsPanelController`/`LayoutPanelController`/`DefaultRegionPanelController`), Initial Activity + descriptor + Skip Data Loading at
both the model and the per-Menu level (`ActivityPanelController`, used from both `ApplicationModelEditorController` and `ModuleEditorController`), Modules with
Menu/Child Menu (name, Activity Descriptor, Skip Data Loading, per-locale Label via the shared `LocalizedTextPanelController`, Roles via
`ModuleRolesPanelController`/`AbstractRolesPanelController`), Flows, Scenes (name, description, Prior Scene, Default Case), Match Conditions, Scene Change
onEnter/onExit with both directive types (Region Clear: layout name + free-form JSON settings; View Add: component name, Constraints, ordered Models list,
free-form JSON configuration, Load Data), and Cases (name, per-locale Label, On Enter only - matching SME's "no On Exit for Cases" note). The Model Settings
screen (Name/Version/Description/Locales/Labels/Roles/Annotations) is the generic `ModelSettingsDialog` shared by every model type, and **SME's "Model
References" feature (`AppModelSettingsModelReferences`: referencing a Master-Detail Module Model adds a Module with a Master-Detail layout) is already built** -
`ModelReferencesPanelController` locks the Model Type to `module-masterdetail` for an Application Model, and `MasterDetailModuleGenerator` (used only by
`PreviewAppDeployer`, mirroring SME's `toFileContentForUpload` doing the expansion only at upload time, not at save time) generates the Module/Flow/Overview-or-Tree-scene/Detail-scenes/menu exactly as `document/masterDetailModule.ts` does. **Cross-model rename propagation is also already built, and more robustly than
SME's own**: `ModelReferenceRewriter` (generic JSON-tree walk keyed on field name, used by every model type) treats any object with a sibling `modelType` field as
a `ModelDescriptor` shape and rewrites its `name`/`documentModel`, so renaming a referenced Form/Overview/Document/Tree Model already updates every
`ViewAddDirective.models[]` entry project-wide - SME's own `refactorAppModel` (`transformation/appModelRefactoring.ts`) does the same but by hand-walking only
`onEnter`/`onExit` directives, so it is actually narrower. The gaps that remain are round-trip fidelity, two validator correctness bugs, one missing validator, a
small typo, and within-model refactoring (renaming/deleting a Region/Scene/Case) - listed below.

**Fixed 2026-09-27 (gaps 1-8):** all gaps except 9 (nested subregion editing, still not needed - no real fixture
nests past depth 2). Headlines: `Constraints` gained a catch-all `extras` map (`@JsonAnySetter`/`@JsonAnyGetter`,
the same trick `ContentConfiguration` uses) so a non-`MasterDetail` View Add directive's arbitrary keys survive a
load-then-save, plus a raw-JSON "Additional Constraints" area in `DirectiveDialogController` to author them (gap
1); `ApplicationUniqueNamesValidator.collectRegionNames` now scopes its `seen` set per parent (a fresh set per
recursive call, compared only against that region's own `subRegions`) instead of the whole tree (gap 2);
`ApplicationViewAddValidator` now checks a referenced model's actual `ModelType` against the `ModelDescriptor`'s
declared one (widening `document` to also accept a Combination Model, mirroring `context.hasOtherDocumentOrCombinedModel`)
before accepting it, and range-checks `Constraints.preferredWidth` (1-11), both as new validator errors and as
`DirectiveDialogController` submit-time gating (gaps 3, 5); `HeaderRolesValidator` is now registered in
`ApplicationModelValidationService` (gap 4); `DefaultRegionPanelController`'s and `DirectiveDialogController`'s
region fields are now editable `ComboBox`es whose dropdown shows every (sub)region as a breadcrumb (`RegionReferenceOptions`,
new, in `a12-studio-ui/.../editors/applicationmodel/`) sourced from the model's own region tree, while the
committed value stays the plain region name (gap 7); `"menuEnty"` is now `"menuEntry"` (gap 8); and a new
`ApplicationModelStructuralRefactoring` (`a12-studio-models-validation/.../refactoring/`) auto-rewrites (rename) or
clears (delete) `content.defaultRegion`/every `Directive.region` on a Region rename/delete (whole-model scope,
wired from `RegionPanelController`/`SubregionsPanelController`), a Flow's other Scenes' `priorScene` on a Scene
rename/delete (scoped to the owning Flow, wired from `FlowsPanelController`), and a Scene's own `defaultCase` on a
Case rename/delete (wired from `CasesPanelController`) - applied silently, consistent with the rest of this
codebase's cross-model rename machinery (`ProjectReferenceRefactoring`/`ModelReferenceRewriter`/`RoleRenameRefactoring`'s
own caller), not SME's own interactive per-reference Commit/Edit/Ignore dialog (still not built for any model type).
`ApplicationUniqueNamesValidator_region_invalid` (existing fixture) was reshaped to a true sibling collision (two
`HIDDEN` subregions under one parent) since its old shape (a subregion sharing its own ancestor's name) is valid
under the corrected per-parent rule; a new `..._region_validDifferentBranches` fixture pins that a name may recur
across unrelated branches.

| # | Gap in Studio | What SME does | Effect today |
|---|---|---|---|
| **Round-trip / data model** | | | |
| 1 | **`Constraints` cannot hold anything but `type`/`preferredWidth`.** `Constraints` (`a12-studio-models/.../applicationmodel/Constraints.java`) is `@JsonIgnoreProperties(ignoreUnknown = true)` with no catch-all map, and `DirectiveDialogController`'s Constraints section only offers a "MasterDetail" combo + Preferred Width field | `constraints` is genuinely free-form JSON on a `ViewAddDirective` - real fixtures carry arbitrary keys (`client/resources/test/modules/appModel/transformation/keepKnownConstraintsProperties/testAppModel.json`: `{"propertyA": "A", "propertyB": "B"}`, and one integration fixture nests a `label` array inside it) alongside the documented `MasterDetail`/`preferredWidth` shape; `onConstraintTypeChangeMiddleware.ts` only strips `type` when switching *away* from MasterDetail, it never assumes the object has no other keys | A View Add directive with non-`MasterDetail` constraints (e.g. Dashboard tile sizing/label config) silently loses those keys the moment the model is loaded and re-saved in the studio, and there is no way to author them through the dialog at all - only by hand-editing the JSON outside the studio. No existing fixture in `testing/workspaces/**` exercises this, so `BasicProjectModelsRoundTripTest` and friends do not catch it |
| **Validators (`ApplicationModelValidationService`, 3 validators)** | | | |
| 2 | **Region/Subregion name uniqueness is checked model-wide, not per-parent.** `ApplicationUniqueNamesValidator.collectRegionNames` recurses the whole region tree with one shared `seen` set | The docs are explicit: a Subregion's "name... acts as an identifier and must be unique **among siblings**" (`03_subeditors_app_model.adoc`) - two subregions in different branches of the tree may share a name | A model with, say, two unrelated `"HIDDEN"` subregions under different parents gets a false "not unique" error the user cannot fix by renaming anything meaningfully different |
| 3 | **`ApplicationViewAddValidator` does not check that a referenced model is the right *type*.** It only calls `context.findOtherModel(descriptor.getName())`/`getDocumentModel()` and checks non-null - `findOtherModel` ignores `ModelType` entirely | SME's reference provider (`getUIModel` in `amReferenceProviders.ts`) filters candidates by the `modelType` sibling (`form`→also `scdmForm`, `overview`→every overview variant, `document`→also combination/transformer) before offering them, and the Document Model reference calculator is filtered to document/combination/transformer types only | A `ModelDescriptor` naming e.g. an Overview Model while declaring `modelType: form` (or a Print Model as a `documentModel`) passes validation silently; the studio only catches "does not exist", never "wrong kind of model" |
| 4 | **`HeaderRolesValidator` is not registered for Application Models** (`ApplicationModelValidationService`'s validator list has no `HeaderRolesValidator`, unlike Tree/Content/Typesetting's services) | The kernel's shared `ModelHeader` roles rules (invalid characters, duplicates, empty entries, "not in the workspace's roles file") apply to every model's header `roles` annotation, Application Model included (`02_editor_app_model.adoc`'s "Roles" section) | An Application Model's header Roles field (edited via the generic `RolesEditorPanelController` in `ModelSettingsDialog`, so the field itself works) is never actually validated - a typo'd or duplicate role, or one absent from the workspace's roles file, is accepted silently. The same rules also have no equivalent for a Menu/Child Menu's own `permission` roles (`ModuleRolesPanelController` documents this as a deliberate simplification: "minus the roles-file warning") |
| 5 | *Minor:* **No range check on `Constraints.preferredWidth`** (docs: "a number from 1 to 11"; `preferredWidthField` only restricts input to digits, not the range) | Schema-level restriction to 1-11 | An out-of-range width is accepted and just clips/overflows at render time in the real app; low priority since it is cosmetic in the editor |
| **Editor: refactoring** | | | |
| 6 | **No within-model refactoring for Region/Scene/Case rename or delete.** Renaming a subregion (`SubregionsPanelController.editSubregion`), a scene (`SceneDialogController.nameField`) or a case (analogous) writes straight to the model with no scan for other references; deleting one is a plain list removal | SME ships this as a named, documented feature (`05_refactoring_app_model.adoc`, "Within the Model" table): renaming/deleting a Region updates Default Region and every Scene Change directive Region; renaming/deleting a Scene updates other scenes' Prior Scene; renaming/deleting a Case updates the owning Scene's Default Case - each shown in a dialog with a per-reference Commit/Edit/Ignore choice | Renaming or deleting a Region/Scene/Case leaves every `defaultRegion`/directive `region`/`priorScene`/`defaultCase` string that pointed at the old name dangling; the three validators (gap group above) will eventually flag it as "unknown", but only after the fact and with no assisted fix - the user has to hunt down and retype every reference by hand. (a12-studio has no interactive Commit/Edit/Ignore refactoring dialog anywhere yet, for any model type - its existing cross-model rename machinery, `ProjectReferenceRefactoring`/`ModelReferenceRewriter`, always auto-rewrites silently, so a lighter-weight "auto-rewrite + summary" is more consistent with the rest of the codebase than building a new interactive-dialog framework just for this.) |
| 7 | **Region-valued fields are free-typed, comma-separated text, not a picker.** `DefaultRegionPanelController.defaultRegionField` and `DirectiveDialogController.regionField` are plain `TextField`s (split/joined on `,`) | SME offers a proper dropdown sourced from the actual region tree, shown breadcrumb-style (`› CONTENT › HIDDEN`), built by `regionReferenceProvider` for every region-valued path (`content/defaultRegion`, `.onEnter/region`, `.onExit/region`) | A region name must be retyped exactly by hand every time; a typo is only caught later by `ApplicationSceneGraphValidator`, not prevented while typing, and there's no "pick from the tree" convenience at all |
| 8 | *Trivial bug:* **`ActivityPanelController.DESCRIPTOR_KEYS` misspells a suggestion**: `"menuEnty"` instead of `"menuEntry"` (SME docs: "instance, model, module, engine and menuEntry") | — | Picking that suggestion from the dropdown produces a descriptor key the runtime engine will not recognize; a one-line fix |
| 9 | *Fixed 2026-10-01: `SubregionsPanelController` shows the whole tree indented, with an "Add nested subregion" row action, per-sibling reorder (drag refused across levels), deep copy, and subtree-wide reference cleanup on delete.* **Only one level of the Region tree is editable.** `SubregionsPanelController`/`SubregionDialogController` manage only the top-level Region's direct `subRegions`; a subregion's own nested `subRegions` (schema-legal, arbitrary depth) can be neither seen nor added through the UI, though an existing one loaded from disk round-trips unharmed (`editSubregion` only overwrites name/layout, never the subregion's own children) | The region tree is recursive in principle | No real fixture anywhere in the SME resources tree nests past depth 2 (root + one level) - checked programmatically across every `*.json` under `client/resources` - so this is a theoretical schema capability nobody currently uses, not an observed real-world need |

**Not gaps (checked).** Prior Scene is scoped to the owning Flow in both the combo box (`SceneDialogController.priorSceneOptions`) and the validator
(`ApplicationSceneGraphValidator.checkPriorScene`'s own javadoc states this restriction). SME's `sceneProvider.ts` reference provider is structurally
model-wide (it excludes only the current scene, not other flows/modules), but a full sweep of every `priorScene` value in every SME fixture found **zero**
cross-flow references - every real Prior Scene names a scene in the same Flow - so the stricter same-flow scoping matches actual usage and is not a functional
gap, just a documented, intentional divergence from what the schema would technically allow. Also checked and fine: Match Condition and View-Add-Name suggestion
lists are editable combo boxes seeded with sensible (if not identical to SME's) defaults, matching the docs' "any value may be entered" framing for both; Layout
name suggestions (ApplicationFrame/MasterDetail/Dashboard/Stack/Null) are present everywhere SME lists them (`DirectiveDialogController`, `SubregionDialogController`);
Region/Layout `settings` and View Add `configuration` are already free-form JSON maps that round-trip unknown keys correctly (only `Constraints`, gap 1, doesn't).

**Suggested order.**
1. **Quick fixes first**: the `"menuEnty"` typo (8) and the `preferredWidth` 1-11 range check (5) - both one-line changes with no design work.
2. **Round-trip (1)**: give `Constraints` a catch-all `extras`/`Map<String,Object>` (the same trick `ContentConfiguration` and `GenericDirective` already use elsewhere in this codebase) so unknown keys survive load-then-save; extend `DirectiveDialogController`'s Constraints section with a raw-JSON fallback area for non-`MasterDetail` shapes, mirroring how `configurationArea`/`layoutSettingsArea` already work. Add a `testing/workspaces/**` fixture (or a dedicated unit fixture) with non-`MasterDetail` constraints to pin it, since none exists today.
3. **Validator fixes (2, 3, 4)**: make `ApplicationUniqueNamesValidator.collectRegionNames` scope its `seen` set per parent (a fresh set per recursive call, only compared against that region's own `subRegions`); make `ApplicationViewAddValidator` compare `context.findOtherModel(...).getModelType()`/`findOtherDocumentModel(...)` against `descriptor.getModelType()` before accepting a reference; add `HeaderRolesValidator` to `ApplicationModelValidationService`'s validator list. (The last one is really a cross-cutting gap - most model-type services besides Tree/Content/Typesetting lack it - worth a small follow-up ticket to wire it in everywhere, not just here.)
4. **Region picker (7)**: extract a small helper that walks `content.region`/`subRegions` into breadcrumb-labelled options (mirroring `regionReferenceProvider`'s shape), and use it to turn `DefaultRegionPanelController`'s and `DirectiveDialogController`'s region fields into editable combo boxes (editable, since "any value may be entered" per the docs) instead of plain text fields.
5. **Within-model refactoring (6)**, the biggest remaining item: on a Region/Scene/Case rename or delete (`SubregionsPanelController`, `FlowsPanelController`, `CasesPanelController`), scan the model for `content/defaultRegion`, every `Directive.region`, every other Scene's `priorScene`, and the owning Scene's `defaultCase`, and auto-rewrite (rename) or clear (delete) them, then surface what changed in a short summary (toast/dialog) rather than building a full interactive Commit/Edit/Ignore review flow from scratch - consistent with how `ProjectTreeMenuActions.rewriteProjectReferences` already handles cross-model renames silently. A full per-reference review dialog, if ever wanted for closer SME parity, is a separate, larger follow-up that would also benefit every other model type's rename operations, not just this one.
6. **(6) nested subregion editing (9)** only if a real project ever needs more than one level - no evidence today that it does.

### Master Detail Model: gap review (2026-09-27)

Full review against SME's `masterDetailModel` module (`document/index.ts`, `middlewares.ts`, `references/mdReferenceProviders.ts`, `transformer/masterDetailRefactoring.ts`), its self-hosted meta-model DM (`client/resources/models/masterDetailModel/ModuleMasterDetail.json` - the Document Model that defines the Master Detail Module Model's own JSON schema and every validation rule on it, edited through `ModuleMasterDetailEditor.json`), and the BA doc (`docs/modules/masterDetailModuleModel/index.adoc`, including its dedicated "Heterogeneous Overview Module"/"Tree Module" sections and the `testHeterogeneousModels.ts` cypress test).

**Already solid.** `MainDetailModelEditorController` + its five extracted panels (`MainModelReferencePanelController`, `FormWidthPanelController`, and the `AbstractDocumentFormMappingPanelController` subclasses for Form Mapping/Relationship Editors/Link Document Editors) mirror SME's `formMappingMiddleware`/`syncRelationshipEditors`/`syncLinkDocumentEditors` closely, down to reading the master model's `document-model-for-{overview,tree}` header references the same way `syncFormMappings` does. `MasterDetailReferenceValidator`/`MasterDetailTypeConsistencyValidator` cover every rule in `ModuleMasterDetail.json`'s content group (`overviewModelMustBeSet`/`treeModelMustBeSet`/the four `*MustBeValidReference` rules across all three mapping groups) with correctly field-naming messages. `MasterDetailModuleGenerator` is a faithful, tested port of `document/masterDetailModule.ts` (`MasterDetailModuleGeneratorTest`, 4 tests). Cross-model rename propagation already works and needs no masterDetailModel-specific code: `ModelReferenceRewriter`'s generic field-name-keyed JSON walk already includes `overviewModel`/`treeModel`/`documentModel`/`formModel` in `REFERENCE_FIELD_NAMES`, covering everything SME's own per-model-type `refactorMasterDetail` (`transformer/masterDetailRefactoring.ts`) does by hand. Form Width's 1-11 range is enforced by the Spinner's value factory.

| # | Gap in Studio | What SME does | Effect today |
|---|---|---|---|
| 1 | *Fixed 2026-09-29.* **No heterogeneous (abstract/subtype) or CDM expansion in the Form Mapping / Relationship Editors candidate lists.** `MainDetailModelEditorController.referencedDocumentModelIds`/`relationshipEditorDocumentModelIds` return the master model's `document-model-for-*` header references (respectively a tree node's `documentModelRef`) as-is | `middlewares.ts`'s `resolveAndFilterAbstractDocuments` post-processes that same raw list: a Composed Document Model member is replaced by its query root (`ComposedDocumentModelApi.getSCDMQueryRoot`), and an abstract Document Model (`dmInfo.abstract`) is dropped in favour of its direct sub types, recursively - this is a documented, cypress-tested feature (`testHeterogeneousModels.ts`; BA doc's "Heterogeneous Overview Module"/"Tree Module" sections: "If the super-type Document Model is abstract, it will not appear in the Form Mapping list... Select the correct Form Model for each subtype Document Model") | New `MainDetailModelEditorController.resolveAndExpand` does exactly this - replaces a Composed Document Model reference with its query root (`ComposedDocumentModelResolver.getQueryRootId`) and expands an abstract result into its concrete subtypes recursively (`TreeHeterogeneity.info`/`allDocuments`), applied to `referencedDocumentModelIds`/`relationshipEditorDocumentModelIds` (not `linkDocumentEditorDocumentModelIds`, matching SME's own `syncLinkDocumentEditors`). Pinned by 2 new `MainDetailModelEditorControllerTest` cases against in-memory fixtures; no real heterogeneous fixture exists under `testing/workspaces/**` yet (still worth adding, per the suggested order below) |
| 2 | **Binding Overview Models are not excluded from the Overview Model combo.** `MainDetailModelEditorController.overviewModelOptions()` lists every project Overview Model unfiltered | The BA doc is explicit: "Please note that Binding Overview Models are excluded from the list of available references." SME determines this dynamically (`omParser.isBindingOverviewModel`, scanning Form Models for a Binding/BindingRepeat component's Available/Selected Items overview) | The combo offers Overview Models that exist only to back an in-form multi-select widget (typically missing the columns/actions a standalone master list needs) as if they were normal top-level references. a12-studio already ported the detection logic as `OverviewBindingPurpose.resolve(id, ValidationContext)` (`modelsvalidation/validators/overview/`, built for the Overview Model's own gap review) but it is not yet called from any UI code - wiring it into this combo needs a `ValidationContext` built from the `ProjectItem`, which no `a12-studio-ui` code does today either |
| 3 | **`HeaderRolesValidator` is not registered in `MasterDetailModelValidationService`** | SME's own meta-model DM for this exact model type (`ModuleMasterDetail.json`) carries the identical roles rule set (invalid characters, duplicates, blank entries, "not in the workspace's roles file", "workspace has a roles file so roles are required") that every model header gets | A Master Detail Module's header Roles annotation (edited via the shared `ModelSettingsDialog`, so the field itself works) is accepted with a typo, a duplicate, or a role absent from the workspace's roles file, with no warning. This is the same cross-cutting gap already flagged for the Application Model (gap 4 there) - only `ContentModelValidationService`/`TreeModelValidationService`/`TypesettingModelValidationService` currently register it |
| 4 | *Minor, defense-in-depth:* **No validator requires a Form Mapping/Relationship Editors/Link Document Editors row's `documentModel`/`formModel` to be non-blank**, only that a non-blank value resolves (`MasterDetailReferenceValidator`) | `ModuleMasterDetail.json` marks both fields `requirednessConfig: absoluteOrRelativeToNextRepAncestor` (required) on all three groups | In practice moot for editor-driven changes (the UI always fills `documentModel` itself and blocks saving via its own inline error when `formModel` is blank), but a hand-edited or imported file with a blank `documentModel`/`formModel` in one of these lists passes model validation silently |
| 5 | *Fixed 2026-09-29.* **Form Mapping/Relationship Editors/Link Document Editors panels can go stale without a visible refresh.** `AbstractEditorController.modelSaved` only calls `onDocumentModelChangedElsewhere()` when the model saved elsewhere is a `DocumentModel` | Saving the Overview or Tree Model currently selected as this module's master list elsewhere (e.g. adding a new Document Model reference to it) doesn't retrigger `refreshFormMapping()`, since an Overview/Tree Model save doesn't match `instanceof DocumentModel` | `MainDetailModelEditorController` now overrides `modelSaved` (`super.modelSaved(event)` first, then also dispatching to `onDocumentModelChangedElsewhere()` when the saved model is an `OverviewModel`/`TreeModel`) so the three mapping panels refresh immediately instead of only on tab reopen. Pinned by the new `MainDetailModelEditorControllerTest`, which also pins gap 2's exclusion now that the harness exists |

**Suggested order.**
1. **Gap 3 first: one-line fix.** Add `HeaderRolesValidator` to `MasterDetailModelValidationService`'s validator list, same as the Application Model follow-up - worth doing both in the same small change since it's the identical missing line in two services.
2. **Gap 1, the real feature gap:** give `MainDetailModelEditorController` a helper mirroring `resolveAndFilterAbstractDocuments` - for each raw candidate id, resolve a CDM member via `ComposedDocumentModelResolver.getQueryRootId`, then expand via `TreeHeterogeneity.info(documentModels, id)` + `TreeHeterogeneity.allDocuments(documentModels, info, true)` (already exactly this shape; despite the class name it operates on the plain Document Model super/subtype graph via `DocumentModelHeterogeneity`, not on tree structure) instead of writing new expansion logic. Apply to both `referencedDocumentModelIds` (Form Mapping) and `relationshipEditorDocumentModelIds` (Relationship Editors) - not `linkDocumentEditorDocumentModelIds`, which SME's `syncLinkDocumentEditors` does not expand either. Add a heterogeneous fixture (mirroring the cypress test's `AbstractExample`/`ConcreteExample1`/`ConcreteExample2` shape) since none exists in `testing/workspaces/**` today.
3. **Gap 2:** add a UI-side way to build a `ValidationContext` from a `ProjectItem` (or a narrower standalone helper that doesn't need the full context) so `OverviewBindingPurpose.resolve` can be called from `overviewModelOptions()` and filtered out; this is the first UI call site for that validator-side utility, so the constructor helper is worth landing in a way other editors can reuse later.
4. **Gaps 4, 5, last:** low priority, fold in opportunistically next time this editor is touched.

**Gap 4 fixed 2026-09-29:** `MasterDetailReferenceValidator` now rejects a blank `documentModel`/`formModel` on
any `formMapping`/`relationshipEditors`/`linkDocumentEditors` row, in addition to its existing dangling-reference
check - pinned by `MasterDetailValidatorsTest.referenceValidatorReportsBlankDocumentAndFormModel`. Gap 5 stays open.

### Relationship Model: gap review (2026-09-27)

**Architecture note.** SME's Relationship Model is unlike every other module reviewed in this doc: it has **no hand-written editor or validator code at all**. It is itself a Document Model (`RelationshipMetaModel.json`, shipped as a real meta-model under `client/resources/models/relationshipModel/`) whose editor is a Form Model (`RelationshipModelEditor.json`) rendered through SME's own generic Form Engine (`RMEditorView.tsx` = `EnhancedFormEngine` + one custom `EventButton` for "Generate Document Models"), and every validation rule is a declarative Rule/Computation embedded in that meta-model rather than a `*Validator.ts`/Kotlin class. There is zero backend/kernel involvement (`grep` across `backend/` for "Relationship" is empty). SME's real, much richer relationship-*authoring* surface is the Model Graph Diagram (ER canvas) calling straight into `updateEntityCharacteristic` - the form editor compared here is SME's secondary, non-diagram path, and porting the diagram itself is already a closed decision (TODO.md, Composed Document Models section: "no plan to port the SME diagram UX"). a12-studio's hand-built `RelationshipModelEditorController` + `RelationshipModelValidationService` (9 validators) is a faithful, already fairly complete port of that form-based path: the Related Entities table (role/document model/computed upper-limit/computed explanation sentence/orderable, row-click opens a detail dialog, add hidden once 2 entities exist), the Link Document Model + Duplicates Allowed panel, the many-to-many gating warning, and the "Generate Document Models" feature (per-role generated DM with a `target` Include and, if set, a `relationship` Include of the link DM - matches SME's `dmGenerator.ts` shape field-for-field) are all present and correct. The gaps below are the real, verified differences.

**Fixed 2026-09-27 (gaps 3-7; gap 1 partial):** `RelationshipDocumentModelReferenceValidator`'s entity-loop lookup now calls `context.hasOtherDocumentOrCombinedModel(...)`, matching its own link-DM check three lines below (gap 4); `RelationshipModelEditorController.entityDocumentModelOptions()` now calls `ProjectDocumentModels.getOtherDocumentModelsWithCombinations(projectItem)`, so both the entity and Link Document Model pickers offer Combination Models too, matching SME's `rmReferenceProvider.ts` (gap 3 - Transformer Model still doesn't exist in a12-studio, so that third option stays moot); new `RelationshipRoleFormatValidator` ports SME's `[_a-zA-Z][-_.a-zA-Z0-9]*`/100-char role pattern (gap 5); `RoleRenameRefactoring` gained a `TreeModel` branch that rewrites a `TreeChildRelationshipConfiguration.parentRole` the same way the existing Query/Form/RelationshipUI/Overview branches do (gap 7, pinned by a new test in `RoleRenameRefactoringTest`); `HeaderRolesValidator` is now wired into both `RelationshipModelValidationService` and `RelationshipUiModelValidationService` (gap 6, batched with Combination Model's identical gap - see below). Gap 1's `storage`/`embeddedGroupPath` round-trip hole is closed the same defensive way `associationType` already was (`@JsonInclude(NON_EMPTY)`, pinned by `RoundTripReportRegressionTest`); gap 1's other three fields (`navigable`/`lowerLimit`/`candidateConstraints`) stay unfixed as originally scoped (lowest priority - unreachable from either SME's or a12-studio's own editor UI). The Labels item (already tracked in `TODO.md` as "hide them") is fixed too: `EntityCharacteristicDialogController` now calls `labelsController.setVisible(false)` after wiring it, so the panel stays registered (round-trip and SME's own read-only row-detail dialog both still apply) but is no longer shown in the default add/edit dialog.

Also reviewed: a12-studio's separate **Relationship UI Model** (`relationship-ui`, `Ru` - `RelationshipUiModelEditorController` + 3 validators, consumed by binding/overview screens). **SME has no equivalent module** - a case-insensitive search of `client/src`, `docs/` and `backend/` for "relationshipUi"/"RelationshipUI" returns nothing; the nearest SME concept is a Form-Model-canvas palette widget that creates a relationship-bound binding directly on a form, not a persisted model type. Since there is nothing on the SME side to diff feature-by-feature, Relationship UI Model is an a12-studio-original design and is left out of the gap table below rather than listed as "missing" functionality.

| # | Gap in Studio | What SME does | Effect today |
|---|---|---|---|
| **Round-trip / data model** | | | |
| 1 | **`RelationshipModelContent` has no `storage`/`embeddedGroupPath` fields** (the class is `@JsonIgnoreProperties(ignoreUnknown = true)`, so unknown keys are silently dropped on the next save) | `storage` (`EMBEDDED`/`EXTERNAL`, default `EXTERNAL`) + `embeddedGroupPath` (required when `EMBEDDED`) sit on a genuine hidden Form-Engine screen in SME's editor - never user-editable, but every real fixture (e.g. `PersonCompany.json`) carries `storage` with a concrete value regardless. `associationType` (SME: also hidden, default `SHARED`) already got the same defensive treatment in a12-studio's model (`@JsonInclude(NON_EMPTY)`, comment "kept only so old files round-trip"), so the two hidden-but-always-present sibling fields are handled inconsistently | A Relationship Model authored or exported by real SME (or the installed SME backend) and then opened, edited and re-saved in a12-studio silently loses its `storage`/`embeddedGroupPath` keys. No fixture anywhere in this repo (`a12-studio-models{,-validation}/src/test/resources/relationshipmodel/**`, `testing/workspaces/**/*_Re.json`) exercises these keys today, so `BasicProjectModelsRoundTripTest` and friends do not catch it - this is a latent hole, not an observed break |
| 2 | *Won't do (2026-10-01): Relationship Model 4.0.0 - the version a12-studio writes - removed all three (Data Services docs, "Relationship Model Version 4.0.0 - Unused Properties Removed"), so dropping them is correct.* **`EntityCharacteristic` has no `navigable` field; `Multiplicity` has no `lowerLimit` field; `EntityCharacteristic` has no `candidateConstraints` field** (`population`/`populationParameters`) - same silent-drop risk | All three exist in SME's schema, but (unlike `storage`) **none of the three is reachable from SME's own shipped editor UI either** - `navigable` only ever gets `initialValue: "true"` in `fieldConfiguration` (no screen shows it, so it is silently always `true` in practice in SME too), `lowerLimit` has no screen, and `candidateConstraints` exists purely as kernel-compatibility plumbing (`rmFixCandidateConstraints.ts` null↔`{}`-normalizes it on import/export so the kernel doesn't choke - SME's own comment: "To be non-breaking we set empty values on import... set back to null if empty on export") | Same latent, unobserved round-trip hole as gap 1, but lower priority - since SME's own users can't set `navigable: false`, provide a `lowerLimit`, or configure `candidateConstraints` through the reference UI either, a real-world SME-authored file is very unlikely to carry non-default values for these three, unlike `storage` (gap 1), which every real fixture *does* carry |
| **Editor** | | | |
| 3 | **The entity Document Model picker and the Link Document Model picker both only offer Document Models.** `RelationshipModelEditorController.entityDocumentModelOptions()` calls `ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.DOCUMENT)` exclusively, and `load()` passes that same list to `LinkDocumentModelPanelController` | SME's reference provider (`rmReferenceProvider.ts`) resolves both the entity `documentModel` and `linkDocumentModel` fields against `getModelReferenceCalculator(documentModelProto.type, combinationModelProto.type, transformerModelProto.type)` - Document, Combination, **or** Transformer Model. a12-studio has a real, built `ModelType.COMBINATION` (Transformer Model doesn't exist in a12-studio yet, so that third option is moot here) | A Relationship Model cannot be built against a Combination Model entity or link at all - the picker simply never lists one, even though the type exists and other model types (e.g. Query Model, per the priority table) already treat Combination Models as first-class Document-Model-shaped references |
| **Validators (`RelationshipModelValidationService`, 9 validators)** | | | |
| 4 | **Bug: `RelationshipDocumentModelReferenceValidator`'s per-entity check and its own link-DM check use two different, inconsistent reference lookups.** The entity loop calls `context.findOtherDocumentModel(entity.getDocumentModel())` (Document Model only); three lines later, the link-DM check on the very same validator correctly calls `context.hasOtherDocumentOrCombinedModel(linkDocumentModel)` (Document **or** Combination) | SME's reference provider (gap 3) treats entity and link references identically - both resolve against the same widened type set | Even after fixing gap 3's picker, an entity hand-pointed (or loaded from a real SME/installed-backend file) at a Combination Model is falsely reported "not found" by this validator, while the exact same reference used as the link Document Model is accepted - the two halves of one validator disagree about what a valid reference looks like |
| 5 | **No role name pattern/length validation.** The role `TextField` in `EntityCharacteristicDialogController`/`EntityCharacteristicsPanelController` accepts any non-blank text; no validator checks shape | SME's meta-model constrains `role` to the pattern `[_a-zA-Z][-_.a-zA-Z0-9]*`, 1-100 characters | A role starting with a digit, containing a space, or otherwise not a valid identifier is accepted by a12-studio; since roles are referenced from Query/Form/Tree/Overview/RelationshipUI as bare identifiers (`targetRole`, `parentRole`, `Has(...)`), an invalid one is likely to break those consumers or their own expression parsers downstream rather than fail cleanly here |
| 6 | **`HeaderRolesValidator` is not wired into `RelationshipModelValidationService` or `RelationshipUiModelValidationService`** | The kernel's shared `ModelHeader` roles rules (invalid characters, duplicates, "not in the workspace's roles file") apply to every model's header `roles` annotation | Same cross-cutting gap already flagged for Application Model and Master Detail Model above - a Relationship(-UI) Model's header Roles field is editable but never actually validated. Worth folding into that same follow-up ticket rather than fixing per model type |
| **Refactoring** | | | |
| 7 | **`RoleRenameRefactoring` does not cover the Tree Model.** Its `editsFor` dispatches on `QueryModel`, `FormModel`, `RelationshipUiModel` and `OverviewModel` - there is no `TreeModel` branch, even though `TreeChildRelationshipConfiguration` (`relationshipModelRef` + `parentRole`) is exactly the same "relationship id + role name" reference shape the other four consumers already get auto-rewritten | Not applicable to SME (SME has no dedicated rename-rewrite pass at all for this direction - see below) but is a real internal a12-studio inconsistency: renaming a role that a Tree Model's child-relationship configuration points at leaves `parentRole` silently dangling. It isn't invisible forever - `TreeChildRelationshipValidator` (2026-09-27 Tree Model review) checks the parent role against the entity's actual role and reports "Invalid Reference" - but only after the fact, with no auto-fix, unlike the four consumer types `RoleRenameRefactoring` already handles in the same undo step as the rename itself |

**Not gaps (checked).** SME itself has no bespoke rename-rewrite code for the "role renamed inside the Relationship Model → propagate to consumers" direction either (only the reverse - another Document Model renamed → Relationship Model's own `documentModel`/`linkDocumentModel` references updated, `relationshipRefactoring.ts`); SME's consumers instead re-resolve role names live through the same generic reference-provider framework used for every other broken-reference case, so a stale SME role is only ever caught as "invalid reference", never auto-fixed - a12-studio's `RoleRenameRefactoring` (auto-rewriting Query/Form/RelationshipUI/Overview in the same undo step, gap 7 aside) is actually **more capable than SME's own reference implementation** here, not just at parity. Also checked and fine: exactly-two-entities and no cross-relationship-uniqueness check match SME's own behavior (SME hard-enforces exactly 2 via `mustHaveExactlyTwoEntityCharacteristics` and has no rule preventing two Relationship Models between the same Document Model pair either); the Labels section being fully editable per entity in a12-studio's dialog (already tracked in `TODO.md`'s "Relationship Models" section as "hide them") is now confirmed against the actual SME behavior - SME's own row-detail dialog shows Labels read-only (add/remove disabled) - reinforcing that the existing TODO item is correct, not a case where a12-studio should instead build the feature out; `EntityCharacteristicSupport`'s human-readable Upper Limit / Upper Limit Description strings are a faithful port of SME's equivalent computed-expression text.

**Suggested order.**
1. **Validator bug (4)** is the cheapest, highest-value fix: swap the entity-loop lookup in `RelationshipDocumentModelReferenceValidator` to `context.hasOtherDocumentOrCombinedModel(...)`, matching its own link-DM check three lines below - a one-method fix with no design work.
2. **Combination Model picker (3)**: widen `RelationshipModelEditorController.entityDocumentModelOptions()` to also list Combination Models (mirroring however Query Model already does this, per the priority table), which then makes gap 4's fix actually reachable through the UI rather than only via hand-edited/imported JSON.
3. **Role pattern validation (5)**: add a lightweight regex+length check, either as a new small validator or folded into `RelationshipUniqueRolesValidator`'s file, matching SME's `[_a-zA-Z][-_.a-zA-Z0-9]*`/100-char rule.
4. **Tree Model role-rename coverage (7)**: add a `TreeModel` branch to `RoleRenameRefactoring.editsFor` that walks every node's `childRelationshipConfigurations` and rewrites `parentRole`, the same shape as the existing four branches.
5. **`HeaderRolesValidator` wiring (6)** - batch this with Application Model's and Master Detail Model's identical gap into one cross-cutting follow-up rather than fixing model-by-model.
6. **Round-trip fields (1, 2)**: give `storage`/`embeddedGroupPath` the same defensive `@JsonInclude(NON_EMPTY)`-plus-comment treatment `associationType` already has (gap 1, worth doing given real fixtures always carry `storage`); `navigable`/`lowerLimit`/`candidateConstraints` (gap 2) are lowest priority - add only if a real imported/hand-authored file is ever found to carry non-default values, since neither SME's nor a12-studio's UI can produce them today.

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
