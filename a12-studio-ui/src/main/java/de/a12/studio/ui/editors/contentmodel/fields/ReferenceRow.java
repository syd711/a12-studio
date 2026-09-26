package de.a12.studio.ui.editors.contentmodel.fields;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentProps;
import de.a12.studio.ui.editors.contentmodel.ContentReferences;
import de.a12.studio.ui.util.StudioBundle;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A setting that refers to something of the Document Model the Content Model is bound to (SME's group and field
 * pickers): a combo box of what the element may reference at its position, shown by path, storing the id. Without a
 * Document Model there is nothing to pick from and the row says so. A stored id that is not among the choices (the
 * Document Model changed, or the reference is wrong) stays shown and selected, marked as not available - it is the
 * validators' job to report it, not the row's to drop it.
 */
public class ReferenceRow extends SettingRow {

  /** What the row lets the user pick. */
  public enum Kind {
    /** Any group below the element's data context. */
    GROUP,
    /** Only groups that can be added to, i.e. repeated ones. */
    REPEATED_GROUP,
    /** Only the attachment groups (the source of pictures), below the element's data context. */
    ATTACHMENT_GROUP,
    /** A field reachable from the element's data context. */
    FIELD,
    /** The Document Model element a form element shows, restricted to what its type can show. */
    FORM_ELEMENT
  }

  private final ComboBox<String> combo = new ComboBox<>();
  private final Map<String, String> labels = new HashMap<>();

  private Kind kind = Kind.FIELD;

  public ReferenceRow() {
    combo.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(combo, Priority.ALWAYS);
    combo.setConverter(new StringConverter<>() {
      @Override
      public String toString(String id) {
        return id == null ? "" : labels.getOrDefault(id, id);
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    });
    controls().getChildren().add(combo);
    combo.valueProperty().addListener((observable, oldValue, id) ->
        edited(props -> {
          if (id == null || id.isBlank()) {
            props.remove(getPath());
          }
          else {
            props.set(getPath(), id);
          }
        }));
  }

  public Kind getKind() {
    return kind;
  }

  public void setKind(Kind kind) {
    this.kind = kind;
  }

  @Override
  protected void load(@NonNull ContentProps props) {
    ContentElement element = props.getElement();
    ContentReferences references = context() != null ? context().references() : null;
    boolean bound = references != null && references.isBound();
    List<ContentReferences.Choice> choices = !bound ? List.of() : switch (kind) {
      case GROUP -> references.groups(element, false);
      case REPEATED_GROUP -> references.groups(element, true);
      case ATTACHMENT_GROUP -> references.attachmentGroups(element);
      case FIELD -> references.fields(element);
      case FORM_ELEMENT -> references.formElements(element);
    };
    String stored = props.getString(getPath());
    boolean hasStored = stored != null && !stored.isBlank();

    labels.clear();
    List<String> ids = new ArrayList<>();
    for (ContentReferences.Choice choice : choices) {
      labels.put(choice.id(), choice.label());
      ids.add(choice.id());
    }
    if (hasStored && !labels.containsKey(stored)) {
      // Stays visible and selected, marked, instead of silently disappearing from the row.
      labels.put(stored, StudioBundle.get("content_settings.reference_unavailable", bound ? references.labelOf(stored) : stored));
      ids.add(stored);
    }
    combo.getItems().setAll(ids);
    combo.setValue(hasStored ? stored : null);
    combo.setDisable(!bound && !hasStored);
    combo.setPromptText(StudioBundle.get(bound ? "content_settings.reference_select" : "content_settings.reference_needs_document_model"));
  }
}
