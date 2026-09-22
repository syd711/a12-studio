# TODO

Rewritten 2026-09-20. Solved items, finished SME-gap-backlog entries (#1-#14) and empty sections were removed; what was done is in `git log` and, per feature, in `docs/sme-reference-comparison.md` (which was itself refreshed on 2026-09-20). Everything below is open. Conventions are in `CLAUDE.md`.

## Open decisions (need the owner)

1. **Kernel dependency: may a12-studio rely on `internal`/`a12internal` kernel classes?** The 2026-09-19 spike found kernel `31.1.1` viable in-process (condition validation, DM expansion, additive join; TDG is enterprise-only and stays blocked), but nearly everything it uses has no stability guarantee. Needs mgm's answer, plus contract tests if yes. Until then only slices that need the reference graph alone are built clean-room (as the loop detection was). Blocks: semantic condition validation, real DM expansion / additive join, ad hoc testing (below). Details: "Kernel dependency spike" in `docs/sme-reference-comparison.md`.
2. **Which model types to finish next.** Tree, Print and Content have editors and validators but are `enabled: false` in `model-versions.json` (opening one shows "not supported yet"); Mapping has target + sources only, Structural Mapping is a 24-line stub (both disabled); Print Typesetting, Transformer, Model Graph Diagram, Link/Document do not exist. The comparison doc's ranking is structural mapping → mapping → additive overlay editing → print → typesetting, but it does not know why the three built editors are still disabled. Decide the order, and what "ready to enable" means for the disabled ones.

## Open issues (defects and unverified behaviour)

Fixed 2026-09-21 (details in `git log` and `docs/sme-reference-comparison.md`): the two red tests (both were stale fixtures: a Query sort fixture still in the old `sortBy`-wrapper shape, and `basic/models/QueryModel.json`, which the application-groups test read, was removed), the Overview sub-header slot rows (Search/Filter/Multi-Selection are now editable, with SME's shared element fields), Form `Binding` losing its UI-component configuration on save, stale `dependentControls` ids after a Form delete/cut, `SortableColumnCustomCondition`, syntax validation of rule/computation conditions as a `ModelValidator`, the Multi-Column Section `lg` requirement, the `City_Fm.json` fixture, and the refactoring gaps (Print calculation steps, open editors of a rewritten model, paths through a chain of Includes or an Additive base, Cut/Paste as a real move). Nothing is open in this section any more; the known limits of the refactoring work are in `docs/sme-reference-comparison.md` ("Move/rename refactoring").

Fixed 2026-09-22: role rename in a Relationship Model now updates every cross-model reference (Query `targetRole` on sorts/links/`Has()`, Form Binding `targetRole`, Relationship UI Model `targetRole`, Overview `ColumnLinkReference.targetRole`) - `RoleRenameRefactoring`, applied from `RelatedEntitiesPanelController.applyRoleRename` once the entity edit dialog is confirmed (the role field only ever commits there, not per keystroke on the panel itself, so the old/new role names are unambiguous). Pinned by `RoleRenameRefactoringTest` (there was none before).

Fixed 2026-09-22: Additive Document Model context ambiguity is now resolved the same way SME does it (`SelectModelWithContextView`) - never persisted, resolved fresh on every open. `AdditiveDocumentModelResolver.findCandidateContexts` returns every Combination Model that references the Additive Document Model; `DocumentModelElementsTreeController.resolveAdditiveState`/`resolveContext` uses it silently when there is at most one candidate, and otherwise shows the new "Select Combination Model" picker (`AdditiveContextDialogController`/`Dialogs.showAdditiveContext`) on the tab's initial open only - a background `modelSaved` re-resolve keeps the previous choice if it's still valid, or silently falls back to the first candidate rather than interrupting the user. The choice lives only for the open editor tab; closing and reopening asks again. `AdditiveDocumentModelResolver.findBaseModel` (used by validation/refactoring, which have no user to ask) keeps its original silent first-wins behavior, now implemented in terms of `findCandidateContexts`.

## Open todos

### Form Models
Manual checks (no known defect, just not yet verified):
- Detached and embedded repeats.
- Drag and drop in general; error handling when dropping from a repeatable group into a regular group; dnd of sections with multi-select.
- Trigger and dependency icons on tree rows: SME's T/D flags are not ported, so check what is shown and add them.
- Merge the Settings and Control tabs of the field editor; note that dependencies are only shown for fields that have values.
- Every combo box should offer an empty value so a selection can be reset.
- When a rule is created, pre-fill its name from the field or group it targets.

Features:
- A Form Model that is bound to a Combined Document Model cannot be the host of an include: the form editor does not resolve a combination for its tree, so *Include Form Model...* is not offered there.

### Document Model
Manual checks:
- How the fields of a new validation rule are initialised.
- Whitespace in rule names and other name fields.
- References in error messages that use the `$path$` notation. (Rename/move rewriting for these is unit-tested; what is left is checking it in the UI.)
- The tree updates after moving groups or creating validation rules.
- Validation rules for repeatable groups and "field not filled" (kcp3).

Features:
- Base Model / Include pickers should not offer candidates that would create a loop: SME filters the Base Model picker, and calls the combination module from a Document Model's Include picker (`createsIncludeLoop`). Here the loop is only reported after selection.

### Additive Document Model
- Verify manually: an Additive Document Model opened on its own must hide the heterogeneity annotations in the raw annotations panel too (the filter keys off `instanceof DocumentModel`, and `AdditiveDocumentModel` extends it, so it should hold).
- Add and remove elements against the base model (SME's overlay editing mode; only the read-only "Additive Elements Only" preview exists).

### Relationship Models
- The Labels (entity add/edit dialog, `entity-characteristic-dialog.fxml`) are not used by the default UI for relationships: hide them.

### Composed Document Models
- There is no Composed Document Model type, only the CdM annotation on a plain Document Model. Add a dedicated property editor for the `cdm.queryRoot` annotation (today it is only reachable in the raw annotations panel), following the heterogeneity panel.

### Query Model
- Filter expressions are only existence-checked; type and enum-value checking is not done, and there are no "did you mean" candidates.
- `QueryFieldReferenceValidator` (the `fields[]` projection) still accepts `indexed = false` fields, and the tree checkbox does not disable them.
- The Model Tree tab's root DM is picked in the Settings tab, not through an ER-diagram picker like SME.

### Overview Model
- No dedicated section in `docs/sme-reference-comparison.md` yet (the survey only says "present"); write one, like Query/Selection/Combined have.

## Blocked (waiting for an input)

- **Ad hoc testing (Document Model, SME Alt+T)** is deferred, not rejected: it will be built. A reduced test Document + Validation model is rendered in a preview. It needs (1) the kernel's `createReducedDocumentModel` (decision 1), (2) compiled `validationCode` (kernel `generateValidationCode`), (3) a real Form Engine renderer plus a Document-to-Form-Model generator (SME's `fmm-support` is private, licence unconfirmed; see `.claude/memory/project_form_preview_real_rendering.md`). Revisit when 1 and 3 are answered.
- **Overview filter items:** Boolean/Confirm criteria-based configuration and Enumeration/Multi-select Initial Criteria, Pinned Values and join behaviour are not modeled: no fixture on disk (including the `A12 Tools - 2026.06` sample workspaces) has an example and the BA doc only has screenshots. Re-checked 2026-09-22 (a12-studio fixtures, the upstream `A12 Tools - 2026.06` workspaces, and SME's `overviewModel` TS types, which are simpler than a12-studio's own model and have no `FilterItem`/`FilterGroup`/pinned/initial-criteria concept at all) - still no example anywhere. Implement once a real example JSON turns up.
- **Overview DateFragment/DateRange periods:** `OverviewElementOptions.defaultPeriods` reuses Date's subset ({date, year, yearMonth, month}) by analogy. Verify against a real example and adjust.
- **Wire shapes never checked against a real SME file** (no fixture has them; shapes come from SME's meta model and the Data Services docs): `Control.index`, Query `aggregation` (`alias` only from SME's transformer), a `Has(...)` call inside a Query `filterDefinition`, and the Query filter reference validator (no real `filterDefinition` with references to sweep for false positives). Verify when a sample appears.

## Parked (do not start unless asked)

- Markdown report per element (SME `createMarkdownReport`).
- Form Model `BindingRepeat` and Binding's UI-component configuration: blocked on Composed Document Model support.
- Query Model multi-target types (Composed Document Model / Transformer as target) and SME's structured-AST filter editor.
- Real Form Engine preview (the current in-editor preview is a wireframe; the real "Deploy → Preview App" exists separately).

## Misc
- Init datetime format for access generated fields

## Won't do (decided 2026-09-19)

- AI-assisted Document Model generation (SME `documentModel/ai/*`).
- Model diff/compare editor.
