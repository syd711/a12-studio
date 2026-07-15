package de.a12.studio.dataservices.services.documentmodel.features.expansion;

import de.a12.studio.dataservices.services.support.TypeDefinitionInfo;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
public class TypeDefInfo {
  private final Map<String, TypeDefinitionInfo> includedTDs;
  private final Map<String, TypeDefinitionInfo> importedTDs;
  private final Map<String, TypeDefinitionInfo> includedImportedTDs;
  private final List<String> overwrittenTDs;
  private final List<String> referenceTDs;

  public TypeDefInfo(
      Map<String, TypeDefinitionInfo> includedTDs,
      Map<String, TypeDefinitionInfo> importedTDs,
      Map<String, TypeDefinitionInfo> includedImportedTDs,
      List<String> overwrittenTDs,
      List<String> referenceTDs) {
    this.includedTDs = includedTDs;
    this.importedTDs = importedTDs;
    this.includedImportedTDs = includedImportedTDs;
    this.overwrittenTDs = overwrittenTDs;
    this.referenceTDs = referenceTDs;
  }

  public TypeDefInfo(
      Map<String, TypeDefinitionInfo> includedTDs,
      Map<String, TypeDefinitionInfo> importedTDs,
      Map<String, TypeDefinitionInfo> includedImportedTDs) {
    this(includedTDs, importedTDs, includedImportedTDs, Collections.emptyList(), Collections.emptyList());
  }
}
