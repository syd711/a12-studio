package de.a12.studio.ui.util;

import de.a12.studio.models.ModelType;

public interface Icons {

  String WINDOW_MINIMIZE = "mdi2w-window-minimize";
  String WINDOW_MAXIMIZE = "mdi2w-window-maximize";
  String WINDOW_RESTORE = "mdi2w-window-restore";
  String WINDOW_CLOSE = "mdi2w-window-close";

  String FOLDER = "mdi2f-folder";
  String FOLDER_OPEN = "mdi2f-folder-open";
  String FOLDER_OUTLINE = "mdi2f-folder-outline";
  String FOLDER_OPEN_OUTLINE = "mdi2f-folder-open-outline";
  String FOLDER_LOCK_OUTLINE = "mdi2f-folder-lock-outline";
  String FILE_OUTLINE = "mdi2f-file-outline";
  String FILE_TABLE_OUTLINE = "mdi2f-file-table-outline";

  String ARROW_EXPAND_ALL = "mdi2a-arrow-expand-vertical";
  String ARROW_COLLAPSE_ALL = "mdi2a-arrow-collapse-vertical";
  String ARROW_UP = "mdi2a-arrow-up";
  String ARROW_DOWN = "mdi2a-arrow-down";
  String DRAG_HANDLE = "mdi2d-drag-vertical";

  String PLUS = "mdi2p-plus";
  String RELOAD = "mdi2r-refresh";
  String CLOSE = "mdi2c-close";
  String TRASH = "mdi2t-trash-can-outline";
  String COPY = "mdi2c-content-copy";
  String COG_OUTLINE = "mdi2c-cog-outline";
  String ACCOUNT_KEY_OUTLINE = "mdi2a-account-key-outline";
  String ACCOUNT_MULTIPLE_OUTLINE = "mdi2a-account-multiple-outline";
  String PENCIL = "mdi2p-pencil-outline";
  String OPEN_IN_NEW = "mdi2o-open-in-new";
  String ZIP = "mdi2f-folder-zip-outline";
  String CUT = "mdi2c-content-cut";
  String PASTE = "mdi2c-content-paste";
  String UNDO = "mdi2u-undo";
  String REDO = "mdi2r-redo";
  String PLAY = "mdi2p-play";
  String STOP = "mdi2s-stop";
  String CONSOLE = "mdi2c-console";
  String SCENE = "mdi2m-movie-open";
  String FLOW = "mdi2a-arrow-bottom-right-bold-box";
  String SERVER = "mdi2s-server";

  String ELEMENT_GROUP = "mdi2f-folder-outline";
  // PNG assets (in de/a12/studio/ui/icons), not Ikonli glyph literals like the constants above.
  String PNG_MODEL_DOCUMENT = "/de/a12/studio/ui/icons/Model-Document.png";
  String PNG_MODEL_FORM = "/de/a12/studio/ui/icons/Model-Form.png";
  String PNG_MODEL_OVERVIEW = "/de/a12/studio/ui/icons/Model-Overview.png";
  String PNG_MODEL_RELATIONSHIP = "/de/a12/studio/ui/icons/Model-Relationship.png";
  String PNG_MODEL_APPLICATION = "/de/a12/studio/ui/icons/Model-App.png";
  String PNG_MODEL_CONTENT = "/de/a12/studio/ui/icons/Model-Content.png";
  String PNG_MODEL_TYPE_DEFINITION = "/de/a12/studio/ui/icons/Model-Type-Definition.png";
  String PNG_MODEL_MASTERDETAIL = "/de/a12/studio/ui/icons/Model-Master-Detail.png";
  String PNG_MODEL_PRINT = "/de/a12/studio/ui/icons/Model-Print.png";
  String PNG_MODEL_TREE = "/de/a12/studio/ui/icons/Model-Tree.png";
  String PNG_MODEL_STRUCTURALMAPPING = "/de/a12/studio/ui/icons/Structural-Mapping.png";
  // No dedicated assets exist yet for these model types; reusing the closest existing icons as placeholders.
  String PNG_MODEL_COMBINATION = "/de/a12/studio/ui/icons/Model-Transformer.png";
  String PNG_MODEL_MAPPING = "/de/a12/studio/ui/icons/Model-Graph-Diagram.png";
  String PNG_MODEL_QUERY = "/de/a12/studio/ui/icons/Model-Document_SCDM.png";

  String ELEMENT_MULTI_SELECT = "mdi2c-checkbox-multiple-marked-outline";
  String ELEMENT_INCLUDE = "mdi2l-link";
  String ELEMENT_GENERIC = "mdi2s-shape-outline";
  String ELEMENT_FIELD = "mdi2a-alpha-f-box";
  String ELEMENT_COMPUTATION = "mdi2a-alpha-c-circle";
  String ELEMENT_ATTACHMENT = "mdi2d-database-plus-outline";
  String ELEMENT_ANNOTATION = "mdi2a-alpha-a-box-outline";
  String ELEMENT_REQUIRED = "mdi2a-asterisk-circle-outline";

  String ELEMENT_VALIDATION_RULE = "mdi2a-alpha-v-circle";
  String ELEMENT_EXPRESSION = "mdi2e-epsilon";

  // Form Model structural tree node types (formmodel.formtree.FormModelTreeController/FormElementViewModel).
  String FORM_SCREEN = "mdi2m-monitor-dashboard";
  String FORM_SECTION = "mdi2v-view-agenda-outline";
  String FORM_MULTI_COLUMN_SECTION = "mdi2v-view-column-outline";
  String FORM_CONTROL_GRID = "mdi2v-view-grid-outline";
  String FORM_ROW = "mdi2t-table-row";
  String FORM_CONTROL = "mdi2f-form-textbox";
  String FORM_TEXT_CELL = "mdi2f-format-text";
  String FORM_EXPRESSION_CELL = "mdi2e-epsilon";
  String FORM_INLINE_REPEAT = "mdi2r-repeat";
  String FORM_EMBEDDED_REPEAT = "mdi2r-repeat-once";
  String FORM_DETACHED_REPEAT = "mdi2r-repeat-variant";
  String FORM_CUSTOM_SCREEN_ELEMENT = "mdi2p-puzzle-outline";

  static String forModelType(ModelType modelType) {
    if (modelType == null) {
      return null;
    }

    return switch (modelType) {
      case DOCUMENT -> PNG_MODEL_DOCUMENT;
      case FORM -> PNG_MODEL_FORM;
      case OVERVIEW -> PNG_MODEL_OVERVIEW;
      case RELATIONSHIP -> PNG_MODEL_RELATIONSHIP;
      case APPLICATION -> PNG_MODEL_APPLICATION;
      case CONTENT -> PNG_MODEL_CONTENT;
      case TYPEDEFINITION -> PNG_MODEL_TYPE_DEFINITION;
      case MASTERDETAIL -> PNG_MODEL_MASTERDETAIL;
      case PRINT -> PNG_MODEL_PRINT;
      case TREE -> PNG_MODEL_TREE;
      case COMBINATION -> PNG_MODEL_COMBINATION;
      case MAPPING -> PNG_MODEL_MAPPING;
      case QUERY -> PNG_MODEL_QUERY;
      case STRUCTURALMAPPING -> PNG_MODEL_STRUCTURALMAPPING;
    };
  }
}
