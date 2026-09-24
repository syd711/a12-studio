package de.a12.studio.models.projects;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.a12.studio.models.util.JsonSettings;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Collects load-then-save mismatches from round-trip tests and writes them as a machine-readable
 * JSON report meant to drive fixes in the model classes.
 * <p>
 * Every difference carries its JSON pointer, the expected (on disk) and actual (after save) value,
 * the Java class owning the property and the member (field/getter) Jackson maps it to, resolved by
 * walking the in-memory model along the pointer, plus a hint naming the usual fix for that kind of
 * mismatch. {@code patterns} groups identical (kind, class, property) mismatches across all files,
 * so each entry there is roughly one code fix.
 */
final class ModelRoundTripReport {

  enum Kind {
    /** Present in the file, gone after save. */
    MISSING_AFTER_SAVE,
    /** Absent in the file, written by save. */
    ADDED_BY_SAVE,
    /** Same number, different token shape (e.g. 1 vs 1.0). */
    NUMBER_FORMAT_CHANGED,
    /** JSON node type changed (e.g. string to number, object to array). */
    TYPE_CHANGED,
    /** Same type, different value. */
    VALUE_CHANGED,
    /** Load or save threw. */
    ERROR
  }

  private static final int MAX_DIFFERENCES_PER_FILE = 200;
  private static final int MAX_FILES_PER_PATTERN = 10;
  private static final Pattern SYNTHETIC_GETTER_NAME = Pattern.compile("^(orCreate|or)[A-Z].*");

  private final Path root;
  private final List<FileResult> results = Collections.synchronizedList(new ArrayList<>());

  ModelRoundTripReport(Path root) {
    this.root = root;
  }

  record Difference(Kind kind, String path, JsonNode expected, JsonNode actual,
                    String ownerClass, String member, String memberDeclaringClass, String hint) {
  }

  record FileResult(String modelsFolder, String file, Path sourceFile, String modelType, String modelClass,
                    List<Difference> differences, boolean truncated, String error) {
    boolean failed() {
      return error != null || !differences.isEmpty();
    }
  }

  /** Compares both trees, records the result and returns it. {@code model} is the saved in-memory model. */
  FileResult compare(String modelsFolder, String file, Path sourceFile, Object model, JsonNode expected, JsonNode actual) {
    List<Difference> differences = new ArrayList<>();
    boolean truncated = diff(model, expected, actual, "", differences);
    FileResult result = new FileResult(modelsFolder, file, sourceFile, modelType(expected),
        model != null ? model.getClass().getName() : null, differences, truncated, null);
    results.add(result);
    return result;
  }

  FileResult error(String modelsFolder, String file, Path sourceFile, Object model, JsonNode expected, Throwable error) {
    StringWriter trace = new StringWriter();
    error.printStackTrace(new PrintWriter(trace));
    FileResult result = new FileResult(modelsFolder, file, sourceFile, expected != null ? modelType(expected) : null,
        model != null ? model.getClass().getName() : null, List.of(), false, trace.toString());
    results.add(result);
    return result;
  }

  // ---------------------------------------------------------------------------------------------
  // Diff
  // ---------------------------------------------------------------------------------------------

  /** @return true if the per-file difference limit was hit */
  private static boolean diff(Object model, JsonNode expected, JsonNode actual, String path, List<Difference> out) {
    if (out.size() >= MAX_DIFFERENCES_PER_FILE) {
      return true;
    }
    if (expected.equals(actual)) {
      return false;
    }
    if (expected.isObject() && actual.isObject()) {
      Set<String> names = new LinkedHashSet<>();
      expected.properties().forEach(e -> names.add(e.getKey()));
      actual.properties().forEach(e -> names.add(e.getKey()));
      for (String name : names) {
        String childPath = path + "/" + escape(name);
        JsonNode e = expected.get(name);
        JsonNode a = actual.get(name);
        if (e == null) {
          out.add(difference(model, Kind.ADDED_BY_SAVE, childPath, null, a));
        }
        else if (a == null) {
          out.add(difference(model, Kind.MISSING_AFTER_SAVE, childPath, e, null));
        }
        else if (diff(model, e, a, childPath, out)) {
          return true;
        }
        if (out.size() >= MAX_DIFFERENCES_PER_FILE) {
          return true;
        }
      }
      return false;
    }
    if (expected.isArray() && actual.isArray()) {
      int max = Math.max(expected.size(), actual.size());
      for (int i = 0; i < max; i++) {
        String childPath = path + "/" + i;
        if (i >= actual.size()) {
          out.add(difference(model, Kind.MISSING_AFTER_SAVE, childPath, expected.get(i), null));
        }
        else if (i >= expected.size()) {
          out.add(difference(model, Kind.ADDED_BY_SAVE, childPath, null, actual.get(i)));
        }
        else if (diff(model, expected.get(i), actual.get(i), childPath, out)) {
          return true;
        }
        if (out.size() >= MAX_DIFFERENCES_PER_FILE) {
          return true;
        }
      }
      return false;
    }
    if (expected.isNumber() && actual.isNumber()
        && new BigDecimal(expected.toString()).compareTo(new BigDecimal(actual.toString())) == 0) {
      out.add(difference(model, Kind.NUMBER_FORMAT_CHANGED, path, expected, actual));
      return false;
    }
    Kind kind = expected.getNodeType() == actual.getNodeType() ? Kind.VALUE_CHANGED : Kind.TYPE_CHANGED;
    out.add(difference(model, kind, path, expected, actual));
    return false;
  }

