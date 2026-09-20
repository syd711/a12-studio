# TODO

Rewritten 2026-09-20. Solved items, finished SME-gap-backlog entries (#1-#14) and empty sections were removed; what was done is in `git log` and, per feature, in `docs/sme-reference-comparison.md` (which was itself refreshed on 2026-09-20). Everything below is open. Conventions are in `CLAUDE.md`.

## Open decisions (need the owner)

1. **Kernel dependency: may a12-studio rely on `internal`/`a12internal` kernel classes?** The 2026-09-19 spike found kernel `31.1.1` viable in-process (condition validation, DM expansion, additive join; TDG is enterprise-only and stays blocked), but nearly everything it uses has no stability guarantee. Needs mgm's answer, plus contract tests if yes. Until then only slices that need the reference graph alone are built clean-room (as the loop detection was). Blocks: semantic condition validation, real DM expansion / additive join, ad hoc testing (below). Details: "Kernel dependency spike" in `docs/sme-reference-comparison.md`.
2. **Role rename in a Relationship Model does not update anything.** Queries (`targetRole`), Form Bindings and Relationship UI Models keep the old role name; the Query validators only flag the dangling reference afterwards. The role field commits per keystroke, so a rename needs its own commit-time design (commit on blur, or an explicit rename action) before refactoring can hook in. Decide which.
3. **Additive Document Model context.** An Additive Document Model's base model is found by reverse lookup over the Combination Models that use it, and if several use it the first one wins silently (`AdditiveDocumentModelResolver`). Original intent: the context only needs to be selected when the ADM is referenced more than once. Decide whether to add that explicit context picker (and where it is stored, since the ADM file has no reference to its base).
4. **Which model types to finish next.** Tree, Print and Content have editors and validators but are `enabled: false` in `model-versions.json` (opening one shows "not supported yet"); Mapping has target + sources only, Structural Mapping is a 24-line stub (both disabled); Print Typesetting, Transformer, Model Graph Diagram, Link/Document do not exist. The comparison doc's ranking is structural mapping → mapping → additive overlay editing → print → typesetting, but it does not know why the three built editors are still disabled. Decide the order, and what "ready to enable" means for the disabled ones.

## Open issues (defects and unverified behaviour)

- **Two tests are red on a clean `main`** (baseline as of the last full run, 2026-09-20, 1419 tests): `QueryValidatorsTest.sortFieldReferenceValidatorReportsMissingField` (expects 1 error, gets 0) and `ApplicationGroupFeatureTest.prefixesEveryModelAndRewritesReferences` (`App_QueryModel.json` expected to exist). Find out whether the tests or the code are wrong and fix; until then any *other* red test is a regression. See `.claude/memory/project_known_failing_tests.md`.
- **Overview Model: not every row of the sub-header slot panel is editable** (`subheader-slot-panel.fxml`). In SME all rows are editable for `testing/workspaces/basic/models/Company_OM.json`. Find out why rows are locked here and fix it.
- **Form `Binding` loses data on save.** Only the relationship linkage (`relationshipName`, `targetRole`) is modeled; `BindingContent` ignores unknown properties, so SME's UI-component configuration (dropdown / dual-pane / table-list selection, CDM child-activity wiring, edit-modal) is dropped on load-then-save. That breaks the round-trip fidelity the other models keep. Either keep the unknown properties (like the `JsonNode`-backed fields elsewhere) or document the loss in the UI.
- **Deleting a Form screen element leaves its id in `Control.dependentControls`.** SME's delete refactoring cleans these up; here only `DependentControlOptionsMustExistValidator` and the Dependencies tab's stale-entry warning report it. Add the cleanup to the delete command (one undo step).
- **`SortableColumnCustomCondition` is not ported.** `AbstractRepeat.initialSorting` is editable since 2026-09-06, but nothing checks that the chosen column is sortable; add a validator in `validators/form/` (the Overview Model has its own `OverviewInitialSortingReferenceValidator` as a template).
- **Rule/computation condition text is only syntax-checked in the editors.** `RuleLanguageSyntaxChecker` runs per keystroke in the rule editors but is not part of any `ModelValidator`, so a broken condition does not appear in the validation list or the tree. Register a validator for `errorCondition`, `precondition`, `operation`, `commonPrecondition` (semantic checks stay blocked on decision 1).
- **Multi-Column Section without `lg` raises no error** (SME's `mustHaveLayout`).
- **Refactoring gaps** (move/rename of a Document Model element): Print calculation steps, paths through a chain of Includes or an Additive base model, and an already open non-Document-Model editor (Print/Query/...) is not told to redraw, so it shows the old text until reopened (the model itself is correct). Cut/Paste is delete + paste-as-clone, so it is not refactored.
- **Stale fixture:** `testing/workspaces/advanced_new/.../City_Fm.json` has a dependent-group case `"false"` on the Confirm field `HelperDistrict`; `FixtureWorkspacesFormValidatorsTest` exempts it by name. Fix the fixture (or decide it is intended) and drop the exemption.

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
- **Overview filter items:** Boolean/Confirm criteria-based configuration and Enumeration/Multi-select Initial Criteria, Pinned Values and join behaviour are not modeled: no fixture on disk (including the `A12 Tools - 2026.06` sample workspaces) has an example and the BA doc only has screenshots. Implement once a real example JSON turns up.
- **Overview DateFragment/DateRange periods:** `OverviewElementOptions.defaultPeriods` reuses Date's subset ({date, year, yearMonth, month}) by analogy. Verify against a real example and adjust.
- **Wire shapes never checked against a real SME file** (no fixture has them; shapes come from SME's meta model and the Data Services docs): `Control.index`, Query `aggregation` (`alias` only from SME's transformer), a `Has(...)` call inside a Query `filterDefinition`, and the Query filter reference validator (no real `filterDefinition` with references to sweep for false positives). Verify when a sample appears.

## Parked (do not start unless asked)

- Markdown report per element (SME `createMarkdownReport`).
- Form Model `BindingRepeat` and Binding's UI-component configuration: blocked on Composed Document Model support.
- Query Model multi-target types (Composed Document Model / Transformer as target) and SME's structured-AST filter editor.
- Real Form Engine preview (the current in-editor preview is a wireframe; the real "Deploy → Preview App" exists separately).

## Won't do (decided 2026-09-19)

- AI-assisted Document Model generation (SME `documentModel/ai/*`).
- Model diff/compare editor.
