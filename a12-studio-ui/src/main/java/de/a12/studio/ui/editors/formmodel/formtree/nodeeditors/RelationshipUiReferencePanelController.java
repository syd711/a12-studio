package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.ProjectDocumentModels;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The Form Model tree's editor for a {@link de.a12.studio.models.formmodel.CustomScreenElement} node whose
 * {@code reference} resolves to a Relationship UI Model in the project ({@link
 * FormNodeEditorCustomScreenElementPanelController}, shown in place of the plain {@code name-panel.fxml} used
 * for a generic custom component): a combo box offering every
 * Relationship UI Model in the project plus an "open" button, same idiom as {@code TargetModelPanelController}
 * (a plain single-combobox "pick one model" field), adapted to this tree's {@code
 * NamePanelController}-style {@code setCustom(reader, writer)} convention since {@code reference} isn't tied
 * to a single {@code Element}.
 */
public class RelationshipUiReferencePanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private ComboBox<String> referenceField;
  @FXML
  private Button openReferenceButton;

  private Consumer<String> writer;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    bindComboBox(referenceField, (el, value) -> writer.accept(value));
    openReferenceButton.disableProperty().bind(referenceField.valueProperty().isNull());
  }

  @FXML
  private void onOpenReference(ActionEvent event) {
    String reference = referenceField.getValue();
    if (reference != null) {
      ProjectDocumentModels.openModelInEditor(reference);
    }
  }

  public void setCustom(@NonNull List<String> relationshipUiModelIds, @NonNull Supplier<String> reader,
      @NonNull Consumer<String> writer) {
    this.writer = writer;
    setComboBoxItems(referenceField, relationshipUiModelIds);
    setFieldValue(referenceField, reader.get());
  }
}
