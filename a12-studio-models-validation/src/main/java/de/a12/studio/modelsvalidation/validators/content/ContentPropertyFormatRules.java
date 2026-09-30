package de.a12.studio.modelsvalidation.validators.content;

import java.util.List;
import java.util.Set;

/**
 * The CSS-shaped settings SME's own property controllers accept, ported from the a12-studio-ui rows that edit
 * them (their FXML {@code path}/{@code types}/{@code keywords}/{@code units} attributes are the source of
 * truth here - see {@code de.a12.studio.ui.editors.contentmodel.fields.LengthEditor}/{@code SpacingRow}/{@code
 * NumberRow} and the panels under {@code a12-studio-ui/.../editors/contentmodel/*-panel.fxml}). Gap 9 of
 * "Content Model: gap review" ({@code docs/sme-reference-comparison.md}): SME's numeric/length/spacing
 * controllers report {@code Invalid setting for property "..."} when a stored value fails their converter;
 * this table is the model-layer half of the same check, kept independent of the UI rows for now (a manual,
 * documented duplication - see the class-level TODO note in {@link ContentSettingValueValidator}) rather than
 * a shared extraction, to keep this pass's scope bounded.
 * <p>
 * Deliberately not covered: {@code ColorRow} and {@code ShadowRow} settings. Both accept the full breadth of
 * CSS color/shadow syntax that SME's own {@code Color.web}-equivalent parser understands (hex, named colors,
 * {@code rgb()}/{@code rgba()}/{@code hsl()}, multi-layer shadows); cloning that grammar accurately enough to
 * avoid false positives on legitimately valid real files needs the same "read it out of the installed bundle,
 * don't guess" treatment the rest of this gap review used - left open rather than attempted with an
 * under-specified port.
 */
final class ContentPropertyFormatRules {

  enum Kind {
    /** A single CSS length token: one of {@code keywords}, or a number with one of {@code units}, or (when
     * {@code units} is non-empty) the bare number {@code 0} - mirrors {@code LengthEditor}. */
    LENGTH,
    /** SME's "oriented" shorthand: 1-4 whitespace-separated {@link #LENGTH} tokens (Padding/Margin/Border
     * Radius) - mirrors {@code SpacingRow}. */
    SPACING,
    /** A whole number (SME's numeric input, e.g. a date picker year) - mirrors {@code NumberRow}. */
    NUMBER
  }

  record Rule(Set<String> types, String path, Kind kind, List<String> keywords, List<String> units) {
    boolean appliesTo(String type) {
      return types.isEmpty() || types.contains(type);
    }
  }

  private static Rule length(String types, String path, String keywords, String units) {
    return new Rule(typeSet(types), path, Kind.LENGTH, csv(keywords), csv(units));
  }

  private static Rule spacing(String types, String path, String keywords, String units) {
    return new Rule(typeSet(types), path, Kind.SPACING, csv(keywords), csv(units));
  }

  private static Rule number(String types, String path) {
    return new Rule(typeSet(types), path, Kind.NUMBER, List.of(), List.of());
  }

  private static Set<String> typeSet(String types) {
    return Set.copyOf(csv(types));
  }

  private static List<String> csv(String value) {
    return value == null || value.isBlank() ? List.of() : List.of(value.split(","));
  }

  // dimensions-panel.fxml
  static final List<Rule> RULES = List.of(
      length("Box,Grid,GridRow,Table,MessageBox,Image,Button,ButtonGroup,ButtonGroupContainer,Video", "style.width", "auto", "px,%"),
      length("Box,GridRow,MessageBox,Image,Button,ButtonGroup,ButtonGroupContainer", "style.height", "auto,fit-content", "px,%"),
      spacing("Box,Grid,GridRow,GridColumn,Paragraph,Heading,ListItem,OrderedList,UnorderedList,Table,MessageBox,Image,Button,"
          + "ButtonGroup,ButtonGroupContainer,Link,Expandable,FieldOutput,InteractiveList", "style.padding", null, "px,%,rem"),
      spacing("Box,Grid,GridRow,Paragraph,Heading,ListItem,OrderedList,UnorderedList,Table,MessageBox,Image,Button,ButtonGroup,"
          + "ButtonGroupContainer,Link,Expandable,FieldOutput,InteractiveList,InteractiveListItem,InteractiveTile,Video", "style.margin", "auto", "px,%,rem"),

      // appearance-panel.fxml
      length("Icon", "size", "medium,big", "px"),

      // background-image-panel.fxml
      length("Box", "style.backgroundSize", "auto,cover,contain", "px,%"),
      length("Box", "style.backgroundPositionX", "left,center,right", "px,%"),
      length("Box", "style.backgroundPositionY", "top,center,bottom", "px,%"),

      // border-panel.fxml
      length("Box,ListItem,OrderedList,UnorderedList,Image,Button,ButtonGroup", "style.borderWidth", "auto", "px"),
      spacing("Box,ListItem,OrderedList,UnorderedList,Image,Button,ButtonGroup", "style.borderRadius", null, "px,%,rem"),

      // icons-panel.fxml
      length("Expandable", "icons.size", "medium,big", "px"),

      // layout-panel.fxml
      length("Box", "style.justifyContent", "start,center,end,space-between,space-around,space-evenly", null),
      length("Box,ButtonGroupContainer,ButtonGroup", "style.gap", null, "px,%"),
      length("Box", "style.overflow", "visible,hidden,scroll,auto", null),

      // form-element-date-picker-panel.fxml
      number("DatePicker", "datePickerConfig.minYear"),
      number("DatePicker", "datePickerConfig.maxYear"),
      number("DatePicker", "datePickerConfig.preselectionYear"));

  private ContentPropertyFormatRules() {
  }
}
