package de.a12.studio.models.composeddocumentmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Annotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Reads/writes a Document Model's {@code cdm.*} header annotations (see {@link ComposedDocumentModel}'s
 * javadoc) as usable data, mirroring the role
 * {@code de.a12.studio.models.additivedocumentmodel.AdditiveDocumentModelResolver} plays for Additive Document
 * Models. Takes a plain {@link A12Model} (not {@link ComposedDocumentModel}) on purpose: exactly like the
 * {@code additive-document} annotation, {@code cdm.queryRoot} is set on an ordinary
 * {@code de.a12.studio.models.documentmodel.DocumentModel} through its Settings dialog panel *before* the
 * model is recognized as a {@link ComposedDocumentModel} - that only happens on the next load, since
 * {@code ModelFactory} decides the runtime type once, at load time.
 * <p>
 * SME authors one {@code cdm.relationship}/{@code cdm.sourceRole}/{@code cdm.targetRole}/
 * {@code cdm.targetDocumentModel} annotation set per "Relationship Element" node dragged onto a diagram (BA
 * docs, Figures 84-87) - a12-studio has no diagram canvas, so it represents the same chain as an ordered list
 * (see {@link CdmRelationshipStep}) and needs a way to keep more than one step's annotations from colliding.
 * This class does that with a numeric suffix: the first step's annotations are unsuffixed ({@code
 * cdm.relationship}, ...), matching a hand-authored single-step SME file byte-for-byte; step index {@code i >=
 * 1}'s annotations are suffixed {@code .i} (e.g. {@code cdm.relationship.1}). The annotation *names and values*
 * SME itself writes are unaffected - only which JSON key a given step's data lives under, for steps beyond the
 * first, is an a12-studio-side convention.
 */
public final class ComposedDocumentModelResolver {

  public static final String QUERY_ROOT_ANNOTATION = "cdm.queryRoot";
  public static final String RELATIONSHIP_ANNOTATION = "cdm.relationship";
  public static final String SOURCE_ROLE_ANNOTATION = "cdm.sourceRole";
  public static final String TARGET_ROLE_ANNOTATION = "cdm.targetRole";
  public static final String TARGET_DOCUMENT_MODEL_ANNOTATION = "cdm.targetDocumentModel";

  private ComposedDocumentModelResolver() {
  }

  public static Optional<String> getQueryRootId(A12Model<?> model) {
    return findAnnotation(model.getAnnotations(), QUERY_ROOT_ANNOTATION).map(Annotation::getValue);
  }

  public static void setQueryRootId(A12Model<?> model, String rootDocumentModelId) {
    setAnnotationValue(model, QUERY_ROOT_ANNOTATION, rootDocumentModelId);
  }

  /**
   * The relationship chain this CDM traverses, in authoring order (step 0 = unsuffixed annotations, step 1 =
   * {@code .1}-suffixed, ...). Stops at the first missing index, so gaps in the suffix sequence (e.g. only
   * {@code .0} and {@code .2} present) silently truncate rather than skip - callers that hand-author fixtures
   * must keep the suffixes contiguous starting at the unsuffixed step.
   */
  public static List<CdmRelationshipStep> getRelationshipSteps(A12Model<?> model) {
    List<CdmRelationshipStep> steps = new ArrayList<>();
    for (int index = 0; ; index++) {
      String suffix = index == 0 ? "" : "." + index;
      Optional<String> relationshipName = findAnnotation(model.getAnnotations(), RELATIONSHIP_ANNOTATION + suffix).map(Annotation::getValue);
      if (relationshipName.isEmpty()) {
        break;
      }
      String sourceRole = findAnnotation(model.getAnnotations(), SOURCE_ROLE_ANNOTATION + suffix).map(Annotation::getValue).orElse(null);
      String targetRole = findAnnotation(model.getAnnotations(), TARGET_ROLE_ANNOTATION + suffix).map(Annotation::getValue).orElse(null);
      String targetDocumentModel = findAnnotation(model.getAnnotations(), TARGET_DOCUMENT_MODEL_ANNOTATION + suffix)
          .map(Annotation::getValue).orElse(null);
      steps.add(new CdmRelationshipStep(relationshipName.get(), sourceRole, targetRole, targetDocumentModel));
    }
    return steps;
  }

  public static void setRelationshipSteps(A12Model<?> model, List<CdmRelationshipStep> steps) {
    removeAnnotationsStartingWith(model, RELATIONSHIP_ANNOTATION);
    removeAnnotationsStartingWith(model, SOURCE_ROLE_ANNOTATION);
    removeAnnotationsStartingWith(model, TARGET_ROLE_ANNOTATION);
    removeAnnotationsStartingWith(model, TARGET_DOCUMENT_MODEL_ANNOTATION);

    for (int index = 0; index < steps.size(); index++) {
      String suffix = index == 0 ? "" : "." + index;
      CdmRelationshipStep step = steps.get(index);
      setAnnotationValue(model, RELATIONSHIP_ANNOTATION + suffix, step.getRelationshipName());
      setAnnotationValue(model, SOURCE_ROLE_ANNOTATION + suffix, step.getSourceRole());
      setAnnotationValue(model, TARGET_ROLE_ANNOTATION + suffix, step.getTargetRole());
      setAnnotationValue(model, TARGET_DOCUMENT_MODEL_ANNOTATION + suffix, step.getTargetDocumentModel());
    }
  }

  private static void removeAnnotationsStartingWith(A12Model<?> model, String prefix) {
    model.getAnnotations().removeIf(annotation -> annotation.getName() != null
        && (annotation.getName().equals(prefix) || annotation.getName().startsWith(prefix + ".")));
  }

  private static void setAnnotationValue(A12Model<?> model, String name, String value) {
    Optional<Annotation> existing = findAnnotation(model.getAnnotations(), name);
    if (value == null || value.isBlank()) {
      existing.ifPresent(model.getAnnotations()::remove);
      return;
    }
    if (existing.isPresent()) {
      existing.get().setValue(value);
    }
    else {
      Annotation annotation = new Annotation();
      annotation.setName(name);
      annotation.setValue(value);
      model.getAnnotations().add(annotation);
    }
  }

  private static Optional<Annotation> findAnnotation(List<Annotation> annotations, String name) {
    return annotations.stream().filter(a -> name.equals(a.getName())).findFirst();
  }
}
