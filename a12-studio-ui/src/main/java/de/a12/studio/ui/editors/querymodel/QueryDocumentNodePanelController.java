package de.a12.studio.ui.editors.querymodel;

import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.querymodel.ql.QueryLanguageEmitter;
import de.a12.studio.models.querymodel.ql.QueryLanguageException;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.query.QueryFilterReferenceChecker;
import de.a12.studio.ui.editors.propertyeditors.BracketedPathSuggestionProvider;
import de.a12.studio.ui.editors.propertyeditors.RuleEditorController;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * The right-hand panel shown by {@link QueryModelTreeController} whenever a "document node" row is selected
 * ({@link QueryTreeRow#isDocumentNode()} - the query's own target Document Model, or a relationship link that
 * resolved to one): its "Filter Definition" (a {@link RuleEditorController}, retitled/rebound to whichever
 * {@link QueryFilterableNode} is currently selected) and "Fields included in Result Set" ({@link
 * QueryFieldsProjectionPanelController}), plus - root only - "Only Links" ({@link
 * QueryOnlyLinksPanelController}). See docs/sme-reference-comparison.md "Query Model" section for how this
 * mirrors SME's {@code QMDetailsDMElementForm}/{@code QMDetailsRootDMElementForm}.
 *
 * <p>Not itself an {@link de.a12.studio.ui.editors.AbstractPropertyEditor} - like {@code
 * DocumentModelFieldEditorController}, it's a plain composing controller whose children do the actual
 * element/model-header binding. {@link #filterDefinitionPanelController}'s writer does its own full commit
 * (save + {@link StudioEventManager#fireModelSavedEvent} + {@link #onChange}) rather than relying on its
 * inherited {@code commitChange()}, which would silently skip firing that event: {@code RuleEditorController}
 * is never {@code setElement()}-bound here (there is no single {@link de.a12.studio.models.documentmodel.Element}
 * backing a {@link QueryFilterableNode}), and {@code AbstractPropertyEditor#commitChange()} only fires the
 * event when a bound {@code Element} exists. The resulting extra {@code ProjectItem#save()} from that inherited
 * call afterward is a harmless redundant write of the same already-current content.
 */
public class QueryDocumentNodePanelController {

  private static final QueryLanguageEmitter EMITTER = new QueryLanguageEmitter();

  @FXML
  private Label targetDocumentModelLabel;

  @FXML
  private RuleEditorController filterDefinitionPanelController;

  @FXML
  private QueryFieldsProjectionPanelController fieldsProjectionPanelController;

  @FXML
  private QueryOnlyLinksPanelController onlyLinksPanelController;

  private ProjectItem projectItem;
  private DocumentModel scopeModel;
  private QueryFilterReferenceChecker referenceChecker;
  private Runnable onChange = () -> {
  };

  @FXML
  private void initialize() {
    filterDefinitionPanelController.configureCustom("filterDefinition", StudioBundle.get("filter_definition"));
    filterDefinitionPanelController.setValidator(this::validate);
    fieldsProjectionPanelController.setOnChange(() -> onChange.run());
    onlyLinksPanelController.setOnChange(() -> onChange.run());
  }

  /** Invoked after any field in this panel (or one of its sub-panels) commits a change, so {@link
   * QueryModelTreeController} can refresh its own "In Result" checkboxes - they read/write the very same
   * {@code fields} list {@link QueryFieldsProjectionPanelController} does. */
  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  public void load(@NonNull ProjectItem projectItem, @NonNull QueryFilterableNode node, DocumentModel targetDocumentModel,
      @NonNull String targetDocumentModelId, boolean isRoot) {
    this.projectItem = projectItem;
    targetDocumentModelLabel.setText(StudioBundle.get(
        isRoot ? "query_document_node.target_document_model" : "query_document_node.linked_document_model", targetDocumentModelId));

    // Snapshot of the project's models for the semantic filter check, taken once per load like the suggestion
    // provider's index below: the validator runs (debounced) on every keystroke, and collecting the project's
    // models is a tree walk. Must be in place before setCustom, which validates the initial value.
    scopeModel = targetDocumentModel;
    referenceChecker = new QueryFilterReferenceChecker(new QueryFilterReferenceChecker.Models(
        ProjectDocumentModels.getOtherDocumentModels(projectItem),
        ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.RELATIONSHIP).stream()
            .filter(RelationshipModel.class::isInstance).map(RelationshipModel.class::cast).toList()));

    if (targetDocumentModel != null) {
      filterDefinitionPanelController.setSuggestionProvider(new BracketedPathSuggestionProvider(new ElementIndex(targetDocumentModel)));
    }
    filterDefinitionPanelController.setCustom(node::getFilterDefinition, value -> {
      node.setFilterDefinition(value);
      commitChange();
    });

    fieldsProjectionPanelController.setNode(node, targetDocumentModel);

    onlyLinksPanelController.setNode(node);
    onlyLinksPanelController.setVisible(isRoot);
  }

  /** Re-renders {@link #fieldsProjectionPanelController}'s field list/add-combo against its currently bound
   * node's current {@code fields} - see {@link QueryModelTreeController#toggleInResult} for why an external
   * change (the tree's own "In Result" checkboxes) needs this. */
  public void refresh() {
    fieldsProjectionPanelController.refresh();
  }

  private void commitChange() {
    projectItem.save();
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
    onChange.run();
  }

  /** Syntax first (nothing else can be resolved in an expression that does not parse), then every field/
   * relationship reference that does not resolve - one line per problem. */
  private String validate(String text) {
    try {
      EMITTER.emit(text);
    } catch (QueryLanguageException e) {
      return "Invalid filter expression: " + e.getMessage();
    }
    if (referenceChecker == null) {
      return null;
    }
    List<String> problems = referenceChecker.check(text, scopeModel);
    return problems.isEmpty() ? null : String.join("\n", problems);
  }

  /** Flushes any still-debounced Filter Definition edit and releases {@link #filterDefinitionPanelController}'s
   * resources - called from {@link QueryModelTreeController#destroy()} when this model's tab closes, mirroring
   * {@code QueryFilterDefinitionDialogController#destroy()}'s identical reasoning for the same panel. */
  public void destroy() {
    filterDefinitionPanelController.destroy();
  }
}
