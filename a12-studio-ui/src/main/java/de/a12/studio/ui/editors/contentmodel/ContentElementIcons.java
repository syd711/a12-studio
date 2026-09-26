package de.a12.studio.ui.editors.contentmodel;

/** The icon shown for a content element type, in the element tree and in the insert dialog. */
public final class ContentElementIcons {

  public static final String DEFAULT = "mdi2s-shape-outline";

  private ContentElementIcons() {
  }

  public static String iconFor(String type) {
    if (type == null) {
      return DEFAULT;
    }
    return switch (type) {
      case "Box" -> "mdi2s-square-outline";
      case "MediaQuery" -> "mdi2m-monitor-cellphone";
      case "Expandable" -> "mdi2c-chevron-down-box-outline";
      case "ExpandableCollapsed" -> "mdi2a-arrow-collapse-vertical";
      case "ExpandableExpanded" -> "mdi2a-arrow-expand-vertical";
      case "ExpandableTitle" -> "mdi2f-format-title";
      case "ExpandableContent" -> "mdi2t-text-box-outline";
      case "InteractiveTile" -> "mdi2c-card-outline";
      case "Grid" -> "mdi2v-view-grid-outline";
      case "GridRow", "TableHeadRow", "TableBodyRow", "TableFootRow" -> "mdi2t-table-row";
      case "GridColumn", "TableBodyCell", "TableFootCell" -> "mdi2t-table-column";
      case "Paragraph" -> "mdi2f-format-paragraph";
      case "Heading" -> "mdi2f-format-header-1";
      case "UnorderedList" -> "mdi2f-format-list-bulleted";
      case "OrderedList" -> "mdi2f-format-list-numbered";
      case "ListItem" -> "mdi2c-circle-small";
      case "InteractiveList" -> "mdi2v-view-agenda-outline";
      case "InteractiveListItem" -> "mdi2c-card-text-outline";
      case "Table" -> "mdi2t-table";
      case "TableHead" -> "mdi2t-table-arrow-up";
      case "TableHeadCell" -> "mdi2t-table-headers-eye";
      case "TableBody" -> "mdi2t-table-large";
      case "TableFoot" -> "mdi2t-table-arrow-down";
      case "MessageBox" -> "mdi2m-message-alert-outline";
      case "Image" -> "mdi2i-image-outline";
      case "Video" -> "mdi2v-video-outline";
      case "Link" -> "mdi2l-link-variant";
      case "Icon" -> "mdi2e-emoticon-happy-outline";
      case "Tooltip" -> "mdi2t-tooltip-text-outline";
      case "Conditional" -> "mdi2s-source-branch";
      case "FieldOutput" -> "mdi2t-text-box-outline";
      case "Group" -> "mdi2r-repeat";
      case "ButtonGroupContainer" -> "mdi2v-view-parallel-outline";
      case "ButtonGroup" -> "mdi2b-button-cursor";
      case "Button" -> "mdi2c-cursor-default-click-outline";
      case "TextLine" -> "mdi2f-form-textbox";
      case "TextArea" -> "mdi2t-text-long";
      case "Checkbox" -> "mdi2c-checkbox-marked-outline";
      case "Switch" -> "mdi2t-toggle-switch-outline";
      case "DatePicker" -> "mdi2c-calendar-outline";
      case "Select" -> "mdi2f-form-dropdown";
      case "AutoComplete" -> "mdi2t-text-search";
      case "Radio" -> "mdi2r-radiobox-marked";
      case "MultiSelect" -> "mdi2f-format-list-checks";
      case "CheckboxGroup" -> "mdi2f-format-list-checkbox";
      case "MessageGroupContainer" -> "mdi2m-message-processing-outline";
      case "MessageGroupDisplay" -> "mdi2c-comment-alert-outline";
      default -> DEFAULT;
    };
  }
}
