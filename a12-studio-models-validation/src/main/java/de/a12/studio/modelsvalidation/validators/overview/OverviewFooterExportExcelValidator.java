package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.composeddocumentmodel.ComposedDocumentModel;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.overviewmodel.BoxElement;
import de.a12.studio.models.overviewmodel.ConfigurableBoxElement;
import de.a12.studio.models.overviewmodel.ElementBox;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * A Footer button whose event is {@code export_excel} only works when the Overview is bound to a Composed
 * Document Model (SME: {@code major/minorEventExcelExportWorksWithCDMOnly}, {@code
 * [majorElements/event]=="export_excel" AND CustomCondition ShouldWorkForCDMOnly}, WARNING "Event
 * \"export_excel\" works for CDM models only."). SME has a second WARNING for a CDM with repeatable elements
 * ({@code ShouldWorkForNonRepeatableCDMOnly}) that isn't ported yet - it needs the same relationship-multiplicity
 * walk as {@code FormBindingRepeatCdmRequiredValidator}'s to-many check, generalized across the CDM's whole
 * relationship chain rather than one Binding's role.
 */
public final class OverviewFooterExportExcelValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/footerBox";

  private static final String EVENT_EXPORT_EXCEL = "export_excel";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof OverviewModel overviewModel)) {
      return List.of();
    }
    ElementBox footerBox = overviewModel.getContent().getFooterBox();
    if (footerBox == null) {
      return List.of();
    }
    boolean hasExportExcel = hasExportExcelEvent(footerBox.getLeftSlot()) || hasExportExcelEvent(footerBox.getRightSlot());
    if (!hasExportExcel) {
      return List.of();
    }

    DocumentModel documentModel = OverviewElementResolution.referencedDocumentModel(overviewModel, context);
    if (documentModel instanceof ComposedDocumentModel) {
      return List.of();
    }
    return List.of(new ModelValidationError(model, ELEMENT_ID,
        ValidationMessages.get("validation.overviewFooterExportExcel.cdmOnly"), Severity.WARNING.name()));
  }

  private static boolean hasExportExcelEvent(List<BoxElement> elements) {
    return elements.stream()
        .filter(ConfigurableBoxElement.class::isInstance)
        .map(ConfigurableBoxElement.class::cast)
        .anyMatch(element -> EVENT_EXPORT_EXCEL.equals(element.getEvent()));
  }
}
