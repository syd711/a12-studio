package de.a12.studio.ui.util;

import de.a12.studio.dataservices.models.ModelType;

public interface Icons {

  String WINDOW_MINIMIZE = "mdi2w-window-minimize";
  String WINDOW_MAXIMIZE = "mdi2w-window-maximize";
  String WINDOW_RESTORE = "mdi2w-window-restore";
  String WINDOW_CLOSE = "mdi2w-window-close";

  String FOLDER = "mdi2f-folder";
  String FOLDER_OPEN = "mdi2f-folder-open";
  String FOLDER_OUTLINE = "mdi2f-folder-outline";
  String FOLDER_OPEN_OUTLINE = "mdi2f-folder-open-outline";
  String FILE_OUTLINE = "mdi2f-file-outline";
  String FILE_TABLE_OUTLINE = "mdi2f-file-table-outline";

  String ARROW_EXPAND_ALL = "mdi2a-arrow-expand-vertical";
  String ARROW_COLLAPSE_ALL = "mdi2a-arrow-collapse-vertical";
  String ARROW_UP = "mdi2a-arrow-up";
  String ARROW_DOWN = "mdi2a-arrow-down";

  String RELOAD = "mdi2r-refresh";
  String CLOSE = "mdi2c-close";
  String TRASH = "mdi2t-trash-can-outline";
  String COPY = "mdi2c-content-copy";
  String ZIP = "mdi2f-folder-zip-outline";
  String CUT = "mdi2c-content-cut";
  String PASTE = "mdi2c-content-paste";
  String UNDO = "mdi2u-undo";
  String REDO = "mdi2r-redo";

  String ELEMENT_GROUP = "mdi2f-folder-outline";
  // PNG assets (in de/a12/studio/ui/icons), not Ikonli glyph literals like the constants above.
  String PNG_MODEL_DOCUMENT = "/de/a12/studio/ui/icons/Model-Document.png";
  String PNG_MODEL_FORM = "/de/a12/studio/ui/icons/Model-Form.png";
  String PNG_MODEL_OVERVIEW = "/de/a12/studio/ui/icons/Model-Overview.png";
  String PNG_MODEL_RELATIONSHIP = "/de/a12/studio/ui/icons/Model-Relationship.png";
  String PNG_MODEL_APPLICATION = "/de/a12/studio/ui/icons/Model-App.png";
  String PNG_MODEL_CONTENT = "/de/a12/studio/ui/icons/Model-Content.png";
  String PNG_MODEL_TYPE_DEFINITION = "/de/a12/studio/ui/icons/Model-Type-Definition.png";

  String ELEMENT_MULTI_SELECT = "mdi2c-checkbox-multiple-marked-outline";
  String ELEMENT_INCLUDE = "mdi2l-link";
  String ELEMENT_GENERIC = "mdi2s-shape-outline";
  String ELEMENT_FIELD = "mdi2a-alpha-f-box";
  String ELEMENT_COMPUTATION = "mdi2a-alpha-c-circle";
  String ELEMENT_ATTACHMENT = "mdi2d-database-plus-outline";
  String ELEMENT_ANNOTATION = "mdi2a-alpha-a-box-outline";
  String ELEMENT_REQUIRED = "mdi2a-asterisk-circle-outline";

  String ELEMENT_VALIDATION_RULE = "mdi2a-alpha-v-circle";

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
    };
  }
}
