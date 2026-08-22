package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.Label;
import de.a12.studio.models.Locale;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.events.LocalesChangedEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import org.jspecify.annotations.NonNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Read-only, table-like display of a per-locale text (a list of {@link Label}): a "Locale"/"Text" header
 * row over plain {@link javafx.scene.control.Label} rows (one per locale, divider line underneath) instead
 * of the editable {@link javafx.scene.control.TextField} grid used by {@link LocalizedTextPanelController}.
 * Bound via {@link #setCustom} to a caller-supplied {@code Supplier<List<Label>>}, mirroring that controller's generic escape hatch
 * ({@code customTextsSupplier}) since this panel never writes back. Used to show the model-wide Field
 * Configuration label for reference (not editable here) in {@link
 * de.a12.studio.ui.editors.formmodel.formtree.nodeeditors.ControlLabelPanelController}.
 */
public class LocalizedTextReadonlyPanelController extends AbstractPropertyEditor {

  // Row 0 is the "Locale"/"Text" header (declared in the FXML), row 1 is its divider - locale rows
  // are appended by buildLocaleRows() starting here.
  private static final int FIRST_DATA_ROW = 2;

  @FXML
  private GridPane localesGrid;

  private Supplier<List<Label>> textsSupplier;

  // Captured whenever setCustom is called, so a locales-changed event meant for a different, unrelated
  // model open in another tab is ignored - mirrors LocalizedTextPanelController.
  private ProjectItem projectItem;

  private final Map<String, javafx.scene.control.Label> valueLabelsByLocale = new LinkedHashMap<>();

  public void configureCustom(@NonNull String fieldKey, @NonNull String title) {
    setTitle(title);
    setSettingsKeySuffix("." + fieldKey);
  }

  public void setCustom(@NonNull Supplier<List<Label>> textsSupplier) {
    this.textsSupplier = textsSupplier;
    this.projectItem = Studio.getSelectedProjectItem();
    buildLocaleRows();
    populateLocaleRows();
  }

  @Override
  public void localesChanged(@NonNull LocalesChangedEvent event) {
    if (event.getItem().equals(projectItem)) {
      buildLocaleRows();
      populateLocaleRows();
    }
  }

  private void buildLocaleRows() {
    localesGrid.getChildren().removeIf(node -> {
      Integer rowIndex = GridPane.getRowIndex(node);
      return rowIndex != null && rowIndex >= FIRST_DATA_ROW;
    });
    valueLabelsByLocale.clear();

    int row = FIRST_DATA_ROW;
    for (Locale locale : getModelLocales()) {
      javafx.scene.control.Label localeLabel = new javafx.scene.control.Label(locale.getCode());
      localeLabel.getStyleClass().add("readonly-table-cell");
      javafx.scene.control.Label valueLabel = new javafx.scene.control.Label();
      valueLabel.getStyleClass().add("readonly-table-cell");
      valueLabel.setWrapText(true);

      localesGrid.addRow(row, localeLabel, valueLabel);
      valueLabelsByLocale.put(locale.getCode(), valueLabel);
      row++;

      Separator divider = new Separator();
      divider.getStyleClass().add("readonly-table-divider");
      localesGrid.add(divider, 0, row, 2, 1);
      row++;
    }
  }

  private void populateLocaleRows() {
    List<Label> texts = textsSupplier != null ? textsSupplier.get() : List.of();
    valueLabelsByLocale.forEach((localeCode, valueLabel) -> {
      String text = texts.stream()
          .filter(label -> localeCode.equals(label.getLocale()))
          .findFirst()
          .map(Label::getText)
          .orElse("");
      valueLabel.setText(text);
    });
  }

  private List<Locale> getModelLocales() {
    if (projectItem == null || projectItem.getModel() == null) {
      return List.of();
    }
    return projectItem.getModel().getLocales();
  }
}
