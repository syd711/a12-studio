package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.dataservices.models.documentmodel.Element;
import org.jspecify.annotations.NonNull;

public interface ElementEditorController {

  void setElement(@NonNull Element element);
}
