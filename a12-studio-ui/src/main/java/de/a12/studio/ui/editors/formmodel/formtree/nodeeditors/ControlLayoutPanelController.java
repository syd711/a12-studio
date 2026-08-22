package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.Cell;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.GridSpan;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Edits a {@link Control}'s {@code offset} ({@link Control#getOffset()}) and {@code span} ({@link
 * Cell#getSpan()}, inherited from {@link Cell}) grid values per responsive breakpoint (lg/md/sm). Not bound to
 * a single {@link de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern used by
 * e.g. {@link de.a12.studio.ui.editors.querymodel.PagingPanelController}. A spinner left at its breakpoint's
 * default (0 for offset, 1 for span) is treated as "not set" and written back as {@code null}, matching how
 * {@link GridSpan}'s fields serialize (omitted when {@code null}).
 */
public class ControlLayoutPanelController extends AbstractPropertyEditor implements Initializable {

  private static final int MIN = 0;
  private static final int MAX = 12;
  private static final int OFFSET_DEFAULT = 0;
  private static final int SPAN_DEFAULT = 1;

  @FXML
  private Spinner<Integer> offsetLgSpinner;
  @FXML
  private Spinner<Integer> offsetMdSpinner;
  @FXML
  private Spinner<Integer> offsetSmSpinner;
  @FXML
  private Spinner<Integer> spanLgSpinner;
  @FXML
  private Spinner<Integer> spanMdSpinner;
  @FXML
  private Spinner<Integer> spanSmSpinner;

  private Control control;

  // Set while fields are being repopulated from the model, so those programmatic updates aren't mistaken for
  // user edits and don't trigger a save.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    bindOffset(offsetLgSpinner, GridSpan::setLg);
    bindOffset(offsetMdSpinner, GridSpan::setMd);
    bindOffset(offsetSmSpinner, GridSpan::setSm);
    bindSpan(spanLgSpinner, GridSpan::setLg);
    bindSpan(spanMdSpinner, GridSpan::setMd);
    bindSpan(spanSmSpinner, GridSpan::setSm);
  }

  public void setControl(@NonNull Control control) {
    this.control = control;
    updatingFromModel = true;
    try {
      GridSpan offset = control.getOffset();
      setValue(offsetLgSpinner, offset != null ? offset.getLg() : null, OFFSET_DEFAULT);
      setValue(offsetMdSpinner, offset != null ? offset.getMd() : null, OFFSET_DEFAULT);
      setValue(offsetSmSpinner, offset != null ? offset.getSm() : null, OFFSET_DEFAULT);
      GridSpan span = control.getSpan();
      setValue(spanLgSpinner, span != null ? span.getLg() : null, SPAN_DEFAULT);
      setValue(spanMdSpinner, span != null ? span.getMd() : null, SPAN_DEFAULT);
      setValue(spanSmSpinner, span != null ? span.getSm() : null, SPAN_DEFAULT);
    } finally {
      updatingFromModel = false;
    }
  }

  private void bindOffset(@NonNull Spinner<Integer> spinner, @NonNull GridSpanSetter setter) {
    spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN, MAX, OFFSET_DEFAULT));
    spinner.setEditable(true);
    WidgetFactory.restrictToNumericInput(spinner.getEditor());
    spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || control == null) return;
      setter.set(ensureOffset(), newValue == OFFSET_DEFAULT ? null : newValue);
      commitHeaderChange();
    });
  }

  private void bindSpan(@NonNull Spinner<Integer> spinner, @NonNull GridSpanSetter setter) {
    spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN, MAX, SPAN_DEFAULT));
    spinner.setEditable(true);
    WidgetFactory.restrictToNumericInput(spinner.getEditor());
    spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || control == null) return;
      setter.set(ensureSpan(), newValue == SPAN_DEFAULT ? null : newValue);
      commitHeaderChange();
    });
  }

  private static void setValue(@NonNull Spinner<Integer> spinner, Integer value, int defaultValue) {
    spinner.getValueFactory().setValue(value != null ? value : defaultValue);
  }

  private GridSpan ensureOffset() {
    if (control.getOffset() == null) {
      control.setOffset(new GridSpan());
    }
    return control.getOffset();
  }

  private GridSpan ensureSpan() {
    if (control.getSpan() == null) {
      control.setSpan(new GridSpan());
    }
    return control.getSpan();
  }

  @FunctionalInterface
  private interface GridSpanSetter {
    void set(GridSpan span, Integer value);
  }
}
