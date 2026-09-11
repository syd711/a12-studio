package de.a12.studio.ui.editors.relationshipuimodel;

import de.a12.studio.models.relationshipuimodel.DropDownSelectionComponent;
import de.a12.studio.models.relationshipuimodel.DualPaneSelectionComponent;
import de.a12.studio.models.relationshipuimodel.RelationshipUiComponent;
import de.a12.studio.models.relationshipuimodel.RelationshipUiModel;
import de.a12.studio.models.relationshipuimodel.TableListComponent;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Edits a {@link RelationshipUiModel}'s {@code content.component.componentType}: picking a new type replaces
 * {@code content.component} with a brand-new instance of the matching {@link RelationshipUiComponent} subtype
 * (discarding whatever fields the previous one had, same as {@link
 * de.a12.studio.ui.editors.maindetailmodel.MainModelReferencePanelController}'s type-switch clears the other
 * reference). The owning editor ({@link RelationshipUiModelEditorController}) reacts to {@link #setOnChange} by
 * showing the matching detail panel and rebinding it to the new component instance.
 */
public class ComponentTypePanelController implements Initializable {

  @FXML
  private ComboBox<String> componentTypeField;
  @FXML
  private ErrorContainerController errorContainerController;

  private RelationshipUiModel model;

  // Set while fields are being repopulated from the model, so those programmatic updates aren't mistaken for
  // user edits and don't trigger onChange/a save.
  private boolean updatingFromModel;

  private Runnable onChange = () -> {
  };

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    componentTypeField.setItems(FXCollections.observableArrayList(
        RelationshipUiComponent.TYPE_DUAL_PANE_SELECTION, RelationshipUiComponent.TYPE_TABLE_LIST, RelationshipUiComponent.TYPE_DROP_DOWN_SELECTION));
    componentTypeField.setConverter(new StringConverter<>() {
      @Override
      public String toString(String type) {
        return type == null ? "" : StudioBundle.get("component_type." + type);
      }

      @Override
      public String fromString(String string) {
        return null;
      }
    });
    componentTypeField.valueProperty().addListener((observable, oldValue, newValue) -> {
      validate();
      if (updatingFromModel || model == null) {
        return;
      }
      model.getContent().setComponent(newValue == null ? null : createComponent(newValue));
      onChange.run();
    });
  }

  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  public void setModel(@NonNull RelationshipUiModel model) {
    this.model = model;
    updatingFromModel = true;
    try {
      componentTypeField.setValue(componentTypeOf(model.getContent().getComponent()));
    }
    finally {
      updatingFromModel = false;
    }
    validate();
  }

  private void validate() {
    if (componentTypeField.getValue() == null) {
      errorContainerController.show("ERROR", StudioBundle.get("select_a_component_type"));
    }
    else {
      errorContainerController.hide();
    }
  }

  private static String componentTypeOf(RelationshipUiComponent component) {
    if (component instanceof DualPaneSelectionComponent) {
      return RelationshipUiComponent.TYPE_DUAL_PANE_SELECTION;
    }
    if (component instanceof TableListComponent) {
      return RelationshipUiComponent.TYPE_TABLE_LIST;
    }
    if (component instanceof DropDownSelectionComponent) {
      return RelationshipUiComponent.TYPE_DROP_DOWN_SELECTION;
    }
    return null;
  }

  private static RelationshipUiComponent createComponent(@NonNull String type) {
    return switch (type) {
      case RelationshipUiComponent.TYPE_TABLE_LIST -> new TableListComponent();
      case RelationshipUiComponent.TYPE_DROP_DOWN_SELECTION -> new DropDownSelectionComponent();
      default -> new DualPaneSelectionComponent();
    };
  }
}
