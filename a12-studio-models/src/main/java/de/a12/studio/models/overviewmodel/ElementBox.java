package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ElementBox {

  // Legacy files use majorElements/minorElements (renamed to rightSlot/leftSlot in A12) and omit the slot keys,
  // so each slot is only written when non-empty or explicit on load.
  @JsonIgnore
  private List<BoxElement> leftSlot = new ArrayList<>();

  @JsonIgnore
  private boolean leftSlotExplicit;

  @JsonProperty("leftSlot")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<BoxElement> getLeftSlotForJson() {
    return leftSlotExplicit || !leftSlot.isEmpty() ? leftSlot : null;
  }

  @JsonProperty("leftSlot")
  private void setLeftSlotForJson(List<BoxElement> value) {
    leftSlotExplicit = value != null;
    leftSlot = value != null ? value : new ArrayList<>();
  }

  @JsonIgnore
  private List<BoxElement> rightSlot = new ArrayList<>();

  @JsonIgnore
  private boolean rightSlotExplicit;

  @JsonProperty("rightSlot")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<BoxElement> getRightSlotForJson() {
    return rightSlotExplicit || !rightSlot.isEmpty() ? rightSlot : null;
  }

  @JsonProperty("rightSlot")
  private void setRightSlotForJson(List<BoxElement> value) {
    rightSlotExplicit = value != null;
    rightSlot = value != null ? value : new ArrayList<>();
  }

  /** A new box as created in the editor: both slots are written, even while empty. */
  public static ElementBox createEmpty() {
    ElementBox box = new ElementBox();
    box.leftSlotExplicit = true;
    box.rightSlotExplicit = true;
    return box;
  }

  // Legacy (pre-rename) slots; null when absent so they are only written back for files that use them.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<BoxElement> majorElements;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<BoxElement> minorElements;
}
