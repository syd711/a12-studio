package de.a12.studio.ui.editors.querymodel;

import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Root-only "Only Links" checkbox ({@link QueryFilterableNode#getExclude()}, mirroring SME's root-only
 * {@code RootDocumentModel.exclude} - "retrieve only the Links from further Relationship Model elements, not
 * the target Document itself", a performance optimization) - see docs/sme-reference-comparison.md "Query
 * Model" section. Shown only when {@link QueryDocumentNodePanelController} is bound to the query's root
 * (relationship-link nodes have no such concept - {@link QueryFilterableNode#of(de.a12.studio.models.querymodel.QueryLink)}
 * answers {@code null}/no-ops for it). Model-header style, same reasoning as {@link
 * QueryFieldsProjectionPanelController} (not tied to a single {@link Element}).
 */
public class QueryOnlyLinksPanelController extends AbstractPropertyEditor {

  @FXML
  private CheckBox onlyLinksCheckBox;

  private QueryFilterableNode node;
  private Runnable onChange = () -> {
  };

  // Set while setNode() is repopulating onlyLinksCheckBox from the model - see class doc.
  private boolean updatingFromModel;

  @Override
  protected boolean suppressErrorContainer() {
    return true;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    onlyLinksCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || node == null) {
        return;
      }
      node.setExclude(newValue ? Boolean.TRUE : null);
      commitHeaderChange();
      onChange.run();
    });
  }

  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  public void setNode(@NonNull QueryFilterableNode node) {
    this.node = node;
    updatingFromModel = true;
    try {
      onlyLinksCheckBox.setSelected(Boolean.TRUE.equals(node.getExclude()));
    } finally {
      updatingFromModel = false;
    }
  }

  /** Shows/hides this whole panel - see class doc on why it's root-only. */
  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }
}
