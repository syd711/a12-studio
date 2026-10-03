package de.a12.studio.modelsvalidation;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModel;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.transformermodel.GeneratedDocumentModels;
import de.a12.studio.models.transformermodel.TransformerModel;
import de.a12.studio.modelsvalidation.validators.ElementIndex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Per-validate call state shared by every validator in {@code de.a12.studio.modelsvalidation.validators}:
 * the project the model being validated belongs to, the project item backing it (its file — needed e.g.
 * for the id-matches-filename rule; may be null when the model has no file yet), every other
 * {@link DocumentModel} in that project (cross-model checks such as resolving an Include reference), and
 * every other model of any type (reference-integrity checks such as a tree model's document model refs).
 *
 * <p>Not a record: {@link #elementIndex()} memoizes the {@link ElementIndex} built for the model being
 * validated, since several document-model validators each used to build their own from scratch (see its
 * javadoc) — a record has no room for that extra mutable field.
 */
public final class ValidationContext {

  private final Project project;
  private final ProjectItem projectItem;
  private final List<DocumentModel> otherDocumentModels;
  private final List<A12Model<?>> otherModels;
  private final A12Model<?> model;
  private ElementIndex elementIndex;
  private final Map<Object, Object> cache = new HashMap<>();

  public ValidationContext(Project project, ProjectItem projectItem, List<DocumentModel> otherDocumentModels,
      List<A12Model<?>> otherModels, A12Model<?> model) {
    this.project = project;
    this.projectItem = projectItem;
    this.otherDocumentModels = otherDocumentModels;
    this.otherModels = otherModels;
    this.model = model;
  }

  public Project project() {
    return project;
  }

  public ProjectItem projectItem() {
    return projectItem;
  }

  public List<DocumentModel> otherDocumentModels() {
    return otherDocumentModels;
  }

  public List<A12Model<?>> otherModels() {
    return otherModels;
  }

  /** The other model with the given header id, or null; used by reference-integrity validators. */
  public A12Model<?> findOtherModel(String id) {
    if (id == null) {
      return null;
    }
    return otherModels.stream().filter(model -> id.equals(model.getId())).findFirst().orElse(null);
  }

  /** The other {@link DocumentModel} with the given header id, or null. */
  public DocumentModel findOtherDocumentModel(String id) {
    if (id == null) {
      return null;
    }
    DocumentModel found = otherDocumentModels.stream().filter(model -> id.equals(model.getId())).findFirst().orElse(null);
    return found != null ? found : findGeneratedDocumentModel(id);
  }

  /**
   * The Document Model a Transformer Model of the project last generated, if {@code id} names one. Not part of
   * {@link #otherDocumentModels()} (that list feeds the cross-model rules such as the time zone), only of the lookup
   * by id that reference checks use.
   */
  private DocumentModel findGeneratedDocumentModel(String id) {
    ProjectItem context = projectItem != null ? projectItem : project != null ? project.getRoot() : null;
    return context == null ? null : GeneratedDocumentModels.resolve(context, id);
  }

  /**
   * True if {@code id} matches another {@link DocumentModel} or {@link CombinedDocumentModel} in the
   * project - the model types SME's Form Model allows as a data-binding document reference (alongside
   * Composed and Transformer Document Models, neither implemented as a distinct type in a12-studio yet;
   * see {@code client/src/modules/formModel/references/documentModelsEnum/index.ts}'s
   * {@code createDocumentModelsProvider}). {@link #findOtherDocumentModel} alone rejects a
   * {@link CombinedDocumentModel} reference since that type doesn't extend {@link DocumentModel}.
   */
  public boolean hasOtherDocumentOrCombinedModel(String id) {
    return findOtherDocumentModel(id) != null || findOtherModel(id) instanceof CombinedDocumentModel
        || findOtherModel(id) instanceof TransformerModel;
  }

  /**
   * The {@link ElementIndex} for the model being validated, built once (with {@link #otherDocumentModels()}
   * for transitive Include/Import resolution, and {@link #otherModels()} to resolve an Additive Document
   * Model's base model, see {@link ElementIndex}'s 3-arg constructor) and cached here so every validator in
   * this call shares it instead of each re-walking the same element tree from scratch. Null when the model
   * being validated isn't a {@link DocumentModel} (e.g. this context was built for a Form/Overview/Print
   * model that needs an index over a *different*, referenced document model — those still build their own
   * {@link ElementIndex}).
   */
  public ElementIndex elementIndex() {
    if (elementIndex == null && model instanceof DocumentModel documentModel) {
      elementIndex = new ElementIndex(documentModel, otherDocumentModels, otherModels);
    }
    return elementIndex;
  }

  /**
   * Something derived from the models of this validation call that several validators of one model type need (e.g. the
   * expanded Document Model a Content Model's reference checks resolve against): computed by {@code compute} on first
   * use, then shared for the rest of the call. The key must identify what is computed; a {@code null} result is cached too.
   */
  @SuppressWarnings("unchecked")
  public <T> T cached(Object key, Supplier<T> compute) {
    if (!cache.containsKey(key)) {
      cache.put(key, compute.get());
    }
    return (T) cache.get(key);
  }
}
