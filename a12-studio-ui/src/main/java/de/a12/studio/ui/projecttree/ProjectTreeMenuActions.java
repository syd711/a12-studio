package de.a12.studio.ui.projecttree;

import de.a12.studio.models.documentmodel.DateTimeFieldType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.FieldConfig;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.ModelInfo;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.documentmodel.BooleanFieldType;
import de.a12.studio.models.documentmodel.DateFieldType;
import de.a12.studio.models.documentmodel.NumberFieldType;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.ui.components.StudioFolderChooser;
import de.a12.studio.ui.projecttree.dialogs.ImportFromAccessDialogController;
import de.a12.studio.ui.projecttree.dialogs.ImportFromAccessDialogController.AccessImportInput;
import de.a12.studio.ui.projecttree.dialogs.ImportFromExcelDialogController;
import de.a12.studio.ui.projecttree.dialogs.ImportFromExcelDialogController.ExcelImportInput;
import de.a12.studio.ui.projecttree.importdb.AccessImportService.ColumnFieldType;
import de.a12.studio.ui.projecttree.importdb.AccessImportService.ColumnInfo;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.ui.util.zip.ZipUtil;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.NewModelFactory;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.projects.settings.ProjectRootSettings;
import de.a12.studio.models.Locale;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.projecttree.dialogs.NewModelDialogController;
import de.a12.studio.ui.projecttree.dialogs.NewModelDialogController.NewModelInput;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
public class ProjectTreeMenuActions {

  private final Supplier<Stage> stageSupplier;
  private final Runnable onReload;
  private final Consumer<ProjectItemViewModel> onOpen;

  public ProjectTreeMenuActions(@NonNull Supplier<Stage> stageSupplier, @NonNull Runnable onReload,
                                @NonNull Consumer<ProjectItemViewModel> onOpen) {
    this.stageSupplier = stageSupplier;
    this.onReload = onReload;
    this.onOpen = onOpen;
  }

  void onOpenItem(@NonNull ProjectItemViewModel item) {
    onOpen.accept(item);
  }

  void onCreateNewFolder(@NonNull ProjectItem parent) {
    String title = StudioBundle.get("new_folder_title");
    String name = WidgetFactory.showInputDialog(getStage(), title, title, null, null, null);
    if (name == null || name.isBlank()) {
      return;
    }

    try {
      parent.createChildFolder(name.trim());
      onReload.run();
    }
    catch (IOException e) {
      showError(StudioBundle.get("could_not_create_item", name), e);
    }
  }

  void onCreateNewModel(@NonNull ProjectItem parent) {
    onCreateNewModel(parent, null);
  }

  void onCreateNewModel(@NonNull ProjectItem parent, ModelType preselectedType) {
    Optional<NewModelInput> input = NewModelDialogController.show(getStage(), parent, preselectedType);
    if (input.isEmpty()) {
      return;
    }

    ModelType modelType = input.get().modelType();
    String name = input.get().name();
    String documentModelId = input.get().documentModelId();
    try {
      ProjectItem item = NewModelFactory.createModel(parent, modelType, name, documentModelId);
      onReload.run();
      onOpen.accept(new ProjectItemViewModel(item, Map.of()));
    }
    catch (IOException e) {
      showError(StudioBundle.get("could_not_create_item", name), e);
    }
  }

  void onImportFromExcel(@NonNull ProjectItem parent) {
    Optional<ExcelImportInput> input = ImportFromExcelDialogController.show(getStage(), parent);
    if (input.isEmpty()) {
      return;
    }

    ExcelImportInput data = input.get();
    // ExcelImportService.ColumnInfo and AccessImportService.ColumnInfo share the same ColumnFieldType
    // enum, so we convert to the shared type for the common builder method.
    List<ColumnInfo> columns = data.columns().stream()
        .map(c -> new ColumnInfo(c.name(), c.fieldType()))
        .toList();
    try {
      DocumentModel model = buildDocumentModelFromColumns(parent, data.modelName(), data.sheetName(), columns);
      ProjectItem item = NewModelFactory.createModelFromExisting(parent, model, data.modelName());
      onReload.run();
      onOpen.accept(new ProjectItemViewModel(item, Map.of()));
    }
    catch (IOException e) {
      log.error("Failed to create document model from Excel sheet '{}': {}", data.sheetName(), e.getMessage(), e);
      showError(StudioBundle.get("could_not_create_item", data.modelName()), e);
    }
  }

  void onImportFromAccessDatabase(@NonNull ProjectItem parent) {
    Optional<AccessImportInput> input = ImportFromAccessDialogController.show(getStage(), parent);
    if (input.isEmpty()) {
      return;
    }

    AccessImportInput data = input.get();
    try {
      DocumentModel model = buildDocumentModelFromColumns(parent, data.modelName(), data.tableName(), data.columns());
      ProjectItem item = NewModelFactory.createModelFromExisting(parent, model, data.modelName());
      onReload.run();
      onOpen.accept(new ProjectItemViewModel(item, Map.of()));
    }
    catch (IOException e) {
      log.error("Failed to create document model from Access table '{}': {}", data.tableName(), e.getMessage(), e);
      showError(StudioBundle.get("could_not_create_item", data.modelName()), e);
    }
  }

