package de.a12.studio.dataservices.services.documentmodel.features.migration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModelForMigration {
  private String content;
  private String explorerPath;

  public ModelForMigration(String content, String explorerPath) {
    this.content = content;
    this.explorerPath = explorerPath;
  }
}
