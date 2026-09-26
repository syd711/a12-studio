package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.models.contentmodel.ContentElement;
import org.jspecify.annotations.NonNull;

/**
 * The Date Picker Config of a Date Picker: the range of years the picker offers and the year it starts on (SME's "Date
 * Picker Config" section). SME shows it only while the picker shows a field of a date type (date, date-time, date
 * range) of the Document Model, so the panel follows the element picked in the Form Element panel.
 */
public class DatePickerConfigPanelController extends ContentSettingsPanelController {

  @Override
  public boolean followsOtherPanels() {
    return true;
  }

  @Override
  protected void populate(@NonNull ContentElement element) {
    super.populate(element);
    setEditorVisible(showsDateField(element));
  }

  private boolean showsDateField(ContentElement element) {
    ContentReferences references = context() != null ? context().references() : null;
    Object elementId = element.getProps() != null ? element.getProps().get("elementId") : null;
    return references != null && references.isBound() && elementId instanceof String id && references.isDateField(id);
  }
}