  /**
   * Builds a {@link DocumentModel} whose fields mirror the provided columns.
   * All fields are placed in a single root group named after the source (table/sheet name).
   *
   * @param parent    target project folder (used to resolve default locales)
   * @param modelName the document model name (also the {@link de.a12.studio.models.documentmodel.ModelInfo} name)
   * @param groupName label for the root group (typically the table or sheet name)
   * @param columns   ordered list of columns to turn into fields
   */
  private DocumentModel buildDocumentModelFromColumns(@NonNull ProjectItem parent,
                                                      @NonNull String modelName,
                                                      @NonNull String groupName,
                                                      @NonNull List<ColumnInfo> columns) {
    DocumentModel model = new DocumentModel();

    DocumentModelContent content = new DocumentModelContent();

    ModelInfo modelInfo = new ModelInfo();
    modelInfo.setName(modelName);
    content.setModelInfo(modelInfo);
    content.setModelConfig(NewModelFactory.defaultModelConfig());

    // Build a single root group containing one field per source column.
    GroupElement rootGroup = new GroupElement();
    rootGroup.setId(sanitizeId(groupName));
    rootGroup.setName(groupName);
    GroupConfig groupConfig = new GroupConfig();

    for (ColumnInfo col : columns) {
      FieldElement field = new FieldElement();
      field.setId(sanitizeId(col.name()));
      field.setName(col.name());

      FieldConfig fieldConfig = new FieldConfig();
      fieldConfig.setFieldType(toFieldType(col.fieldType()));
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

  /** Converts an {@link AccessImportService.ColumnFieldType} to the appropriate Document Model {@link de.a12.studio.models.documentmodel.FieldType}. */
  private de.a12.studio.models.documentmodel.FieldType toFieldType(@NonNull ColumnFieldType columnFieldType) {
    return switch (columnFieldType) {
      case BOOLEAN -> new BooleanFieldType();
      case NUMBER -> new NumberFieldType();
      case DATE -> new DateFieldType();
      case DATE_TIME -> new DateTimeFieldType();
      default -> new StringFieldType();
    };
  }

  /**
   * Turns an arbitrary name into a valid document model element id: strips leading digits,
   * replaces any character that isn't a letter/digit/underscore with an underscore.
   */
  private String sanitizeId(@NonNull String name) {
    String id = name.replaceAll("[^A-Za-z0-9_]", "_");
    if (!id.isEmpty() && Character.isDigit(id.charAt(0))) {
      id = "_" + id;
    }
    return id.isEmpty() ? "field" : id;
  }

  private List<Locale> resolveDefaultLocales(@NonNull ProjectItem parent) {
    ProjectItem root = parent;
    while (root.getParent() != null) {
      root = root.getParent();
    }
    List<Locale> projectLocales = ProjectRootSettings.load(root.getFile()).getGeneral().getLocales();
    if (projectLocales.isEmpty()) {
      Locale en = new Locale();
      en.setCode("en");
      Locale de = new Locale();
      de.setCode("de");
      return new ArrayList<>(List.of(en, de));
    }
    List<Locale> locales = new ArrayList<>();
    for (Locale pl : projectLocales) {
      Locale locale = new Locale();
      locale.setCode(pl.getCode());
      locales.add(locale);
    }
    return locales;
  }

  void onRenameItem(@NonNull ProjectItem item) {
    String title = StudioBundle.get("rename_title");
    String name = WidgetFactory.showInputDialog(getStage(), title, title, null, null, item.getName());
    if (name == null || name.isBlank() || name.equals(item.getName())) {
      return;
    }

    String oldPath = item.getPath();
    try {
      item.renameTo(name.trim());
      StudioEventManager.getInstance().fireModelRenamedEvent(oldPath, item);
      onReload.run();
    }
    catch (IOException e) {
      showError(StudioBundle.get("could_not_rename_to", name), e);
    }
  }

  void onCreateCopy(@NonNull ProjectItem item) {
    try {
      item.createCopy();
      onReload.run();
    }
    catch (IOException e) {
      showError(StudioBundle.get("could_not_copy_item", item.getName()), e);
    }
  }

  void onZipFolder(@NonNull ProjectItem item) {
    StudioFolderChooser chooser = new StudioFolderChooser();
    chooser.setTitle(StudioBundle.get("choose_destination_folder"));
    File destinationFolder = chooser.showOpenDialog(getStage());
    if (destinationFolder == null) {
      return;
    }

    String dateSuffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss"));
    File zipFile = new File(destinationFolder, item.getName() + "_" + dateSuffix + ".zip");

    try {
      ZipUtil.zipFolder(item.getFile(), zipFile, (file, path) -> { });
    }
    catch (IOException e) {
      showError(StudioBundle.get("could_not_zip_item", item.getName()), e);
    }
  }

  boolean canMoveItem(@NonNull ProjectItem source, @NonNull ProjectItem targetFolder) {
    if (source.isRoot() || !targetFolder.isFolder()) {
      return false;
    }
    if (source.equals(targetFolder) || source.isAncestorOf(targetFolder)) {
      return false;
    }
    return !targetFolder.equals(source.getParent());
  }

  void onMoveItem(@NonNull ProjectItem source, @NonNull ProjectItem targetFolder) {
    if (!canMoveItem(source, targetFolder)) {
      return;
    }

    try {
      source.moveTo(targetFolder);
      onReload.run();
    }
    catch (IOException e) {
      showError(StudioBundle.get("could_not_move_item", source.getName(), targetFolder.getName()), e);
    }
  }

  void onDeleteItem(@NonNull ProjectItem item) {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(getStage(),
        StudioBundle.get("confirm_delete_item", item.getName()), null, null, StudioBundle.get("delete"));
    if (result.isPresent() && result.get() == ButtonType.OK) {
      try {
        item.delete();
        StudioEventManager.getInstance().fireModelDeletedEvent(item);
        onReload.run();
      }
      catch (IOException e) {
        showError(StudioBundle.get("could_not_delete_item", item.getName()), e);
      }
    }
  }

  private void showError(@NonNull String message, @NonNull Exception e) {
    WidgetFactory.showAlert(getStage(), message, e.getMessage());
  }

  private Stage getStage() {
    return stageSupplier.get();
  }
}
