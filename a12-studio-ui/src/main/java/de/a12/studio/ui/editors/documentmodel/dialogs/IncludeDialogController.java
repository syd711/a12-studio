package de.a12.studio.ui.editors.documentmodel.dialogs;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.typedefinitionmodel.TypeDefinitionModel;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.util.FileUtils;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Modal dialog for creating a new "Include" group: prompts for the element name and the referenced
 * {@link DocumentModel}, and only lets the user submit once both are filled in, so a new Include always
 * starts out valid (see {@link de.a12.studio.modelsvalidation.validators.MissingReferenceValidator}) instead
 * of surfacing a "Missing Include Reference" error the user has to notice and fix afterwards.
 */
public class IncludeDialogController implements DialogController {

  public record IncludeInput(String name, String reference) {
  }

  @FXML
  private ComboBox<DocumentModel> referenceComboBox;

  @FXML
  private TextField nameField;

  @FXML
  private Button okButton;

  @FXML
  private Button cancelButton;

  private Stage stage;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @FXML
  private void initialize() {
    referenceComboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(DocumentModel model) {
        return model == null ? "" : model.getId();
      }

      @Override
      public DocumentModel fromString(String string) {
        return null;
      }
    });

    okButton.disableProperty().bind(Bindings.createBooleanBinding(
        () -> !FileUtils.isValidWindowsFilename(nameField.getText()) || referenceComboBox.getValue() == null,
        nameField.textProperty(), referenceComboBox.valueProperty()));

    nameField.requestFocus();
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  public static Optional<IncludeInput> show(Stage owner, @NonNull Project project, DocumentModel excludedModel, String defaultName) {
    FXMLLoader fxmlLoader = new FXMLLoader(IncludeDialogController.class.getResource("include-dialog.fxml"));
    Stage stage = WidgetFactory.createDialogStage("include-dialog", fxmlLoader, owner, "New Include");
    IncludeDialogController controller = (IncludeDialogController) stage.getUserData();
    controller.stage = stage;
    controller.nameField.setText(defaultName == null ? "" : defaultName);
    controller.referenceComboBox.getItems().setAll(includableModels(project, excludedModel));
    stage.showAndWait();

    if (controller.result.isPresent() && controller.result.get() == ButtonType.OK) {
      DocumentModel reference = controller.referenceComboBox.getValue();
      String name = controller.nameField.getText();
      if (reference != null && name != null && !name.isBlank()) {
        return Optional.of(new IncludeInput(name.trim(), reference.getId()));
      }
    }
    return Optional.empty();
  }

  /**
   * Every {@link DocumentModel} in {@code project} that can be the target of an Include: excludes {@code
   * excludedModel} itself (a model can't include itself) and Type Definition models (which aren't
   * includable business objects), sorted by id for a stable, predictable dropdown order.
   */
  private static List<DocumentModel> includableModels(@NonNull Project project, DocumentModel excludedModel) {
    List<DocumentModel> result = new ArrayList<>();
    collectIncludableModels(project.getRoot(), excludedModel, result);
    result.sort(Comparator.comparing(DocumentModel::getId));
    return result;
  }

  private static void collectIncludableModels(@NonNull ProjectItem item, DocumentModel excludedModel, @NonNull List<DocumentModel> result) {
    if (item.isFolder()) {
      for (ProjectItem child : item.getChildren()) {
        collectIncludableModels(child, excludedModel, result);
      }
    }
    else if (item.getModel() instanceof DocumentModel documentModel
        && !(documentModel instanceof TypeDefinitionModel)
        && documentModel != excludedModel) {
      result.add(documentModel);
    }
  }
}
