# a12-studio

## Project background

a12-studio is a from-scratch reimplementation of **SME (Simple Model Editor)**, located at `C:\workspace\sme`. SME is part of the mgm A12 low-code platform and is the original/reference implementation of the same modeling tool concept.

Key differences in approach:
- **SME** (reference): polyglot monorepo — Kotlin/Spring Boot backend, TypeScript/React/Redux frontend (`client/src` split into `core`, `modules` [one per model type: documentModel, formModel, appModel, printModel, modelGraphDiagram, treeModel, overviewModel, relationshipModel, mappingModel, structuralMappingModel, combinationModel, queryModel, typeDefinitionModel, masterDetailModel, selectionModel, transformerModel, umModule, contentModel, etc.], `app`, `a12Extension`, `packages`), Electron desktop wrapper.
- **a12-studio** (this project): Java implementation — `a12-studio-ui` (JavaFX-style editors under `editors/documentmodel`, `editors/formmodel`), `a12-studio-data-services` (model services: documentmodel, combinationmodel, mappingmodel, printmodel, structuralmappingmodel), `a12-studio-server`, `a12-studio-commons`.

When adding, fixing, or evaluating an editor/model-type feature here, check the corresponding module in `C:\workspace\sme\client\src\modules\<modelType>` (editor UI/behavior) and `C:\workspace\sme\backend` (server-side validation/computation, e.g. SMT-based rule contradiction solving, print rendering) as the reference for what "correct" behavior looks like. Don't assume 1:1 architectural mapping — a12-studio is Java/desktop, not React/Redux, so translate concepts (load/save/validate flow, feature set) rather than copying implementation patterns. Key architectural insight: SME's Kotlin backend is just a REST wrapper around the same proprietary `com.mgmtp.a12.kernel:*`/`tdg:*`/`print:*` libraries a12-studio pulls in directly as JVM deps — so the port strategy is "call the same kernel APIs in-process," not "reimplement the REST endpoints."

A full write-up (feature-by-feature gap list for documentModel, full formModel feature inventory, priority-ranked list of unbuilt model types, backend/kernel capability map) is at `docs/sme-reference-comparison.md` — read that first for anything beyond this summary; update it (not just this file) when the comparison changes materially.

Overall a12 platform documentation (kernel, CMS, form_engine, expression, diagram_editor, content_engine, data_distribution, notification_center, build_and_deployment, plus cross-cutting `overall-*.md` files on modeling, UI/UX, workspace, QA, RBAC, deployment) lives outside this repo at `C:\workspace\a12\2606-06-doc`. Consult it for authoritative a12 domain concepts (e.g. what a directive, scene, or content type means in the platform) rather than guessing from code alone.

## Conventions

**Model id must equal filename.** A model's `header.id` field (JSON) must always equal its filename without the `.json` suffix (e.g. `Company_OM.json` → `id: "Company_OM"`), confirmed by convention across existing model files (`testing/basic/models/*.json`). Whenever code creates, renames, or copies a model file (`.json` under a project tree), the model's `id` must be (re)set to match the resulting filename (minus `.json`) and the model must be re-saved so the change is persisted. See `ProjectItem.idFromFileName()` in `a12-studio-models/src/main/java/de/a12/studio/dataservices/projects/ProjectItem.java` and its use in `renameTo()`/`createCopy()`/`NewModelFactory.createModel()`. Any future code path that produces a new/renamed model file (drag-move, import/export, batch operations) should be checked against this same invariant.

**Extract property editors.** Whenever a "property editor" (a labeled field/group of fields on a model editor screen) is added, extract it into its own component: a `<name>-panel.fxml` with a matching `<Name>PanelController.java`, included into the parent editor via `<fx:include fx:id="xyz" .../>`. Do this even for a single-field editor (e.g. a lone Spinner) — don't leave it inlined in the parent FXML.

