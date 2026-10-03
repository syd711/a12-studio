package de.a12.studio.ui.editors.formmodel.formtree;

import de.a12.studio.models.formmodel.Button;
import de.a12.studio.models.formmodel.NavigationButton;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.util.StudioBundle;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SME's refactoring choice for navigation buttons whose target screen is being deleted: per button either
 * <em>Commit</em> (clear the target), <em>Edit</em> (point it to another screen) or <em>Delete</em> (remove the button).
 */
final class NavigationReferenceDialog {

  enum Action {
    CLEAR_TARGET("form_model_tree.ref_action_clear"),
    RETARGET("form_model_tree.ref_action_retarget"),
    DELETE_BUTTON("form_model_tree.ref_action_delete");

    private final String key;

    Action(String key) {
      this.key = key;
    }

    String label() {
      return StudioBundle.get(key);
    }
  }

  /** What to do with one button; {@code newTarget} is only set for {@link Action#RETARGET}. */
  record Decision(NavigationButton button, Action action, String newTarget) {
  }

  private NavigationReferenceDialog() {
  }

  /** Empty when the user cancelled (the screen is then not deleted). */
  static Optional<List<Decision>> show(List<NavigationButton> buttons, List<Screen> alternatives) {
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.initOwner(Studio.stage);
    dialog.setTitle(StudioBundle.get("form_model_tree.ref_dialog_title"));
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(8);
    grid.setPadding(new Insets(12));
    grid.add(new Label(StudioBundle.get("form_model_tree.ref_dialog_help")), 0, 0, 3, 1);

    List<ComboBox<Action>> actions = new ArrayList<>();
    List<ComboBox<Screen>> targets = new ArrayList<>();
    for (int i = 0; i < buttons.size(); i++) {
      Button button = buttons.get(i);
      int row = i + 1;
      ComboBox<Action> actionBox = new ComboBox<>();
      actionBox.getItems().add(Action.CLEAR_TARGET);
      if (!alternatives.isEmpty()) {
        actionBox.getItems().add(Action.RETARGET);
      }
      actionBox.getItems().add(Action.DELETE_BUTTON);
      actionBox.setConverter(new StringConverter<>() {
        @Override
        public String toString(Action action) {
          return action == null ? "" : action.label();
        }

        @Override
        public Action fromString(String string) {
          return null;
        }
      });
      actionBox.setValue(Action.DELETE_BUTTON);

      ComboBox<Screen> targetBox = new ComboBox<>();
      targetBox.getItems().addAll(alternatives);
      targetBox.setConverter(new StringConverter<>() {
        @Override
        public String toString(Screen screen) {
          return screen == null ? "" : (screen.getName() != null && !screen.getName().isBlank() ? screen.getName() : screen.getId());
        }

        @Override
        public Screen fromString(String string) {
          return null;
        }
      });
      if (!alternatives.isEmpty()) {
        targetBox.setValue(alternatives.get(0));
      }
      targetBox.disableProperty().bind(actionBox.valueProperty().isNotEqualTo(Action.RETARGET));

      String name = button.getName() != null && !button.getName().isBlank() ? button.getName() : button.getId();
      grid.add(new Label(name), 0, row);
      grid.add(actionBox, 1, row);
      grid.add(targetBox, 2, row);
      actions.add(actionBox);
      targets.add(targetBox);
    }
    dialog.getDialogPane().setContent(grid);

    Optional<ButtonType> result = dialog.showAndWait();
    if (result.isEmpty() || result.get() != ButtonType.OK) {
      return Optional.empty();
    }
    List<Decision> decisions = new ArrayList<>();
    for (int i = 0; i < buttons.size(); i++) {
      Action action = actions.get(i).getValue();
      Screen target = targets.get(i).getValue();
      decisions.add(new Decision((NavigationButton) buttons.get(i), action, action == Action.RETARGET && target != null ? target.getId() : null));
    }
    return Optional.of(decisions);
  }
}
