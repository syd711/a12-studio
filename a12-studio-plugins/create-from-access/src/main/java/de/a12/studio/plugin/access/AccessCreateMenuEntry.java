package de.a12.studio.plugin.access;

import de.a12.studio.models.NewModelFactory;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.plugin.manager.ICreateItemMenuEntry;
import de.a12.studio.ui.util.DocumentModelBuilder;
import de.a12.studio.ui.util.DocumentModelBuilder.ColumnDescriptor;
import de.a12.studio.ui.util.DocumentModelBuilder.ColumnType;
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
 * {@link ICreateItemMenuEntry} implementation that shows the "Create from Access Database"
 * import dialog and creates a Document Model from the selected Access table.
 *
 * <p>This class is instantiated reflectively by {@code PluginManager} via the
 * {@code plugin.json} extension point registration.
 */
@Slf4j
public class AccessCreateMenuEntry implements ICreateItemMenuEntry {

  @Override
  @NonNull
  public String getMenuLabel() {
    return "Create from Access Database...";
  }

  @Override
  @Nullable
  public Node getMenuGraphic() {
    return WidgetFactory.createIcon("mdi2d-database-import-outline");
  }

  @Override
  public void execute(@NonNull Stage owner, @NonNull ProjectItem targetFolder) {
    Optional<ImportFromAccessDialogController.AccessImportInput> input =
        ImportFromAccessDialogController.show(owner, targetFolder);
    if (input.isEmpty()) {
      return;
    }

    ImportFromAccessDialogController.AccessImportInput data = input.get();
    List<ColumnDescriptor> columns = data.columns().stream()
        .map(c -> new ColumnDescriptor(c.name(), toColumnType(c.fieldType())))
        .toList();

    try {
      var model = DocumentModelBuilder.build(targetFolder, data.modelName(), data.tableName(), columns);
      NewModelFactory.createModelFromExisting(targetFolder, model, data.modelName());
    }
    catch (IOException e) {
      log.error("Failed to create document model from Access table '{}': {}",
          data.tableName(), e.getMessage(), e);
      WidgetFactory.showAlert(owner, "Error", e.getMessage());
    }
  }

  private static ColumnType toColumnType(AccessImportService.@NonNull ColumnFieldType type) {
    return switch (type) {
      case BOOLEAN   -> ColumnType.BOOLEAN;
      case NUMBER    -> ColumnType.NUMBER;
      case DATE      -> ColumnType.DATE;
      case DATE_TIME -> ColumnType.DATE_TIME;
      default        -> ColumnType.STRING;
    };
  }
}
