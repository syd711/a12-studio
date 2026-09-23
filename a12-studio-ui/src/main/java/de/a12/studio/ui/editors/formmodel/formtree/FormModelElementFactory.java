package de.a12.studio.ui.editors.formmodel.formtree;

import de.a12.studio.models.formmodel.Binding;
import de.a12.studio.models.formmodel.BindingContent;
import de.a12.studio.models.formmodel.BindingDetails;
import de.a12.studio.models.formmodel.BindingMetaInformation;
import de.a12.studio.models.formmodel.BindingRepeat;
import de.a12.studio.models.formmodel.ButtonPanel;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.CustomCell;
import de.a12.studio.models.formmodel.CustomScreenElement;
import de.a12.studio.models.formmodel.DetachedRepeat;
import de.a12.studio.models.formmodel.EmbeddedRepeat;
import de.a12.studio.models.formmodel.ExpressionCell;
import de.a12.studio.models.formmodel.ExpressionRepeatOverviewColumn;
import de.a12.studio.models.formmodel.FieldBasedRepeatOverviewColumn;
import de.a12.studio.models.formmodel.InlineRepeat;
import de.a12.studio.models.formmodel.MultiColumnSection;
import de.a12.studio.models.formmodel.Row;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.models.formmodel.TextCell;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

/**
 * Creates new Form Model tree nodes for the "Add" context menu ({@link FormModelNodeTypes}) and for drag-and-drop
 * from the Document Model source tree ({@link FormModelTreeController}). Every {@code newXxx} generates a fresh
 * id (also reused by {@link FormModelActions#regenerateIds} when duplicating/pasting a subtree) matching the
 * {@code <lowercase-type>-<5-hex-digits>} convention already used by existing Form Model files (see
 * {@code Invoice_FM.json}, e.g. {@code "section-fc573"}, {@code "controlgrid-95f39"}).
 */
final class FormModelElementFactory {

  private static final Random ID_RANDOM = new SecureRandom();

  private FormModelElementFactory() {
  }

  static String generateId(String prefix) {
    return prefix + "-" + String.format("%05x", ID_RANDOM.nextInt(0x100000));
  }

  static Screen newScreen() {
    Screen screen = new Screen();
    screen.setId(generateId("screen"));
    screen.setName("Screen");
    return screen;
  }

  static Section newSection() {
    Section section = new Section();
    section.setId(generateId("section"));
    section.setName("Section");
    return section;
  }

  static MultiColumnSection newMultiColumnSection() {
    MultiColumnSection section = new MultiColumnSection();
    section.setId(generateId("multicolumnsection"));
    section.setName("MultiColumnSection");
    return section;
  }

  static ControlGrid newControlGrid() {
    ControlGrid grid = new ControlGrid();
    grid.setId(generateId("controlgrid"));
    grid.setName("ControlGrid");
    return grid;
  }

  static CustomScreenElement newCustomScreenElement() {
    CustomScreenElement element = new CustomScreenElement();
    element.setId(generateId("customscreenelement"));
    element.setName("CustomScreenElement");
    return element;
  }

  static ButtonPanel newButtonPanel() {
    ButtonPanel panel = new ButtonPanel();
    panel.setId(generateId("buttonpanel"));
    panel.setName("ButtonPanel");
    return panel;
  }

  static CustomCell newCustomCell() {
    CustomCell cell = new CustomCell();
    cell.setId(generateId("customcell"));
    cell.setName("CustomCell");
    return cell;
  }

  static InlineRepeat newInlineRepeat() {
    InlineRepeat repeat = new InlineRepeat();
    repeat.setId(generateId("inlinerepeat"));
    repeat.setName("InlineRepeat");
    return repeat;
  }

  static InlineRepeat newInlineRepeat(String groupRef) {
    InlineRepeat repeat = newInlineRepeat();
    repeat.setGroupRef(groupRef);
    return repeat;
  }

  static EmbeddedRepeat newEmbeddedRepeat() {
    EmbeddedRepeat repeat = new EmbeddedRepeat();
    repeat.setId(generateId("embeddedrepeat"));
    repeat.setName("EmbeddedRepeat");
    return repeat;
  }

  static DetachedRepeat newDetachedRepeat() {
    DetachedRepeat repeat = new DetachedRepeat();
    repeat.setId(generateId("detachedrepeat"));
    repeat.setName("DetachedRepeat");
    return repeat;
  }

  static Row newRow() {
    Row row = new Row();
    row.setId(generateId("row"));
    return row;
  }

  static Control newControl() {
    Control control = new Control();
    control.setId(generateId("control"));
    return control;
  }

  static Control newControl(String elementRef) {
    Control control = newControl();
    control.setElementRef(elementRef);
    return control;
  }

  static TextCell newTextCell() {
    TextCell cell = new TextCell();
    cell.setId(generateId("textcell"));
    cell.setName("TextCell");
    return cell;
  }

  static ExpressionCell newExpressionCell() {
    ExpressionCell cell = new ExpressionCell();
    cell.setId(generateId("expressioncell"));
    cell.setName("ExpressionCell");
    return cell;
  }

  static FieldBasedRepeatOverviewColumn newFieldBasedRepeatOverviewColumn() {
    FieldBasedRepeatOverviewColumn column = new FieldBasedRepeatOverviewColumn();
    column.setId(generateId("repeatoverviewcolumn"));
    return column;
  }

  static FieldBasedRepeatOverviewColumn newFieldBasedRepeatOverviewColumn(String elementRef) {
    FieldBasedRepeatOverviewColumn column = newFieldBasedRepeatOverviewColumn();
    column.setElementRef(elementRef);
    return column;
  }

