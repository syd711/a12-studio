package de.a12.studio.models.typesettingmodel;

/** Constants shared by the model factory, the editor and the validators. */
public final class TypesettingModelDefaults {

  /** SME's {@code DEFAULT_LINE_VALUE}: the orphan and widow limit of a newly created model. */
  public static final int LINE_LIMIT = 2;

  /** The meta-model's {@code minValue}/{@code maxValue} for {@code orphan} and {@code widow}. */
  public static final int MIN_LINE_LIMIT = 0;
  public static final int MAX_LINE_LIMIT = 10;

  private TypesettingModelDefaults() {
  }
}
