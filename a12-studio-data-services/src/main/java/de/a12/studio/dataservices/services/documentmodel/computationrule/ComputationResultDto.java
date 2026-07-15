package de.a12.studio.dataservices.services.documentmodel.computationrule;

import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import lombok.Value;

import java.util.List;

@Value
public class ComputationResultDto {
  List<String> semanticErrors;
  List<ComputationValidationResult> errors;
  List<Computation.ComputationAlternative> expanded;
}
