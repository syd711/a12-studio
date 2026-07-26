package de.a12.studio.ui.editors.propertyeditors.dialogs;

import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class Dialogs {

  public record CategoryInput(String name, String description) {
  }

  public static Optional<CategoryInput> showCategory(Stage owner, String title, String name, String description) {
    FXMLLoader fxmlLoader = new FXMLLoader(CategoryDialogController.class.getResource("category-dialog.fxml"));
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
    Stage stage = WidgetFactory.createDialogStage(null, fxmlLoader, owner, title);
    SuggestionsDialogController controller = (SuggestionsDialogController) stage.getUserData();
    controller.initDialog(stage, initialValues);
    stage.showAndWait();

    if (controller.getResult().isEmpty() || controller.getResult().get() != ButtonType.OK) {
      return Optional.empty();
    }
    return Optional.of(controller.getValues());
  }
}