  private static Difference difference(Object model, Kind kind, String path, JsonNode expected, JsonNode actual) {
    Resolution resolution = resolve(model, path);
    String hint = hint(kind, lastSegment(path), expected, actual, resolution);
    return new Difference(kind, path, expected, actual,
        resolution.owner != null ? resolution.owner.getName() : null,
        resolution.member != null ? describe(resolution.member) : null,
        resolution.member != null ? resolution.member.getDeclaringClass().getName() : null,
        hint);
  }

  private static String hint(Kind kind, String property, JsonNode expected, JsonNode actual, Resolution resolution) {
    boolean arrayElement = !property.isEmpty() && property.chars().allMatch(Character::isDigit);
    switch (kind) {
      case ADDED_BY_SAVE -> {
        if (arrayElement) {
          return "Save wrote an extra array element: check list initialisation/defaults or element duplication on load.";
        }
        if (SYNTHETIC_GETTER_NAME.matcher(property).matches()
            || (resolution.member instanceof Method && resolution.backingField == null)) {
          return "Synthetic property from a getter-shaped helper: add @JsonIgnore to the getter.";
        }
        if (actual != null && actual.isNull()) {
          return "Absent key written as null: add @JsonInclude(JsonInclude.Include.NON_NULL).";
        }
        if (resolution.backingField != null && resolution.backingField.getType().isPrimitive()) {
          return "Primitive field is always serialized: switch to the boxed type with @JsonInclude(NON_NULL).";
        }
        if (actual != null && (actual.isArray() || actual.isObject()) && actual.isEmpty()) {
          return "Absent collection written as empty: use @JsonInclude(NON_EMPTY), or track 'explicit on load' if other files write it empty (see A12Model labels/locales).";
        }
        return "Key absent in source but written on save: default value is serialized; make it nullable with @JsonInclude(NON_NULL/NON_DEFAULT).";
      }
      case MISSING_AFTER_SAVE -> {
        if (arrayElement) {
          return "Array element dropped on save: check deserialization of this element type (unknown subtype, filtering, dedup).";
        }
        if (resolution.member == null) {
          return "Property not mapped by the owning class and silently ignored on load (FAIL_ON_UNKNOWN_PROPERTIES is off): add a field for it, or @JsonAnySetter/@JsonAnyGetter to preserve unknown keys.";
        }
        return "Mapped property dropped on save: check @JsonIgnore/@JsonInclude(NON_EMPTY/NON_DEFAULT) or setter/getter mismatch.";
      }
      case NUMBER_FORMAT_CHANGED -> {
        return "Number token shape changed (e.g. 1 vs 1.0): if fixtures mix both forms, back the field with JsonNode to preserve the original token (see overviewmodel.Column.width).";
      }
      case TYPE_CHANGED -> {
        return "JSON type changed on save: check the field's Java type or a custom (de)serializer.";
      }
      case VALUE_CHANGED -> {
        return "Value normalised on save: check setter/constructor logic, enum mapping, or defaults applied on load.";
      }
      default -> {
        return null;
      }
    }
  }

  // ---------------------------------------------------------------------------------------------
  // Java member resolution (best-effort reflection walk along the JSON pointer)
  // ---------------------------------------------------------------------------------------------

  private static final class Resolution {
    Class<?> owner;
    Member member;
    Field backingField;
  }

  private static Resolution resolve(Object model, String path) {
    Resolution resolution = new Resolution();
    if (model == null) {
      return resolution;
    }
    List<String> segments = segments(path);
    if (segments.isEmpty()) {
      resolution.owner = model.getClass();
      return resolution;
    }
    Object current = model;
    try {
      for (int i = 0; i < segments.size() - 1 && current != null; i++) {
        current = child(current, segments.get(i));
        if (current instanceof JsonNode) {
          return resolution; // raw JSON subtree: no Java class owns the deeper property
        }
      }
      if (current == null) {
        return resolution;
      }
      String last = segments.get(segments.size() - 1);
      if (current instanceof List<?> || current instanceof Map<?, ?> || current.getClass().isArray()) {
        resolution.owner = current.getClass();
        return resolution;
      }
      resolution.owner = current.getClass();
      resolution.backingField = findField(current.getClass(), last);
      resolution.member = resolution.backingField != null ? resolution.backingField : findGetter(current.getClass(), last);
    }
    catch (RuntimeException | ReflectiveOperationException ignored) {
      // best-effort: leave whatever was resolved so far
    }
    return resolution;
  }

