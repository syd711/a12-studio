package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.models.contentmodel.ContentElement;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * What an element of the Content Model may reference in the Document Model the model is bound to: the choices the
 * property column's pickers offer. They are exactly what the validators accept (see {@code DocumentStructure}), taking
 * the data context of the element into account, i.e. which Repeatable Group or base group it is relative to.
 */
public interface ContentReferences {

  /** One thing that can be picked: the id that is stored, and how it is shown (its path in the Document Model). */
  record Choice(String id, String label) {
  }

  /** How a condition can compare a field: what kind of value it takes. */
  enum ValueKind {
    /** Yes, No or no data. */
    BOOLEAN,
    /** Yes or no data. */
    CONFIRM,
    /** One of {@link FieldInfo#values()}. */
    ENUMERATION,
    NUMBER,
    TEXT,
    /** Any other type (dates, ...): the value is entered as text and stored as it is. */
    OTHER
  }

  /** A field's kind of value and, for an enumeration, its values. */
  record FieldInfo(ValueKind kind, List<String> values) {
  }

  /** Whether the Content Model is bound to a Document Model that exists, i.e. whether there is anything to pick from. */
  boolean isBound();

  /**
   * The groups {@code element} may reference; only the ones that can be added to (repeated) when {@code repeatedOnly}.
   * An event node of an element's click setting takes the element that holds it.
   */
  List<Choice> groups(ContentElement element, boolean repeatedOnly);

  /** The attachment groups {@code element} may use as a dynamic source, e.g. the picture of an Image. */
  List<Choice> attachmentGroups(ContentElement element);

  /**
   * The groups whose index a text of {@code element} may show (SME's {@code IndexOf}): the group the element is relative
   * to and the repeatable groups above it.
   */
  List<Choice> indexGroups(ContentElement element);

  /** The fields {@code element} may reference. */
  List<Choice> fields(ContentElement element);

  /**
   * Every field of the Document Model that can be listed by an element that only reports about them (the Message Group
   * Container), including those inside repeated groups.
   */
  List<Choice> listableFields(ContentElement element);

  /** Every group that can be listed by an element that only reports about them, see {@link #listableFields}. */
  List<Choice> listableGroups(ContentElement element);

  /** The fields and groups a Message Group Container collects by itself from the form elements inside it. */
  record Collected(List<Choice> fields, List<Choice> groups) {
  }

  /**
   * What {@code container} collects when it collects automatically: the fields and groups of the form elements below it,
   * except those inside a nested Message Group Container or Display.
   */
  Collected autoCollected(ContentElement container);

  /** The Document Model element of a date field: whether {@code id} is a field of a date type (date, date-time, range). */
  boolean isDateField(String id);

  /** The Document Model elements the form element {@code element} can show at its position. */
  List<Choice> formElements(ContentElement element);

  /** What a condition on the field {@code id} compares, or {@code null} if there is no such field. */
  @Nullable FieldInfo fieldInfo(String id);

  /** The path of {@code id} in the Document Model, or {@code id} itself if it is not there. */
  String labelOf(String id);
}
