package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.DatePickerConfig;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.form.DatePickerSupport;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * "Date Picker" property editor: the year range of the date picker of a date, date-time or {@code YYYY-MM-DD}
 * date-range field ({@link DatePickerConfig}), for a {@link de.a12.studio.models.formmodel.Control} and for a
 * field-based repeat overview column alike - hence the getter/setter pair of {@link #setConfig} instead of a
 * typed owner. Without "Absolute" the years are offsets from the current year (SME's default range is -7/+7),
 * with it calendar years; the preselection year is what the picker opens on.
 * <p>
 * The config is created on the first edit and dropped again when it carries no information ({@link
 * DatePickerSupport#isRedundant}, SME's {@code removeEmptyDatePickerConfig}), so opening the panel or clearing
 * every field leaves no empty {@code datePickerConfig} behind. An untouched config - including one with an
 * explicit {@code "absolute": false} - is never rewritten. The range rules ({@link DatePickerSupport#problems})
 * are shown in the panel's error container as you type, and by {@code FormDatePickerConfigValidator} for the
 * whole model. Not tied to a document-model {@code Element}, so it follows the model-header pattern.
 */
public class DatePickerConfigPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private TextField minYearField;
  @FXML
  private TextField maxYearField;
  @FXML
  private CheckBox absoluteCheckBox;
  @FXML
  private TextField preselectionYearField;

  private Supplier<DatePickerConfig> getter;
  private Consumer<DatePickerConfig> setter;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    bindTextField(minYearField, (el, value) -> editYear(value, DatePickerConfig::setMinYear));
    bindTextField(maxYearField, (el, value) -> editYear(value, DatePickerConfig::setMaxYear));
    bindTextField(preselectionYearField, (el, value) -> editYear(value, DatePickerConfig::setPreselectionYear));
    bindCheckBox(absoluteCheckBox, (el, value) -> update(config -> config.setAbsolute(value ? Boolean.TRUE : null)));
  }

  /** Shows or hides the whole panel, e.g. when the bound field has no date picker. */
  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  /** Binds the panel to a date picker config that may not exist yet: {@code getter} may return {@code null}. */
  public void setConfig(@NonNull Supplier<DatePickerConfig> getter, @NonNull Consumer<DatePickerConfig> setter) {
    this.getter = getter;
    this.setter = setter;
    DatePickerConfig config = getter.get();
    setFieldValue(minYearField, text(config == null ? null : config.getMinYear()));
    setFieldValue(maxYearField, text(config == null ? null : config.getMaxYear()));
    setFieldValue(preselectionYearField, text(config == null ? null : config.getPreselectionYear()));
    setFieldValue(absoluteCheckBox, config != null && Boolean.TRUE.equals(config.getAbsolute()));
    showProblems(config);
  }

  private void editYear(String text, BiConsumer<DatePickerConfig, Integer> write) {
    String trimmed = text == null ? "" : text.strip();
    Integer year = null;
    if (!trimmed.isEmpty()) {
      try {
        year = Integer.valueOf(trimmed);
      }
      catch (NumberFormatException e) {
        showError("ERROR", StudioBundle.get("date_picker_year_invalid", trimmed));
        return;
      }
    }
    Integer parsed = year;
    update(config -> write.accept(config, parsed));
  }

  private void update(Consumer<DatePickerConfig> change) {
    DatePickerConfig config = getter.get();
    if (config == null) {
      config = new DatePickerConfig();
      setter.accept(config);
    }
    change.accept(config);
    if (DatePickerSupport.isRedundant(config)) {
      setter.accept(null);
    }
    showProblems(getter.get());
  }

  private void showProblems(@Nullable DatePickerConfig config) {
    List<DatePickerSupport.Problem> problems = DatePickerSupport.problems(config);
    if (problems.isEmpty()) {
      hideError();
    }
    else {
      showError("ERROR", ValidationMessages.get(problems.get(0).messageKey()));
    }
  }

  private static String text(@Nullable Integer year) {
    return year == null ? "" : year.toString();
  }
}
