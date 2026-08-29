package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.a12.studio.models.Annotation;
import de.a12.studio.models.EventButtonLike;
import de.a12.studio.models.Label;

import java.util.List;

// Structural shape shared by overviewmodel.Button and overviewmodel.ButtonElement - the fields
// EventButtonDialogController edits beyond EventButtonsPanelController's summary row (Event/Priority/
// Destructive/Icon name, see EventButtonLike): Confirmation, the button's own Label/Description, Styles,
// Annotations and labelHidden. Mirrors SME's AnnotatedButton (client/src/modules/common/types/index.ts).
public interface OverviewButtonLike extends EventButtonLike {

  Confirmation getConfirmation();

  void setConfirmation(Confirmation confirmation);

  Icon getIcon();

  void setIcon(Icon icon);

  List<Label> getLabel();

  void setLabel(List<Label> label);

  List<Label> getDescription();

  void setDescription(List<Label> description);

  Boolean getLabelHidden();

  void setLabelHidden(Boolean labelHidden);

  List<String> getStyles();

  void setStyles(List<String> styles);

  List<Annotation> getAnnotations();

  void setAnnotations(List<Annotation> annotations);

  @JsonIgnore
  default Confirmation getOrCreateConfirmation() {
    if (getConfirmation() == null) {
      setConfirmation(new Confirmation());
    }
    return getConfirmation();
  }
}
