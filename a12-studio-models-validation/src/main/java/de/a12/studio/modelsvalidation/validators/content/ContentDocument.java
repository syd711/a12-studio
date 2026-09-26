package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModelElements;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.modelsvalidation.ValidationContext;
import org.jspecify.annotations.Nullable;

/**
 * The Document Model a Content Model is bound to, as far as it can be resolved, and the base group inside it. Built
 * once per validation call and shared by the reference validators ({@link ValidationContext#cached}).
 *
 * @param documentModelId the bound Document Model's id, {@code null} if the Content Model is not bound
 * @param model           the bound Document Model (a Combination Model's stand-in for a combination), {@code null} if it is
 *                        not bound or not in the project
 * @param structure       its expanded groups and fields, {@code null} if {@code model} is
 * @param baseGroupId     {@code content.configuration.baseGroupId}, {@code null} if not set
 * @param baseGroup       the base group in {@code structure}, {@code null} if not set or not found
 */
record ContentDocument(@Nullable String documentModelId, @Nullable DocumentModel model, @Nullable DocumentStructure structure,
                       @Nullable String baseGroupId, DocumentStructure.@Nullable Node baseGroup) {

  private static final String KEY = ContentDocument.class.getName();

  static ContentDocument of(ContentModel model, ValidationContext context) {
    return context.cached(KEY, () -> resolve(model, context));
  }

  private static ContentDocument resolve(ContentModel model, ValidationContext context) {
    String documentModelId = model.getDocumentModelId();
    DocumentModel documentModel = null;
    if (documentModelId != null) {
      documentModel = context.findOtherDocumentModel(documentModelId);
      if (documentModel == null && context.projectItem() != null) {
        documentModel = CombinedDocumentModelElements.resolveForFieldReferences(context.projectItem(), documentModelId);
      }
    }
    DocumentStructure structure = documentModel != null ? new DocumentStructure(documentModel, context.otherDocumentModels()) : null;
    String baseGroupId = model.getContent() != null && model.getContent().getConfiguration() != null
        ? model.getContent().getConfiguration().getBaseGroupId()
        : null;
    DocumentStructure.Node baseGroup = structure != null ? structure.find(baseGroupId) : null;
    return new ContentDocument(documentModelId, documentModel, structure, baseGroupId, baseGroup);
  }

  /** Whether the base group is set but cannot serve as the starting point (SME then reports it and checks nothing else). */
  boolean baseGroupInvalid() {
    return baseGroupId != null && (baseGroup == null || !baseGroup.isGroup());
  }
}
