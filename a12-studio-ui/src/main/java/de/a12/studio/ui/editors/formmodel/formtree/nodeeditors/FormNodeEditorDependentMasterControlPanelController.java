package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.editors.formmodel.formtree.FormModelTreeController;
import javafx.fxml.FXML;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The Form Model tree's right-hand editor pane for a {@link Control} whose Document Model field is a Boolean or
 * Enumeration field (Confirm fields have their own editor, {@link FormNodeEditorConfirmControlPanelController}):
 * the unchanged standard editor ({@link FormNodeEditorControlPanelController}) in a "Control" tab, next to a
 * "Dependencies" tab ({@link DependentControlsPanelController}) that lets the user pick the screen blocks hidden
 * for each value of the field.
 *
 * <p>Routing: {@link FormModelTreeController} routes a {@link Control} here when
 * {@link de.a12.studio.modelsvalidation.validators.form.DependentControlSupport#masterValues(Control, ElementIndex)}
 * is non-empty, i.e. the field can be a dependent-controls master.</p>
 */
public class FormNodeEditorDependentMasterControlPanelController {

  @FXML private FormNodeEditorControlPanelController controlController;
  @FXML private DependentControlsPanelController dependenciesController;

  public void setControl(@NonNull Control control,
      @Nullable DocumentModel documentModel,
      @Nullable ElementIndex elementIndex,
      @Nullable FormModelContent content) {
    controlController.setControl(control, documentModel, elementIndex, content);
    dependenciesController.setControl(control, content, elementIndex);
  }
}
