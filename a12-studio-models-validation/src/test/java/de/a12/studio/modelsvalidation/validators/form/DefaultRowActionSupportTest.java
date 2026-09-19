package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.Label;
import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.DefaultRowAction;
import de.a12.studio.models.formmodel.DetachedRepeat;
import de.a12.studio.models.formmodel.EmbeddedRepeat;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.InlineRepeat;
import de.a12.studio.models.formmodel.RowAction;
import de.a12.studio.models.formmodel.RowActionGroup;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.TextContainer;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultRowActionSupportTest {

  private static RowAction action(String event) {
    RowAction action = new RowAction();
    action.setEvent(event);
    return action;
  }

  private static void withConfirmation(RowAction action, String text) {
    Label label = new Label();
    label.setLocale("en");
    label.setText(text);
    TextContainer container = new TextContainer();
    container.getText().add(label);
    action.setConfirmation(container);
  }

  private static <T extends AbstractRepeat> T repeat(T repeat, String... events) {
    repeat.setId("repeat1");
    repeat.setName("Items");
    RowActionGroup group = new RowActionGroup();
    for (String event : events) {
      group.getAction().add(action(event));
    }
    repeat.setRowActionGroup(group);
    return repeat;
  }

  private static DefaultRowAction custom(String event) {
    return DefaultRowActionSupport.fromTechnicalEvent(event, null);
  }

  @Test
  void convertsBetweenWireShapeAndTechnicalEvent() {
    DefaultRowAction builtIn = DefaultRowActionSupport.fromTechnicalEvent(DefaultRowActionSupport.EDIT, Boolean.TRUE);
    assertEquals("edit", builtIn.getEvent());
    assertNull(builtIn.getCustom(), "a built-in action is not marked custom on the wire");
    assertEquals(Boolean.TRUE, builtIn.getHideButton());
    assertEquals(DefaultRowActionSupport.EDIT, DefaultRowActionSupport.technicalEvent(builtIn));

    DefaultRowAction custom = DefaultRowActionSupport.fromTechnicalEvent("event_remove", null);
    assertEquals("event_remove", custom.getEvent());
    assertEquals(Boolean.TRUE, custom.getCustom());
    assertEquals("event_remove", DefaultRowActionSupport.technicalEvent(custom));

    assertNull(DefaultRowActionSupport.fromTechnicalEvent(null, Boolean.TRUE));
    assertNull(DefaultRowActionSupport.fromTechnicalEvent(" ", null));
    assertNull(DefaultRowActionSupport.technicalEvent(null));
  }

  @Test
  void onlyDetachedAndEmbeddedRepeatsSupportADefaultRowAction() {
    assertTrue(DefaultRowActionSupport.isSupported(new DetachedRepeat()));
    assertTrue(DefaultRowActionSupport.isSupported(new EmbeddedRepeat()));
    assertFalse(DefaultRowActionSupport.isSupported(new InlineRepeat()));
  }

  @Test
  void candidatesAreEditDownloadAndUnconfirmedCustomEvents() {
    EmbeddedRepeat embedded = repeat(new EmbeddedRepeat(), "event_a", "event_b", "event_a", "event_confirmed");
    withConfirmation(embedded.getRowActionGroup().getAction().get(3), "Sure?");

    assertEquals(List.of(DefaultRowActionSupport.EDIT, "event_a", "event_b"), DefaultRowActionSupport.candidates(embedded));

    embedded.setMultiFileUpload(true);
    assertEquals(List.of(DefaultRowActionSupport.EDIT, DefaultRowActionSupport.DOWNLOAD, "event_a", "event_b"),
        DefaultRowActionSupport.candidates(embedded));

    assertEquals(List.of(DefaultRowActionSupport.EDIT), DefaultRowActionSupport.candidates(new DetachedRepeat()));
  }

  @Test
  void aConfirmationWithoutAnyTextDoesNotCount() {
    DetachedRepeat detached = repeat(new DetachedRepeat(), "event_a");
    withConfirmation(detached.getRowActionGroup().getAction().get(0), "  ");

    assertTrue(DefaultRowActionSupport.candidates(detached).contains("event_a"));
  }

  @Test
  void deletingTheDefaultRowActionClearsTheDefault() {
    DetachedRepeat detached = repeat(new DetachedRepeat(), "event_a", "event_b");
    detached.setDefaultRowAction(custom("event_a"));
    List<String> before = DefaultRowActionSupport.events(detached);

    detached.getRowActionGroup().getAction().remove(0);

    assertTrue(DefaultRowActionSupport.reconcile(detached, before));
    assertNull(detached.getDefaultRowAction());
  }

  @Test
  void deletingOneOfTwoActionsWithTheSameEventKeepsTheDefault() {
    DetachedRepeat detached = repeat(new DetachedRepeat(), "event_a", "event_a");
    detached.setDefaultRowAction(custom("event_a"));
    List<String> before = DefaultRowActionSupport.events(detached);

    detached.getRowActionGroup().getAction().remove(0);

    assertFalse(DefaultRowActionSupport.reconcile(detached, before));
    assertEquals("event_a", detached.getDefaultRowAction().getEvent());
  }

  @Test
  void renamingTheDefaultRowActionFollowsIt() {
    DetachedRepeat detached = repeat(new DetachedRepeat(), "event_a", "event_b");
    DefaultRowAction defaultAction = custom("event_b");
    defaultAction.setHideButton(true);
    detached.setDefaultRowAction(defaultAction);
    List<String> before = DefaultRowActionSupport.events(detached);

    detached.getRowActionGroup().getAction().get(1).setEvent("event_renamed");

    assertTrue(DefaultRowActionSupport.reconcile(detached, before));
    assertEquals("event_renamed", detached.getDefaultRowAction().getEvent());
    assertEquals(Boolean.TRUE, detached.getDefaultRowAction().getHideButton(), "renaming keeps the other settings");
  }

  @Test
  void addingAConfirmationToTheDefaultRowActionClearsTheDefault() {
    DetachedRepeat detached = repeat(new DetachedRepeat(), "event_a");
    detached.setDefaultRowAction(custom("event_a"));
    List<String> before = DefaultRowActionSupport.events(detached);

    withConfirmation(detached.getRowActionGroup().getAction().get(0), "Really?");

    assertTrue(DefaultRowActionSupport.reconcile(detached, before));
    assertNull(detached.getDefaultRowAction());
  }

  @Test
  void builtInAndUnrelatedChangesLeaveTheDefaultAlone() {
    DetachedRepeat builtIn = repeat(new DetachedRepeat(), "event_a");
    builtIn.setDefaultRowAction(DefaultRowActionSupport.fromTechnicalEvent(DefaultRowActionSupport.EDIT, null));
    List<String> before = DefaultRowActionSupport.events(builtIn);
    builtIn.getRowActionGroup().getAction().clear();
    assertFalse(DefaultRowActionSupport.reconcile(builtIn, before));
    assertEquals("edit", builtIn.getDefaultRowAction().getEvent());

    DetachedRepeat custom = repeat(new DetachedRepeat(), "event_a", "event_b");
    custom.setDefaultRowAction(custom("event_a"));
    List<String> customBefore = DefaultRowActionSupport.events(custom);
    custom.getRowActionGroup().getAction().add(action("event_c"));
    assertFalse(DefaultRowActionSupport.reconcile(custom, customBefore));
    assertEquals("event_a", custom.getDefaultRowAction().getEvent());

    assertFalse(DefaultRowActionSupport.reconcile(new DetachedRepeat(), List.of()), "no default row action");
  }

  @Test
  void disablingMultiFileUploadClearsADownloadDefault() {
    EmbeddedRepeat embedded = repeat(new EmbeddedRepeat(), "event_a");
    embedded.setMultiFileUpload(true);
    embedded.setDefaultRowAction(DefaultRowActionSupport.fromTechnicalEvent(DefaultRowActionSupport.DOWNLOAD, null));

    embedded.setMultiFileUpload(null);
    assertTrue(DefaultRowActionSupport.onMultiFileUploadDisabled(embedded));
    assertNull(embedded.getDefaultRowAction());

    embedded.setDefaultRowAction(DefaultRowActionSupport.fromTechnicalEvent(DefaultRowActionSupport.EDIT, null));
    assertFalse(DefaultRowActionSupport.onMultiFileUploadDisabled(embedded));
    assertEquals("edit", embedded.getDefaultRowAction().getEvent());
  }

  // ---- validator ----

  private static List<ModelValidationError> validate(AbstractRepeat... repeats) {
    FormModel model = new FormModel();
    model.setId("Default_FM");
    model.setContent(new FormModelContent());
    Screen screen = new Screen();
    screen.setId("screen1");
    for (AbstractRepeat repeat : repeats) {
      screen.getScreenElements().add(repeat);
    }
    model.getContent().getScreens().add(screen);
    return new FormDefaultRowActionValidator().validate(model, TestModels.context(model));
  }

  @Test
  void validatorAcceptsEveryCandidate() {
    DetachedRepeat detached = repeat(new DetachedRepeat(), "event_a");
    detached.setDefaultRowAction(DefaultRowActionSupport.fromTechnicalEvent(DefaultRowActionSupport.EDIT, null));
    EmbeddedRepeat embedded = repeat(new EmbeddedRepeat(), "event_a");
    embedded.setId("repeat2");
    embedded.setMultiFileUpload(true);
    embedded.setDefaultRowAction(DefaultRowActionSupport.fromTechnicalEvent(DefaultRowActionSupport.DOWNLOAD, null));
    DetachedRepeat custom = repeat(new DetachedRepeat(), "event_a");
    custom.setId("repeat3");
    custom.setDefaultRowAction(custom("event_a"));

    assertEquals(List.of(), validate(detached, embedded, custom));
    assertEquals(List.of(), validate(repeat(new DetachedRepeat())), "no default row action");
  }

  @Test
  void validatorReportsEachWayADefaultCanBeInvalid() {
    DetachedRepeat download = repeat(new DetachedRepeat());
    download.setId("download");
    download.setDefaultRowAction(DefaultRowActionSupport.fromTechnicalEvent(DefaultRowActionSupport.DOWNLOAD, null));
    DetachedRepeat unknown = repeat(new DetachedRepeat());
    unknown.setId("unknown");
    DefaultRowAction unknownAction = new DefaultRowAction();
    unknownAction.setEvent("frobnicate");
    unknown.setDefaultRowAction(unknownAction);
    DetachedRepeat missing = repeat(new DetachedRepeat(), "event_a");
    missing.setId("missing");
    missing.setDefaultRowAction(custom("event_gone"));
    DetachedRepeat confirmed = repeat(new DetachedRepeat(), "event_a");
    confirmed.setId("confirmed");
    withConfirmation(confirmed.getRowActionGroup().getAction().get(0), "Sure?");
    confirmed.setDefaultRowAction(custom("event_a"));

    List<ModelValidationError> errors = validate(download, unknown, missing, confirmed);

    assertEquals(List.of("download", "unknown", "missing", "confirmed"), errors.stream().map(ModelValidationError::elementId).toList());
    assertTrue(errors.get(0).message().contains("multi file upload"), errors.get(0).message());
    assertTrue(errors.get(1).message().contains("frobnicate"), errors.get(1).message());
    assertTrue(errors.get(2).message().contains("event_gone"), errors.get(2).message());
    assertTrue(errors.get(3).message().contains("confirmation"), errors.get(3).message());
    assertTrue(errors.stream().allMatch(error -> error.message().contains("Items")), "the repeat is named in every message");
  }
}
