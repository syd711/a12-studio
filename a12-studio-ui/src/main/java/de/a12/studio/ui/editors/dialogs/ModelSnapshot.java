package de.a12.studio.ui.editors.dialogs;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Annotation;
import de.a12.studio.models.Label;
import de.a12.studio.models.Locale;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.ModelConfig;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Captures the subset of a {@link DocumentModel} that {@link ModelSettingsDialog}'s property editors
 * can change (the header fields, plus {@link ModelConfig#getSupportedCharacters()} and {@link
 * ModelConfig#getTimeZone()}), so {@link #restore()} can undo whatever they already applied to the live model
 * object while the dialog was open. Deliberately narrower than a whole-model snapshot: it never touches {@link
 * DocumentModelContent#getModelRoot()} (the element tree), which this dialog never edits and which other,
 * non-modal parts of the UI may already hold live {@code Element} references into - replacing it wholesale
 * (e.g. via a JSON round-trip of the whole model) would silently detach those references.
 */
class ModelSnapshot {

  private final A12Model<?> model;

  private final String id;
  private final String description;
  private final List<Locale> locales = new ArrayList<>();
  private final List<Label> labels = new ArrayList<>();
  private final List<Annotation> annotations = new ArrayList<>();
  private final String timeZone;
  private final List<String> supportedCharacters;

  ModelSnapshot(@NonNull A12Model<?> model) {
    this.model = model;
    this.id = model.getId();
    this.description = model.getDescription();
    copyLocales(model.getLocales(), locales);
    copyLabels(model.getLabels(), labels);
    copyAnnotations(model.getAnnotations(), annotations);

    ModelConfig modelConfig = model instanceof DocumentModel documentModel ? getModelConfig(documentModel) : null;
    this.timeZone = modelConfig != null ? modelConfig.getTimeZone() : null;
    // null = the model had no supportedCharacters key at all; restore() must put that state back as-is.
    this.supportedCharacters = modelConfig != null && modelConfig.getSupportedCharacters() != null
        ? new ArrayList<>(modelConfig.getSupportedCharacters())
        : null;
  }

  /**
   * Restores every captured field onto the model in place, preserving the identity of its collections (rather
   * than replacing them) so anything already holding a reference to e.g. {@code model.getLocales()} keeps
   * working.
   */
  void restore() {
    model.setId(id);
    model.setDescription(description);
    replaceContents(model.getLocales(), locales);
    replaceContents(model.getLabels(), labels);
    replaceContents(model.getAnnotations(), annotations);

    ModelConfig modelConfig = model instanceof DocumentModel documentModel ? getModelConfig(documentModel) : null;
    if (modelConfig != null) {
      modelConfig.setTimeZone(timeZone);
      replaceContents(modelConfig.getSupportedCharacters(), supportedCharacters);
    }
  }

  private static void copyLocales(List<Locale> source, List<Locale> target) {
    for (Locale locale : source) {
      Locale copy = new Locale();
      copy.setCode(locale.getCode());
      target.add(copy);
    }
  }

  private static void copyLabels(List<Label> source, List<Label> target) {
    for (Label label : source) {
      Label copy = new Label();
      copy.setLocale(label.getLocale());
      copy.setText(label.getText());
      target.add(copy);
    }
  }

  private static void copyAnnotations(List<Annotation> source, List<Annotation> target) {
    for (Annotation annotation : source) {
      Annotation copy = new Annotation();
      copy.setName(annotation.getName());
      copy.setValue(annotation.getValue());
      target.add(copy);
    }
  }

  private static <T> void replaceContents(List<T> target, List<T> snapshot) {
    target.clear();
    target.addAll(snapshot);
  }

  private static ModelConfig getModelConfig(DocumentModel model) {
    DocumentModelContent content = model.getContent();
    return content != null ? content.getModelConfig() : null;
  }
}
