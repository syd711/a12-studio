package de.a12.studio.ui.util;

/**
 *
 */
public enum ReturnCodes {
  OK, ERROR;

  @Override
  public String toString() {
    return super.toString();
  }

  public boolean isOk() {
    return this.equals(OK);
  }
}
