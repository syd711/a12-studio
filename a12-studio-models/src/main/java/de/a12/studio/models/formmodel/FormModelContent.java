package de.a12.studio.models.formmodel;

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
  private LocalizedText subtitle;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private HeaderFooterBox subHeaderBox;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private HeaderFooterBox footerBox;
  private List<Screen> screens = new ArrayList<>();
  private FieldConfiguration fieldConfiguration = new FieldConfiguration();
  private GroupConfiguration groupConfiguration = new GroupConfiguration();
  private Defaults defaults = new Defaults();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String readonlyPresentation;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String inlineRepeatReadonlyPresentation;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String markingOfRequiredFields;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String detachedRepeatCommitButtonEnablement;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String disableRuleConfirmation;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean hideConfirmationSummary;
}
