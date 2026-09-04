package de.a12.studio.ui.util;

import de.a12.studio.models.Locale;
import de.a12.studio.models.NewModelFactory;
import de.a12.studio.models.documentmodel.BooleanFieldType;
import de.a12.studio.models.documentmodel.DateFieldType;
import de.a12.studio.models.documentmodel.DateTimeFieldType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.FieldConfig;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.ModelInfo;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.documentmodel.NumberFieldType;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.projects.settings.ProjectRootSettings;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared utility for building a {@link DocumentModel} from a flat list of column descriptors.
 *
 * <p>Used by both the built-in import actions in {@code a12-studio-ui} and by plugin
 * {@code ICreateItemMenuEntry} implementations so that the model-assembly logic lives
 * in exactly one place.
 */
public final class DocumentModelBuilder {

  private DocumentModelBuilder() {
  }

  // ---------------------------------------------------------------------------
  // Column descriptor
  // ---------------------------------------------------------------------------

  /**
   * Minimal column descriptor that is type-system agnostic.
   * Both the Access and the Excel import services produce these after mapping
   * their source-specific types.
   */
  public enum ColumnType {
    STRING, NUMBER, BOOLEAN, DATE, DATE_TIME
  }

  public record ColumnDescriptor(@NonNull String name, @NonNull ColumnType type) {
  }

  // ---------------------------------------------------------------------------
  // Public API
  // ---------------------------------------------------------------------------

  /**
   * Builds a {@link DocumentModel} whose fields mirror the provided columns.
   * All fields are placed in a single root group named after the source
   * (table or sheet name).
   *
   * @param parent    target project folder (used to resolve default locales)
   * @param modelName document model name shown in the model info
   * @param groupName label for the single root group (typically table / sheet name)
   * @param columns   ordered list of columns to turn into fields
   * @return a fully constructed (but not yet persisted) {@link DocumentModel}
   */
  @NonNull
  public static DocumentModel build(@NonNull ProjectItem parent,
                                    @NonNull String modelName,
                                    @NonNull String groupName,
                                    @NonNull List<ColumnDescriptor> columns) {
    DocumentModel model = new DocumentModel();
    DocumentModelContent content = new DocumentModelContent();

    ModelInfo modelInfo = new ModelInfo();
    modelInfo.setName(modelName);
    content.setModelInfo(modelInfo);
    content.setModelConfig(NewModelFactory.defaultModelConfig());

    GroupElement rootGroup = new GroupElement();
    rootGroup.setId(sanitizeId(groupName));
    rootGroup.setName(groupName);
    GroupConfig groupConfig = new GroupConfig();

    for (ColumnDescriptor col : columns) {
      FieldElement field = new FieldElement();
      field.setId(sanitizeId(col.name()));
      field.setName(col.name());

      FieldConfig fieldConfig = new FieldConfig();
      fieldConfig.setFieldType(toFieldType(col.type()));
      field.setField(fieldConfig);

      groupConfig.getElements().add(field);
    }

    rootGroup.setGroup(groupConfig);
    ModelRoot modelRoot = new ModelRoot();
    modelRoot.getRootGroups().add(rootGroup);
    content.setModelRoot(modelRoot);

    model.setContent(content);
    model.setLocales(resolveDefaultLocales(parent));
    return model;
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  /** Maps a {@link ColumnType} to the corresponding Document Model {@link FieldType}. */
  @NonNull
  public static FieldType toFieldType(@NonNull ColumnType type) {
    return switch (type) {
      case BOOLEAN  -> new BooleanFieldType();
      case NUMBER   -> new NumberFieldType();
      case DATE     -> new DateFieldType();
      case DATE_TIME -> new DateTimeFieldType();
      default       -> new StringFieldType();
    };
  }

  /**
   * Turns an arbitrary name into a valid element id: replaces non-alphanumeric characters
   * with underscores and prepends an underscore if the result starts with a digit.
   */
  @NonNull
  public static String sanitizeId(@NonNull String name) {
    String id = name.replaceAll("[^A-Za-z0-9_]", "_");
    if (!id.isEmpty() && Character.isDigit(id.charAt(0))) {
      id = "_" + id;
    }
    return id.isEmpty() ? "field" : id;
  }

  /** Resolves the project's configured locales, falling back to the JVM's system locale if none are set. */
  @NonNull
  public static List<Locale> resolveDefaultLocales(@NonNull ProjectItem parent) {
    ProjectItem root = parent;
    while (root.getParent() != null) {
      root = root.getParent();
    }
    List<Locale> projectLocales = ProjectRootSettings.load(root.getFile()).getGeneral().getLocales();
    if (projectLocales.isEmpty()) {
      return systemLocaleFallback();
    }
    List<Locale> locales = new ArrayList<>();
    for (Locale pl : projectLocales) {
      Locale locale = new Locale();
      locale.setCode(pl.getCode());
      locales.add(locale);
    }
    return locales;
  }

  /**
   * A single-element locales list wrapping the JVM's default locale, used wherever no project locales are
   * configured yet (e.g. {@link #resolveDefaultLocales} and the New Model dialog) so the locales editor
   * starts from the machine's own locale instead of an arbitrary hardcoded language.
   */
  @NonNull
  public static List<Locale> systemLocaleFallback() {
    Locale locale = new Locale();
    locale.setCode(java.util.Locale.getDefault().toLanguageTag());
    return new ArrayList<>(List.of(locale));
  }
}
