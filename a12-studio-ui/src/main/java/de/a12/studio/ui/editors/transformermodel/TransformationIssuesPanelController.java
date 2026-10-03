package de.a12.studio.ui.editors.transformermodel;

import de.a12.studio.ui.editors.transformermodel.TransformationOutcome.Issue;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * The outcome of the last transformation at the bottom of the Transformation tab: whether the Document Model could be
 * generated and the transformer's issues (SME's {@code displayLastTransformationIssues}, which lists only errors and
 * warnings on the Transformation tab - the info messages, mostly "Fallback to default type mappings ...", are an
 * option here). Not a property editor: it edits nothing, so it is a plain controller.
 */
public class TransformationIssuesPanelController implements Initializable {

  @FXML
  private TitledPane root;

  @FXML
  private Label statusLabel;

  @FXML
  private CheckBox showInfoCheckBox;

  @FXML
  private ListView<Issue> issuesList;

  private final ObservableList<Issue> shownIssues = FXCollections.observableArrayList();

  private List<Issue> allIssues = List.of();

  private Runnable onRunRequested = () -> {
  };

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    issuesList.setItems(shownIssues);
    issuesList.setPlaceholder(new Label(StudioBundle.get("transformer_model.issues.none")));
    issuesList.setCellFactory(view -> new IssueCell());
    showInfoCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> applyFilter());
  }

  /** Called when the user asks for another run. */
  public void setOnRunRequested(@NonNull Runnable onRunRequested) {
    this.onRunRequested = onRunRequested;
  }

  @FXML
  private void onRun() {
    onRunRequested.run();
  }

  public void showRunning() {
    setStatus(StudioBundle.get("transformer_model.issues.running"), false);
  }

  public void show(@NonNull TransformationOutcome outcome) {
    allIssues = outcome.issues();
    switch (outcome.state()) {
      case INCOMPLETE -> setStatus(outcome.message(), false);
      case UNAVAILABLE -> setStatus(StudioBundle.get("transformer_model.issues.unavailable", outcome.message()), true);
      case DONE -> {
        if (outcome.success()) {
          setStatus(StudioBundle.get("transformer_model.issues.success", outcome.documentModel().getId(),
              outcome.count(TransformationOutcome.SEVERITY_ERROR), outcome.count(TransformationOutcome.SEVERITY_WARNING)), false);
        }
        else {
          setStatus(StudioBundle.get("transformer_model.issues.failed"), true);
        }
      }
    }
    applyFilter();
  }

  private void setStatus(String text, boolean failed) {
    statusLabel.setText(text);
    statusLabel.getStyleClass().removeAll("transformer-status", "transformer-status-failed");
    statusLabel.getStyleClass().add(failed ? "transformer-status-failed" : "transformer-status");
    if (text == null || text.isEmpty() || !failed) {
      return;
    }
    root.setExpanded(true);
  }

  private void applyFilter() {
    boolean info = showInfoCheckBox.isSelected();
    List<Issue> visible = new ArrayList<>();
    for (Issue issue : allIssues) {
      if (info || !TransformationOutcome.SEVERITY_INFO.equals(issue.severity())) {
        visible.add(issue);
      }
    }
    shownIssues.setAll(visible);
  }

  /** The issues currently listed (for tests and the editor's badge). */
  List<Issue> shownIssues() {
    return List.copyOf(shownIssues);
  }

  String statusText() {
    return statusLabel.getText();
  }

  private final class IssueCell extends ListCell<Issue> {

    @Override
    protected void updateItem(Issue issue, boolean empty) {
      super.updateItem(issue, empty);
      if (empty || issue == null) {
        setGraphic(null);
        setText(null);
        return;
      }
      Label message = new Label(issue.source() == null || issue.source().isBlank() ? issue.message() : issue.message() + "  [" + issue.source() + "]");
      message.setWrapText(true);
      message.prefWidthProperty().bind(issuesList.widthProperty().subtract(52));
      var icon = switch (issue.severity()) {
        case TransformationOutcome.SEVERITY_ERROR -> {
          message.getStyleClass().add("transformer-issue-error");
          yield WidgetFactory.createExclamationIcon(null);
        }
        case TransformationOutcome.SEVERITY_WARNING -> {
          message.getStyleClass().add("transformer-issue-warning");
          yield WidgetFactory.createWarningIcon(null);
        }
        default -> {
          message.getStyleClass().add("transformer-issue-info");
          yield WidgetFactory.createIcon("mdi2i-information-outline");
        }
      };
      HBox box = new HBox(8, icon, message);
      setGraphic(box);
      setText(null);
    }
  }
}
