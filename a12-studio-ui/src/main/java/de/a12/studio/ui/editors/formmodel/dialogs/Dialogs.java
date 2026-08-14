package de.a12.studio.ui.editors.formmodel.dialogs;

import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.util.FXResizeHelper;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

public class Dialogs {

  public static void openRepeatDefaultButtonLabels(@NonNull FormModel model) {
    FXMLLoader fxmlLoader = new FXMLLoader(RepeatDefaultButtonLabelsDialogController.class.getResource("repeat-default-button-labels-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("repeat-default-button-labels", fxmlLoader, Studio.stage, StudioBundle.get("repeat_default_button_labels"));
    RepeatDefaultButtonLabelsDialogController controller = (RepeatDefaultButtonLabelsDialogController) stage.getUserData();
    controller.setStage(stage);
    controller.setModel(model);

    FXResizeHelper.install(stage, 30, 6, WidgetFactory.DIALOG_SHADOW_MARGIN);
    stage.setMinWidth(700);
    stage.setMinHeight(500);
    stage.setOnHidden(event -> controller.destroy());

    stage.showAndWait();
  }

  public static void openSubheaderAndFooter(@NonNull FormModel model) {
    FXMLLoader fxmlLoader = new FXMLLoader(SubheaderAndFooterDialogController.class.getResource("subheader-and-footer-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("subheader-and-footer", fxmlLoader, Studio.stage, StudioBundle.get("subheader_and_footer"));
    SubheaderAndFooterDialogController controller = (SubheaderAndFooterDialogController) stage.getUserData();
    controller.setStage(stage);
    controller.setModel(model);

    FXResizeHelper.install(stage, 30, 6, WidgetFactory.DIALOG_SHADOW_MARGIN);
    stage.setMinWidth(700);
    stage.setMinHeight(500);

    stage.showAndWait();
  }
}
