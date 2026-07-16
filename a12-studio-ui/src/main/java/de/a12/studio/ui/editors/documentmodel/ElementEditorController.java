package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.dataservices.models.documentmodel.Element;
import org.jspecify.annotations.NonNull;

import java.util.List;

public interface ElementEditorController {

  void setElement(@NonNull Element element, @NonNull List<Element> ancestors);
}
