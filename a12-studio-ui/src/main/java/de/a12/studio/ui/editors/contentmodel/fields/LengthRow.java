package de.a12.studio.ui.editors.contentmodel.fields;

import de.a12.studio.models.contentmodel.ContentPropertyFormats;
import de.a12.studio.models.contentmodel.ContentProps;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.jspecify.annotations.NonNull;

/**
 * A single CSS length setting (Width, Height, Gap, ...): keywords and numeric units, see {@link LengthEditor}.
 * The keywords and units come from {@link ContentPropertyFormats} by {@code path} (shared with the model-layer
 * validator); {@code keywords="auto,fit-content"} / {@code units="px:400,%:100"} (unit and the number used when
 * switching to it) override them for a row whose path has no entry there. {@code initial} is what SME's controller
 * shows while the key is absent.
 */
public class LengthRow extends SettingRow {

  private final LengthEditor editor = new LengthEditor();

  private String keywords;
  private String units;
  private String initial;
  private boolean allowUnspecified = true;

  public LengthRow() {
    HBox.setHgrow(editor, Priority.ALWAYS);
    controls().getChildren().add(editor);
    editor.setOnValue(value -> edited(props -> props.set(getPath(), value)));
  }

  @Override
  public void setPath(String path) {
    super.setPath(path);
    reconfigure();
  }

  public String getKeywords() {
    return keywords;
  }

  public void setKeywords(String keywords) {
    this.keywords = keywords;
    reconfigure();
  }

  public String getUnits() {
    return units;
  }

  public void setUnits(String units) {
    this.units = units;
    reconfigure();
  }

  public String getInitial() {
    return initial;
  }

  public void setInitial(String initial) {
    this.initial = initial;
  }

  public boolean isAllowUnspecified() {
    return allowUnspecified;
  }

  public void setAllowUnspecified(boolean allowUnspecified) {
    this.allowUnspecified = allowUnspecified;
    reconfigure();
  }

  private void reconfigure() {
    ContentPropertyFormats.Format format = ContentPropertyFormats.forPath(getPath());
    String effectiveKeywords = keywords != null ? keywords : format != null ? format.keywords() : "";
    String effectiveUnits = units != null ? units : format != null ? format.units() : "";
    editor.configure(LengthEditor.parseKeywords(effectiveKeywords), LengthEditor.parseUnits(effectiveUnits), allowUnspecified,
        java.util.List.of());
  }

  @Override
  protected void load(@NonNull ContentProps props) {
    String value = props.getString(getPath());
    editor.setValue(value != null ? value : initial);
  }
}
