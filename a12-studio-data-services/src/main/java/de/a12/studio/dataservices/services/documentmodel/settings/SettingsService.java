package de.a12.studio.dataservices.services.documentmodel.settings;

import com.mgmtp.a12.kernel.md.facade.a12internal.KernelUtils;
import de.a12.studio.dataservices.services.support.ProblemReporter;

import java.util.List;
import java.util.Locale;

public class SettingsService {

  public boolean validateSupportedCharacters(List<String> supportedCharacters) {
    ProblemReporter pr = new ProblemReporter();
    KernelUtils.validateCharset(supportedCharacters, Locale.US, pr);
    return !pr.hasProblemOccurred();
  }

  public List<String> simplifySupportedCharacters(List<String> supportedCharacters) {
    return KernelUtils.simplifyCharset(supportedCharacters, Locale.US, new ProblemReporter());
  }
}
