package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.contentmodel.ContentPropertyFormats;
import de.a12.studio.models.contentmodel.ContentPropertyFormats.Format;

import java.util.List;
import java.util.Set;

/**
 * The CSS-shaped settings SME's own property controllers accept. The keywords/units of each length/spacing
 * setting come from {@link ContentPropertyFormats}, the same table the a12-studio-ui rows ({@code LengthRow}/{@code
 * SpacingRow}) configure their editors from; this class only adds which element types each setting applies to and
 * its kind. Gap 9 of "Content Model: gap review" ({@code docs/sme-reference-comparison.md}): SME's
 * numeric/length/spacing controllers report {@code Invalid setting for property "..."} when a stored value fails
 * their converter; this table is the model-layer half of the same check.
 * <p>
 * Color and shadow settings follow the Content Engine editor's own controllers ({@code color-controller.ts},
 * {@code shadow-controller.ts} of {@code contentengine-editor}, see {@link CssColor}), which have no format table.
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
    NUMBER,
    /** A color {@link CssColor} understands, or empty - mirrors the editor's color controller. */
    COLOR,
    /** {@code [inset] offsetX offsetY blur spread color} - mirrors the editor's shadow controller. */
    SHADOW
  }

  record Rule(Set<String> types, String path, Kind kind, List<String> keywords, List<String> units) {
    boolean appliesTo(String type) {
      return types.isEmpty() || types.contains(type);
    }
  }

  private static Rule length(String types, String path) {
    Format format = ContentPropertyFormats.require(path);
    return new Rule(typeSet(types), path, Kind.LENGTH, format.keywordList(), format.unitNames());
  }

  private static Rule spacing(String types, String path) {
    Format format = ContentPropertyFormats.require(path);
    return new Rule(typeSet(types), path, Kind.SPACING, format.keywordList(), format.unitNames());
  }

  private static Rule number(String types, String path) {
    return new Rule(typeSet(types), path, Kind.NUMBER, List.of(), List.of());
  }

  private static Rule color(String types, String path) {
    return new Rule(typeSet(types), path, Kind.COLOR, List.of(), List.of());
  }

  private static Rule shadow(String types, String path) {
    return new Rule(typeSet(types), path, Kind.SHADOW, List.of(), List.of());
  }

  private static Set<String> typeSet(String types) {
    return Set.copyOf(csv(types));
  }

  private static List<String> csv(String value) {
    return value == null || value.isBlank() ? List.of() : List.of(value.split(","));
  }

  // dimensions-panel.fxml
  static final List<Rule> RULES = List.of(
      length("Box,Grid,GridRow,Table,MessageBox,Image,Button,ButtonGroup,ButtonGroupContainer,Video", "style.width"),
      length("Box,GridRow,MessageBox,Image,Button,ButtonGroup,ButtonGroupContainer", "style.height"),
      spacing("Box,Grid,GridRow,GridColumn,Paragraph,Heading,ListItem,OrderedList,UnorderedList,Table,MessageBox,Image,Button,"
          + "ButtonGroup,ButtonGroupContainer,Link,Expandable,FieldOutput,InteractiveList", "style.padding"),
      spacing("Box,Grid,GridRow,Paragraph,Heading,ListItem,OrderedList,UnorderedList,Table,MessageBox,Image,Button,ButtonGroup,"
          + "ButtonGroupContainer,Link,Expandable,FieldOutput,InteractiveList,InteractiveListItem,InteractiveTile,Video", "style.margin"),

      // appearance-panel.fxml
      length("Icon", "size"),

      // background-image-panel.fxml
      length("Box", "style.backgroundSize"),
      length("Box", "style.backgroundPositionX"),
      length("Box", "style.backgroundPositionY"),

      // border-panel.fxml
      length("Box,ListItem,OrderedList,UnorderedList,Image,Button,ButtonGroup", "style.borderWidth"),
      spacing("Box,ListItem,OrderedList,UnorderedList,Image,Button,ButtonGroup", "style.borderRadius"),

      color("Box,ListItem,OrderedList,UnorderedList,Image,Button,ButtonGroup", "style.borderColor"),

      // color-panel.fxml
      color("Box,MessageBox", "style.color"),
      color("Box,MessageBox", "style.backgroundColor"),

      // shadow-panel.fxml
      shadow("Box,Image", "style.boxShadow"),

      // icons-panel.fxml
      length("Expandable", "icons.size"),

      // layout-panel.fxml
      length("Box", "style.justifyContent"),
      length("Box,ButtonGroupContainer,ButtonGroup", "style.gap"),
      length("Box", "style.overflow"),

      // form-element-date-picker-panel.fxml
      number("DatePicker", "datePickerConfig.minYear"),
      number("DatePicker", "datePickerConfig.maxYear"),
      number("DatePicker", "datePickerConfig.preselectionYear"));

  private ContentPropertyFormatRules() {
  }
}
