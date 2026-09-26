package de.a12.studio.models.contentmodel;

import de.a12.studio.models.contentmodel.ContentModule.ChildRule;
import de.a12.studio.models.contentmodel.ContentModule.ModuleRule;
import de.a12.studio.models.contentmodel.ContentModule.ParentRule;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The element types SME's Content Model editor offers: the default Content Engine elements
 * ({@code @com.mgmtp.a12.contentengine/contentengine-editor}, one {@code *.module.tsx} each) and the form elements of
 * {@code formengine-content-elements-editor} that SME's library adds. Labels, categories and the parent/child rules are
 * taken from there; the ordering is SME's {@code orderingConfigurations}.
 * <p>
 * The action modules (Save, Cancel, Commit, Add Row, Delete Row) are left out on purpose: SME never offers them for
 * insertion (Save/Cancel/Commit are excluded in its library, Add Row/Delete Row have no allowed parent), they only
 * exist as an element's click event.
 */
public final class ContentElementLibrary {

  public static final String NAMESPACE = ContentElementDefaults.DEFAULT_NAMESPACE;

  public static final String FORM_ELEMENTS_NAMESPACE = "com.mgmtp.a12.formengine";

  public static final String LAYOUT = "Layout";

  public static final String CONTENT = "Content";

  public static final String GENERAL = "General";

  public static final String FORM_ELEMENTS = "Form Elements";

  // Categories and the elements in them, in the order SME lists them; whatever is not listed follows, by id.
  private static final List<String> CATEGORY_ORDER = List.of(LAYOUT, CONTENT, GENERAL);

  private static final Map<String, List<String>> ELEMENT_ORDER = Map.of(
      LAYOUT, ids("MediaQuery", "Box", "Expandable", "InteractiveTile", "Grid", "GridRow", "GridColumn", "Table",
          "TableHeadRow", "TableHeadCell", "TableBodyRow", "TableBodyCell", "TableFootRow", "TableFootCell"),
      CONTENT, ids("Heading", "Paragraph", "MessageBox", "OrderedList", "UnorderedList", "Image", "Video", "Link",
          "Icon", "Tooltip"),
      GENERAL, ids("Group", "ButtonGroupContainer", "ButtonGroup", "Button"));

  private static final List<ContentModule> MODULES = buildModules();

  private static final Map<String, ContentModule> BY_ID = MODULES.stream()
      .collect(Collectors.toUnmodifiableMap(ContentModule::id, Function.identity()));

  private ContentElementLibrary() {
  }

  /** All element types, in no particular order. */
  public static @NonNull List<ContentModule> modules() {
    return MODULES;
  }

  public static @NonNull Optional<ContentModule> find(String namespace, String type) {
    return Optional.ofNullable(BY_ID.get(ContentModule.moduleId(namespace, type)));
  }

  public static @NonNull Optional<ContentModule> find(@NonNull ContentElement element) {
    return find(element.getNamespace() != null ? element.getNamespace() : NAMESPACE, element.getType());
  }

  /** Whether the element is a Repeatable Group or a Conditional, which the rules look through. */
  public static boolean isTransitive(@NonNull ContentElement element) {
    String namespace = element.getNamespace() != null ? element.getNamespace() : NAMESPACE;
    return NAMESPACE.equals(namespace) && ("Group".equals(element.getType()) || "Conditional".equals(element.getType()));
  }

  /** The order the insert dialog lists categories and, within one, the types: SME's configured order, then by id. */
  public static @NonNull Comparator<ContentModule> displayOrder() {
    return Comparator
        .comparingInt((ContentModule module) -> rank(CATEGORY_ORDER, module.category()))
        .thenComparing(ContentModule::category)
        .thenComparingInt(module -> rank(ELEMENT_ORDER.getOrDefault(module.category(), List.of()), module.id()))
        .thenComparing(ContentModule::id);
  }

  private static int rank(List<String> order, String key) {
    int index = order.indexOf(key);
    return index >= 0 ? index : order.size();
  }

  private static List<String> ids(String... types) {
    List<String> result = new ArrayList<>();
    for (String type : types) {
      result.add(ContentModule.moduleId(NAMESPACE, type));
    }
    return result;
  }

  private static String ce(String type) {
    return ContentModule.moduleId(NAMESPACE, type);
  }

  private static ModuleRule rule(String type) {
    return ModuleRule.of(ce(type));
  }

  private static ModuleRule rule(String type, Integer min, Integer max) {
    return ModuleRule.of(ce(type), min, max);
  }

  private static ContentModule content(String type, String label, String category, ParentRule parent, ChildRule child) {
    return new ContentModule(NAMESPACE, type, label, category, parent, child, false);
  }

  private static ParentRule anyParent() {
    return ParentRule.anyOf(ContentModule.ANY);
  }

  private static ParentRule parents(String... types) {
    return ParentRule.anyOf(Arrays.stream(types).map(ContentElementLibrary::ce).toArray(String[]::new));
  }

  private static ContentModule formElement(String type, String label, ChildRule child) {
    return new ContentModule(FORM_ELEMENTS_NAMESPACE, type, label, FORM_ELEMENTS, anyParent(), child, false);
  }

