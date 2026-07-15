package de.a12.studio.dataservices.services.combinationmodel.exceptions;

public class CombinationModelNotFoundException extends ModelNotFoundException {
  public CombinationModelNotFoundException(String id) {
    super(id, "Combination Model");
  }
}