  private static Object child(Object parent, String segment) throws ReflectiveOperationException {
    if (parent instanceof List<?> list) {
      int index = Integer.parseInt(segment);
      return index < list.size() ? list.get(index) : null;
    }
    if (parent.getClass().isArray()) {
      int index = Integer.parseInt(segment);
      return index < Array.getLength(parent) ? Array.get(parent, index) : null;
    }
    if (parent instanceof Map<?, ?> map) {
      return map.get(segment);
    }
    Field field = findField(parent.getClass(), segment);
    if (field != null) {
      field.setAccessible(true);
      return field.get(parent);
    }
    Method getter = findGetter(parent.getClass(), segment);
    if (getter != null) {
      getter.setAccessible(true);
      return getter.invoke(parent);
    }
    return null;
  }

  private static Field findField(Class<?> type, String jsonName) {
    for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
      for (Field field : c.getDeclaredFields()) {
        if (Modifier.isStatic(field.getModifiers())) {
          continue;
        }
        JsonProperty property = field.getAnnotation(JsonProperty.class);
        String name = property != null && !property.value().isEmpty() ? property.value() : field.getName();
        if (name.equals(jsonName)) {
          return field;
        }
      }
    }
    return null;
  }

  private static Method findGetter(Class<?> type, String jsonName) {
    String capitalized = jsonName.isEmpty() ? jsonName : Character.toUpperCase(jsonName.charAt(0)) + jsonName.substring(1);
    for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
      for (Method method : c.getDeclaredMethods()) {
        if (method.getParameterCount() != 0 || Modifier.isStatic(method.getModifiers())) {
          continue;
        }
        JsonProperty property = method.getAnnotation(JsonProperty.class);
        if (property != null && jsonName.equals(property.value())) {
          return method;
        }
        if (method.getName().equals("get" + capitalized) || method.getName().equals("is" + capitalized)) {
          return method;
        }
      }
    }
    return null;
  }

  private static String describe(Member member) {
    String owner = member.getDeclaringClass().getSimpleName();
    if (member instanceof Field field) {
      return "field " + owner + "." + field.getName() + " : " + field.getType().getSimpleName();
    }
    Method method = (Method) member;
    return "method " + owner + "." + method.getName() + "() : " + method.getReturnType().getSimpleName();
  }

  // ---------------------------------------------------------------------------------------------
  // Output
  // ---------------------------------------------------------------------------------------------

  Path write(Path reportFile) throws IOException {
    List<FileResult> snapshot;
    synchronized (results) {
      snapshot = new ArrayList<>(results);
    }
    snapshot.sort((a, b) -> (a.modelsFolder + "/" + a.file).compareTo(b.modelsFolder + "/" + b.file));

    ObjectNode report = JsonSettings.objectMapper.createObjectNode();
    report.put("generatedAt", OffsetDateTime.now().toString());
    report.put("root", root.toString());
    report.put("description", "Model round-trip (load then save) mismatches. 'expected' = file on disk, "
        + "'actual' = content after save. 'path' is a JSON pointer into the model file. 'patterns' groups "
        + "identical mismatches across files; each pattern is usually one fix in 'memberDeclaringClass'.");

    int failedFiles = 0;
    int differences = 0;
    for (FileResult result : snapshot) {
      if (result.failed()) {
        failedFiles++;
      }
      differences += result.differences.size();
    }
    ObjectNode summary = report.putObject("summary");
    summary.put("filesTested", snapshot.size());
    summary.put("filesFailed", failedFiles);
    summary.put("differences", differences);

    ArrayNode patterns = report.putArray("patterns");
    for (Group pattern : groups(snapshot)) {
      ObjectNode node = patterns.addObject();
      node.put("kind", pattern.kind.name());
      node.put("property", pattern.property);
      node.put("ownerClass", pattern.ownerClass);
      node.put("member", pattern.member);
      node.put("memberDeclaringClass", pattern.memberDeclaringClass);
      node.put("count", pattern.count);
      node.put("hint", pattern.hint);
      ObjectNode example = node.putObject("example");
      example.put("file", pattern.exampleFile);
      example.put("path", pattern.examplePath);
      setIfPresent(example, "expected", pattern.exampleExpected);
      setIfPresent(example, "actual", pattern.exampleActual);
      ArrayNode files = node.putArray("files");
      pattern.files.forEach(files::add);
    }

    ArrayNode files = report.putArray("files");
    for (FileResult result : snapshot) {
      if (!result.failed()) {
        continue;
      }
      ObjectNode node = files.addObject();
      node.put("modelsFolder", result.modelsFolder);
      node.put("file", result.file);
      node.put("sourceFile", result.sourceFile.toString());
      node.put("modelType", result.modelType);
      node.put("modelClass", result.modelClass);
      if (result.error != null) {
        node.put("error", result.error);
      }
      if (result.truncated) {
        node.put("truncatedAfter", MAX_DIFFERENCES_PER_FILE);
      }
      ArrayNode diffs = node.putArray("differences");
      for (Difference d : result.differences) {
        ObjectNode diff = diffs.addObject();
        diff.put("kind", d.kind.name());
        diff.put("path", d.path);
        setIfPresent(diff, "expected", d.expected);
        setIfPresent(diff, "actual", d.actual);
        diff.put("ownerClass", d.ownerClass);
        diff.put("member", d.member);
        diff.put("memberDeclaringClass", d.memberDeclaringClass);
        diff.put("hint", d.hint);
      }
    }

    Files.createDirectories(reportFile.toAbsolutePath().getParent());
    Files.writeString(reportFile, JsonSettings.objectMapper.writeValueAsString(report), StandardCharsets.UTF_8);
    return reportFile;
  }

  private static final class Group {
    Kind kind;
    String property;
    String ownerClass;
    String member;
    String memberDeclaringClass;
    String hint;
    int count;
    String exampleFile;
    String examplePath;
    JsonNode exampleExpected;
    JsonNode exampleActual;
    final Set<String> files = new LinkedHashSet<>();
  }

  private static List<Group> groups(List<FileResult> results) {
    Map<String, Group> byKey = new LinkedHashMap<>();
    for (FileResult result : results) {
      String file = result.modelsFolder + "/" + result.file;
      if (result.error != null) {
        Group p = byKey.computeIfAbsent("ERROR|" + result.modelClass, k -> {
          Group n = new Group();
          n.kind = Kind.ERROR;
          n.ownerClass = result.modelClass;
          n.hint = "Load or save threw; see 'error' in the file entry.";
          n.exampleFile = file;
          return n;
        });
        p.count++;
        if (p.files.size() < MAX_FILES_PER_PATTERN) {
          p.files.add(file);
        }
      }
      for (Difference d : result.differences) {
        String property = lastSegment(d.path);
        if (!property.isEmpty() && property.chars().allMatch(Character::isDigit)) {
          property = "[]";
        }
        String owner = d.memberDeclaringClass != null ? d.memberDeclaringClass : d.ownerClass;
        String key = d.kind + "|" + owner + "|" + property;
        final String prop = property;
        Group p = byKey.computeIfAbsent(key, k -> {
          Group n = new Group();
          n.kind = d.kind;
          n.property = prop;
          n.ownerClass = d.ownerClass;
          n.member = d.member;
          n.memberDeclaringClass = d.memberDeclaringClass;
          n.hint = d.hint;
          n.exampleFile = file;
          n.examplePath = d.path;
          n.exampleExpected = d.expected;
          n.exampleActual = d.actual;
          return n;
        });
        p.count++;
        if (p.files.size() < MAX_FILES_PER_PATTERN) {
          p.files.add(file);
        }
      }
    }
    List<Group> list = new ArrayList<>(byKey.values());
    list.sort((a, b) -> Integer.compare(b.count, a.count));
    return list;
  }

  // ---------------------------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------------------------

  private static String modelType(JsonNode tree) {
    JsonNode header = tree.get("header");
    JsonNode type = header != null ? header.get("modelType") : null;
    return type != null && type.isString() ? type.asString() : null;
  }

  private static String escape(String name) {
    return name.replace("~", "~0").replace("/", "~1");
  }

  /** Leaves the key out for an absent side, so "absent" stays distinguishable from JSON null. */
  private static void setIfPresent(ObjectNode node, String name, JsonNode value) {
    if (value != null) {
      node.set(name, value);
    }
  }

  private static List<String> segments(String path) {
    List<String> segments = new ArrayList<>();
    if (path.isEmpty()) {
      return segments;
    }
    for (String raw : path.substring(1).split("/", -1)) {
      segments.add(raw.replace("~1", "/").replace("~0", "~"));
    }
    return segments;
  }

  private static String lastSegment(String path) {
    List<String> segments = segments(path);
    return segments.isEmpty() ? "" : segments.get(segments.size() - 1);
  }
}
