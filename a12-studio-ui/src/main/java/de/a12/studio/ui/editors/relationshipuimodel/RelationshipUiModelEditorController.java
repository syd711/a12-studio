package de.a12.studio.ui.editors.relationshipuimodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.models.relationshipuimodel.DropDownSelectionComponent;
import de.a12.studio.models.relationshipuimodel.DualPaneSelectionComponent;
import de.a12.studio.models.relationshipuimodel.EditConfiguration;
import de.a12.studio.models.relationshipuimodel.RelationshipUiComponent;
import de.a12.studio.models.relationshipuimodel.RelationshipUiModel;
import de.a12.studio.models.relationshipuimodel.TableListComponent;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.events.ModelSaveEvent;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.ProjectDocumentModels;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Edits a {@link RelationshipUiModel}: the relationship/target-role it presents ({@link
 * RelationshipReferencePanelController}), its component type ({@link ComponentTypePanelController}), and the
 * fields of whichever component variant is currently selected - exactly one of {@link
 * DualPaneSelectionPanelController}/{@link TableListPanelController}/{@link
 * DropDownSelectionPanelController} is shown at a time, mirroring {@link
 * de.a12.studio.ui.editors.maindetailmodel.MainModelReferencePanelController}'s type-switch pattern. The
 * header's {@code modelReferences} are kept in sync with the component's populated Overview/Query/Form Model
 * fields on every change (see {@link #syncModelReferences}), the same way {@link
 * de.a12.studio.ui.editors.relationshipmodel.RelationshipModelEditorController} does for entities.
 */
public class RelationshipUiModelEditorController extends AbstractEditorController implements Initializable {

  private static final Set<String> RELATIONSHIP_UI_PURPOSES = Set.of(
      ModelReference.PURPOSE_RELATIONSHIP_UI_AVAILABLE_ITEMS,
      ModelReference.PURPOSE_RELATIONSHIP_UI_SELECTED_ITEMS,
      ModelReference.PURPOSE_RELATIONSHIP_UI_LINK,
      ModelReference.PURPOSE_RELATIONSHIP_UI_AVAILABLE_ITEMS_IN_EDIT_MODAL,
      ModelReference.PURPOSE_RELATIONSHIP_UI_SELECTED_ITEMS_IN_EDIT_MODAL,
      ModelReference.PURPOSE_RELATIONSHIP_UI_AVAILABLE_ITEMS_QUERY,
      ModelReference.PURPOSE_RELATIONSHIP_UI_SELECTED_ITEM_QUERY);

  @FXML
  private RelationshipReferencePanelController relationshipReferenceController;
  @FXML
  private ComponentTypePanelController componentTypeController;
  @FXML
  private DualPaneSelectionPanelController dualPaneSelectionController;
  @FXML
  private TableListPanelController tableListController;
  @FXML
  private DropDownSelectionPanelController dropDownSelectionController;

  private RelationshipUiModel model;

  @Override
  public void initialize(URL url, ResourceBundle resources) {
    relationshipReferenceController.setOnChange(this::commitChange);
    componentTypeController.setOnChange(this::onComponentTypeChanged);
    dualPaneSelectionController.setOnChange(this::syncModelReferences);
    tableListController.setOnChange(this::syncModelReferences);
    dropDownSelectionController.setOnChange(this::syncModelReferences);
  }

  @Override
  public void loadModel(@NonNull A12Model<?> model) {
    load((RelationshipUiModel) model);
    updateSettingsErrorBadge();
  }

  private void load(@NonNull RelationshipUiModel model) {
    this.model = model;

    relationshipReferenceController.load(model, modelsOfType(ModelType.RELATIONSHIP, RelationshipModel.class));
    componentTypeController.setModel(model);

    List<String> overviewModelOptions = modelIds(ModelType.OVERVIEW, OverviewModel.class);
    List<String> formModelOptions = modelIds(ModelType.FORM, FormModel.class);
    List<String> queryModelOptions = modelIds(ModelType.QUERY, QueryModel.class);
    dualPaneSelectionController.setModelOptions(overviewModelOptions, formModelOptions);
    tableListController.setModelOptions(overviewModelOptions, formModelOptions);
    dropDownSelectionController.setModelOptions(queryModelOptions);

    updateComponentVisibility();
  }

  /**
   * Refreshes every model-reference option list (and, since a Relationship Model can add/remove roles, the
   * target role list too) whenever such a model is saved in a different tab, so they don't go stale while this
   * tab stays open. Unlike most editors, several referenced model types matter here (not just Document
   * Models), so this replaces rather than extends {@link #onDocumentModelChangedElsewhere}.
   */
  @Override
  public void modelSaved(@NonNull ModelSaveEvent event) {
    if (projectItem != null && !event.getItem().equals(projectItem) && isRelevantModelType(event.getItem().getModel())) {
      load(model);
      return;
    }
    super.modelSaved(event);
  }

  private static boolean isRelevantModelType(A12Model<?> other) {
    return other instanceof OverviewModel || other instanceof QueryModel || other instanceof FormModel || other instanceof RelationshipModel;
  }

  private void onComponentTypeChanged() {
    syncModelReferences();
    updateComponentVisibility();
    commitChange();
  }

  private void updateComponentVisibility() {
    RelationshipUiComponent component = model.getContent().getComponent();
    boolean dualPane = component instanceof DualPaneSelectionComponent;
    boolean tableList = component instanceof TableListComponent;
    boolean dropDown = component instanceof DropDownSelectionComponent;

    dualPaneSelectionController.setVisible(dualPane);
    if (dualPane) {
      dualPaneSelectionController.setModel(model);
    }
    tableListController.setVisible(tableList);
    if (tableList) {
      tableListController.setModel(model);
    }
    dropDownSelectionController.setVisible(dropDown);
    if (dropDown) {
      dropDownSelectionController.setModel(model);
    }
  }

  /** Rebuilds the header's model references from the currently selected component's populated fields. */
  private void syncModelReferences() {
    List<ModelReference> references = model.getModelReferences();
    references.removeIf(reference -> RELATIONSHIP_UI_PURPOSES.contains(reference.getPurpose()));

    RelationshipUiComponent component = model.getContent().getComponent();
    if (component instanceof DualPaneSelectionComponent dualPane) {
      addReference(references, ModelReference.PURPOSE_RELATIONSHIP_UI_AVAILABLE_ITEMS, ModelType.OVERVIEW, dualPane.getAvailableItemsOverviewModel());
      addReference(references, ModelReference.PURPOSE_RELATIONSHIP_UI_SELECTED_ITEMS, ModelType.OVERVIEW, dualPane.getSelectedItemsOverviewModel());
      addReference(references, ModelReference.PURPOSE_RELATIONSHIP_UI_LINK, ModelType.FORM, dualPane.getLinkFormModel());
    }
    else if (component instanceof TableListComponent tableList) {
      addReference(references, ModelReference.PURPOSE_RELATIONSHIP_UI_SELECTED_ITEMS, ModelType.OVERVIEW, tableList.getSelectedItemsOverviewModel());
      addReference(references, ModelReference.PURPOSE_RELATIONSHIP_UI_LINK, ModelType.FORM, tableList.getLinkFormModel());
      EditConfiguration editConfiguration = tableList.getEditConfiguration();
      if (editConfiguration != null) {
        addReference(references, ModelReference.PURPOSE_RELATIONSHIP_UI_AVAILABLE_ITEMS_IN_EDIT_MODAL, ModelType.OVERVIEW,
            editConfiguration.getAvailableItemsOverviewModel());
        addReference(references, ModelReference.PURPOSE_RELATIONSHIP_UI_SELECTED_ITEMS_IN_EDIT_MODAL, ModelType.OVERVIEW,
            editConfiguration.getSelectedItemsOverviewModel());
      }
    }
    else if (component instanceof DropDownSelectionComponent dropDown) {
      addReference(references, ModelReference.PURPOSE_RELATIONSHIP_UI_AVAILABLE_ITEMS_QUERY, ModelType.QUERY, dropDown.getAvailableItemsQueryModel());
      addReference(references, ModelReference.PURPOSE_RELATIONSHIP_UI_SELECTED_ITEM_QUERY, ModelType.QUERY, dropDown.getSelectedItemQueryModel());
    }
  }

  private void addReference(List<ModelReference> references, String purpose, ModelType modelType, String value) {
    if (value == null || value.isBlank()) {
      return;
    }
    ModelReference reference = new ModelReference();
    reference.setPurpose(purpose);
    reference.setModelType(modelType);
    reference.setReference(value);
    references.add(reference);
  }

  private <T extends A12Model<?>> List<T> modelsOfType(ModelType modelType, Class<T> type) {
    return ProjectDocumentModels.getOtherModelsOfType(projectItem, modelType).stream()
        .filter(type::isInstance)
        .map(type::cast)
        .sorted(Comparator.comparing(A12Model::getId))
        .toList();
  }

  private <T extends A12Model<?>> List<String> modelIds(ModelType modelType, Class<T> type) {
    return modelsOfType(modelType, type).stream().map(A12Model::getId).toList();
  }

  private void commitChange() {
    save();
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.RELATIONSHIPUI;
  }
}
