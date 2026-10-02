package de.a12.studio.kernel;

import java.util.Locale;
import java.util.Map;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;

/** The minimal {@link IRule} the kernel needs to format a condition text (SME builds the same). */
record FormattableRule(String fullName, IEntity errorEntity, String conditionText) implements IRule {

  @Override
  public String getFullName() {
    return fullName;
  }

  @Override
  public String getErrorConditionText() {
    return conditionText;
  }

  @Override
  public String getErrorMessage(Locale loc) {
    return "";
  }

  @Override
  public Map<Locale, String> getErrorMessages() {
    return Map.of();
  }

  @Override
  public IEntity getErrorEntity() {
    return errorEntity;
  }

  @Override
  public String getErrorCode() {
    return "";
  }

  @Override
  public RuleSeverityType getSeverityType() {
    return RuleSeverityType.ERROR;
  }
}
