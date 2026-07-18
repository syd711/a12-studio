package de.a12.studio.dataservices.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class OverviewModelContent {

  private OverviewConfiguration configuration;
  private ElementBox subHeaderBox;
  private ElementBox footerBox;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Column> columns = new ArrayList<>();
  private RowActionGroup rowActionGroup;
}
