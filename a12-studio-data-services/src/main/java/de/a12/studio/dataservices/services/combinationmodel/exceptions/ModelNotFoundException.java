package de.a12.studio.dataservices.services.combinationmodel.exceptions;

import lombok.Getter;

@Getter
public class ModelNotFoundException extends RuntimeException {
  private final String id;
  private final String modelType;

  public ModelNotFoundException(String id, String modelType) {
    super(modelType + " with ID '" + id + "' was not found.");
    this.id = id;
    this.modelType = modelType;
  }

  public ModelNotFoundException(String id) {
    this(id, "Model");
  }
}
