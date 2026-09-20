package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.ui.editors.documentmodel.DocumentModelTreeFilter.ElementKind;
import de.a12.studio.ui.editors.documentmodel.DocumentModelTreeFilter.FieldKind;
import de.a12.studio.ui.editors.documentmodel.DocumentModelTreeFilter.SearchIn;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * The content of the Document Model tree's filter popup: edits a {@link DocumentModelTreeFilter} (search-in
 * property, annotated only, element types, field types, requiredness) and calls back after every change so the
 * tree can rebuild itself. The search text itself lives in the tree's search field, not here.
 */
public class DocumentModelTreeFilterController {

  @FXML
  private ToggleGroup searchInGroup;

  @FXML
  private RadioButton searchInNameRadio;

  @FXML
  private RadioButton searchInIdRadio;

  @FXML
  private RadioButton searchInLabelRadio;

  @FXML
  private CheckBox onlyAnnotatedCheckBox;

  @FXML
  private VBox elementTypesBox;

  @FXML
  private VBox fieldTypesBox;

  @FXML
  private CheckBox alwaysRequiredCheckBox;

  @FXML
  private CheckBox requiredIfParentFilledCheckBox;

  private final Map<ElementKind, CheckBox> elementKindCheckBoxes = new EnumMap<>(ElementKind.class);

  private final Map<FieldKind, CheckBox> fieldKindCheckBoxes = new EnumMap<>(FieldKind.class);

  private DocumentModelTreeFilter filter;

  private Runnable onChange = () -> {
  };

  // Set while the controls are refreshed from the filter, so that doing so does not write back to it or fire onChange.
  private boolean updatingControls;

  @FXML
  private void initialize() {
    searchInNameRadio.setUserData(SearchIn.NAME);
    searchInIdRadio.setUserData(SearchIn.ID);
    searchInLabelRadio.setUserData(SearchIn.LABEL);
    searchInGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        edit(() -> filter.setSearchIn((SearchIn) newValue.getUserData()));
      }
    });

    onlyAnnotatedCheckBox.selectedProperty().addListener((observable, oldValue, newValue) ->
        edit(() -> filter.setOnlyAnnotated(newValue)));
    alwaysRequiredCheckBox.selectedProperty().addListener((observable, oldValue, newValue) ->
        edit(() -> filter.setAlwaysRequired(newValue)));
    requiredIfParentFilledCheckBox.selectedProperty().addListener((observable, oldValue, newValue) ->
        edit(() -> filter.setRequiredIfParentFilled(newValue)));

    for (ElementKind kind : ElementKind.values()) {
      CheckBox checkBox = shownCheckBox("document_model_tree_filter.element_kind." + kind.name().toLowerCase(Locale.ROOT));
      checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> edit(() -> filter.setShown(kind, newValue)));
      elementKindCheckBoxes.put(kind, checkBox);
      elementTypesBox.getChildren().add(checkBox);
    }
    for (FieldKind kind : FieldKind.values()) {
      CheckBox checkBox = shownCheckBox("document_model_tree_filter.field_kind." + kind.name().toLowerCase(Locale.ROOT));
      checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> edit(() -> filter.setShown(kind, newValue)));
      fieldKindCheckBoxes.put(kind, checkBox);
      fieldTypesBox.getChildren().add(checkBox);
    }
  }

  private static CheckBox shownCheckBox(@NonNull String bundleKey) {
    CheckBox checkBox = new CheckBox(StudioBundle.get(bundleKey));
    checkBox.setMnemonicParsing(false);
    return checkBox;
  }

  /** Shows {@code filter}'s current state and edits it from now on; {@code onChange} runs after each edit. */
  public void init(@NonNull DocumentModelTreeFilter filter, @NonNull Runnable onChange) {
    this.filter = filter;
    this.onChange = onChange;
    syncControls();
  }

  /** Refreshes the controls from the filter, for a filter that was changed from outside (or reset). */
  public void syncControls() {
    updatingControls = true;
    try {
      switch (filter.getSearchIn()) {
        case NAME -> searchInNameRadio.setSelected(true);
        case ID -> searchInIdRadio.setSelected(true);
        case LABEL -> searchInLabelRadio.setSelected(true);
      }
      onlyAnnotatedCheckBox.setSelected(filter.isOnlyAnnotated());
      alwaysRequiredCheckBox.setSelected(filter.isAlwaysRequired());
      requiredIfParentFilledCheckBox.setSelected(filter.isRequiredIfParentFilled());
      elementKindCheckBoxes.forEach((kind, checkBox) -> checkBox.setSelected(filter.isShown(kind)));
      fieldKindCheckBoxes.forEach((kind, checkBox) -> checkBox.setSelected(filter.isShown(kind)));
    }
    finally {
      updatingControls = false;
    }
  }

  @FXML
  private void onReset() {
    filter.resetNarrowingFilters();
    syncControls();
    onChange.run();
  }

  private void edit(@NonNull Runnable change) {
    if (updatingControls || filter == null) {
      return;
    }
    change.run();
    onChange.run();
  }
}
