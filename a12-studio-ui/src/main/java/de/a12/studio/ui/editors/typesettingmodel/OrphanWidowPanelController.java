package de.a12.studio.ui.editors.typesettingmodel;

import de.a12.studio.models.typesettingmodel.TypesettingModel;
import de.a12.studio.models.typesettingmodel.TypesettingModelDefaults;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.validators.typesetting.TypesettingElementIds;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Edits a {@link TypesettingModel}'s orphan and widow limits ({@code content.orphan}/{@code content.widow}):
 * how many isolated lines of a paragraph the print engine leaves at the bottom (orphans) or top (widows) of a
 * page, 0 to 10, {@link TypesettingModelDefaults#LINE_LIMIT} by default. Not bound to a single {@link
 * de.a12.studio.models.documentmodel.Element} (they live on the model's content), so it follows the model-header
 * pattern of {@code FormWidthPanelController}.
 *
 * <p>The spinner itself keeps its value within 0-10 (typing 11 becomes 10), but a file can carry a value outside
 * that range. It is shown as it is in the spinner's text, so the error {@code TypesettingLineLimitValidator}
 * reports in this panel's error container matches what the user sees; editing the field brings it back in range.
 */
public class OrphanWidowPanelController extends AbstractPropertyEditor implements Initializable {

  private static final PseudoClass ERROR_PSEUDO_CLASS = PseudoClass.getPseudoClass("error");

  @FXML
  private Spinner<Integer> orphanSpinner;

  @FXML
  private Spinner<Integer> widowSpinner;

  private TypesettingModel model;

  // Set while the spinners are being repopulated from the model, so that programmatic updates aren't mistaken
  // for user edits and don't trigger a save.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    configure(orphanSpinner, value -> model.getContent().setOrphan(value));
    configure(widowSpinner, value -> model.getContent().setWidow(value));
  }

  private void configure(Spinner<Integer> spinner, Consumer<Integer> setter) {
    spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
        TypesettingModelDefaults.MIN_LINE_LIMIT, TypesettingModelDefaults.MAX_LINE_LIMIT, TypesettingModelDefaults.LINE_LIMIT));
    WidgetFactory.restrictToNumericInput(spinner.getEditor());
    spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      setter.accept(newValue);
      commitHeaderChange();
      refreshValidation();
    });
  }

  public void setModel(@NonNull TypesettingModel model) {
    this.model = model;

    updatingFromModel = true;
    try {
      // An absent limit is shown as the default the print engine then applies, without writing it back.
      show(orphanSpinner, model.getContent().getOrphan());
      show(widowSpinner, model.getContent().getWidow());
    } finally {
      updatingFromModel = false;
    }
    refreshValidation();
  }

  private static void show(Spinner<Integer> spinner, Integer value) {
    int shown = value != null ? value : TypesettingModelDefaults.LINE_LIMIT;
    // The value factory clamps to 0-10, so the file's value goes into the editor text instead.
    spinner.getValueFactory().setValue(shown);
    spinner.getEditor().setText(String.valueOf(shown));
  }

  private void refreshValidation() {
    if (model == null || Studio.getSelectedProjectItem() == null) {
      hideError();
      return;
    }
    List<ModelValidationError> errors = Studio.getValidationService().validate(model);
    List<ModelValidationError> panelErrors = new ArrayList<>();
    panelErrors.addAll(errorsFor(errors, TypesettingElementIds.ORPHAN, orphanSpinner));
    panelErrors.addAll(errorsFor(errors, TypesettingElementIds.WIDOW, widowSpinner));

    if (panelErrors.isEmpty()) {
      hideError();
    }
    else {
      ModelValidationError first = panelErrors.get(0);
      showError(first.severity(), first.message());
    }
  }

  private static List<ModelValidationError> errorsFor(List<ModelValidationError> errors, String elementId, Spinner<Integer> spinner) {
    List<ModelValidationError> fieldErrors = errors.stream().filter(error -> elementId.equals(error.elementId())).toList();
    spinner.getEditor().pseudoClassStateChanged(ERROR_PSEUDO_CLASS, !fieldErrors.isEmpty());
    return fieldErrors;
  }
}