  private static List<ContentModule> buildModules() {
    ChildRule none = ChildRule.noChildren();
    ChildRule any = ChildRule.anyChildren();
    ParentRule anyParent = anyParent();
    // Group and Conditional may stand wherever their parent takes the elements they wrap
    ModuleRule group = rule("Group");
    ModuleRule conditional = rule("Conditional");

    List<ContentModule> modules = new ArrayList<>(List.of(
        content("Box", "Box", LAYOUT, anyParent, any),
        content("MediaQuery", "Media Query", LAYOUT, anyParent, any),
        content("Expandable", "Expandable", LAYOUT, anyParent, expandableChildren()),
        content("InteractiveTile", "Interactive Tile", LAYOUT, anyParent, ChildRule.anyOf(rule("Box", 1, 1))),
        content("Grid", "Grid", LAYOUT, anyParent, ChildRule.anyOf(rule("GridRow"), group, conditional)),
        content("GridRow", "Row", LAYOUT, parents("Grid", "GridColumn"), ChildRule.anyOf(rule("GridColumn"))),
        content("GridColumn", "Column", LAYOUT, parents("GridRow"), ChildRule.noneOf(ce("GridColumn"))),

        content("Table", "Table", CONTENT, anyParent, ChildRule.sequence(
            rule("TableHead", 1, 1), rule("TableBody", 1, 1), rule("TableFoot", 1, 1))),
        content("TableHead", "Head", CONTENT, parents("Table"), ChildRule.anyOf(rule("TableHeadRow", null, 1))),
        content("TableHeadRow", "Head Row", CONTENT, parents("TableHead"), none),
        content("TableHeadCell", "Head Cell", CONTENT, parents("TableHeadRow"), any),
        new ContentModule(NAMESPACE, "TableBody", "Body", CONTENT, parents("Table"), ChildRule.anyOfSequences(
            new ContentModule.Sequence(List.of(rule("TableBodyRow"))),
            new ContentModule.Sequence(List.of(new ModuleRule(ce("Group"), null, 1,
                ChildRule.anyOf(rule("TableBodyRow", null, 1)))))), true),
        content("TableBodyRow", "Body Row", CONTENT, parents("TableBody"), none),
        content("TableBodyCell", "Body Cell", CONTENT, parents("TableBodyRow"), any),
        content("TableFoot", "Foot", CONTENT, parents("Table"), ChildRule.anyOf(rule("TableFootRow", null, 1))),
        content("TableFootRow", "Foot Row", CONTENT, parents("TableFoot"), none),
        content("TableFootCell", "Foot Cell", CONTENT, parents("TableFootRow"), any),

        content("Heading", "Heading", CONTENT, anyParent, none),
        content("Paragraph", "Paragraph", CONTENT, anyParent, none),
        content("MessageBox", "Message Box", CONTENT, anyParent, none),
        content("OrderedList", "Numbered List", CONTENT, anyParent, ChildRule.anyOf(
            group, rule("ListItem"), conditional, rule("OrderedList"), rule("UnorderedList"))),
        content("UnorderedList", "Bulleted List", CONTENT, anyParent, ChildRule.anyOf(
            rule("ListItem"), rule("OrderedList"), rule("UnorderedList"), group, conditional)),
        content("ListItem", "List Item", CONTENT, parents("OrderedList", "UnorderedList"), any),
        content("InteractiveList", "Interactive List", CONTENT, anyParent, ChildRule.anyOf(
            rule("InteractiveListItem"), group, conditional)),
        content("InteractiveListItem", "Interactive List Item", CONTENT, parents("InteractiveList"), any),
        content("Image", "Image", CONTENT, anyParent, none),
        content("Video", "Video", CONTENT, anyParent, none),
        content("Link", "Link", CONTENT, anyParent, any),
        content("Icon", "Icon", CONTENT, anyParent, none),
        content("Tooltip", "Tooltip", CONTENT, anyParent, none),
        content("Conditional", "Conditional", CONTENT, anyParent, any),
        content("FieldOutput", "Field Output", CONTENT, anyParent, none),
        content("Group", "Repeatable Group", CONTENT, anyParent, none),
        content("ExpandableCollapsed", "Collapsed", CONTENT, parents("Expandable"),
            ChildRule.anyOf(rule("ExpandableTitle", 1, 1))),
        content("ExpandableExpanded", "Expanded", CONTENT, parents("Expandable"), ChildRule.anyOfSequences(
            new ContentModule.Sequence(List.of(rule("ExpandableTitle", 1, 1), rule("ExpandableContent", 1, 1))))),
        content("ExpandableTitle", "Title", CONTENT, parents("ExpandableCollapsed", "ExpandableExpanded"), any),
        content("ExpandableContent", "Content", CONTENT, parents("ExpandableExpanded"), any),

        content("ButtonGroupContainer", "Button Group Container", GENERAL, anyParent,
            ChildRule.anyOf(rule("ButtonGroup", 2, 2))),
        content("ButtonGroup", "Button Group", GENERAL, parents("ButtonGroupContainer", "TableBodyCell"),
            ChildRule.anyOf(group, conditional, rule("Button"))),
        content("Button", "Button", GENERAL, anyParent, none)));

    ChildRule noChildren = ChildRule.anyOf();
    modules.add(formElement("TextLine", "Text Line", noChildren));
    modules.add(formElement("TextArea", "Text Area", noChildren));
    modules.add(formElement("Checkbox", "Checkbox", noChildren));
    modules.add(formElement("Switch", "Switch", noChildren));
    modules.add(formElement("DatePicker", "Date Picker", noChildren));
    modules.add(formElement("Select", "Select", noChildren));
    modules.add(formElement("AutoComplete", "AutoComplete", noChildren));
    modules.add(formElement("Radio", "Radio", noChildren));
    modules.add(formElement("MultiSelect", "Multi Select", noChildren));
    modules.add(formElement("CheckboxGroup", "Checkbox Group", noChildren));
    modules.add(formElement("MessageGroupContainer", "Message Group Container", any));
    modules.add(formElement("MessageGroupDisplay", "Message Group Display", none));
    return List.copyOf(modules);
  }

  private static ChildRule expandableChildren() {
    return ChildRule.anyOfSequences(new ContentModule.Sequence(List.of(
        rule("ExpandableCollapsed", 1, 1), rule("ExpandableExpanded", 1, 1))));
  }
}
