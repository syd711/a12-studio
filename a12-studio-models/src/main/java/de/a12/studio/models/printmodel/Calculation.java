package de.a12.studio.models.printmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Calculation extends PrintNode {

  private String model;
  private String name;
  private List<ComputationStep> computationAlternatives = new ArrayList<>();

  // Only required when the computation does not return a String.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String resultType;
}
