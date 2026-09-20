package de.a12.studio.modelsvalidation.formincludes;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.Binding;
import de.a12.studio.models.formmodel.Button;
import de.a12.studio.models.formmodel.Cell;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.DependentCase;
import de.a12.studio.models.formmodel.DependentConfig;
import de.a12.studio.models.formmodel.DependentEnumeration;
import de.a12.studio.models.formmodel.ExpressionCell;
import de.a12.studio.models.formmodel.ExpressionRepeatOverviewColumn;
import de.a12.studio.models.formmodel.FieldBasedRepeatOverviewColumn;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.FormModelWalker;
import de.a12.studio.models.formmodel.GroupConfigEntry;
import de.a12.studio.models.formmodel.HeaderFooterBox;
import de.a12.studio.models.formmodel.HideCondition;
import de.a12.studio.models.formmodel.MultiFileUploadOptions;
import de.a12.studio.models.formmodel.RepeatOverviewColumn;
import de.a12.studio.models.formmodel.Row;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Expands a Form Model include: copies the elements of the <em>first screen</em> of another Form Model into the
 * host Form Model, rebinding everything that points into the source's Document Model to the host's. Ported from
 * the behavior of the A12 Form Engine's {@code IncludeExpansion}/{@code IncludeMapper} (the batch expansion SME
 * runs at build time; SME's editor itself only shows the result), verified against that library's published
 * sources and against SME's {@code HostModel.json}/{@code IncludedModel.json} fixture pair:
 *
 * <ul>
 * <li><b>Ids.</b> Every copied id gets the prefix {@code <includeId>_}, so the same form can be included several
 * times.</li>
 * <li><b>Provenance.</b> Each copied <em>top-level</em> element carries {@code includeId}, {@code formModelRef}
 * and {@code hostDocumentModelPath}; nothing below it does. The provenance is what makes an include recognizable
 * again, so it can be re-expanded ({@link #includeRun}).</li>
 * <li><b>References into the Document Model are path-based.</b> A reference is resolved to its path in the source
 * Document Model, the source's root group name is dropped, the rest is appended to {@code hostDocumentModelPath}
 * and resolved in the host Document Model - normally an Include group of the source's Document Model, whose
 * elements are addressed as {@code <includeGroupId>_<elementId>} ({@link ElementIndex#resolveIdByPath}). A path of
 * {@code /} maps the source path unchanged.</li>
 * <li><b>Names.</b> A single copied element takes the requested name (the source's own if none is given); with
 * several, each gets {@code <includeId>-} in front of its name so siblings stay unique.</li>
 * <li><b>Configuration.</b> The field/group configuration entries of everything the copied elements bind to are
 * copied along and re-keyed; an entry that already exists in the host only gets the attributes it lacks.</li>
 * </ul>
 *
 * Where the Form Engine leaves references untouched (hide-condition and dependency masters, dependent controls) they
 * are rebound here as well, since a leftover source id would make the host invalid.
 *
 * <p>Nothing here modifies the host or the source: the result holds copies that the caller inserts.
 */
public final class FormIncludeExpander {

  /**
   * The outcome of an expansion.
   *
   * @param elements     the copies to put where the include goes, each carrying the include's provenance
   * @param fieldEntries field configuration entries to add to (or merge into) the host, already re-keyed
   * @param groupEntries group configuration entries to add to (or merge into) the host, already re-keyed
   * @param warnings     localized notes about things that could not be carried over
   */
  public record Expansion(List<ScreenElement> elements, List<FieldConfigEntry> fieldEntries,
      List<GroupConfigEntry> groupEntries, List<String> warnings) {

    /** Whether this is what fits into the grid slot of an Embedded Repeat: exactly one Control Grid. */
    public boolean isSingleControlGrid() {
      return elements.size() == 1 && elements.get(0) instanceof ControlGrid;
    }
  }

  private static final ObjectMapper MAPPER = JsonSettings.objectMapper;

  private final List<DocumentModel> documentModels;

  /** @param documentModels every Document Model of the project - needed to follow Includes inside them */
  public FormIncludeExpander(List<DocumentModel> documentModels) {
    this.documentModels = documentModels;
  }

  /**
   * @param source                 the Form Model to include
   * @param host                   the Form Model the copy goes into
   * @param hostDocumentModelPath  the group of {@code host}'s Document Model that stands for the source's root
   *                               group, e.g. {@code /Person/address}
   * @param includeId              identifies this include and prefixes every copied id; must not exist in the host
   * @param name                   the name of the copied element when there is only one; null/blank keeps the
   *                               source's name
   * @throws FormIncludeException  if the include cannot be expanded
   */
  public Expansion expand(FormModel source, FormModel host, String hostDocumentModelPath, String includeId, String name) {
    Screen screen = source.getContent() == null || source.getContent().getScreens().isEmpty()
        ? null : source.getContent().getScreens().get(0);
    if (screen == null) {
      throw new FormIncludeException("noScreen", source.getId());
    }
    DocumentModel sourceModel = documentModelOf(source, documentModels)
        .orElseThrow(() -> new FormIncludeException("noDocumentModel", source.getId()));
    DocumentModel hostModel = documentModelOf(host, documentModels)
        .orElseThrow(() -> new FormIncludeException("noDocumentModel", host.getId()));
    String hostPath = normalizePath(hostDocumentModelPath);

    List<ScreenElement> elements = copy(screen.getScreenElements(), new TypeReference<>() {});
    rejectUnsupported(source, elements);

    Expander expander = new Expander(source, sourceModel, hostModel, hostPath, includeId);
    expander.prefixIds(elements);
    expander.rebindReferences(elements);

    List<FieldConfigEntry> fieldEntries = new ArrayList<>();
    List<GroupConfigEntry> groupEntries = new ArrayList<>();
    if (source.getContent().getFieldConfiguration() != null) {
      for (FieldConfigEntry entry : source.getContent().getFieldConfiguration().getField()) {
        if (expander.usedRefs.contains(entry.getElementRef())) {
          FieldConfigEntry copy = copy(entry, FieldConfigEntry.class);
          copy.setElementRef(expander.map(entry.getElementRef()));
          expander.rebindConfigReferences(copy);
          fieldEntries.add(copy);
        }
      }
    }
    if (source.getContent().getGroupConfiguration() != null) {
      for (GroupConfigEntry entry : source.getContent().getGroupConfiguration().getGroup()) {
        if (expander.usedRefs.contains(entry.getGroupRef())) {
          GroupConfigEntry copy = copy(entry, GroupConfigEntry.class);
          copy.setGroupRef(expander.map(entry.getGroupRef()));
          expander.rebindConfigReferences(copy);
          groupEntries.add(copy);
        }
      }
    }

    expander.applyProvenance(elements, source.getId(), hostPath, name);
    return new Expansion(elements, fieldEntries, groupEntries, expander.warnings);
  }

  /**
   * The Document Model {@code form}'s elements are bound to: the reference with the "data binding" purpose,
   * otherwise the first Document Model reference (which is what the Form Engine takes).
   */
  public static Optional<DocumentModel> documentModelOf(FormModel form, List<DocumentModel> documentModels) {
    String id = documentModelIdOf(form);
    if (id == null) {
      return Optional.empty();
    }
    return documentModels.stream().filter(model -> Objects.equals(model.getId(), id)).findFirst();
  }

  /** The id {@link #documentModelOf} looks for: what the form's data-binding (else first) Document Model reference names. */
  public static String documentModelIdOf(FormModel form) {
    List<ModelReference> references = form.getModelReferences() == null ? List.of() : form.getModelReferences().stream()
        .filter(reference -> reference.getModelType() == ModelType.DOCUMENT).toList();
    return references.stream()
        .filter(reference -> ModelReference.PURPOSE_DATA_BINDING.equals(reference.getPurpose()))
        .findFirst().or(() -> references.stream().findFirst()).map(ModelReference::getReference).orElse(null);
  }

  /**
   * The paths of the groups in {@code hostModel} that include {@code sourceModel} - the natural values for the
   * {@code hostDocumentModelPath} of an include of a form bound to {@code sourceModel}.
   */
  public static List<String> candidateHostPaths(DocumentModel hostModel, DocumentModel sourceModel,
      List<DocumentModel> documentModels) {
    if (hostModel.getContent() == null || hostModel.getContent().getModelRoot() == null) {
      return List.of();
    }
    ElementIndex index = new ElementIndex(hostModel, documentModels);
    List<String> paths = new ArrayList<>();
    for (Element element : index.allElements()) {
      if (element instanceof GroupElement group && group.getGroup() != null && group.getGroup().getIncludeConfig() != null
          && Objects.equals(group.getGroup().getIncludeConfig().getReference(), sourceModel.getId())) {
        paths.add(index.getPath(element));
      }
    }
    return paths;
  }

  /** Whether {@code includeId} can be used for a new include in {@code content}: no id is, or starts with, it. */
  public static boolean isIncludeIdFree(FormModelContent content, String includeId) {
    String prefix = includeId + "_";
    return FormModelWalker.find(content, Object.class, node -> true).stream()
        .map(FormIncludeExpander::idOf)
        .noneMatch(id -> id != null && (id.equals(includeId) || id.startsWith(prefix)))
        && FormModelWalker.find(content, ScreenElement.class).stream()
            .noneMatch(element -> includeId.equals(element.getIncludeId()));
  }

  /**
   * The elements of {@code siblings} that belong to the include {@code siblings.get(index)} was expanded from -
   * that one plus its neighbors carrying the same {@code includeId} (one include yields several elements when the
   * source screen has several). Returns {@code {first, last}}, both inclusive; {@code {index, index}} for an
   * element that is not part of an include.
   */
  public static int[] includeRun(List<?> siblings, int index) {
    String includeId = siblings.get(index) instanceof ScreenElement element ? element.getIncludeId() : null;
    if (includeId == null || includeId.isEmpty()) {
      return new int[] {index, index};
    }
    int first = index;
    while (first > 0 && includeId.equals(includeIdOf(siblings.get(first - 1)))) {
      first--;
    }
    int last = index;
    while (last + 1 < siblings.size() && includeId.equals(includeIdOf(siblings.get(last + 1)))) {
      last++;
    }
    return new int[] {first, last};
  }

  /**
   * {@code added} merged into {@code existing}: an attribute the existing entry lacks (absent in its JSON) is
   * taken over, one it has stays as it is. Returns a new instance, both arguments stay untouched.
   */
  public static <T> T fillUnset(T existing, T added, Class<T> type) {
    ObjectNode merged = MAPPER.valueToTree(existing);
    ObjectNode addition = MAPPER.valueToTree(added);
    addition.properties().forEach(property -> {
      if (!merged.has(property.getKey())) {
        merged.set(property.getKey(), property.getValue());
      }
    });
    return MAPPER.treeToValue(merged, type);
  }

  private static String includeIdOf(Object node) {
    return node instanceof ScreenElement element ? element.getIncludeId() : null;
  }

  private static String normalizePath(String path) {
    String normalized = path == null ? "" : path.strip();
    while (normalized.length() > 1 && normalized.endsWith("/")) {
      normalized = normalized.substring(0, normalized.length() - 1);
    }
    return normalized.isEmpty() || normalized.startsWith("/") ? normalized : "/" + normalized;
  }

  private static void rejectUnsupported(FormModel source, List<ScreenElement> elements) {
    // Their expressions hold Document Model paths as text, which are not rebound.
    if (!FormModelWalker.find(elements, ExpressionCell.class, node -> true).isEmpty()
        || !FormModelWalker.find(elements, ExpressionRepeatOverviewColumn.class, node -> true).isEmpty()) {
      throw new FormIncludeException("unsupportedElement", source.getId(), "Expression");
    }
    if (!FormModelWalker.find(elements, Binding.class, node -> true).isEmpty()) {
      throw new FormIncludeException("unsupportedElement", source.getId(), "Binding");
    }
  }

  private static <T> T copy(Object value, Class<T> type) {
    return MAPPER.readValue(MAPPER.writeValueAsString(value), type);
  }

  // Written with the declared type: a list serialized as a bare Object loses the polymorphic "type" of its elements
  // (they come back as generic elements), which is why a single node is cloned by its own class above.
  private static <T> T copy(Object value, TypeReference<T> type) {
    return MAPPER.readValue(MAPPER.writerFor(type).writeValueAsString(value), type);
  }

  private static String idOf(Object node) {
    return switch (node) {
      case ScreenElement element -> element.getId();
      case Row row -> row.getId();
      case Cell cell -> cell.getId();
      case RepeatOverviewColumn column -> column.getId();
      case Button button -> button.getId();
      case Screen screen -> screen.getId();
      case HeaderFooterBox box -> box.getId();
      default -> null;
    };
  }

  /** The state of one expansion; keeps {@link #expand} readable. */
  private final class Expander {

    private final FormModel source;
    private final DocumentModel sourceModel;
    private final DocumentModel hostModel;
    private final String hostPath;
    private final String includeId;
    private final ElementIndex sourceIndex;
    private final ElementIndex hostIndex;

    /** source Document Model id -> host Document Model id, filled lazily by {@link #map}. */
    private final Map<String, String> mapping = new LinkedHashMap<>();
    /** The source ids the copied elements bind to - only their configuration entries come along. */
    private final Set<String> usedRefs = new LinkedHashSet<>();
    /** old form element id -> new form element id. */
    private final Map<String, String> renamedIds = new HashMap<>();
    private final List<String> warnings = new ArrayList<>();

    private Expander(FormModel source, DocumentModel sourceModel, DocumentModel hostModel, String hostPath, String includeId) {
      this.source = source;
      this.sourceModel = sourceModel;
      this.hostModel = hostModel;
      this.hostPath = hostPath;
      this.includeId = includeId;
      this.sourceIndex = new ElementIndex(sourceModel, documentModels);
      this.hostIndex = new ElementIndex(hostModel, documentModels);
    }

    /** Every id in the copied tree gets {@code <includeId>_} in front. */
    private void prefixIds(List<ScreenElement> elements) {
      List<Object> nodes = new ArrayList<>();
      for (Class<?> type : List.of(ScreenElement.class, Row.class, Cell.class, RepeatOverviewColumn.class,
          Button.class, Screen.class, HeaderFooterBox.class)) {
        nodes.addAll(FormModelWalker.find(elements, type, node -> true));
      }
      for (Object node : nodes) {
        String id = idOf(node);
        if (id != null) {
          String newId = includeId + "_" + id;
          renamedIds.put(id, newId);
          setId(node, newId);
        }
      }
    }

    private void setId(Object node, String id) {
      switch (node) {
        case ScreenElement element -> element.setId(id);
        case Row row -> row.setId(id);
        case Cell cell -> cell.setId(id);
        case RepeatOverviewColumn column -> column.setId(id);
        case Button button -> button.setId(id);
        case Screen screen -> screen.setId(id);
        case HeaderFooterBox box -> box.setId(id);
        default -> throw new IllegalArgumentException(node.getClass().getName());
      }
    }

    /** Points everything that references the source's Document Model, or a copied element, at the host's. */
    private void rebindReferences(List<ScreenElement> elements) {
      for (Control control : FormModelWalker.find(elements, Control.class, node -> true)) {
        control.setElementRef(bind(control.getElementRef()));
        rebindDependentControls(control);
      }
      for (FieldBasedRepeatOverviewColumn column : FormModelWalker.find(elements, FieldBasedRepeatOverviewColumn.class, node -> true)) {
        column.setElementRef(bind(column.getElementRef()));
      }
      for (AbstractRepeat repeat : FormModelWalker.find(elements, AbstractRepeat.class, node -> true)) {
        repeat.setGroupRef(bind(repeat.getGroupRef()));
      }
      for (MultiFileUploadOptions options : FormModelWalker.find(elements, MultiFileUploadOptions.class, node -> true)) {
        if (options.getElementRef() != null && !options.getElementRef().isBlank()) {
          options.setElementRef(bind(options.getElementRef()));
        }
      }
      // Not something the element binds to itself, so no configuration entry follows from these.
      for (HideCondition condition : FormModelWalker.find(elements, HideCondition.class, node -> true)) {
        condition.setMasterField(map(condition.getMasterField()));
      }
    }

    private void rebindDependentControls(Control control) {
      if (control.getDependentControls() == null) {
        return;
      }
      control.getDependentControls().getScreenElement().removeIf(entry -> {
        String renamed = renamedIds.get(entry.getIdref());
        if (renamed == null) {
          warnings.add(ValidationMessages.get("validation.formInclude.droppedDependentControl", control.getId(), entry.getIdref()));
          return true;
        }
        entry.setIdref(renamed);
        return false;
      });
      if (control.getDependentControls().getScreenElement().isEmpty()) {
        control.setDependentControls(null);
      }
    }

    /** The references inside a configuration entry that point at other Document Model elements or copied elements. */
    private void rebindConfigReferences(Object entry) {
      for (DependentConfig config : FormModelWalker.find(entry, DependentConfig.class, node -> true)) {
        config.setMasterField(map(config.getMasterField()));
        for (DependentCase dependentCase : config.getCases()) {
          rebindCase(dependentCase);
        }
      }
      for (DependentEnumeration enumeration : FormModelWalker.find(entry, DependentEnumeration.class, node -> true)) {
        enumeration.setMasterField(map(enumeration.getMasterField()));
      }
    }

    private void rebindCase(DependentCase dependentCase) {
      if (dependentCase.getFieldRef() != null && !dependentCase.getFieldRef().isEmpty()) {
        dependentCase.setFieldRef(map(dependentCase.getFieldRef()));
      }
      List<String> nodes = new ArrayList<>();
      for (String node : dependentCase.getNotRelevantNodes()) {
        String renamed = renamedIds.get(node);
        if (renamed != null) {
          nodes.add(renamed);
        }
      }
      dependentCase.setNotRelevantNodes(nodes);
    }

    /** {@link #map} for something the copied elements bind to: remembers it, so its configuration comes along. */
    private String bind(String sourceId) {
      if (sourceId == null || sourceId.isBlank()) {
        return sourceId;
      }
      usedRefs.add(sourceId);
      return map(sourceId);
    }

    /** The id in the host Document Model of the element the source id refers to (see the class description). */
    private String map(String sourceId) {
      if (sourceId == null || sourceId.isBlank()) {
        return sourceId;
      }
      String cached = mapping.get(sourceId);
      if (cached != null) {
        return cached;
      }
      if (!sourceIndex.isResolvable(sourceId)) {
        throw new FormIncludeException("sourceElementMissing", sourceId, sourceModel.getId());
      }
      String originalPath = sourceIndex.resolveDisplayPath(sourceId);
      String belowRoot;
      if (hostPath.isEmpty() || hostPath.equals("/")) {
        belowRoot = originalPath;
      }
      else if (originalPath.chars().filter(character -> character == '/').count() > 1) {
        belowRoot = originalPath.substring(originalPath.indexOf('/', 1));
      }
      else {
        belowRoot = "";
      }
      String mappedPath = (hostPath.equals("/") ? "" : hostPath) + belowRoot;
      String hostId = hostIndex.resolveIdByPath(mappedPath)
          .orElseThrow(() -> new FormIncludeException("hostElementMissing", mappedPath, hostModel.getId()));
      mapping.put(sourceId, hostId);
      return hostId;
    }

    /**
     * Provenance goes on the top-level elements only - a nested include of the source is part of this one now -
     * and the names are made unique.
     */
    private void applyProvenance(List<ScreenElement> elements, String formModelRef, String documentModelPath, String name) {
      for (ScreenElement element : FormModelWalker.find(elements, ScreenElement.class, node -> true)) {
        element.setIncludeId(null);
        element.setFormModelRef(null);
        element.setHostDocumentModelPath(null);
      }
      for (ScreenElement element : elements) {
        element.setIncludeId(includeId);
        element.setFormModelRef(formModelRef);
        element.setHostDocumentModelPath(documentModelPath);
        if (elements.size() > 1) {
          element.setName(element.getName() == null || element.getName().isBlank() ? includeId
              : includeId + "-" + element.getName());
        }
        else if (name != null && !name.isBlank()) {
          element.setName(name);
        }
      }
    }
  }
}
