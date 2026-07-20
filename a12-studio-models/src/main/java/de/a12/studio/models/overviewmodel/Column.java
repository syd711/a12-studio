package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.models.Label;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Column {

  public static final String PIN_DIRECTION_LEFT = "LEFT";
  public static final String PIN_DIRECTION_RIGHT = "RIGHT";

  public static final String PREFERRED_SORTING_ASC = "ASC";
  public static final String PREFERRED_SORTING_DESC = "DESC";

  public static final String ATTACHMENT_DISPLAY_MODE_PREVIEW = "preview";
  public static final String ATTACHMENT_DISPLAY_MODE_ICON = "icon";
  public static final String ATTACHMENT_DISPLAY_MODE_FILE_NAME = "file_name";
  public static final String ATTACHMENT_DISPLAY_MODE_ICON_WITH_FILE_NAME = "icon_with_file_name";

  private String id;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> label = new ArrayList<>();
  private Double width;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean fixedWidth;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private ColumnAlignment alignment;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String pinDirection;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private Map<String, Object> styles = new LinkedHashMap<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String elementRef;
  private Boolean sortable;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String preferredSorting;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String attachmentDisplayMode;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> suffix = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<SummaryConfig> summary = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String name;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String expression;
}
