package de.a12.studio.ui.editors.maindetailmodel;

import de.a12.studio.models.masterdetailmodel.FormMapping;
import de.a12.studio.models.masterdetailmodel.MasterDetailModel;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Edits a {@link MasterDetailModel}'s {@link FormMapping} list: one row per Document Model the currently
 * selected master model (Overview or Tree) references (mirroring SME's {@code formMappingMiddleware}), each
 * showing that Document Model in the first column and a combobox of the Form Models available for it in the
 * second.
 */
public class MainDetailFormMappingPanelController extends AbstractDocumentFormMappingPanelController {

  @Override
  protected List<FormMapping> currentMappings() {
    return model.getContent().getFormMapping();
  }

  @Override
  protected void applyMappings(@NonNull List<FormMapping> mappings) {
    List<FormMapping> formMapping = model.getContent().getFormMapping();
    formMapping.clear();
    formMapping.addAll(mappings);
  }

  @Override
  protected String rowIdPrefix() {
    return "formMappingFormModel";
  }
}
