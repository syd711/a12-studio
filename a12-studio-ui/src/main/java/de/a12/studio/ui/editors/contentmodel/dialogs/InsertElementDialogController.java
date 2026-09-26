package de.a12.studio.ui.editors.contentmodel.dialogs;

import de.a12.studio.models.contentmodel.ContentModule;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.editors.contentmodel.ContentElementIcons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The "Add Element" dialog of the Content Model editor: the element types that may be added at the selected place
 * ({@link de.a12.studio.models.contentmodel.ContentInsertion}), grouped by category like SME's insert panel. Nothing is
 * preselected: the user picks one tile, then Add (or a double click on the tile) confirms.
 */
public class InsertElementDialogController implements DialogController {

  private static final double TILE_WIDTH = 116.0;

  private static final double TILE_HEIGHT = 76.0;

  @FXML
  private Label hintLabel;
  @FXML
  private VBox categoriesBox;
  @FXML
  private Label emptyLabel;
  @FXML
  private Button okButton;

  private final ToggleGroup tiles = new ToggleGroup();

  private Stage stage;

  private ContentModule selected;

  private boolean confirmed;

  @FXML
  private void initialize() {
    tiles.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
      selected = newToggle != null ? (ContentModule) newToggle.getUserData() : null;
      okButton.setDisable(selected == null);
    });
  }

  void init(Stage stage, @NonNull String targetLabel, @NonNull List<ContentModule> modules) {
    this.stage = stage;
    hintLabel.setText(StudioBundle.get("content_model_insert.hint", targetLabel));

    Map<String, List<ContentModule>> byCategory = new LinkedHashMap<>();
    for (ContentModule module : modules) {
      byCategory.computeIfAbsent(module.category(), key -> new ArrayList<>()).add(module);
    }
    byCategory.forEach((category, categoryModules) -> {
      Label heading = new Label(categoryTitle(category));
      heading.getStyleClass().add("section-header");
      FlowPane flow = new FlowPane(8.0, 8.0);
      categoryModules.forEach(module -> flow.getChildren().add(createTile(module)));
      categoriesBox.getChildren().add(new VBox(6.0, heading, flow));
    });

    boolean empty = modules.isEmpty();
    emptyLabel.setVisible(empty);
    emptyLabel.setManaged(empty);
  }

  private ToggleButton createTile(ContentModule module) {
    ToggleButton tile = new ToggleButton(module.label());
    tile.setToggleGroup(tiles);
    tile.setUserData(module);
    tile.setContentDisplay(ContentDisplay.TOP);
    tile.setGraphic(WidgetFactory.createIcon(ContentElementIcons.iconFor(module.type()), 24, null));
    tile.setGraphicTextGap(6.0);
    tile.setWrapText(true);
    tile.setAlignment(Pos.CENTER);
    tile.setMinSize(TILE_WIDTH, TILE_HEIGHT);
    tile.setPrefSize(TILE_WIDTH, TILE_HEIGHT);
    tile.setMaxSize(TILE_WIDTH, TILE_HEIGHT);
    tile.getStyleClass().add("insert-element-tile");
    tile.setOnMouseClicked(event -> {
      if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
        onDialogSubmit();
      }
    });
    return tile;
  }

  private static String categoryTitle(String category) {
    String key = switch (category) {
      case "Layout" -> "content_model_insert.category.layout";
      case "Content" -> "content_model_insert.category.content";
      case "General" -> "content_model_insert.category.general";
      case "Form Elements" -> "content_model_insert.category.form_elements";
      default -> null;
    };
    return key != null ? StudioBundle.get(key) : category;
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    if (selected == null) {
      return;
    }
    confirmed = true;
    stage.close();
  }

  boolean isConfirmed() {
    return confirmed;
  }

  ContentModule getSelected() {
    return selected;
  }

  /** For tests: the tile of {@code type}, or null. */
  ToggleButton tileOf(String type) {
    for (Toggle toggle : tiles.getToggles()) {
      if (toggle.getUserData() instanceof ContentModule module && module.type().equals(type)) {
        return (ToggleButton) toggle;
      }
    }
    return null;
  }
}
