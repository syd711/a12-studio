package de.a12.studio.plugin.excel;

import de.a12.studio.models.NewModelFactory;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.plugin.manager.ICreateItemMenuEntry;
import de.a12.studio.ui.editors.propertyeditors.RolesEditorPanelController;
import de.a12.studio.ui.util.DocumentModelBuilder;
import de.a12.studio.ui.util.DocumentModelBuilder.ColumnDescriptor;
import de.a12.studio.ui.util.DocumentModelBuilder.ColumnType;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.scene.Node;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * {@link ICreateItemMenuEntry} implementation that shows the "Create from Excel"
 * import dialog and creates a Document Model from the first sheet of the workbook.
 *
 * <p>This class is instantiated reflectively by {@code PluginManager} via the
 * {@code plugin.json} extension point registration.
 */
@Slf4j
public class ExcelCreateMenuEntry implements ICreateItemMenuEntry {

  @Override
  @NonNull
  public String getMenuLabel() {
    return "Create from Excel...";
  }

  @Override
  @Nullable
  public Node getMenuGraphic() {
    return WidgetFactory.createIcon(Icons.FILE_TABLE_OUTLINE);
  }

  @Override
  public void execute(@NonNull Stage owner, @NonNull ProjectItem targetFolder) {
    Optional<ImportFromExcelDialogController.ExcelImportInput> input =
        ImportFromExcelDialogController.show(owner, targetFolder);
    if (input.isEmpty()) {
      return;
    }

    ImportFromExcelDialogController.ExcelImportInput data = input.get();
    List<ColumnDescriptor> columns = data.columns().stream()
        .map(c -> new ColumnDescriptor(c.name(), toColumnType(c.fieldType())))
        .toList();

    try {
      DocumentModel model = DocumentModelBuilder.build(data.folder(), data.modelName(), data.modelName(), columns);
      ProjectItem item = NewModelFactory.createModelFromExisting(data.folder(), model, data.modelName());
      boolean needsSave = false;
      if (!data.locales().isEmpty()) {
        item.getModel().setLocales(data.locales());
        needsSave = true;
      }
      if (!data.roles().isEmpty()) {
        RolesEditorPanelController.applyRoles(item.getModel(), data.roles());
        needsSave = true;
      }
      if (needsSave) {
        item.save();
      }
    }
    catch (IOException e) {
      log.error("Failed to create document model from Excel file '{}': {}",
          data.excelFile().getName(), e.getMessage(), e);
      WidgetFactory.showAlert(owner, "Error", e.getMessage());
    }
  }

  private static ColumnType toColumnType(ExcelImportService.@NonNull ColumnFieldType type) {
    return switch (type) {
      case BOOLEAN   -> ColumnType.BOOLEAN;
      case NUMBER    -> ColumnType.NUMBER;
      case DATE      -> ColumnType.DATE;
      case DATE_TIME -> ColumnType.DATE_TIME;
      default        -> ColumnType.STRING;
    };
  }
}
