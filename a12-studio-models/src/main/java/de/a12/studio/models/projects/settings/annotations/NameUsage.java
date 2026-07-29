package de.a12.studio.models.projects.settings.annotations;

/**
 * How many elements currently use a given annotation name, and the value most recently seen
 * alongside it (used to prefill the value field when the name is picked from suggestions).
 */
public class NameUsage {
  private int count = 1;
  private String value;

  public NameUsage() {
  }

  public NameUsage(String value) {
    this.value = value;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public void incrementCount() {
    count++;
  }

  public int decrementCount() {
    return --count;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
