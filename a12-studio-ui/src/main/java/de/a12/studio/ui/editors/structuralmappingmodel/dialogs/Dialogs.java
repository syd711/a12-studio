package de.a12.studio.ui.editors.structuralmappingmodel.dialogs;

import de.a12.studio.models.structuralmappingmodel.SmmNode;
import de.a12.studio.models.structuralmappingmodel.StructuralMappingModel;
import de.a12.studio.modelsvalidation.kernel.StructuralMappingContext;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class Dialogs {

  private Dialogs() {
  }

  /**
   * Opens the resolution strategy editor for {@code node} (a Fold or Slice node of {@code model}); {@code model}
   * is only changed - the strategy of the node rewritten in place - once OK is pressed.
   *
   * @return whether the strategy was changed
   */
  public static boolean showResolutionStrategy(Stage owner, StructuralMappingContext context, StructuralMappingModel model, SmmNode node) {
    FXMLLoader fxmlLoader = new FXMLLoader(ResolutionStrategyDialogController.class.getResource("resolution-strategy-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("resolution-strategy-dialog", fxmlLoader, owner, StudioBundle.get("structural_mapping.rs_dialog_title"));
    ResolutionStrategyDialogController controller = (ResolutionStrategyDialogController) stage.getUserData();
    controller.initDialog(stage, context, model, node);
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return controller.isConfirmed();
  }

  /**
   * Opens the dialog that lets the user pick the mapping block a field mapping moves to.
   *
   * @return the chosen block, empty if cancelled or the mapping stays where it is
   */
  public static Optional<MoveFieldMappingDialogController.Choice> showMoveFieldMapping(Stage owner, String mappingDescription,
      List<MoveFieldMappingDialogController.Choice> choices, MoveFieldMappingDialogController.Choice current) {
    FXMLLoader fxmlLoader = new FXMLLoader(MoveFieldMappingDialogController.class.getResource("move-field-mapping-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("move-field-mapping-dialog", fxmlLoader, owner, StudioBundle.get("structural_mapping.move_dialog_title"));
    MoveFieldMappingDialogController controller = (MoveFieldMappingDialogController) stage.getUserData();
    controller.initDialog(stage, mappingDescription, choices, current);
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return Optional.ofNullable(controller.getResult());
  }
}
