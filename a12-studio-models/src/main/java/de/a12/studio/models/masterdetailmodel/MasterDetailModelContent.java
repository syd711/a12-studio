package de.a12.studio.models.masterdetailmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
  private Integer formWidth;
  private List<FormMapping> formMapping = new ArrayList<>();
}
