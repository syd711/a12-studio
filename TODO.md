# Dialog To-Do

Dialogs that are explicitly documented as incomplete or only partially implemented.

---

## 1. MultiSelectionActionDialog — **Empty content**

**Files:**
- `a12-studio-ui/src/main/resources/de/a12/studio/ui/editors/overviewmodel/dialogs/overview-multi-selection-action-dialog.fxml`
- `a12-studio-ui/src/main/java/de/a12/studio/ui/editors/overviewmodel/dialogs/MultiSelectionActionDialogController.java`

**Status:** The FXML center pane contains only an empty `VBox` with an explicit comment `<!-- Intentionally empty for now -->`. The controller's Javadoc states the same.

**What's missing:** Event, Priority, Destructive, Icon, and any other action-level fields — matching the structure of `OverviewColumnDialogController`.

---

## 2. ModuleDialog — **Name only, further details pending**

**Files:**
- `a12-studio-ui/src/main/resources/de/a12/studio/ui/editors/applicationmodel/dialogs/module-dialog.fxml`
- `a12-studio-ui/src/main/java/de/a12/studio/ui/editors/applicationmodel/dialogs/ModuleDialogController.java`

**Status:** Currently only edits the module `name`. Controller Javadoc notes that further module details are expected to be added later.

**What's missing:** Menu configuration, flow assignments, and any other module-level properties.

---

## 3. SubregionDialog — **Name only, further details pending**

**Files:**
- `a12-studio-ui/src/main/resources/de/a12/studio/ui/editors/applicationmodel/dialogs/subregion-dialog.fxml`
- `a12-studio-ui/src/main/java/de/a12/studio/ui/editors/applicationmodel/dialogs/SubregionDialogController.java`

**Status:** Currently only edits the subregion `name`. Controller Javadoc notes that further subregion details (layout) are expected to be added later.

**What's missing:** Layout configuration and any other subregion-level properties.


## Misc
- Project Creation: Create empty folders with Preview_AM like the preview app workspace option.
- Create the same settings of that empty project
- Open link for user management
- Provide toggle view option for the console log of the preview app
- 

