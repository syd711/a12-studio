package de.a12.studio.models.overviewmodel;

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

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Column> columns = new ArrayList<>();
  private RowActionGroup rowActionGroup;
  private ElementBox subHeaderBox;
  private ElementBox footerBox;
  private OverviewConfiguration configuration;
}
