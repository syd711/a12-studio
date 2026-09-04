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

  private OverviewConfiguration configuration = new OverviewConfiguration();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Column> columns = new ArrayList<>();
  private RowActionGroup rowActionGroup;
  private ElementBox subHeaderBox;
  private ElementBox footerBox;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private RowAction defaultRowAction;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private ContextMenu contextMenu;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<String> styles = new ArrayList<>();
}
