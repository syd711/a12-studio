package de.a12.studio.ui.editors;

import de.a12.studio.ui.editors.dialogs.Dialogs;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Circle;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Supplier;

/**
 * Controller for the reusable "Model Settings" toolbar button with its error badge,
 * included via {@code fx:include} in every editor toolbar that shows a settings button.
 *
 * <p>After loading, call {@link #setIssuesSupplier(Supplier)} so the badge knows which
 * validation issues to reflect — typically backed by the owning editor's {@code projectItem}.
 */
@Slf4j
public class EditorSettingsToolbarButtonController {

  private static final String DEFAULT_SETTINGS_TOOLTIP = "Model Settings";

  @FXML
  private Tooltip settingsButtonTooltip;

  @FXML
  private Circle settingsErrorBadge;

  private Supplier<List<String>> issuesSupplier;

  /**
   * Provide the current validation issue messages for the settings error badge.
   * Call this after the owning controller's {@code projectItem} is available.
   */
  public void setIssuesSupplier(Supplier<List<String>> issuesSupplier) {
    this.issuesSupplier = issuesSupplier;
  }

  @FXML
  private void onSettings(ActionEvent e) {
    Dialogs.openSettings();
    updateErrorBadge();
  }

  public void updateErrorBadge() {
    List<String> issues = issuesSupplier != null ? issuesSupplier.get() : List.of();

    settingsErrorBadge.setVisible(!issues.isEmpty());
    settingsButtonTooltip.setText(issues.isEmpty() ? DEFAULT_SETTINGS_TOOLTIP : String.join("\n\n", issues));
  }
}
