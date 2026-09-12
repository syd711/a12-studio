# Commons
- Field and model name validation: The name of the Combined Document Model. It needs to fulfill certain conventions: Only letters, digits, hyphens, underscores and periods are allowed. Furthermore, the name of the model must not start with "xml" and must be at most 100 characters long.
- Add a drag handler for the items in C:\workspace\a12-studio\a12-studio-ui\src\main\resources\de\a12\studio\ui\editors\overviewmodel\dialogs\filter-items-panel.fxml
- Why are not all rows editable for C:\workspace\a12-studio\a12-studio-ui\src\main\resources\de\a12\studio\ui\editors\overviewmodel\subheader-slot-panel.fxml. For C:\workspace\a12-studio\testing\workspaces\basic\models\Company_OM.json all rows are editable in the SME.

# Overview Model
- `FilterItemDialogController`'s Filter Item editor (`overview-filter-item-dialog.fxml`) now covers String (matching + viewMode), Enumeration (viewMode), Number (ranges), and Date/DateTime/Time/DateFragment/DateRange (ranges/periods) - all fixture-evidenced (see `FilterItemOptions`'s class doc). Still not modeled, because no fixture anywhere on disk (including the broader `A12 Tools - 2026.06` sample workspaces) has an example and the BA doc only shows screenshots: Boolean/Confirm criteria-based configuration, and Enumeration/Multi-select's Initial Criteria, Pinned Values and join behavior. Implement once a real example JSON is found.
- DateFragment/DateRange's Periods row set (`OverviewElementOptions.defaultPeriods`) reuses Date's subset ({date, year, yearMonth, month}) by analogy - not fixture-confirmed. Verify against a real example once found and adjust if the actual subset differs.


# Git-Support:


# Relationship-UI:


# Shortcuts:

# Rule Editor

# Testing
c\workspace\a12-studio\a12-studio-models\src\test\java\de\a12\studio\models\projects\BasicProjectModelsRoundTripTest.java create a copy of this test using the advanced_new workspace and test the roundtrip with that workspace too.


# Form Models:
- check detached and embedded repeats
- check dnd behaviour
- check error handling when dropping from repeatable in regular group
- check dnd of sections with multi select
- check if trigger and dependency icons are visible
- merge tabs of settings and control, note that only fields with values shows deps
- check combobox for empty values so that these can be resetted
- for rules, initialize the name field based on the field or group 

# Performance:


# Document Model:
- check field init for new validation rules
- check white space in rule names and other name fields
- check references in error messages using the $$ notation
- Check the tree update after moving groups or creating validation rules
- Check validation rules for repeatable groups and field not filled. kcp3
- Validate: Document Models that should be connected via a Relationship must possess only one root group each. 

# Application Model


# Additive Document Model:
There's no dedicated ADDITIVE model type — additive models referenced in CombinationStep.additiveModel are plain DocumentModels (via DocumentModelIdRef). The superTypes annotation can be placed on those to declare their relationship. So "additive document model" in the CDM context just means a regular DM used as an additive step — the heterogeneity panel already covers it since it shows on DocumentModel. No separate handling needed; what's missing is just a TODO note to verify the filter also covers those DMs if someone opens their settings.

One note on the "additive document model" concern: there's no separate model type for it — a CDM's additive steps reference plain DocumentModels via DocumentModelIdRef. The superTypes annotation can legitimately live on those DMs (declaring upward parentage), and the filter in AnnotationsPanelController already covers all DocumentModel instances. The TODO captures the one thing worth manually verifying: that when such a DM is opened standalone in its own editor tab, the raw annotations panel correctly hides the heterogeneity annotations there too — which it should, since the filter keys off model instanceof DocumentModel, not off how the model is being used in a CDM.

The Context only needs to be selected if the Additive Document Model is referenced more than once in your workspace.
- Add and remove from ADM


# Relationship Models:
- ~~The Link Document Model reference and the checkbox "Duplicable" are only active for n:n Relationships...~~ Done: `RelationshipLinkDocumentModelValidator` + `LinkDocumentModelPanelController` already implement this, verified against SME's `RelationshipMetaModel.json` rules `warning_linkDocNotAllowed`/`warning_linkDupNotAllowed` (same many-to-many precondition, same warning text). Fixed a related gap where the warning didn't refresh live when an entity's multiplicity was edited via the Related Entities dialog (only `syncModelReferences` was wired to that panel's `onChange`) - now `RelationshipModelEditorController.onEntitiesChanged()` also calls `LinkDocumentModelPanelController.refreshValidation()`.
- The labels which can be maintained for the Relationship Model are currently not used in the default UI for Relationships. => Hide them in the settings.
- 

# Composed Document Models
- Add separate property editor for cdm.queryRoot property
