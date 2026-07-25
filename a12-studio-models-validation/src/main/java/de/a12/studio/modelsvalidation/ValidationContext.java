package de.a12.studio.modelsvalidation;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.Project;

import java.util.List;

/**
 * Per-validate call state shared by every validator in {@code de.a12.studio.modelsvalidation.validators}: the
 * project the model being validated belongs to, and every other {@link DocumentModel} in that project (needed
 * for cross-model checks such as a resolving an Include reference or comparing time zones).
 */
public record ValidationContext(Project project, List<DocumentModel> otherDocumentModels) {
}