- **Package placement:** if the panel is used by only one model editor (e.g. only `documentmodel`'s field editor, or only `overviewmodel`'s editor/dialogs), it lives alongside that editor — `a12-studio-ui/src/main/{java,resources}/de/a12/studio/ui/editors/<modeltype>/<Name>PanelController.java` / `<name>-panel.fxml`, next to `<ModelType>ModelEditorController`. Only panels actually referenced by two or more different model editors' packages (e.g. `LocalizedTextPanelController`, `AnnotationsPanelController`, the panels embedded in the generic `ModelSettingsDialog`) belong in the shared `a12-studio-ui/src/main/{java,resources}/de/a12/studio/ui/editors/propertyeditors/` package. If a single-editor panel later gains a second consumer in a different model editor's package, promote it into the shared `propertyeditors` package at that point (and demote back out if it later drops to one consumer again).
- Panel controller extends `AbstractPropertyEditor`; FXML root is a `TitledPane fx:id="root"` (required — `AbstractPropertyEditor` injects into a `TitledPane`-typed `root` field), `text="SOME TITLE"`, wrapping a `BorderPane` with the fields in `<center>`.
- Include an `error-container.fxml` in `<top>` only if the panel has per-field validation to surface (many single-field header panels, e.g. `LayoutPanelController`, skip it).
- Two binding styles depending on what the panel edits:
  - **Element-bound** fields: use `bindTextField`/`bindComboBox`/`bindSpinner`/`bindCheckBox` helpers + `setElement(Element)`.
  - **Model-header fields** (not tied to a single `Element`, e.g. width/layout settings living on the model's own content): follow `LayoutPanelController`/`ModelSettingsNamePanelController` — manual listener + a local `updatingFromModel` boolean guard, `setModel(SomeModel)` entry point, commit via `commitHeaderChange()`.
- In the parent editor's FXML: `<fx:include fx:id="xyzPanel" source="xyz-panel.fxml"/>` if the panel now lives alongside it (same directory), or `<fx:include fx:id="xyzPanel" source="../propertyeditors/xyz-panel.fxml"/>` if it's one of the genuinely shared panels.
- In the parent controller: `@FXML private XyzPanelController xyzPanelController;` (FXMLLoader auto-wires `fx:id + "Controller"`), then call `xyzPanelController.setModel(model)` (or `setElement`) from the parent's load/select logic.

**Reuse the Target Model panel for "pick one document model" fields.** `TargetModelPanelController` (`a12-studio-ui/src/main/java/de/a12/studio/ui/editors/mappingmodel/TargetModelPanelController.java`) + `target-model-panel.fxml` (same dir under resources) is a plain single-combobox "select a Document Model" property editor: `load(List<DocumentModel>, String selectedId)` / `setOnChange(Runnable)` / `getValue()`. Not wired through `AbstractPropertyEditor` — same reasoning as `MainModelReferencePanelController`/`OverviewReferencePanelController` (edits a content field + header `ModelReference` directly, not a document-model `Element`). First used to wire `MappingModel`'s `content.Target.dmId` in `MappingModelEditorController`: on change it syncs a header `ModelReference` (modelType=DOCUMENT, no purpose — matches SME's mapping-model JSON convention of untagged document references) and drops the stale one for the previously selected target. It currently lives in the `mappingmodel` package since Mapping Model is its only consumer (per the package-placement rule above). Before adding a new "pick a document model" field elsewhere (e.g. Mapping Model's future Source list, PreComputationFragment, StructuralMappingModel refs), check whether this class can be reused as-is — if a second model editor starts using it, promote it into the shared `propertyeditors` package first. The FXML's TitledPane text is hardcoded per instance (matches repo convention), so a new field with a different label needs its own sibling FXML (e.g. `precomputation-model-panel.fxml`) but can likely point at the same controller class if the semantics match (plain combobox, no radio/type toggle).

**Use `WidgetFactory` for widget construction, never raw constructors.** E.g. always create tooltips via `WidgetFactory.createTooltip(text)` (`a12-studio-ui/src/main/java/de/a12/studio/ui/util/WidgetFactory.java`), never `new Tooltip(...)`. Check `WidgetFactory` for an existing factory method before calling a JavaFX control constructor directly.

**Validator messages must name the field.** Never use generic phrasing like "this field is required" or "field is required" — always name the specific field (by its label/name) or describe what is actually missing (e.g. "Name is required", "Target model must be selected"). Generic messages don't tell the user which field or what's wrong, making them useless when multiple fields could be at issue. Applies to both new validators and edits to existing ones (form validators, property editors, model validators).

## Known issues

**Pre-existing `BasicProjectModelsRoundTripTest` failures.** `BasicProjectModelsRoundTripTest` (`a12-studio-models`) walks every JSON file under `testing/basic` and asserts load-then-save doesn't change content. As of 2026-08-01 it has ~11 pre-existing failures on `main`, unrelated to any specific in-progress feature work: `Invoice_FM.json`, `Person_OM.json`, `PrintModel.json`, `Invoice-Includes/Order_DM.json`, `GeneratedDMs/*`, `RelationshipOMs/*`. Confirmed via `git stash` isolation that these fail identically with zero code changes applied. When this test fails for files unrelated to what you're actively changing, don't assume you introduced it — check with `git stash` first.

Two root causes: (1) `A12Model.Header.labels` has no `@JsonInclude(NON_EMPTY)` (unlike `description`), so an absent `labels` key in the source file comes back as `"labels": []` after save — but some *other* fixtures (e.g. `RelationshipModel.json`, `TreeModel.json`, `TypeDefinition.json`) rely on labels round-tripping as an explicit `[]`, so naively adding `NON_EMPTY` to fix the first group breaks the second (tried and reverted — needs an absent-vs-explicit-empty-aware fix, same pattern as `RelationshipModelContent.linkDocumentModel`'s `JsonNode` trick, not a plain annotation). (2) `DocumentModelContent.modelInfo` writes `"immutable": false` when the source omits the key. Fixing the `labels` gap properly requires per-field absent/explicit tracking (JsonNode-based), not a blanket `@JsonInclude` change on the shared `A12Model.Header` class.

## Model Types

| Model Name                  | Abbr. | `modelType`                      |
| --------------------------- | ----- | -------------------------------- |
| Document Model              | DcM   | `document`                       |
| Type Definition Model       | TdM   | `document`                       |
| Composed Document Model     | CdM   | `document` (with CdM annotation) |
| Additive Document Model     | AdM   | `document` (with AdM annotation) |
| Relationship Model          | ReM   | `relationship`                   |
| Relationship UI Model       | RuM   | `relationship-ui`                |
| Tree Model                  | TrM   | `tree`                           |
| Form Model                  | FmM   | `form`                           |
| Overview Model              | OvM   | `overview`                       |
| Print Model                 | PtM   | `print`                          |
| Application Model           | ApM   | `application`                    |
| Query Model                 | QeM   | `query`                          |
| Mapping Model               | MaM   | `mapping`                        |
| Structural Mapping Model    | SmM   | `structuralmapping`              |
| Content Model               | CtM   | `content`                        |
| Combined Document Model     | CmM   | `combination`                    |
| Master Details Module Model | MdM   | `module-masterdetail`            |
| Transformer Model           | TfM   | `transformer`                    |
| Selection Model             | SeM   | `selection`                      |
| Print Setting Model         | —     | `print-setting` (deprecated)     |
| Print Typesetting Model     | TsM   | `typesetting`                    |
