package de.a12.studio.ui.editors.contentmodel.fields;

/**
 * One choice of a {@link ToggleRow}: the {@code value} stored in the props, the {@code label} on the button and an
 * optional {@code tooltip} explaining it. A bean (not a record) so FXML can declare it.
 */
public class ToggleOption {

  private String value;
  private String label;
  private String tooltip;

  public ToggleOption() {
  }

  public ToggleOption(String value, String label, String tooltip) {
    this.value = value;
    this.label = label;
    this.tooltip = tooltip;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getLabel() {
    return label != null ? label : value;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getTooltip() {
    return tooltip;
  }

  public void setTooltip(String tooltip) {
    this.tooltip = tooltip;
  }
}
