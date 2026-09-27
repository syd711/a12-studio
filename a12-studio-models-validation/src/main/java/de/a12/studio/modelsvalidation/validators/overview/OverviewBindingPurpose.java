package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.Binding;
import de.a12.studio.models.formmodel.BindingComponent;
import de.a12.studio.models.formmodel.BindingComponentModelsSme;
import de.a12.studio.models.formmodel.BindingContent;
import de.a12.studio.models.formmodel.BindingDetails;
import de.a12.studio.models.formmodel.BindingRepeat;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.relationshipuimodel.DualPaneSelectionComponent;
import de.a12.studio.models.relationshipuimodel.EditConfiguration;
import de.a12.studio.models.relationshipuimodel.RelationshipUiComponent;
import de.a12.studio.models.relationshipuimodel.RelationshipUiModel;
import de.a12.studio.models.relationshipuimodel.TableListComponent;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.form.FormBindingElements;

/**
 * Whether an Overview Model is the Available Items or Selected Items overview of a Form Model {@link Binding}/
 * {@link BindingRepeat} component, or of a Relationship UI Model's {@code DualPaneSelection}/{@code TableList}
 * component (55 of the 84 overview fixtures are named like one, e.g. {@code *_AvailableItems_OM}, {@code
 * *_SelectedItems_Ov}) - mirrors SME's {@code omParser.isBindingOverviewModel}, which scans the project's Form
 * Models for a Binding component's {@code modelsSME.availableItemsOverview}/{@code selectedItemsOverview}. SME's
 * scan is Form-Model-only; the Relationship UI Model half is this port's own addition, since a12-studio models
 * that binding shape as a distinct model type (see {@code RelationshipUiComponent}) rather than folding it into
 * {@code formModel} the way SME's own simpler binding shape does - the real fixtures make clear the same
 * "Available/Selected Items" pairing exists there too ({@code DualPaneSelectionComponent}/{@code
 * TableListComponent}/{@code EditConfiguration}). Once known, a purpose gates several other SME rules (see
 * {@code docs/sme-reference-comparison.md}, "Overview Model: gap review", gap 6) - not yet built here.
 */
public final class OverviewBindingPurpose {

  public static final String AVAILABLE_ITEM = "available_item";
  public static final String SELECTED_ITEM = "selected_item";

  private OverviewBindingPurpose() {
  }

  /** {@code null} if {@code overviewModelId} isn't referenced as an Available/Selected Items overview by any
   * Form Model Binding or Relationship UI Model component in {@code context}. The first match wins - real
   * fixtures never reuse one Overview Model as both, so there is no defined tie-break for that case. */
  public static String resolve(String overviewModelId, ValidationContext context) {
    if (overviewModelId == null || overviewModelId.isBlank()) {
      return null;
    }
    for (A12Model<?> other : context.otherModels()) {
      String purpose = switch (other) {
        case FormModel formModel -> resolveFromFormModel(overviewModelId, formModel);
        case RelationshipUiModel relationshipUiModel -> resolveFromRelationshipUiModel(overviewModelId, relationshipUiModel);
        default -> null;
      };
      if (purpose != null) {
        return purpose;
      }
    }
    return null;
  }

  private static String resolveFromFormModel(String overviewModelId, FormModel formModel) {
    for (FormBindingElements.BindingHolder holder : FormBindingElements.findBindingContents(formModel)) {
      BindingContent content = holder.content();
      BindingDetails details = content != null ? content.getDetails() : null;
      if (details == null) {
        continue;
      }
      String purpose = purposeFromComponent(overviewModelId, details.getMainComponent());
      if (purpose == null) {
        purpose = purposeFromComponent(overviewModelId, details.getEditModalComponent());
      }
      if (purpose != null) {
        return purpose;
      }
    }
    return null;
  }

  private static String purposeFromComponent(String overviewModelId, BindingComponent component) {
    BindingComponentModelsSme models = component != null ? component.getModelsSME() : null;
    if (models == null) {
      return null;
    }
    if (overviewModelId.equals(models.getAvailableItemsOverview())) {
      return AVAILABLE_ITEM;
    }
    if (overviewModelId.equals(models.getSelectedItemsOverview())) {
      return SELECTED_ITEM;
    }
    return null;
  }

  private static String resolveFromRelationshipUiModel(String overviewModelId, RelationshipUiModel relationshipUiModel) {
    if (relationshipUiModel.getContent() == null) {
      return null;
    }
    RelationshipUiComponent component = relationshipUiModel.getContent().getComponent();
    if (component instanceof DualPaneSelectionComponent dualPane) {
      if (overviewModelId.equals(dualPane.getAvailableItemsOverviewModel())) {
        return AVAILABLE_ITEM;
      }
      if (overviewModelId.equals(dualPane.getSelectedItemsOverviewModel())) {
        return SELECTED_ITEM;
      }
    }
    else if (component instanceof TableListComponent tableList) {
      if (overviewModelId.equals(tableList.getSelectedItemsOverviewModel())) {
        return SELECTED_ITEM;
      }
      EditConfiguration editConfiguration = tableList.getEditConfiguration();
      if (editConfiguration != null) {
        if (overviewModelId.equals(editConfiguration.getAvailableItemsOverviewModel())) {
          return AVAILABLE_ITEM;
        }
        if (overviewModelId.equals(editConfiguration.getSelectedItemsOverviewModel())) {
          return SELECTED_ITEM;
        }
      }
    }
    return null;
  }
}
