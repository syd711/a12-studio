package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Base of the Content Model editor's property panels. Not bound to a document-model {@code Element} (the shown
 * {@link ContentElement} is a plain props holder), so it uses {@link AbstractPropertyEditor} only for the shared
 * TitledPane chrome, the error container and the persisted expanded state. The panel's FXML file name keys that
 * persisted state, since several panels share one controller class.
 */
public abstract class AbstractContentSettingsPanel extends AbstractPropertyEditor implements ContentSettingsPanel {

  private Runnable onChange = () -> {
  };
  private ContentElement current;
  private Context context;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    if (location != null) {
      String file = location.getPath().substring(location.getPath().lastIndexOf('/') + 1);
      setSettingsKeySuffix("." + file.replace(".fxml", ""));
    }
    super.initialize(location, resources);
    getRootPane().getProperties().put(PANEL_KEY, this);
  }

  /** The panel's own {@code expanded} attribute from its FXML decides whether it starts collapsed. */
  @Override
  protected boolean isExpandedByDefault() {
    return getRootPane().isExpanded();
  }

  @Override
  public final void showElement(@Nullable ContentElement element) {
    current = element;
    boolean applies = element != null && appliesTo(element.getType());
    setEditorVisible(applies);
    if (applies) {
      populate(element);
    }
  }

  @Override
  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  @Override
  public void setContext(@NonNull Context context) {
    this.context = context;
  }

  /** The editor-side context, or {@code null} before the editor has provided it (e.g. in isolated tests). */
  protected Context context() {
    return context;
  }

  /** Whether the panel has any setting for an element of {@code type}; otherwise it stays hidden. */
  protected abstract boolean appliesTo(String type);

  /** Shows {@code element}'s settings; called only for elements the panel {@linkplain #appliesTo applies} to. */
  protected abstract void populate(@NonNull ContentElement element);

  /** The element currently shown, or {@code null}. */
  protected ContentElement currentElement() {
    return current;
  }

  /** To be called after a user edit was applied to {@link #currentElement()}. */
  protected void changed() {
    onChange.run();
  }
}
