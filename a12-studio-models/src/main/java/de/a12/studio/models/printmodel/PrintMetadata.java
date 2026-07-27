package de.a12.studio.models.printmodel;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PrintMetadata extends PrintNode {

  private List<ComputationStep> titleComputation = new ArrayList<>();
  private List<ComputationStep> descriptionComputation = new ArrayList<>();
  private List<ComputationStep> languageComputation = new ArrayList<>();
  private List<ComputationStep> authorComputation = new ArrayList<>();
}
