package de.a12.studio.ui.editors.propertyeditors.dialogs;

import de.a12.studio.ui.util.StudioBundle;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentUniquenessCriterion;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Dialogs {

  public record CategoryInput(String name, String description) {
  }

  public static Optional<CategoryInput> showCategory(Stage owner, String title, String name, String description) {
    FXMLLoader fxmlLoader = new FXMLLoader(CategoryDialogController.class.getResource("category-dialog.fxml"));
fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("category-dialog", fxmlLoader, owner, title);
    CategoryDialogController controller = (CategoryDialogController) stage.getUserData();
    controller.initDialog(stage, name, description);
    stage.showAndWait();

    if (controller.getResult().isEmpty() || controller.getResult().get() != ButtonType.OK) {
      return Optional.empty();
    }
    String resultName = controller.getName();
    if (resultName == null || resultName.isBlank()) {
      return Optional.empty();
    }
    return Optional.of(new CategoryInput(resultName, controller.getDescription()));
  }

  public static Optional<List<String>> showSuggestions(Stage owner, String title, List<String> initialValues) {
    FXMLLoader fxmlLoader = new FXMLLoader(SuggestionsDialogController.class.getResource("suggestions-dialog.fxml"));
fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage(null, fxmlLoader, owner, title);
    SuggestionsDialogController controller = (SuggestionsDialogController) stage.getUserData();
    controller.initDialog(stage, initialValues);
    stage.showAndWait();

    if (controller.getResult().isEmpty() || controller.getResult().get() != ButtonType.OK) {
      return Optional.empty();
    }
    return Optional.of(controller.getValues());
  }

  /**
   * @param criterion the criterion to edit, or {@code null} to create a new one (an empty dialog).
   * @param usedNames every other criterion's name already in use on {@code model}, so the dialog can reject a
   *                   duplicate {@link DocumentUniquenessCriterion#getName()}.
   */
  public static Optional<DocumentUniquenessCriterion> showUniquenessCriterion(Stage owner, DocumentModel model,
                                                                                DocumentUniquenessCriterion criterion, Set<String> usedNames) {
    FXMLLoader fxmlLoader = new FXMLLoader(DocumentUniquenessCriterionDialogController.class.getResource("document-uniqueness-criterion-dialog.fxml"));
fxmlLoader.setResources(StudioBundle.getBundle());
    String title = criterion == null ? StudioBundle.get("new_uniqueness_criterion") : StudioBundle.get("edit_uniqueness_criterion");
    Stage stage = WidgetFactory.createDialogStage("uniqueness-criterion-dialog", fxmlLoader, owner, title);
    DocumentUniquenessCriterionDialogController controller = (DocumentUniquenessCriterionDialogController) stage.getUserData();
    controller.initDialog(stage, model, criterion, usedNames);
    stage.showAndWait();
    return controller.getResult();
  }
}
