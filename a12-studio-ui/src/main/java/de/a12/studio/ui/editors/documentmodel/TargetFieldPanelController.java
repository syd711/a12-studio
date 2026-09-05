package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.models.documentmodel.ComputationElement;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.RuleElement;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.ElementProperty;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Picks a target {@link FieldElement} anywhere in the model and stores the relative-path string (e.g.
 * {@code "../fieldName"}) that reaches it from the bound element, mirroring the a12 kernel's relative-path
 * convention already read by {@link ElementIndex#resolveRelativePath} (and written by the new inverse, {@link
 * ElementIndex#relativePathTo}). Reused for {@link de.a12.studio.models.documentmodel.RuleConfig#getErrorEntityRelPath()}
 * (via {@link #configureRuleErrorEntity()}) and {@link de.a12.studio.models.documentmodel.ComputationConfig#getComputedFieldRelPath()}
 * (via {@link #configureComputedField()}).
 */
public class TargetFieldPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private ComboBox<String> targetFieldComboBox;

  private Function<Element, String> relPathReader;
  private BiConsumer<Element, String> relPathWriter;
  private String ownProperty;

  // Display path (e.g. "/Addresses/Street") -> the field element it resolves to; rebuilt on every setElement().
  private final Map<String, Element> fieldsByDisplayPath = new LinkedHashMap<>();

  // Rebuilt alongside fieldsByDisplayPath, so the bindComboBox listener below (registered once, in initialize())
  // always translates against the index for whichever element is currently bound.
  private ElementIndex currentIndex;

  // Own error tag left null: RuleConfig's three required-field checks (errorCode, errorCondition,
  // errorEntityRelPath) are all tagged ElementProperty.RULE_PROPERTIES, owned by RulePropertiesPanelController -
  // giving this panel the same tag too would just duplicate whichever message ownError() finds first.
  public void configureRuleErrorEntity() {
    configure(el -> getRule(el).getErrorEntityRelPath(), (el, value) -> getRule(el).setErrorEntityRelPath(value),
        "errorEntityRelPath", null, StudioBundle.get("rule_error_entity"));
  }

  public void configureComputedField() {
    configure(el -> getComputation(el).getComputedFieldRelPath(), (el, value) -> getComputation(el).setComputedFieldRelPath(value),
        "computedFieldRelPath", ElementProperty.COMPUTATION_PROPERTIES, StudioBundle.get("computed_field"));
  }

  private void configure(Function<Element, String> reader, BiConsumer<Element, String> writer, String fieldKey, String ownProperty, String title) {
    this.relPathReader = reader;
    this.relPathWriter = writer;
    this.ownProperty = ownProperty;
    setTitle(title);
    setSettingsKeySuffix("." + fieldKey);
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    bindComboBox(targetFieldComboBox, (el, displayPath) -> {
      Element target = fieldsByDisplayPath.get(displayPath);
      relPathWriter.accept(el, target == null || currentIndex == null ? null : currentIndex.relativePathTo(el, target));
    });
  }

  @Override
  public void setElement(@NonNull Element element) {
    super.setElement(element);
    rebuildItems(element);
  }

  @Override
  protected String validationProperty() {
    return ownProperty;
  }

  private void rebuildItems(Element element) {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    fieldsByDisplayPath.clear();
    List<String> items = new ArrayList<>();
    items.add("");

    if (projectItem != null && projectItem.getModel() instanceof DocumentModel documentModel) {
      currentIndex = new ElementIndex(documentModel);
      for (Element candidate : currentIndex.allElements()) {
        if (candidate instanceof FieldElement) {
          String path = currentIndex.getPath(candidate);
          fieldsByDisplayPath.put(path, candidate);
          items.add(path);
        }
      }
    } else {
      currentIndex = null;
    }
    setComboBoxItems(targetFieldComboBox, items);

    String relPath = relPathReader.apply(element);
    String selected = "";
    if (currentIndex != null && relPath != null && !relPath.isBlank()) {
      selected = currentIndex.resolveRelativePath(element, relPath).map(currentIndex::getPath).orElse("");
    }
    setFieldValue(targetFieldComboBox, selected);
  }

  private static de.a12.studio.models.documentmodel.RuleConfig getRule(Element element) {
    return ((RuleElement) element).getRule();
  }

  private static de.a12.studio.models.documentmodel.ComputationConfig getComputation(Element element) {
    return ((ComputationElement) element).getComputation();
  }
}
