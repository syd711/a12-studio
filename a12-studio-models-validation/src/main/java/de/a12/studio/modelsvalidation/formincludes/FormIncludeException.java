package de.a12.studio.modelsvalidation.formincludes;

import de.a12.studio.modelsvalidation.ValidationMessages;

/**
 * An include cannot be expanded. The message is already localized ({@code validation.formInclude.*}) and names
 * the model/element/path that is at fault, so a dialog can show it as it is.
 */
public class FormIncludeException extends RuntimeException {

  public FormIncludeException(String messageKey, Object... args) {
    super(ValidationMessages.get("validation.formInclude." + messageKey, args));
  }
}
