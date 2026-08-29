package de.a12.studio.ui.updater;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdaterTest {

  @Test
  void isLargerVersionThan_detectsNewerPatch() {
    assertTrue(Updater.isLargerVersionThan("1.0.1", "1.0.0"));
    assertFalse(Updater.isLargerVersionThan("1.0.0", "1.0.1"));
  }

  @Test
  void isLargerVersionThan_detectsNewerMinorAndMajor() {
    assertTrue(Updater.isLargerVersionThan("1.1.0", "1.0.9"));
    assertTrue(Updater.isLargerVersionThan("2.0.0", "1.9.9"));
    assertFalse(Updater.isLargerVersionThan("1.0.0", "1.0.0"));
  }

  @Test
  void isLargerVersionThan_returnsFalseForNullInput() {
    assertFalse(Updater.isLargerVersionThan(null, "1.0.0"));
    assertFalse(Updater.isLargerVersionThan("1.0.0", null));
  }

  @Test
  void isLargerVersionThan_handlesRealStudioVersionScheme() {
    assertTrue(Updater.isLargerVersionThan("2606.06-ext0-0.0.2", "2606.06-ext0-0.0.1"));
    assertFalse(Updater.isLargerVersionThan("2606.06-ext0-0.0.1", "2606.06-ext0-0.0.2"));
    assertTrue(Updater.isLargerVersionThan("2606.07-ext0-0.0.1", "2606.06-ext0-0.0.9"));
    assertTrue(Updater.isLargerVersionThan("2606.06-ext1-0.0.1", "2606.06-ext0-0.0.1"));
    assertFalse(Updater.isLargerVersionThan("2606.06-ext0-0.0.1", "2606.06-ext0-0.0.1"));
  }

  @Test
  void isLargerVersionThan_handlesDifferingSegmentCounts() {
    assertTrue(Updater.isLargerVersionThan("1.0.0.1", "1.0.0"));
    assertFalse(Updater.isLargerVersionThan("1.0.0", "1.0.0.1"));
  }
}
