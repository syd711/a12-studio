# Commons
- Field and model name validation: The name of the Combined Document Model. It needs to fulfill certain conventions: Only letters, digits, hyphens, underscores and periods are allowed. Furthermore, the name of the model must not start with "xml" and must be at most 100 characters long.
- Add a drag handler for the items in C:\workspace\a12-studio\a12-studio-ui\src\main\resources\de\a12\studio\ui\editors\overviewmodel\dialogs\filter-items-panel.fxml
- Why are not all rows editable for C:\workspace\a12-studio\a12-studio-ui\src\main\resources\de\a12\studio\ui\editors\overviewmodel\subheader-slot-panel.fxml. For C:\workspace\a12-studio\testing\workspaces\basic\models\Company_OM.json all rows are editable in the SME.


# Git-Support:


# Relationship-UI:


# Shortcuts:

# Rule Editor



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
- Document Model field/group/rule editor is slow to switch between tree nodes (~200ms+ per selection, measured via the timing logs added to DocumentModelEditorController/DocumentModelFieldEditorController/TypeDefinitionPanelController/DataTypeConfigurationPanelController). Root cause: DocumentModelEditorController.loadEditor() calls `new FXMLLoader(...).load()` fresh on every single tree selection, even when switching between two elements of the same kind (e.g. field -> field), rebuilding the whole nested panel tree (~9 property-editor panels, each running its own initialize()) from scratch every time (~50-100ms of that alone). Fix: cache/reuse the loaded editor Node + ElementEditorController per editorFxml type in DocumentModelEditorController (keyed by FIELD_EDITOR_FXML/GROUP_EDITOR_FXML/etc.), and when the next selected element needs the same editor type, just call setElement(...) again on the cached instance instead of reloading the FXML. Needs care around ElementEditorController's destroy()/setElement() lifecycle (currently: destroy() on the old controller before loading a new one) and around DataTypeConfigurationPanelController's dispatcher-panel design, which deliberately calls setElement() on all 8 of its sub-panels every time (not just the active one) because DocumentModelFieldEditorController.updateErrorMessagesVisibility() reads dataTypeConfigurationController.patternProperty() (which always delegates to the String sub-panel) unconditionally - don't break that invariant while doing the FXML-reuse optimization.

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
- The Link Document Model reference and the checkbox "Duplicable" are only active for n:n Relationships. The Relationship Model Editor should show a warning if a 1:n or 1:1 Relationship Model has a Link Document Model or Duplicable set. Check the SME against this. 
- The labels which can be maintained for the Relationship Model are currently not used in the default UI for Relationships. => Hide them in the settings.
- 

# Composed Document Models
- Add separate property editor for cdm.queryRoot property
