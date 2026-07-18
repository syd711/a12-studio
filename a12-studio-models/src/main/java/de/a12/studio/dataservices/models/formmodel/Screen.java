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
public class Screen {

  private String id;
  private String name;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private LocalizedText title;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private HeaderFooterBox subHeaderBox;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private HeaderFooterBox footerBox;
  private List<ScreenElement> screenElements = new ArrayList<>();
}
