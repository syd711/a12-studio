package de.a12.studio.kernel;

/** Gives the kernel the JSON of a model by id while it resolves includes; the id is the model's file name without {@code .json}. */
@FunctionalInterface
public interface KernelModelSource {

  /**
   * @param modelId id of the referenced model
   * @return the model's JSON content
   * @throws java.io.UncheckedIOException if the model cannot be read
   */
  String load(String modelId);
}
