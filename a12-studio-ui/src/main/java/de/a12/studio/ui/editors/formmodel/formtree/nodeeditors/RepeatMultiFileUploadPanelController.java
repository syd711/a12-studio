package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.Label;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.EmbeddedRepeat;
import de.a12.studio.models.formmodel.InlineRepeat;
import de.a12.studio.models.formmodel.MultiFileUploadOptions;
import de.a12.studio.models.formmodel.TextContainer;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.form.MultiFileUploadSupport;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * "Multi File Upload" property editor for a selected {@link InlineRepeat} or {@link EmbeddedRepeat}: the
 * {@code multiFileUpload} switch plus its {@link MultiFileUploadOptions} (download row action, and the
 * description / button text / helper text of the upload area). SME offers this on Inline and Embedded Repeats
 * only, so the panel hides itself for a Detached Repeat.
 * <p>
 * Per the SME docs the attachment group the upload writes into is "automatically set ... and cannot be
 * changed", and multi-file upload is only possible if the repeated group has exactly one non-repeatable
 * attachment group not nested in another repeatable group. So enabling picks that group ({@link
 * MultiFileUploadSupport}) and, if there isn't exactly one, refuses with an error naming the repeated group
 * instead of writing an options object that would point nowhere. Disabling only clears the switch and keeps
 * the options, so re-enabling doesn't lose the texts.
 * <p>
 * Not tied to a single {@code Element} (it edits the repeat form node), so it follows the model-header pattern:
 * a plain {@link #setRepeat} entry point and {@link #commitHeaderChange()}.
 */
public class RepeatMultiFileUploadPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private CheckBox enableCheckBox;
  @FXML
  private VBox optionsBox;
  @FXML
  private javafx.scene.control.Label attachmentGroupValueLabel;
  @FXML
  private CheckBox enableDownloadCheckBox;
  @FXML
  private LocalizedTextPanelController descriptionController;
  @FXML
  private CheckBox hideDescriptionCheckBox;
  @FXML
  private LocalizedTextPanelController buttonTextController;
  @FXML
  private CheckBox hideButtonTextCheckBox;
  @FXML
  private LocalizedTextPanelController helperTextController;

  private AbstractRepeat repeat;
  private @Nullable ElementIndex elementIndex;

  // Set while fields are repopulated from the model, so those programmatic changes aren't taken for user edits.
  private boolean populating;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    descriptionController.configureCustom("multiFileUploadDescription", StudioBundle.get("multi_file_upload_description"));
    buttonTextController.configureCustom("multiFileUploadButtonText", StudioBundle.get("multi_file_upload_button_text"));
    helperTextController.configureCustom("multiFileUploadHelperText", StudioBundle.get("multi_file_upload_helper_text"));

    enableCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!populating) {
        onEnableChanged(newValue);
      }
    });
    enableDownloadCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!populating) {
        getOrCreateOptions().setEnableDownload(newValue ? Boolean.TRUE : null);
        commitHeaderChange();
      }
    });
    hideDescriptionCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!populating) {
        getOrCreateOptions().setHideFileUploadDescription(newValue ? Boolean.TRUE : null);
        commitHeaderChange();
      }
    });
    hideButtonTextCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!populating) {
        getOrCreateOptions().setHideFileUploadButtonText(newValue ? Boolean.TRUE : null);
        commitHeaderChange();
      }
    });
  }

  public void setRepeat(@NonNull AbstractRepeat repeat, @Nullable ElementIndex elementIndex) {
    this.repeat = repeat;
    this.elementIndex = elementIndex;

    boolean supported = repeat instanceof InlineRepeat || repeat instanceof EmbeddedRepeat;
    setEditorVisible(supported);
    hideError();
    if (!supported) {
      return;
    }

    populating = true;
    try {
      MultiFileUploadOptions options = getOptions();
      enableCheckBox.setSelected(Boolean.TRUE.equals(getMultiFileUpload()));
      enableDownloadCheckBox.setSelected(options != null && Boolean.TRUE.equals(options.getEnableDownload()));
      hideDescriptionCheckBox.setSelected(options != null && Boolean.TRUE.equals(options.getHideFileUploadDescription()));
      hideButtonTextCheckBox.setSelected(options != null && Boolean.TRUE.equals(options.getHideFileUploadButtonText()));
      attachmentGroupValueLabel.setText(options != null ? displayName(options.getElementRef()) : "");
    }
    finally {
      populating = false;
    }
    updateOptionsVisibility();

    descriptionController.setCustom(() -> texts(MultiFileUploadOptions::getFileUploadDescription),
        () -> writableTexts(MultiFileUploadOptions::getFileUploadDescription, MultiFileUploadOptions::setFileUploadDescription));
    buttonTextController.setCustom(() -> texts(MultiFileUploadOptions::getFileUploadButtonText),
        () -> writableTexts(MultiFileUploadOptions::getFileUploadButtonText, MultiFileUploadOptions::setFileUploadButtonText));
    helperTextController.setCustom(() -> texts(MultiFileUploadOptions::getFileUploadHelperText),
        () -> writableTexts(MultiFileUploadOptions::getFileUploadHelperText, MultiFileUploadOptions::setFileUploadHelperText));
  }

  @Override
  public void setSaveMode(@NonNull PropertyEditorSaveMode saveMode) {
    super.setSaveMode(saveMode);
    descriptionController.setSaveMode(saveMode);
    buttonTextController.setSaveMode(saveMode);
    helperTextController.setSaveMode(saveMode);
  }

  @Override
  public void destroy() {
    super.destroy();
    descriptionController.destroy();
    buttonTextController.destroy();
    helperTextController.destroy();
  }

  private void onEnableChanged(boolean enabled) {
    if (!enabled) {
      hideError();
      setMultiFileUpload(null);
      updateOptionsVisibility();
      commitHeaderChange();
      return;
    }

    List<GroupElement> candidates = MultiFileUploadSupport.attachmentGroupCandidates(elementIndex, repeat.getGroupRef());
    if (candidates.size() != 1) {
      // Refuse instead of writing options that point at no (or an ambiguous) attachment group.
      populating = true;
      try {
        enableCheckBox.setSelected(false);
      }
      finally {
        populating = false;
      }
      showError("ERROR", StudioBundle.get("multi_file_upload_needs_single_attachment_group",
          repeat.getGroupRef() == null ? "" : displayName(repeat.getGroupRef()), candidates.size()));
      return;
    }

    hideError();
    MultiFileUploadOptions options = getOrCreateOptions();
    options.setElementRef(candidates.get(0).getId());
    attachmentGroupValueLabel.setText(displayName(options.getElementRef()));
    setMultiFileUpload(Boolean.TRUE);
    updateOptionsVisibility();
    commitHeaderChange();
  }

  private void updateOptionsVisibility() {
    boolean enabled = enableCheckBox.isSelected();
    optionsBox.setVisible(enabled);
    optionsBox.setManaged(enabled);
  }

  private String displayName(@Nullable String elementRef) {
    if (elementRef == null || elementRef.isBlank()) {
      return "";
    }
    if (elementIndex == null) {
      return elementRef;
    }
    String path = elementIndex.resolveDisplayPath(elementRef);
    return path != null ? path : elementRef;
  }

  private List<Label> texts(Function<MultiFileUploadOptions, TextContainer> getter) {
    MultiFileUploadOptions options = getOptions();
    TextContainer container = options != null ? getter.apply(options) : null;
    return container != null ? container.getText() : List.of();
  }

  private List<Label> writableTexts(Function<MultiFileUploadOptions, TextContainer> getter,
      BiConsumer<MultiFileUploadOptions, TextContainer> setter) {
    MultiFileUploadOptions options = getOrCreateOptions();
    TextContainer container = getter.apply(options);
    if (container == null) {
      container = new TextContainer();
      setter.accept(options, container);
    }
    return container.getText();
  }

  private @Nullable Boolean getMultiFileUpload() {
    return repeat instanceof InlineRepeat inline ? inline.getMultiFileUpload()
        : repeat instanceof EmbeddedRepeat embedded ? embedded.getMultiFileUpload() : null;
  }

  private void setMultiFileUpload(@Nullable Boolean value) {
    if (repeat instanceof InlineRepeat inline) {
      inline.setMultiFileUpload(value);
    }
    else if (repeat instanceof EmbeddedRepeat embedded) {
      embedded.setMultiFileUpload(value);
    }
  }

  private @Nullable MultiFileUploadOptions getOptions() {
    return repeat instanceof InlineRepeat inline ? inline.getMultiFileUploadOptions()
        : repeat instanceof EmbeddedRepeat embedded ? embedded.getMultiFileUploadOptions() : null;
  }

  private MultiFileUploadOptions getOrCreateOptions() {
    MultiFileUploadOptions options = getOptions();
    if (options == null) {
      options = new MultiFileUploadOptions();
      GroupElement single = MultiFileUploadSupport.singleAttachmentGroup(elementIndex, repeat.getGroupRef());
      options.setElementRef(single != null ? single.getId() : null);
      if (repeat instanceof InlineRepeat inline) {
        inline.setMultiFileUploadOptions(options);
      }
      else if (repeat instanceof EmbeddedRepeat embedded) {
        embedded.setMultiFileUploadOptions(options);
      }
    }
    return options;
  }
}
