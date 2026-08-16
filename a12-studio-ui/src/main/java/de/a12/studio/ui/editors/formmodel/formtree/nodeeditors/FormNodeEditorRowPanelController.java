package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.Label;
import de.a12.studio.models.formmodel.LocalizedText;
import de.a12.studio.models.formmodel.MultilingualText;
import de.a12.studio.models.formmodel.Row;
import de.a12.studio.models.formmodel.TextContainer;
import de.a12.studio.ui.editors.formmodel.NamePanelController;
import de.a12.studio.ui.editors.formmodel.StylesPanelController;
import de.a12.studio.ui.editors.formmodel.formtree.FormModelTreeController;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import javafx.fxml.FXML;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * The Form Model tree's right-hand editor pane for a selected {@link Row} node ({@link
 * FormModelTreeController}): Name, Label (per-locale text bound to {@link Row#getTitle()}), Styles and
 * Annotations.
 */
public class FormNodeEditorRowPanelController {

  @FXML
  private NamePanelController nameController;
  @FXML
  private LocalizedTextPanelController labelController;
  @FXML
  private StylesPanelController stylesController;
  @FXML
  private AnnotationsPanelController annotationsController;

  private Row row;

  @FXML
  private void initialize() {
    labelController.configureCustom("label", "LABEL");
  }

  public void setRow(@NonNull Row row) {
    this.row = row;
    nameController.setCustom(row::getName, row::setName);
    labelController.setCustom(this::currentLabelTexts, this::writeLabelTexts);
    stylesController.setCustom(row::getStyle, row::getStyle);
    annotationsController.setCustom(row::getAnnotations);
  }

  private List<Label> currentLabelTexts() {
    LocalizedText title = row.getTitle();
    if (title instanceof MultilingualText multilingualText && multilingualText.getMultilingualText() != null) {
      return multilingualText.getMultilingualText().getText();
    }
    return List.of();
  }

  private List<Label> writeLabelTexts() {
    return getOrCreateMultilingualTitle().getMultilingualText().getText();
  }

  private MultilingualText getOrCreateMultilingualTitle() {
    LocalizedText title = row.getTitle();
    MultilingualText multilingualText;
    if (title instanceof MultilingualText existing) {
      multilingualText = existing;
    } else {
      multilingualText = new MultilingualText();
      row.setTitle(multilingualText);
    }
    if (multilingualText.getMultilingualText() == null) {
      multilingualText.setMultilingualText(new TextContainer());
    }
    return multilingualText;
  }
}
