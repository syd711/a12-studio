package de.a12.studio.ui.editors.relationshipmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Label;
import de.a12.studio.models.Locale;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.LinkConstraints;
import de.a12.studio.models.relationshipmodel.Multiplicity;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits a {@link RelationshipModel}: the two related entities (Document Model, role, ordering and
 * multiplicity each), whether duplicate links are allowed, and an optional Link Document Model
 * (mirroring SME's relationship model editor). The header's {@code modelReferences} are kept in sync
 * with the entities' Document Models on every change.
 */
public class RelationshipModelEditorController extends AbstractEditorController implements Initializable {

  @FXML
  private CheckBox duplicatesAllowedField;

  @FXML
  private ComboBox<String> linkDocumentModelField;

  @FXML
  private ComboBox<String> entity1DocumentModelField;
  @FXML
  private TextField entity1RoleField;
  @FXML
  private CheckBox entity1OrderedField;
  @FXML
  private CheckBox entity1UnboundedField;
  @FXML
  private Spinner<Integer> entity1UpperLimitField;
  @FXML
  private GridPane entity1LabelsGrid;

  @FXML
  private ComboBox<String> entity2DocumentModelField;
  @FXML
  private TextField entity2RoleField;
  @FXML
  private CheckBox entity2OrderedField;
  @FXML
  private CheckBox entity2UnboundedField;
  @FXML
  private Spinner<Integer> entity2UpperLimitField;
  @FXML
  private GridPane entity2LabelsGrid;

  private RelationshipModel model;

  // Set while fields are being repopulated from the model, so that programmatic updates aren't mistaken
  // for user edits and don't trigger a save.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL url, ResourceBundle resources) {
    initializeUpperLimitSpinner(entity1UpperLimitField);
    initializeUpperLimitSpinner(entity2UpperLimitField);

    duplicatesAllowedField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      model.getContent().setDuplicatesAllowed(newValue);
      commitChange();
    });

    linkDocumentModelField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      model.getContent().setLinkDocumentModelValue(newValue == null || newValue.isBlank() ? null : newValue);
      commitChange();
    });

    wireEntityFields(0, entity1DocumentModelField, entity1RoleField, entity1OrderedField, entity1UnboundedField, entity1UpperLimitField);
    wireEntityFields(1, entity2DocumentModelField, entity2RoleField, entity2OrderedField, entity2UnboundedField, entity2UpperLimitField);
  }

  private void initializeUpperLimitSpinner(Spinner<Integer> spinner) {
    spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 1));
    WidgetFactory.restrictToNumericInput(spinner.getEditor());
  }

  private void wireEntityFields(int index, ComboBox<String> documentModelField, TextField roleField,
                                CheckBox orderedField, CheckBox unboundedField, Spinner<Integer> upperLimitField) {
    documentModelField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      entity(index).setDocumentModel(newValue);
      syncModelReferences();
      commitChange();
    });

    roleField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      entity(index).setRole(newValue);
      syncModelReferences();
      commitChange();
    });

    orderedField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      entity(index).setOrdered(newValue);
      commitChange();
    });

    unboundedField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      Multiplicity multiplicity = multiplicity(index);
      multiplicity.setUnbounded(newValue);
      multiplicity.setUpperLimit(newValue ? null : upperLimitField.getValue());
      upperLimitField.setDisable(newValue);
      commitChange();
    });

    upperLimitField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      Multiplicity multiplicity = multiplicity(index);
      if (!Boolean.TRUE.equals(multiplicity.getUnbounded())) {
        multiplicity.setUpperLimit(newValue);
        commitChange();
      }
    });
  }

  @Override
  public void loadModel(@NonNull A12Model<?> model) {
    load((RelationshipModel) model);
    updateSettingsErrorBadge();
  }

  private void load(@NonNull RelationshipModel model) {
    this.model = model;
    ensureTwoEntities();

    updatingFromModel = true;
    try {
      List<String> documentModelOptions = documentModelOptions();

      duplicatesAllowedField.setSelected(Boolean.TRUE.equals(model.getContent().getDuplicatesAllowed()));

      linkDocumentModelField.getItems().setAll(documentModelOptions);
      linkDocumentModelField.setValue(model.getContent().getLinkDocumentModelValue());

      populateEntityFields(0, documentModelOptions, entity1DocumentModelField, entity1RoleField,
          entity1OrderedField, entity1UnboundedField, entity1UpperLimitField, entity1LabelsGrid);
      populateEntityFields(1, documentModelOptions, entity2DocumentModelField, entity2RoleField,
          entity2OrderedField, entity2UnboundedField, entity2UpperLimitField, entity2LabelsGrid);
    }
    finally {
      updatingFromModel = false;
    }
  }

  // Relationship models always connect exactly two entities; guard against hand-edited files with fewer.
  private void ensureTwoEntities() {
    List<EntityCharacteristic> entities = model.getContent().getEntityCharacteristics();
    while (entities.size() < 2) {
      EntityCharacteristic entity = new EntityCharacteristic();
      entity.setOrdered(false);
      Multiplicity multiplicity = new Multiplicity();
      multiplicity.setUnbounded(true);
      LinkConstraints constraints = new LinkConstraints();
      constraints.setMultiplicity(multiplicity);
      entity.setLinkConstraints(constraints);
      entities.add(entity);
    }
  }

  private void populateEntityFields(int index, List<String> documentModelOptions, ComboBox<String> documentModelField,
                                    TextField roleField, CheckBox orderedField, CheckBox unboundedField,
                                    Spinner<Integer> upperLimitField, GridPane labelsGrid) {
    EntityCharacteristic entity = entity(index);

    documentModelField.getItems().setAll(documentModelOptions);
    documentModelField.setValue(entity.getDocumentModel());
    roleField.setText(entity.getRole() != null ? entity.getRole() : "");
    orderedField.setSelected(Boolean.TRUE.equals(entity.getOrdered()));

    Multiplicity multiplicity = multiplicity(index);
    boolean unbounded = Boolean.TRUE.equals(multiplicity.getUnbounded());
    unboundedField.setSelected(unbounded);
    upperLimitField.setDisable(unbounded);
    upperLimitField.getValueFactory().setValue(multiplicity.getUpperLimit() != null ? multiplicity.getUpperLimit() : 1);

    rebuildLabelRows(labelsGrid, entity);
  }

  /** One label text field per model locale, editing {@code entityCharacteristics[index].labels} in place. */
  private void rebuildLabelRows(GridPane labelsGrid, EntityCharacteristic entity) {
    labelsGrid.getChildren().clear();

    int row = 0;
    for (Locale locale : model.getLocales()) {
      String code = locale.getCode();
      javafx.scene.control.Label localeLabel = new javafx.scene.control.Label(code);
      localeLabel.getStyleClass().add("field-label");

      TextField textField = new TextField(labelText(entity.getLabels(), code));
      textField.setMaxWidth(Double.MAX_VALUE);
      GridPane.setHgrow(textField, javafx.scene.layout.Priority.ALWAYS);
      textField.textProperty().addListener((observable, oldValue, newValue) -> {
        if (updatingFromModel || model == null) {
          return;
        }
        setLabelText(entity.getLabels(), code, newValue);
        commitChange();
      });

      labelsGrid.addRow(row++, localeLabel, textField);
    }
  }

  private static String labelText(List<Label> labels, String locale) {
    return labels.stream()
        .filter(label -> locale.equals(label.getLocale()))
        .map(Label::getText)
        .filter(text -> text != null)
        .findFirst()
        .orElse("");
  }

  private static void setLabelText(List<Label> labels, String locale, String text) {
    Label existing = labels.stream()
        .filter(label -> locale.equals(label.getLocale()))
        .findFirst()
        .orElse(null);
    if (existing == null) {
      existing = new Label();
      existing.setLocale(locale);
      labels.add(existing);
    }
    existing.setText(text == null || text.isBlank() ? null : text);
  }

  private EntityCharacteristic entity(int index) {
    return model.getContent().getEntityCharacteristics().get(index);
  }

  private Multiplicity multiplicity(int index) {
    EntityCharacteristic entity = entity(index);
    if (entity.getLinkConstraints() == null) {
      entity.setLinkConstraints(new LinkConstraints());
    }
    if (entity.getLinkConstraints().getMultiplicity() == null) {
      entity.getLinkConstraints().setMultiplicity(new Multiplicity());
    }
    return entity.getLinkConstraints().getMultiplicity();
  }

  private List<String> documentModelOptions() {
    List<String> options = new ArrayList<>();
    // Empty entry so the optional link document model can be cleared again.
    options.add("");
    ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.DOCUMENT).stream()
        .map(A12Model::getId)
        .sorted(Comparator.naturalOrder())
        .forEach(options::add);
    return options;
  }

  /**
   * Rebuilds the header's Document Model references from the two entities (one reference per entity,
   * alias = role), the shape SME writes for relationship models (see PersonCompany.json).
   */
  private void syncModelReferences() {
    List<ModelReference> references = model.getModelReferences();
    references.removeIf(reference -> ModelReference.PURPOSE_DOCUMENT_MODEL.equals(reference.getPurpose()));
    for (EntityCharacteristic entity : model.getContent().getEntityCharacteristics()) {
      if (entity.getDocumentModel() == null || entity.getDocumentModel().isBlank()) {
        continue;
      }
      ModelReference reference = new ModelReference();
      reference.setPurpose(ModelReference.PURPOSE_DOCUMENT_MODEL);
      reference.setModelType(ModelType.DOCUMENT);
      reference.setAlias(entity.getRole());
      reference.setReference(entity.getDocumentModel());
      references.add(reference);
    }
  }

  private void commitChange() {
    projectItem.save();
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.RELATIONSHIP;
  }
}
