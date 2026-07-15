package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.dataservices.models.documentmodel.Element;
import org.jspecify.annotations.NonNull;

public class DocumentModelGroupEditorController implements ElementEditorController {

  private Element element;

  @Override
  public void setElement(@NonNull Element element) {
    this.element = element;
  }
}
