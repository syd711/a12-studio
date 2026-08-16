package de.a12.studio.ui.editors.formmodel;

import de.a12.studio.models.Label;
import de.a12.studio.models.formmodel.LocalizedText;
import de.a12.studio.models.formmodel.MultiColumnSection;
import de.a12.studio.models.formmodel.MultilingualText;
import de.a12.studio.models.formmodel.TextContainer;
import de.a12.studio.ui.editors.formmodel.formtree.FormModelTreeController;
import de.a12.studio.ui.editors.formmodel.formtree.nodeeditors.FormNodeEditorRowPanelController;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import javafx.fxml.FXML;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * The Form Model tree's right-hand editor pane for a selected {@link MultiColumnSection} ("Multi-Column
 * Section") node ({@link FormModelTreeController}): the same Name/Label/Styles/Annotations fields as {@link
 * FormNodeEditorRowPanelController}, plus a Flex Layout panel for {@link MultiColumnSection#getLayout()}.
 */
public class MultiColumnSectionEditorPanelController {

  @FXML
  private NamePanelController nameController;
  @FXML
  private LocalizedTextPanelController labelController;
  @FXML
  private StylesPanelController stylesController;
  @FXML
  private AnnotationsPanelController annotationsController;
  @FXML
  private FlexLayoutPanelController flexLayoutController;

  private MultiColumnSection section;

  @FXML
  private void initialize() {
    labelController.configureCustom("label", "LABEL");
  }

  public void setSection(@NonNull MultiColumnSection section) {
    this.section = section;
    nameController.setCustom(section::getName, section::setName);
    labelController.setCustom(this::currentLabelTexts, this::writeLabelTexts);
    stylesController.setCustom(section::getStyle, section::getStyle);
    annotationsController.setCustom(section::getAnnotations);
    flexLayoutController.setSection(section);
  }

  private List<Label> currentLabelTexts() {
    LocalizedText title = section.getTitle();
    if (title instanceof MultilingualText multilingualText && multilingualText.getMultilingualText() != null) {
      return multilingualText.getMultilingualText().getText();
    }
    return List.of();
  }

  private List<Label> writeLabelTexts() {
    return getOrCreateMultilingualTitle().getMultilingualText().getText();
  }

  private MultilingualText getOrCreateMultilingualTitle() {
    LocalizedText title = section.getTitle();
    MultilingualText multilingualText;
    if (title instanceof MultilingualText existing) {
      multilingualText = existing;
    } else {
      multilingualText = new MultilingualText();
      section.setTitle(multilingualText);
    }
    if (multilingualText.getMultilingualText() == null) {
      multilingualText.setMultilingualText(new TextContainer());
    }
    return multilingualText;
  }
}