  static ExpressionRepeatOverviewColumn newExpressionRepeatOverviewColumn() {
    ExpressionRepeatOverviewColumn column = new ExpressionRepeatOverviewColumn();
    column.setId(generateId("expressionrepeatoverviewcolumn"));
    return column;
  }

  // Matches the SME reference's Binding_SUFFIX ("_Binding") and its I_Binding mixin's fixed metaInformation
  // version, both applied whenever a Binding is created by dropping a relationship onto the tree (see
  // createBindingFromDroppedRelationshipModel in the SME client sources).
  private static final String BINDING_NAME_SUFFIX = "_Binding";
  private static final String BINDING_META_INFORMATION_VERSION = "1.0.0";

  /**
   * A new {@link Binding} pre-wired to {@code relationshipModel}, created by dropping a row from the Form Model
   * editor's Relationships panel onto the tree ({@code RelationshipModelPanelController}/{@code
   * FormModelTreeController#dropRelationshipModel}). Mirrors the SME reference's {@code
   * createBindingFromDroppedRelationshipModel}: the display name defaults to {@code <relationshipId>_Binding},
   * and {@code targetRole} is pre-selected to whichever of the relationship's two entity roles does *not*
   * belong to {@code boundDocumentModelId} - i.e. the side the binding will show/edit, since the other side is
   * already this Form Model's own bound Document Model - left unset (for the user to choose) whenever that
   * can't be determined unambiguously (a self-relationship where both sides are the bound Document Model, or
   * neither side is).
   */
  static Binding newBinding(@NonNull RelationshipModel relationshipModel, @Nullable String boundDocumentModelId) {
    Binding binding = new Binding();
    binding.setId(generateId("binding"));
    binding.getBinding().setType("relationship");
    BindingDetails details = binding.getBinding().getDetails();
    details.setName(relationshipModel.getId() + BINDING_NAME_SUFFIX);
    details.setRelationshipName(relationshipModel.getId());
    details.setTargetRole(calculateTargetRole(relationshipModel, boundDocumentModelId));
    BindingMetaInformation metaInformation = new BindingMetaInformation();
    metaInformation.setVersion(BINDING_META_INFORMATION_VERSION);
    details.setMetaInformation(metaInformation);
    return binding;
  }

  /**
   * A new {@link BindingRepeat} pre-wired to {@code relationshipModel}, exactly like {@link #newBinding} but for
   * a to-many target role (see {@link #isToManyTargetRole}) - dropping such a relationship creates a repeat
   * instead of a single {@link Binding}, since one row per linked entity makes sense but a single-entity Binding
   * doesn't.
   */
  static BindingRepeat newBindingRepeat(@NonNull RelationshipModel relationshipModel, @Nullable String boundDocumentModelId) {
    BindingRepeat repeat = new BindingRepeat();
    repeat.setId(generateId("bindingrepeat"));
    BindingContent content = new BindingContent();
    content.setType("relationship");
    BindingDetails details = new BindingDetails();
    details.setName(relationshipModel.getId() + BINDING_NAME_SUFFIX);
    details.setRelationshipName(relationshipModel.getId());
    details.setTargetRole(calculateTargetRole(relationshipModel, boundDocumentModelId));
    BindingMetaInformation metaInformation = new BindingMetaInformation();
    metaInformation.setVersion(BINDING_META_INFORMATION_VERSION);
    details.setMetaInformation(metaInformation);
    content.setDetails(details);
    repeat.setBinding(content);
    return repeat;
  }

  /**
   * Whether dropping {@code relationshipModel} onto the tree should create a {@link BindingRepeat} rather than a
   * plain {@link Binding}: true when the target role calculated by {@link #calculateTargetRole} has a to-many
   * multiplicity (unbounded, or an upper limit greater than 1) - one linked entity per row only makes sense
   * when there can be more than one.
   */
  static boolean isToManyTargetRole(@NonNull RelationshipModel relationshipModel, @Nullable String boundDocumentModelId) {
    String targetRole = calculateTargetRole(relationshipModel, boundDocumentModelId);
    if (targetRole == null || relationshipModel.getContent() == null) {
      return false;
    }
    return relationshipModel.getContent().getEntityCharacteristics().stream()
        .filter(characteristic -> targetRole.equals(characteristic.getRole()))
        .findFirst()
        .map(FormModelElementFactory::isToMany)
        .orElse(false);
  }

  private static boolean isToMany(@NonNull EntityCharacteristic characteristic) {
    if (characteristic.getLinkConstraints() == null || characteristic.getLinkConstraints().getMultiplicity() == null) {
      return false;
    }
    var multiplicity = characteristic.getLinkConstraints().getMultiplicity();
    return Boolean.TRUE.equals(multiplicity.getUnbounded()) || (multiplicity.getUpperLimit() != null && multiplicity.getUpperLimit() > 1);
  }

  private static @Nullable String calculateTargetRole(@NonNull RelationshipModel relationshipModel, @Nullable String boundDocumentModelId) {
    List<EntityCharacteristic> characteristics = relationshipModel.getContent() == null
        ? List.of()
        : relationshipModel.getContent().getEntityCharacteristics();
    if (boundDocumentModelId == null || characteristics.size() != 2) {
      return null;
    }
    EntityCharacteristic left = characteristics.get(0);
    EntityCharacteristic right = characteristics.get(1);
    boolean leftMatches = boundDocumentModelId.equals(left.getDocumentModel());
    boolean rightMatches = boundDocumentModelId.equals(right.getDocumentModel());
    if (leftMatches == rightMatches) {
      // Both sides are the bound Document Model (a self-relationship) or neither is - ambiguous either way.
      return null;
    }
    return leftMatches ? right.getRole() : left.getRole();
  }
}
