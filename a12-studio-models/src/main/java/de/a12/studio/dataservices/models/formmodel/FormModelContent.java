package de.a12.studio.dataservices.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class FormModelContent {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private AmountSuffix amountSuffix;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Style> styles = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private HeaderFooterBox subHeaderBox;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private HeaderFooterBox footerBox;
  private List<Screen> screens = new ArrayList<>();
  private FieldConfiguration fieldConfiguration;
  private GroupConfiguration groupConfiguration;
  private Defaults defaults;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String readonlyPresentation;
}
