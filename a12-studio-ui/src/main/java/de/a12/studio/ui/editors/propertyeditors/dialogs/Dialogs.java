package de.a12.studio.ui.editors.propertyeditors.dialogs;

import de.a12.studio.ui.util.StudioBundle;

import de.a12.studio.models.overviewmodel.OverviewButtonLike;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentUniquenessCriterion;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class Dialogs {

  public record CategoryInput(String name, String description) {
  }

  public static Optional<CategoryInput> showCategory(Stage owner, String title, String name, String description) {
    FXMLLoader fxmlLoader = new FXMLLoader(CategoryDialogController.class.getResource("category-dialog.fxml"));
fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("category-dialog", fxmlLoader, owner, title);
    CategoryDialogController controller = (CategoryDialogController) stage.getUserData();
    controller.initDialog(stage, name, description);
    WidgetFactory.installResizable(stage);

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
    Stage stage = WidgetFactory.createDialogStage("suggestions-dialog", fxmlLoader, owner, title);
    SuggestionsDialogController controller = (SuggestionsDialogController) stage.getUserData();
    controller.initDialog(stage, initialValues);
    WidgetFactory.installResizable(stage);

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
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return controller.getResult();
  }

  /**
   * Opens the Add dialog for a brand-new, unattached {@link OverviewButtonLike} row, constructed via {@code
   * newInstanceFactory} (e.g. {@code Button::new} for a Row Action, {@code ButtonElement::new} for a
   * Footer button). The caller only adds the returned row to its owning list once present - it's never
   * attached to the model tree by this method itself.
   */
  public static Optional<OverviewButtonLike> showEventButtonForAdd(Stage owner, Supplier<OverviewButtonLike> newInstanceFactory) {
    return showEventButton(owner, StudioBundle.get("add_event_button_title"), newInstanceFactory.get());
  }

  /**
   * Opens the Edit dialog for a working copy of {@code row}, so a Cancel leaves the real, attached row
   * untouched - mirrors {@link de.a12.studio.ui.editors.formmodel.dialogs.Dialogs#showButtonForEdit}. The
   * caller only replaces the original row with the returned one once present.
   */
  public static Optional<OverviewButtonLike> showEventButtonForEdit(Stage owner, OverviewButtonLike row) {
    return showEventButton(owner, StudioBundle.get("edit_event_button_title"), cloneRow(row));
  }

  private static Optional<OverviewButtonLike> showEventButton(Stage owner, String title, OverviewButtonLike row) {
    FXMLLoader fxmlLoader = new FXMLLoader(EventButtonDialogController.class.getResource("event-button-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("event-button-dialog", fxmlLoader, owner, title);
    EventButtonDialogController controller = (EventButtonDialogController) stage.getUserData();
    controller.init(stage, row);
    stage.setOnHidden(event -> controller.destroy());
    WidgetFactory.installResizable(stage);

    stage.showAndWait();

    if (!controller.isConfirmed()) {
      return Optional.empty();
    }
    OverviewButtonLike result = controller.getButton();
    if (result.getEvent() == null || result.getEvent().isBlank()) {
      return Optional.empty();
    }
    return Optional.of(result);
  }

  @SuppressWarnings("unchecked")
  private static <T extends OverviewButtonLike> T cloneRow(T row) {
    String json = JsonSettings.objectMapper.writeValueAsString(row);
    return (T) JsonSettings.objectMapper.readValue(json, row.getClass());
  }
}
