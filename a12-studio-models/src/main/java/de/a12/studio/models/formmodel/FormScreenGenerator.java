package de.a12.studio.models.formmodel;

import de.a12.studio.models.Label;
import de.a12.studio.models.Locale;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates an initial screen tree for a new Form Model from its bound Document Model's fields - the
 * "Build Screens from Fields" option in the New Model dialog. Mirrors SME's equivalent new-form-model
 * option (client/src/modules/formModel/add/view.tsx's {@code buildScreensFromFields}), whose actual
 * generation is delegated there to the proprietary {@code @com.mgmtp.a12.formengine/form-model-generator}
 * kernel package - not available as source, so the rules below were reverse-engineered from that
 * package's golden test fixture (client/test/modules/formModel/integration/
 * fmFrameDataProvider.integration.test.ts, resources/test/modules/formModel/documentModelReference/
 * createdFormModelWithScreensVerify.json): one root Group becomes one Screen; a Group's direct Fields
 * become a single ControlGrid of Row+Control pairs, in declaration order; a nested Group is recursively
 * turned into a Section; Rule/Computation elements are skipped. Id/name formatting instead follows this
 * project's own generated-fixture convention (see e.g. testing/workspaces/basic/models/Invoice_FM.json's
 * ControlGrid "BillingAddressControls" for group "BillingAddress") rather than SME's internal id scheme.
 * Screen-to-screen navigation is intentionally left unwired - Invoice_FM.json's own multi-screen
 * subHeaderBox.majorButtons is empty, so there's no evidence the generator is expected to produce it.
 */
public final class FormScreenGenerator {

  private FormScreenGenerator() {
  }

  public static void generate(FormModelContent content, DocumentModel documentModel, List<Locale> locales) {
    List<GroupElement> rootGroups = documentModel.getContent().getModelRoot().getRootGroups();
    List<Screen> screens = new ArrayList<>();
    for (GroupElement group : rootGroups) {
      screens.add(buildScreen(group, locales));
    }
    content.setScreens(screens);
  }

  private static Screen buildScreen(GroupElement group, List<Locale> locales) {
    Screen screen = new Screen();
    screen.setId("screen-" + shortId());
    screen.setName(group.getName());
    screen.setTitle(repeatedText(group.getName(), locales));
    screen.setScreenElements(buildScreenElements(group, locales));
    return screen;
  }

  private static List<ScreenElement> buildScreenElements(GroupElement group, List<Locale> locales) {
    List<ScreenElement> result = new ArrayList<>();
    GroupConfig config = group.getGroup();
    if (config == null) {
      return result;
    }

    List<Row> rows = new ArrayList<>();
    for (Element element : config.getElements()) {
      if (element instanceof FieldElement field) {
        rows.add(buildRow(field));
      }
    }
    if (!rows.isEmpty()) {
      ControlGrid grid = new ControlGrid();
      grid.setId("controlgrid-" + shortId());
      grid.setName(group.getName() + "Controls");
      grid.setRow(rows);
      result.add(grid);
    }

    for (Element element : config.getElements()) {
      if (element instanceof GroupElement childGroup) {
        result.add(buildSection(childGroup, locales));
      }
    }
    return result;
  }

  private static Section buildSection(GroupElement group, List<Locale> locales) {
    Section section = new Section();
    section.setId("section-" + shortId());
    section.setName(group.getName());
    section.setTitle(repeatedText(group.getName(), locales));
    section.setScreenElements(buildScreenElements(group, locales));
    return section;
  }

  private static Row buildRow(FieldElement field) {
    Row row = new Row();
    row.setId("row-" + shortId());
    Control control = new Control();
    control.setId("control_" + shortId());
    control.setElementRef(field.getId());
    row.setCell(new ArrayList<>(List.of(control)));
    return row;
  }

  private static LocalizedText repeatedText(String value, List<Locale> locales) {
    List<Label> labels = new ArrayList<>();
    for (Locale locale : locales) {
      Label label = new Label();
      label.setLocale(locale.getCode());
      label.setText(value);
      labels.add(label);
    }
    MultilingualText text = new MultilingualText();
    TextContainer container = new TextContainer();
    container.setText(labels);
    text.setMultilingualText(container);
    return text;
  }

  // Matches the short-hex id suffix convention used throughout this project's own generated fixtures
  // (e.g. Invoice_FM.json's "screen-9ecf7", "controlgrid-95f39").
  private static String shortId() {
    String alphabet = "0123456789abcdef";
    StringBuilder id = new StringBuilder(5);
    ThreadLocalRandom random = ThreadLocalRandom.current();
    for (int i = 0; i < 5; i++) {
      id.append(alphabet.charAt(random.nextInt(alphabet.length())));
    }
    return id.toString();
  }
}
