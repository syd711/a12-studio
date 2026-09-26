package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentElementLibrary;
import de.a12.studio.models.contentmodel.ContentModule;

/** How the validators name an element of the content tree in a message. */
final class ContentLabels {

  private ContentLabels() {
  }

  /** The label SME's insert dialog shows for the element's type ("Field Output"), else the type itself. */
  static String of(ContentElement element) {
    return ContentElementLibrary.find(element).map(ContentModule::label)
        .orElseGet(() -> element.getType() != null ? element.getType() : "?");
  }
}
