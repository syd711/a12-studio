package de.a12.studio.ui.editors.typedefinitionmodel;

import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.TypeDefinition;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.documentmodel.ElementEditorController;
import de.a12.studio.ui.editors.propertyeditors.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TypeDefinitionModelFieldEditorController implements ElementEditorController, Initializable {

  @FXML
  private GeneralInformationPanelController generalInformationController;

  @FXML
  private TypeDefinitionPanelController typeDefinitionController;

  @FXML
  private DataTypeConfigurationPanelController dataTypeConfigurationController;

  private List<AbstractPropertyEditor> propertyEditors;

  private Element element;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    typeDefinitionController.setCustomTypeDisabled();
    typeDefinitionController.hideCheckboxesGrid();
    dataTypeConfigurationController.hideCustomLengthGrid();

    typeDefinitionController.fieldTypeProperty().addListener((observable, oldValue, newValue) -> {
      if (element != null) {
        dataTypeConfigurationController.setElement(element);
      }
    });

    propertyEditors = List.of(generalInformationController, typeDefinitionController, dataTypeConfigurationController);
  }

  @Override
  public void setElement(@NonNull Element element, @NonNull List<Element> ancestors) {
    this.element = element;
    generalInformationController.setAncestors(ancestors);
    typeDefinitionController.setAncestors(ancestors);
    dataTypeConfigurationController.setAncestors(ancestors);
    propertyEditors.forEach(propertyEditor -> propertyEditor.setElement(element));
  }

  public void focusNameField() {
    generalInformationController.focusNameField();
  }

  /**
   * Wraps {@code typeDefinition} as the {@link Element} this editor binds to, keeping {@link
   * TypeDefinitionFieldElement} package-private (it's just the adapter this editor needs internally to reuse
   * the shared field property editors, not part of this class's public contract). Used both by {@code
   * TypeDefintionModelEditorController} (the standalone type-definition-only editor tab) and {@link
   * de.a12.studio.ui.editors.documentmodel.dialogs.TypeDefinitionSettingsDialog} (the type definitions section
   * of a regular document model, edited from within a dialog).
   */
  public void setTypeDefinition(@NonNull TypeDefinition typeDefinition) {
    setElement(new TypeDefinitionFieldElement(typeDefinition), List.of());
  }

  /**
   * Forwarded to every embedded panel, so an owning dialog with its own Save button (see {@link
   * de.a12.studio.ui.editors.documentmodel.dialogs.TypeDefinitionSettingsDialog}) can defer their commits
   * until that button is pressed.
   */
  public void setSaveMode(@NonNull PropertyEditorSaveMode saveMode) {
    propertyEditors.forEach(propertyEditor -> propertyEditor.setSaveMode(saveMode));
  }

  @Override
  public void destroy() {
    propertyEditors.forEach(AbstractPropertyEditor::destroy);
  }
}
