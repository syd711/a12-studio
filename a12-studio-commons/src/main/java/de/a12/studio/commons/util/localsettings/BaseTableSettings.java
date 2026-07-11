package de.a12.studio.commons.util.localsettings;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseTableSettings extends LocalJsonSettings {

  private Map<String, Double> columnWith = new HashMap<>();

  private List<String> columnOrder = new ArrayList<>();

  private Map<String, Double> dividerPositions = new HashMap<>();

  public List<String> getColumnOrder() {
    return columnOrder;
  }

  public void setColumnOrder(List<String> columnOrder) {
    this.columnOrder = columnOrder;
  }

  public Map<String, Double> getColumnWith() {
    return columnWith;
  }

  public void setColumnWith(Map<String, Double> columnWith) {
    this.columnWith = columnWith;
  }

  public double getColumnWidth(@NonNull String key) {
    if (columnWith.containsKey(key)) {
      return columnWith.get(key);
    }
    return 0;
  }

  public Map<String, Double> getDividerPositions() {
    return dividerPositions;
  }

  public void setDividerPositions(Map<String, Double> dividerPositions) {
    this.dividerPositions = dividerPositions;
  }

  public double getDividerPosition(@NonNull String key) {
    if (dividerPositions.containsKey(key)) {
      return dividerPositions.get(key);
    }
    return -1;
  }
}
