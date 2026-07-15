package de.a12.studio.dataservices.services.documentmodel.features.migration;

import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelMigrator;
import com.mgmtp.a12.model.migration.MigrationResult;

import java.util.List;

public class DMMigrationService {

  public List<MigrationResult> migrate(List<ModelForMigration> models) {
    return new DocumentModelMigrator().migrate(models.stream().map(ModelForMigration::getContent).toList());
  }
}
