package de.a12.studio.models.masterdetailmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class MasterDetailModelContent {

  private String type;
  private String overviewModel;
  // Unlike overviewModel (kept unconditionally for on-disk compatibility with existing "overview"-type
  // files that already serialize it as null), this is a newly introduced field: omitting it when unset
  // keeps every pre-existing master-detail model file byte-for-byte unchanged on save.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String treeModel;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer formWidth;
  private List<FormMapping> formMapping = new ArrayList<>();
  // Tree-type only (mirrors SME's MasterDetailTree.relationshipEditors/linkDocumentEditors): null for
  // "overview"-type files so they stay byte-for-byte unchanged, an empty/populated list once the type is
  // "tree" (SME serializes an explicit "[]" rather than omitting the key in that case).
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<FormMapping> relationshipEditors;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<FormMapping> linkDocumentEditors;
}
