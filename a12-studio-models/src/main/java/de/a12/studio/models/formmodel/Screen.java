package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.models.Annotation;
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
  // SME's "annotated_mixin" - a plain "annotations" field on the wire, matching documentmodel.Element.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Annotation> annotations = new ArrayList<>();
  private List<ScreenElement> screenElements = new ArrayList<>();
}
