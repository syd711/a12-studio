package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.formmodel.CustomScreenElement;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.editors.formmodel.NamePanelController;
import de.a12.studio.ui.editors.formmodel.StylesPanelController;
import de.a12.studio.ui.editors.formmodel.formtree.FormModelTreeController;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextTypePanelController;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.scene.Node;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

/**
 * The Form Model tree's right-hand editor pane for a selected {@link CustomScreenElement} node ({@link
 * FormModelTreeController}): Label, Hide Condition, Styles and Annotations, plus either a "Relationship UI
 * Model Reference" combo box ({@link RelationshipUiReferencePanelController}) or a plain Name field ({@link
 * NamePanelController}), whichever fits the node's {@code reference} - a {@link CustomScreenElement} is used
 * for both a generic custom UI component (identified by a free-form {@code reference}) and an embedded
 * Relationship UI Model (see {@code Team_Relationships_Fm.json}'s {@code relationshipuimodel_*}-prefixed
 * elements), and the two need different editors since only the latter has a project-wide list of valid
 * targets to pick from. Previously this node type's editor bound "Name" to the inherited {@code
 * ScreenElement.name} field, which none of the fixtures actually use - {@code reference} (the field these
 * fixtures do set) had no editor UI at all, matching {@link
 * de.a12.studio.ui.editors.formmodel.formtree.FormElementViewModel}'s pre-fix tree label falling back to the
 * raw node id.
 */
public class FormNodeEditorCustomScreenElementPanelController {

  @FXML
  private Node name;
  @FXML
  private NamePanelController nameController;
  @FXML
  private Node relationshipUiReference;
  @FXML
  private RelationshipUiReferencePanelController relationshipUiReferenceController;
  @FXML
  private LocalizedTextTypePanelController labelController;
  @FXML
  private HideConditionPanelController hideConditionController;
  @FXML
  private StylesPanelController stylesController;
  @FXML
  private AnnotationsPanelController annotationsController;

  @FXML
  private void initialize() {
    labelController.configureCustom("label", StudioBundle.get("label"));
  }

  public void setCustomScreenElement(@NonNull CustomScreenElement element, @Nullable ElementIndex elementIndex,
      HideConditionPanelController.@NonNull MasterFieldScope hideConditionScope, @NonNull ProjectItem projectItem) {
    List<String> relationshipUiModelIds = relationshipUiModelIds(projectItem);
    boolean isRelationshipUiReference = element.getReference() != null && relationshipUiModelIds.contains(element.getReference());

    setVisible(relationshipUiReference, isRelationshipUiReference);
    setVisible(name, !isRelationshipUiReference);
    if (isRelationshipUiReference) {
      relationshipUiReferenceController.setCustom(relationshipUiModelIds, element::getReference, element::setReference);
    }
    else {
      nameController.setCustom(element::getName, element::setName);
    }

    labelController.setCustom(element::getTitle, element::setTitle);
    labelController.setFieldSuggestionSource(elementIndex);
    hideConditionController.configure(
        element::getHideCondition, element::setHideCondition,
        elementIndex, hideConditionScope);
    stylesController.setCustom(element::getStyle, element::getStyle);
    annotationsController.setCustom(element::getAnnotations);
  }

  private static List<String> relationshipUiModelIds(@NonNull ProjectItem projectItem) {
    return ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.RELATIONSHIPUI).stream()
        .map(A12Model::getId)
        .sorted(Comparator.naturalOrder())
        .toList();
  }

  private static void setVisible(@NonNull Node node, boolean visible) {
    node.setVisible(visible);
    node.setManaged(visible);
  }
}
