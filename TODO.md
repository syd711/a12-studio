# Commons
- Change the different deployment buttons. Remove the progress circle and just disable the button while deploying. Add a progress bar to the footer, left to the preview app status. display an indetermine progressbar there with the message "Deploying Models..." as progress text while the models are uploaded to the preview app.
- Add a "Download Data" icon button on the start/stop button panel behind the console log button. When pressed, the preview data is downloaded. Take a look at the SME how this is done.
- Field and model name validation: The name of the Combined Document Model. It needs to fulfill certain conventions: Only letters, digits, hyphens, underscores and periods are allowed. Furthermore, the name of the model must not start with "xml" and must be at most 100 characters long.
- Add a drag handler for the items in C:\workspace\a12-studio\a12-studio-ui\src\main\resources\de\a12\studio\ui\editors\overviewmodel\dialogs\filter-items-panel.fxml
- Why are not all rows editable for C:\workspace\a12-studio\a12-studio-ui\src\main\resources\de\a12\studio\ui\editors\overviewmodel\subheader-slot-panel.fxml. For C:\workspace\a12-studio\testing\workspaces\basic\models\Company_OM.json all rows are editable in the SME.
- Add tooltips to the model icons with the model type name
- Fixed the project tree text color to white if the node is selected.

# Git-Support:
For the github support, add commit and revert buttons to the actions toolbar. Make sure that the buttons are only enabled when the file has outgoing changes. On commit, provide a dialog with textarea where the user can enter a commit. Initialize the textarea with the existing value from the version control view commit message text area.

# Relationhip-UI:
For the relationhip-ui model type in a12-studio-models/src/main/resources/de/a12/studio/models/model-versions.json, create a new editor with property fields.
Take the icon a12-studio-ui/src/main/resources/de/a12/studio/ui/icons/Model-Relationship.png and convert the square into a circle, keeping the same colors, the black border and the letter and save the icon Model-Relationship-UI.png in the same folder.
Use this new icon for model in the project tree. Take a look at the SME how the editor looks like and what additional validators are needed.
Take your time, create a plan and implement it.

# Shortcuts:
Fix shortcuts: Use F3 to open or show the project tree, use F4 to open or show the bookmarks view, use F5 to open or show the version control view. Use CTRL+B to toggle the bookmark state of the active workarea tab.
Add the shortcut CTRL+P to open the model preferences dialog of the active tab. add the shortcut CTRL+D to deploy the model of the active tab (if supported).
Add the shortcut CTRL+SHIFT+D to deploy the workspace. Update the tooltips of all these buttons and show the shortcut as additional info.
Update the shortcut list. group shortcuts for general shortcuts and editor based shortcuts.

# Form Models:
- check detached and embedded repeats
- check dnd behaviour
- check error handling when dropping from repeatable in regular group
- check dnd of sections with multi select
- check if trigger and dependency icons are visible
- merge tabs of settings and control, note that only fields with values shows deps
- check combobox for empty values so that these can be resetted
- for rules, initialize the name field based on the field or group 

# Document Model:
- Rename verschieben mit ...
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
- The Link Document Model reference and the checkbox "Duplicable" are only active for n:n Relationships. The Relationship Model Editor shows a warning if a 1:n or 1:1 Relationship Model has a Link Document Model or Duplicable set. 
- The labels which can be maintained for the Relationship Model are currently not used in the default UI for Relationships. => Hide them in the settings.
- Add "Generete Document Models" button if not already there.

# Composed Document Models
- Add separate property editor for cdm.queryRoot property
