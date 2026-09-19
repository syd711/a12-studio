package de.a12.studio.ui.editors.formmodel;

import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FieldConfiguration;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.GroupConfigEntry;
import de.a12.studio.models.formmodel.GroupConfiguration;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.editors.documentmodel.ElementViewModel;
import de.a12.studio.ui.editors.formmodel.formtree.nodeeditors.AttachmentSettingsPanelController;
import de.a12.studio.ui.editors.formmodel.formtree.nodeeditors.DependentEnumerationPanelController;
import de.a12.studio.ui.editors.formmodel.formtree.nodeeditors.DependentFieldPanelController;
import de.a12.studio.ui.editors.formmodel.formtree.nodeeditors.DependentGroupPanelController;
import de.a12.studio.ui.editors.formmodel.formtree.nodeeditors.ExternalEnumerationPanelController;
import de.a12.studio.ui.editors.formmodel.formtree.nodeeditors.HideConditionPanelController.MasterFieldScope;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.components.SearchFieldController;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * "Data Configuration" tab for the Form Model editor: a single place to see and configure every field/group
 * configuration entry's dependency setup, mirroring SME's dedicated Data Configuration tab. Previously this
 * was only reachable per-Control from the Screens tree (and only for {@code dependentEnumeration}/{@code
 * externalEnumeration} - {@code dependentField}/{@code dependentGroup} had no editor at all anywhere), which
 * meant a field/group referenced by more than one Control, or not yet placed in any screen, had no way to be
 * configured.
 * <p>
 * Left: a tree mirroring the linked Document Model's own field/group hierarchy (see {@link #refreshTree}).
 * Selecting a field/group lazily creates its {@link FieldConfigEntry}/{@link GroupConfigEntry} if one doesn't
 * exist yet - mirroring {@code FieldConfigEntryHelper#findOrCreate}'s eager-creation pattern used by the
 * per-Control node editors - so nothing needs to be added up front. Nodes whose entry actually holds
 * configured values are rendered in bold (see {@link #hasConfiguredValues}); an entry created merely by
 * browsing to a node, but never given a value, stays plain and is later pruned by {@link
 * de.a12.studio.modelsvalidation.validators.form.FormConfigEntryCleanup}. Right: the selected entry's
 * dependency editors.
 */
public class DataConfigurationPanelController implements Initializable {

  private record Row(Element element) {
  }

  @FXML
  private TreeView<Row> tree;
  @FXML
  private Button expandAllButton;
  @FXML
  private Button collapseAllButton;
  @FXML
  private SearchFieldController searchController;
  @FXML
  private CheckBox hideUnusedFieldsCheckBox;

  @FXML
  private Label noSelectionLabel;
  @FXML
  private Node fieldDetailPane;
  @FXML
  private Node groupDetailPane;

  @FXML
  private Node externalEnumeration;
  @FXML
  private Node dependentEnumeration;

  @FXML
  private ExternalEnumerationPanelController externalEnumerationController;
  @FXML
  private DependentEnumerationPanelController dependentEnumerationController;
  @FXML
  private DependentFieldPanelController dependentFieldController;
  @FXML
  private DependentGroupPanelController dependentGroupController;
  @FXML
  private AttachmentSettingsPanelController attachmentSettingsController;
  @FXML
  private AnnotationsPanelController annotationsController;
  @FXML
  private TextField numberOfInitialRowsField;

  private FormModelContent content;
  private @Nullable ElementIndex elementIndex;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    tree.setShowRoot(false);
    tree.setCellFactory(view -> new ElementRowTreeCell());
    tree.getSelectionModel().selectedItemProperty()
        .addListener((obs, oldVal, newVal) -> showDetail(newVal == null ? null : newVal.getValue().element()));
    searchController.setOnSearch(term -> refreshTree());

    numberOfInitialRowsField.textProperty().addListener((obs, oldVal, newVal) -> {
      TreeItem<Row> selected = tree.getSelectionModel().getSelectedItem();
      if (selected == null || !(selected.getValue().element() instanceof GroupElement group)) {
        return;
      }
      GroupConfigEntry entry = findOrCreateGroupEntry(group);
      try {
        entry.setNumberOfInitialRows(newVal.isBlank() ? null : Integer.parseInt(newVal.strip()));
        commitChange();
      }
      catch (NumberFormatException ignored) {
        // Leave the stored value unchanged until the user types a valid integer.
      }
    });

    showDetail(null);
  }

  public void setModel(@NonNull FormModelContent content, @Nullable ElementIndex elementIndex) {
    this.content = content;
    this.elementIndex = elementIndex;
    refreshTree();
  }

  @FXML
  private void onHideUnusedFieldsToggled() {
    refreshTree();
  }

  @FXML
  private void onExpandAll() {
    setExpandedRecursive(tree.getRoot(), true);
  }

  @FXML
  private void onCollapseAll() {
    TreeItem<Row> root = tree.getRoot();
    if (root == null) {
      return;
    }
    // Root is hidden (showRoot=false) but must stay expanded, otherwise its top-level children would be
    // hidden along with it.
    root.setExpanded(true);
    for (TreeItem<Row> child : root.getChildren()) {
      setExpandedRecursive(child, false);
    }
  }

  private void setExpandedRecursive(@Nullable TreeItem<Row> item, boolean expanded) {
    if (item == null) {
      return;
    }
    item.setExpanded(expanded);
    for (TreeItem<Row> child : item.getChildren()) {
      setExpandedRecursive(child, expanded);
    }
  }

  /** Mirrors the linked Document Model's own Field/Group hierarchy (Rule/Computation elements are skipped -
   * they never carry a {@link FieldConfigEntry}/{@link GroupConfigEntry}). Elements reached only through an
   * Include aren't indexed by {@link ElementIndex} and so don't appear here either - matching the old flat
   * table's "add" combos, which only ever offered this model's own local elements. Applies the search field's
   * term and the "Hide unused fields" checkbox (see {@link #toFilteredTreeItem}) - a parent is kept whenever
   * any descendant survives either filter, same as {@code DocumentSourceTreeController#applyFilter}. */
  private void refreshTree() {
    TreeItem<Row> root = new TreeItem<>();
    if (elementIndex != null) {
      Map<Element, List<Element>> childrenOf = new LinkedHashMap<>();
      List<Element> roots = new ArrayList<>();
      for (Element element : elementIndex.allElements()) {
        if (!(element instanceof FieldElement) && !(element instanceof GroupElement)) {
          continue;
        }
        GroupElement parent = elementIndex.parentOf(element);
        if (parent == null) {
          roots.add(element);
        }
        else {
          childrenOf.computeIfAbsent(parent, p -> new ArrayList<>()).add(element);
        }
      }
      String term = searchController.getText() == null ? "" : searchController.getText().trim().toLowerCase();
      boolean hideUnused = hideUnusedFieldsCheckBox.isSelected();
      for (Element rootElement : roots) {
        TreeItem<Row> item = toFilteredTreeItem(rootElement, term, hideUnused, childrenOf);
        if (item != null) {
          root.getChildren().add(item);
        }
      }
    }
    tree.setRoot(root);
    setExpandedRecursive(root, true);
  }

  private @Nullable TreeItem<Row> toFilteredTreeItem(Element element, String term, boolean hideUnused, Map<Element, List<Element>> childrenOf) {
    List<TreeItem<Row>> matchingChildren = new ArrayList<>();
    for (Element child : childrenOf.getOrDefault(element, List.of())) {
      TreeItem<Row> filtered = toFilteredTreeItem(child, term, hideUnused, childrenOf);
      if (filtered != null) {
        matchingChildren.add(filtered);
      }
    }
    boolean selfMatches = (term.isEmpty() || element.getName() != null && element.getName().toLowerCase().contains(term))
        && (!hideUnused || hasConfiguredValues(element));
    if (!selfMatches && matchingChildren.isEmpty()) {
      return null;
    }
    TreeItem<Row> item = new TreeItem<>(new Row(element));
    item.getChildren().addAll(matchingChildren);
    return item;
  }

  /** The element's type, formatted the same way as {@code DocumentModelElementsTreeController}'s Type column
   * (e.g. {@code StringType} -> {@code String}). */
  private @Nullable String typeLabel(Element element) {
    String type = new ElementViewModel(element).getType();
    return type == null ? null : type.replaceAll("Type", "");
  }

  /** Resolves the effective (type-definition-aware) {@link FieldType} of a field element, or {@code null}
   * if it doesn't resolve through {@link #elementIndex}. */
  private @Nullable FieldType resolveFieldType(@NonNull FieldElement field) {
    if (elementIndex == null || field.getField() == null) {
      return null;
    }
    return elementIndex.effectiveFieldType(field.getField().getFieldType());
  }

  private @Nullable FieldConfigEntry findFieldEntry(@NonNull Element field) {
    if (content.getFieldConfiguration() == null) {
      return null;
    }
    for (FieldConfigEntry entry : content.getFieldConfiguration().getField()) {
      if (field.getId().equals(entry.getElementRef())) {
        return entry;
      }
    }
    return null;
  }

  private @Nullable GroupConfigEntry findGroupEntry(@NonNull GroupElement group) {
    if (content.getGroupConfiguration() == null) {
      return null;
    }
    for (GroupConfigEntry entry : content.getGroupConfiguration().getGroup()) {
      if (group.getId().equals(entry.getGroupRef())) {
        return entry;
      }
    }
    return null;
  }

  /** Returns the field's existing {@link FieldConfigEntry}, or creates and attaches a new (still empty) one -
   * mirroring {@code FieldConfigEntryHelper#findOrCreate}. Only actually persisted once a value is set on it
   * and {@link #commitChange()} saves the model. */
  private FieldConfigEntry findOrCreateFieldEntry(@NonNull Element field) {
    FieldConfigEntry existing = findFieldEntry(field);
    if (existing != null) {
      return existing;
    }
    if (content.getFieldConfiguration() == null) {
      content.setFieldConfiguration(new FieldConfiguration());
    }
    FieldConfigEntry entry = new FieldConfigEntry();
    entry.setElementRef(field.getId());
    content.getFieldConfiguration().getField().add(entry);
    return entry;
  }

  private GroupConfigEntry findOrCreateGroupEntry(@NonNull GroupElement group) {
    GroupConfigEntry existing = findGroupEntry(group);
    if (existing != null) {
      return existing;
    }
    if (content.getGroupConfiguration() == null) {
      content.setGroupConfiguration(new GroupConfiguration());
    }
    GroupConfigEntry entry = new GroupConfigEntry();
    entry.setGroupRef(group.getId());
    content.getGroupConfiguration().getGroup().add(entry);
    return entry;
  }

  /** Whether this node should render bold: an entry exists for it and actually holds a configured value,
   * as opposed to one that only exists because {@link #findOrCreateFieldEntry}/{@link #findOrCreateGroupEntry}
   * attached an empty placeholder the moment the node was selected. */
  private boolean hasConfiguredValues(@NonNull Element element) {
    if (element instanceof FieldElement field) {
      FieldConfigEntry entry = findFieldEntry(field);
      return entry != null && hasConfiguredValues(entry);
    }
    if (element instanceof GroupElement group) {
      GroupConfigEntry entry = findGroupEntry(group);
      // An attachment group's "Attachment Settings" live in a field entry keyed by the group's id.
      FieldConfigEntry attachmentEntry = isAttachmentGroup(group) ? findFieldEntry(group) : null;
      return entry != null && hasConfiguredValues(entry)
          || attachmentEntry != null && attachmentEntry.getAttachmentConfig() != null;
    }
    return false;
  }

  private boolean hasConfiguredValues(@NonNull FieldConfigEntry entry) {
    return entry.getLabel() != null
        || entry.getPlaceholder() != null
        || entry.getHint() != null
        || (entry.getInitialValue() != null && !entry.getInitialValue().isBlank())
        || entry.getSuffix() != null
        || (entry.getExposition() != null && !entry.getExposition().isBlank())
        || entry.getReadonly() != null
        || entry.getDependentField() != null
        || entry.getDependentEnumeration() != null
        || entry.getExternalEnumeration() != null
        || (entry.getFormatting() != null && !entry.getFormatting().isBlank())
        || entry.getSecret() != null
        || entry.getEnableSelectAll() != null
        || !entry.getAnnotations().isEmpty()
        || entry.getAttachmentConfig() != null;
  }

  private boolean hasConfiguredValues(@NonNull GroupConfigEntry entry) {
    return entry.getDependentGroup() != null
        || entry.getNumberOfInitialRows() != null
        || entry.getLabel() != null
        || entry.getHint() != null
        || entry.getPlaceholder() != null;
  }

  private static boolean isAttachmentGroup(@NonNull GroupElement group) {
    return group.getGroup() != null && group.getGroup().isAttachment();
  }

  private void showDetail(@Nullable Element element) {
    boolean isField = element instanceof FieldElement;
    boolean isGroup = element instanceof GroupElement;

    noSelectionLabel.setVisible(element == null);
    noSelectionLabel.setManaged(element == null);
    fieldDetailPane.setVisible(isField);
    fieldDetailPane.setManaged(isField);
    groupDetailPane.setVisible(isGroup);
    groupDetailPane.setManaged(isGroup);

    if (isField) {
      FieldElement field = (FieldElement) element;
      FieldConfigEntry entry = findOrCreateFieldEntry(field);
      FieldType fieldType = resolveFieldType(field);
      boolean isStringField = fieldType instanceof StringFieldType;
      boolean isEnumerationField = fieldType instanceof EnumerationFieldType;

      // externalEnumeration only applies to string-typed fields (its values replace what would otherwise be
      // free text), dependentEnumeration only to enumeration-like fields (it filters that field's own enum
      // values) - see SME's "External Enumeration"/"Dependent Enumeration" docs.
      externalEnumeration.setVisible(isStringField);
      externalEnumeration.setManaged(isStringField);
      dependentEnumeration.setVisible(isEnumerationField);
      dependentEnumeration.setManaged(isEnumerationField);

      externalEnumerationController.setEntry(entry);
      dependentEnumerationController.setEntry(entry, elementIndex,
          MasterFieldScope.anchoredOrUnbound(entry.getElementRef(), elementIndex));
      dependentFieldController.setEntry(entry, elementIndex);
      annotationsController.setCustom(entry::getAnnotations);
    }
    else if (isGroup) {
      GroupElement group = (GroupElement) element;
      GroupConfigEntry entry = findOrCreateGroupEntry(group);
      dependentGroupController.setEntry(entry, elementIndex);
      boolean attachment = isAttachmentGroup(group);
      attachmentSettingsController.setVisible(attachment);
      if (attachment) {
        attachmentSettingsController.setEntry(findOrCreateFieldEntry(group));
      }
      numberOfInitialRowsField.setText(entry.getNumberOfInitialRows() == null ? "" : entry.getNumberOfInitialRows().toString());
    }
  }

  private void commitChange() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem == null) {
      return;
    }
    projectItem.save();
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
    refreshTreeAppearance();
  }

  /** Re-renders every tree cell's bold-when-configured state (see {@link #hasConfiguredValues}), without
   * rebuilding the tree structure itself. Called by {@link FormModelEditorController#modelSaved} so an edit
   * made through one of this panel's own dependency sub-panels (which each commit their change and fire a
   * saved-event independently, rather than through this class's {@link #commitChange()}) is reflected right
   * away, not just after the next full {@link #refreshTree()}. */
  public void refreshTreeAppearance() {
    tree.refresh();
  }

  /** Icon + "name [Type]" rendering, the name in bold when {@link #hasConfiguredValues} - the same visual
   * idiom as {@code FormSourceElementTreeCell}, plus the type suffix and bold-when-configured styling this
   * panel needs. */
  private class ElementRowTreeCell extends TreeCell<Row> {
    @Override
    protected void updateItem(Row row, boolean empty) {
      super.updateItem(row, empty);
      if (empty || row == null) {
        setText(null);
        setGraphic(null);
        return;
      }
      Element element = row.element();
      Node icon = WidgetFactory.createIcon(new ElementViewModel(element).getIcon());
      icon.getStyleClass().add("tree-icon");
      String type = typeLabel(element);
      String name = type == null || type.isBlank() ? element.getName() : element.getName() + " [" + type + "]";
      Label nameLabel = new Label(name);
      nameLabel.getStyleClass().add("tree-cell-name-label");
      if (hasConfiguredValues(element)) {
        nameLabel.getStyleClass().add("data-configuration-configured-label");
      }
      HBox graphic = new HBox(4, icon, nameLabel);
      graphic.setAlignment(Pos.CENTER_LEFT);
      setText(null);
      setGraphic(graphic);
    }
  }
}
