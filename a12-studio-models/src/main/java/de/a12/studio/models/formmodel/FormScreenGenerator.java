package de.a12.studio.models.formmodel;

import de.a12.studio.models.Label;
import de.a12.studio.models.Locale;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.NumberFieldType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generates the screens of a new Form Model from its bound Document Model - the "Build Screens from Fields"
 * option in the New Model dialog and the Ad Hoc Testing form. A port of the behavior of A12's
 * {@code @com.mgmtp.a12.formengine/form-model-generator} (the generator SME's new-form-model option delegates to;
 * read from the 38.4.3 npm package, see {@code docs/sme-reference-comparison.md}, "Form Model generator"), not of its
 * code. Ids are derived from the Document Model element ids, as the original does, so the result is deterministic:
 * <ul>
 * <li>one Screen {@code Screen_for_<groupId>} per top-level group; a repeatable top-level group yields a Detached
 * Repeat on a screen with the id {@code TL_Screen_for_<id>} plus the detail screen {@code Screen_for_<id>};
 * a top-level attachment/multi-select group yields a Control Grid with that single control;</li>
 * <li>fields (and attachment/multi-select groups, which behave like fields) become Row/Control pairs, appended to
 * the Control Grid directly before them, or to a new one {@code <group>_Controls}, {@code <group>_Controls_1}, ...
 * when something else (a Section, a Repeat) interrupted the fields of the group;</li>
 * <li>a nested group becomes a Section, a nested repeatable group a Detached Repeat with a detail screen and
 * one column per non-repeatable field below it; Rule and Computation elements are skipped;</li>
 * <li>a control gets a label only if its field has none; groups and repeats always get a title (the element's label
 * restricted to the Form Model's locales, else its name in every locale); fields below a group with usage type
 * {@code metadata} are read-only;</li>
 * <li>Number fields with the {@code percent}/{@code permille} trait get a "%"/"‰" suffix in the field configuration;
 * with more than one screen the sub header gets a navigation button per screen.</li>
 * </ul>
 */
public final class FormScreenGenerator {

  private static final String USAGE_TYPE_METADATA = "metadata";
  private static final Map<String, String> TRAIT_SUFFIXES = Map.of("percent", "%", "permille", "‰");

  private final List<String> locales;
  private final List<FieldConfigEntry> suffixEntries = new ArrayList<>();

  private FormScreenGenerator(List<Locale> locales) {
    this.locales = locales.stream().map(Locale::getCode).toList();
  }

  public static void generate(FormModelContent content, DocumentModel documentModel, List<Locale> locales) {
    FormScreenGenerator generator = new FormScreenGenerator(locales);
    List<Screen> screens = new ArrayList<>();
    for (GroupElement group : documentModel.getContent().getModelRoot().getRootGroups()) {
      screens.add(generator.buildTopLevelScreen(group));
    }
    content.setScreens(screens);
    content.setSubHeaderBox(subHeaderBox(screens));
    HeaderFooterBox footerBox = new HeaderFooterBox();
    footerBox.setId("footerBox");
    content.setFooterBox(footerBox);

    for (GroupElement group : documentModel.getContent().getModelRoot().getRootGroups()) {
      generator.collectSuffixes(group);
    }
    if (!generator.suffixEntries.isEmpty()) {
      FieldConfiguration configuration = new FieldConfiguration();
      configuration.setField(generator.suffixEntries);
      content.setFieldConfiguration(configuration);
    }
  }

  private Screen buildTopLevelScreen(GroupElement group) {
    List<ScreenElement> elements;
    if (isRepeatable(group)) {
      elements = new ArrayList<>(List.of(buildRepeat(group, false)));
    }
    else if (isFieldLike(group)) {
      elements = new ArrayList<>(List.of(buildSingleFieldGrid(group, false)));
    }
    else {
      elements = buildScreenElements(group, isMetadata(group));
    }
    Screen screen = buildScreen(group, elements);
    if (isRepeatable(group)) {
      // the top-level screen and the repeat's detail screen are generated for the same group
      screen.setId("TL_" + screen.getId());
    }
    return screen;
  }

  private Screen buildScreen(Element element, List<ScreenElement> screenElements) {
    Screen screen = new Screen();
    screen.setId("Screen_for_" + element.getId());
    screen.setName(element.getName());
    screen.setTitle(titleOf(element));
    screen.setScreenElements(screenElements);
    return screen;
  }

  private List<ScreenElement> buildScreenElements(GroupElement group, boolean readonly) {
    List<ScreenElement> result = new ArrayList<>();
    GroupConfig config = group.getGroup();
    if (config == null) {
      return result;
    }
    for (Element element : config.getElements()) {
      if (isFieldLike(element)) {
        addControl(result, element, group, readonly);
      }
      else if (element instanceof GroupElement child) {
        result.add(isRepeatable(child) ? buildRepeat(child, readonly) : buildSection(child, readonly));
      }
    }
    return result;
  }

  private void addControl(List<ScreenElement> screenElements, Element fieldLike, GroupElement group, boolean readonly) {
    String baseName = group.getName() + "_Controls";
    ScreenElement last = screenElements.isEmpty() ? null : screenElements.getLast();
    ControlGrid grid;
    if (last instanceof ControlGrid lastGrid) {
      grid = lastGrid;
    }
    else {
      long existing = screenElements.stream().filter(e -> e.getName() != null && e.getName().startsWith(baseName)).count();
      String suffix = existing > 0 ? "_" + existing : "";
      grid = new ControlGrid();
      grid.setId("ControlGrid_for_" + group.getId() + suffix);
      grid.setName(baseName + suffix);
      screenElements.add(grid);
    }
    grid.getRow().add(buildRow(fieldLike, readonly));
  }

  private ControlGrid buildSingleFieldGrid(Element fieldLike, boolean readonly) {
    ControlGrid grid = new ControlGrid();
    grid.setId("ControlGrid_for_" + fieldLike.getId());
    grid.setName(fieldLike.getName() + "_Controls");
    grid.getRow().add(buildRow(fieldLike, readonly || isMetadata(fieldLike)));
    return grid;
  }

  private Row buildRow(Element fieldLike, boolean readonly) {
    Control control = new Control();
    control.setId("Control_for_" + fieldLike.getId());
    control.setElementRef(fieldLike.getId());
    if (readonly) {
      control.setReadonly(true);
    }
    // a physical field's own label is left to the Form Engine; everything else gets one
    if (!(fieldLike instanceof FieldElement field && field.getField() != null && !field.getField().getLabel().isEmpty())) {
      control.setLabel(titleOf(fieldLike));
    }
    Row row = new Row();
    row.setId("Row_for_" + fieldLike.getId());
    row.setCell(new ArrayList<>(List.of(control)));
    return row;
  }

  private Section buildSection(GroupElement group, boolean readonly) {
    Section section = new Section();
    section.setId("Section_for_" + group.getId());
    section.setName(group.getName());
    section.setTitle(titleOf(group));
    section.setScreenElements(buildScreenElements(group, readonly || isMetadata(group)));
    return section;
  }

  private DetachedRepeat buildRepeat(GroupElement group, boolean readonly) {
    DetachedRepeat repeat = new DetachedRepeat();
    repeat.setId("DetachedRepeat_for_" + group.getId());
    repeat.setName(group.getName() + "_Repeat");
    repeat.setTitle(titleOf(group));
    List<RepeatOverviewColumn> columns = new ArrayList<>();
    collectColumns(group, columns);
    repeat.setRepeatOverviewColumn(columns);
    repeat.setGroupRef(group.getId());
    repeat.setEnableAdd(true);
    repeat.setEnableRemove(true);
    Screen detail = buildScreen(group, buildScreenElements(group, readonly || isMetadata(group)));
    detail.setName("Details");
    repeat.setDetailScreen(detail);
    return repeat;
  }

  // one column per field-like element below the group, not descending into repeatable groups
  private void collectColumns(GroupElement group, List<RepeatOverviewColumn> columns) {
    GroupConfig config = group.getGroup();
    if (config == null) {
      return;
    }
    for (Element element : config.getElements()) {
      if (isFieldLike(element)) {
        FieldBasedRepeatOverviewColumn column = new FieldBasedRepeatOverviewColumn();
        column.setId("Column_for_" + element.getId());
        column.setElementRef(element.getId());
        columns.add(column);
      }
      else if (element instanceof GroupElement child && !isRepeatable(child)) {
        collectColumns(child, columns);
      }
    }
  }

  // percent/permille Number fields anywhere in the model, in document order
  private void collectSuffixes(GroupElement group) {
    GroupConfig config = group.getGroup();
    if (config == null) {
      return;
    }
    for (Element element : config.getElements()) {
      if (element instanceof FieldElement field) {
        String suffix = suffixOf(field);
        if (suffix != null) {
          FieldConfigEntry entry = new FieldConfigEntry();
          entry.setSuffix(textContainer(suffix));
          entry.setElementRef(field.getId());
          suffixEntries.add(entry);
        }
      }
      else if (element instanceof GroupElement child && !isFieldLike(child)) {
        collectSuffixes(child);
      }
    }
  }

  private static String suffixOf(FieldElement field) {
    if (field.getField() != null && field.getField().getFieldType() instanceof NumberFieldType number
        && number.getNumberType() != null && number.getNumberType().getTrait() != null) {
      return TRAIT_SUFFIXES.get(number.getNumberType().getTrait());
    }
    return null;
  }

  private static HeaderFooterBox subHeaderBox(List<Screen> screens) {
    HeaderFooterBox box = new HeaderFooterBox();
    box.setId("subHeaderBox");
    if (screens.size() > 1) {
      ButtonGroup buttons = new ButtonGroup();
      for (Screen screen : screens) {
        NavigationButton button = new NavigationButton();
        button.setId("Button_for_" + screen.getId());
        button.setName(screen.getName());
        button.setTarget(screen.getId());
        button.setScope("ALWAYS");
        buttons.getButton().add(button);
      }
      box.setMajorButtons(buttons);
    }
    return box;
  }

  /** The element's label restricted to the model's locales, or its name in every locale when it has none. */
  private LocalizedText titleOf(Element element) {
    List<Label> own = element instanceof GroupElement group && group.getGroup() != null ? group.getGroup().getLabel()
        : element instanceof FieldElement field && field.getField() != null ? field.getField().getLabel() : List.of();
    List<Label> labels = new ArrayList<>();
    if (own.isEmpty()) {
      for (String locale : locales) {
        labels.add(label(locale, element.getName()));
      }
    }
    else {
      for (Label label : own) {
        if (locales.contains(label.getLocale())) {
          labels.add(label);
        }
      }
    }
    MultilingualText text = new MultilingualText();
    TextContainer container = new TextContainer();
    container.setText(labels);
    text.setMultilingualText(container);
    return text;
  }

  private TextContainer textContainer(String value) {
    TextContainer container = new TextContainer();
    List<Label> labels = new ArrayList<>();
    for (String locale : locales) {
      labels.add(label(locale, value));
    }
    container.setText(labels);
    return container;
  }

  private static Label label(String locale, String text) {
    Label label = new Label();
    label.setLocale(locale);
    label.setText(text);
    return label;
  }

  private static boolean isFieldLike(Element element) {
    if (element instanceof FieldElement) {
      return true;
    }
    if (element instanceof GroupElement group && group.getGroup() != null) {
      String usage = group.getGroup().getUsageType();
      return GroupConfig.USAGE_TYPE_ATTACHMENT.equals(usage) || GroupConfig.USAGE_TYPE_MULTI_SELECT.equals(usage);
    }
    return false;
  }

  private static boolean isRepeatable(GroupElement group) {
    Integer repeatability = group.getGroup() != null ? group.getGroup().getRepeatability() : null;
    return repeatability != null && repeatability > 1 && !isFieldLike(group);
  }

  private static boolean isMetadata(Element element) {
    return element instanceof GroupElement group && group.getGroup() != null
        && USAGE_TYPE_METADATA.equals(group.getGroup().getUsageType());
  }
}
