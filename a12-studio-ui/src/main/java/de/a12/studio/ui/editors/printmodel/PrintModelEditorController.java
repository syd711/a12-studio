package de.a12.studio.ui.editors.printmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.printmodel.BorderProperties;
import de.a12.studio.models.printmodel.Calculation;
import de.a12.studio.models.printmodel.ComputationStep;
import de.a12.studio.models.printmodel.DefaultSegment;
import de.a12.studio.models.printmodel.GenericPrintElement;
import de.a12.studio.models.printmodel.Measure;
import de.a12.studio.models.printmodel.OverridableValue;
import de.a12.studio.models.printmodel.PrintCalculationElement;
import de.a12.studio.models.printmodel.PrintDimensions;
import de.a12.studio.models.printmodel.PrintElementDefinition;
import de.a12.studio.models.printmodel.PrintElementReference;
import de.a12.studio.models.printmodel.PrintFieldElement;
import de.a12.studio.models.printmodel.FieldRef;
import de.a12.studio.models.printmodel.PrintModel;
import de.a12.studio.models.printmodel.PrintPosition;
import de.a12.studio.models.printmodel.PrintSegmentDefinition;
import de.a12.studio.models.printmodel.PrintStructureEntry;
import de.a12.studio.models.printmodel.PrintTextElement;
import de.a12.studio.models.printmodel.RichText;
import de.a12.studio.models.printmodel.ScreenReadingOrder;
import de.a12.studio.models.printmodel.TextProperties;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.events.StudioEventManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Full-canvas editor for a {@link PrintModel}: segments as tabs over an A4-scaled page pane (see
 * {@link PrintCanvasView}), a structure tree on the left, and properties of the selected segment or
 * element on the right. Elements are moved by dragging on the page; positions snap to 0.5 mm.
 */
public class PrintModelEditorController extends AbstractEditorController implements Initializable {

  private static final List<Double> ZOOM_LEVELS = List.of(0.25, 0.5, 0.75, 1.0, 1.5, 2.0, 3.0, 4.0);

  @FXML
  private TreeView<Object> structureTree;

  @FXML
  private TabPane segmentTabs;

  @FXML
  private ComboBox<String> zoomField;

  @FXML
  private Label orientationLabel;

  @FXML
  private ScrollPane canvasScroll;

  @FXML
  private StackPane canvasHolder;

  // Segment properties (visible when no element is selected).
  @FXML
  private VBox segmentPropertiesBox;
  @FXML
  private TextField segmentTitleField;
  @FXML
  private ComboBox<String> segmentOrientationField;

  // Element properties (visible when an element reference is selected).
  @FXML
  private VBox elementPropertiesBox;
  @FXML
  private Label elementTypeLabel;
  @FXML
  private TextField elementXField;
  @FXML
  private TextField elementYField;
  @FXML
  private TextField elementMinWidthField;
  @FXML
  private TextField elementMinHeightField;

  @FXML
  private VBox textPropertiesBox;
  @FXML
  private TextArea textHtmlField;

  @FXML
  private VBox fieldPropertiesBox;
  @FXML
  private TextField fieldModelField;
  @FXML
  private TextField fieldPathField;

  @FXML
  private VBox calculationPropertiesBox;
  @FXML
  private TextField calculationNameField;
  @FXML
  private TextArea calculationOperationField;
  @FXML
  private TextField calculationResultTypeField;

  @FXML
  private VBox genericPropertiesBox;
  @FXML
  private TextArea genericJsonField;

  private final PrintCanvasView canvas = new PrintCanvasView();
  private final Group zoomGroup = new Group();
  private final Scale zoomScale = new Scale(1, 1);

  private PrintModel model;
  private boolean updatingFromModel;

  @Override
  public void initialize(URL url, ResourceBundle resources) {
    zoomGroup.getChildren().add(canvas.getPage());
    zoomGroup.getTransforms().add(zoomScale);
    canvasHolder.getChildren().add(zoomGroup);

    canvas.setOnSelected(this::showElementProperties);
    canvas.setOnPositionChanged(reference -> {
      refreshPositionFields(reference);
      commitChange();
    });

    initializeZoom();
    initializeSegmentProperties();
    initializeElementProperties();

    segmentTabs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        showSelectedSegment();
      }
    });

    structureTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || newValue == null) {
        return;
      }
      if (newValue.getValue() instanceof PrintSegmentDefinition segment) {
        selectSegmentTab(segment);
      }
      else if (newValue.getValue() instanceof PrintElementReference reference) {
        TreeItem<Object> parent = newValue.getParent();
        if (parent != null && parent.getValue() instanceof PrintSegmentDefinition segment) {
          selectSegmentTab(segment);
          canvas.selectReference(reference);
        }
      }
    });
  }

  private void initializeZoom() {
    zoomField.getItems().setAll(ZOOM_LEVELS.stream().map(level -> Math.round(level * 100) + "%").toList());
    zoomField.setValue("100%");
    zoomField.valueProperty().addListener((observable, oldValue, newValue) -> applyZoom(parseZoom(newValue)));

    // Ctrl+scroll zooms in and out, stepping through the predefined zoom levels.
    canvasScroll.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, event -> {
      if (!event.isControlDown()) {
        return;
      }
      int index = ZOOM_LEVELS.indexOf(parseZoom(zoomField.getValue()));
      int nextIndex = Math.clamp(index + (event.getDeltaY() > 0 ? 1 : -1), 0, ZOOM_LEVELS.size() - 1);
      zoomField.setValue(Math.round(ZOOM_LEVELS.get(nextIndex) * 100) + "%");
      event.consume();
    });
  }

  private static double parseZoom(String text) {
    if (text == null) {
      return 1;
    }
    try {
      return Double.parseDouble(text.replace("%", "").strip()) / 100.0;
    }
    catch (NumberFormatException e) {
      return 1;
    }
  }

  private void applyZoom(double factor) {
    zoomScale.setX(factor);
    zoomScale.setY(factor);
  }

  private void initializeSegmentProperties() {
    segmentTitleField.textProperty().addListener((observable, oldValue, newValue) -> {
      PrintSegmentDefinition segment = selectedSegment();
      if (updatingFromModel || segment == null) {
        return;
      }
      segment.setTitle(newValue);
      Tab tab = segmentTabs.getSelectionModel().getSelectedItem();
      if (tab != null) {
        tab.setText(newValue == null || newValue.isBlank() ? "Segment" : newValue);
      }
      rebuildStructureTree();
      commitChange();
    });

    segmentOrientationField.getItems().setAll("Portrait", "Landscape");
    segmentOrientationField.valueProperty().addListener((observable, oldValue, newValue) -> {
      PrintSegmentDefinition segment = selectedSegment();
      if (updatingFromModel || segment == null || newValue == null) {
        return;
      }
      if (segment.getDefaultSegment() == null) {
        DefaultSegment defaultSegment = new DefaultSegment();
        defaultSegment.setId(nanoId());
        segment.setDefaultSegment(defaultSegment);
      }
      segment.getDefaultSegment().setPageOrientation(newValue);
      orientationLabel.setText(newValue);
      showSelectedSegment();
      commitChange();
    });
  }

  private void initializeElementProperties() {
    wireMeasureField(elementXField, reference -> reference.getPosition() != null ? reference.getPosition().getX() : null);
    wireMeasureField(elementYField, reference -> reference.getPosition() != null ? reference.getPosition().getY() : null);
    wireMeasureField(elementMinWidthField, reference -> reference.getDimensions() != null ? reference.getDimensions().getMinWidth() : null);
    wireMeasureField(elementMinHeightField, reference -> reference.getDimensions() != null ? reference.getDimensions().getMinHeight() : null);

    textHtmlField.focusedProperty().addListener((observable, hadFocus, hasFocus) -> {
      PrintElementReference reference = canvas.getSelectedReference();
      if (hasFocus || updatingFromModel || reference == null) {
        return;
      }
      if (resolveDefinition(reference) instanceof PrintTextElement text && text.getText() != null) {
        text.getText().setText(textHtmlField.getText());
        showSelectedSegment();
        canvas.selectReference(reference);
        commitChange();
      }
    });

    wireTextField(fieldModelField, (reference, value) -> {
      if (resolveDefinition(reference) instanceof PrintFieldElement field && field.getField() != null) {
        field.getField().setModel(value);
        return true;
      }
      return false;
    });
    wireTextField(fieldPathField, (reference, value) -> {
      if (resolveDefinition(reference) instanceof PrintFieldElement field && field.getField() != null) {
        field.getField().setPath(value);
        return true;
      }
      return false;
    });
    wireTextField(calculationNameField, (reference, value) -> {
      if (resolveDefinition(reference) instanceof PrintCalculationElement element && element.getCalculation() != null) {
        element.getCalculation().setName(value);
        return true;
      }
      return false;
    });
    wireTextField(calculationResultTypeField, (reference, value) -> {
      if (resolveDefinition(reference) instanceof PrintCalculationElement element && element.getCalculation() != null) {
        element.getCalculation().setResultType(value == null || value.isBlank() ? null : value);
        return true;
      }
      return false;
    });

    calculationOperationField.focusedProperty().addListener((observable, hadFocus, hasFocus) -> {
      PrintElementReference reference = canvas.getSelectedReference();
      if (hasFocus || updatingFromModel || reference == null) {
        return;
      }
      if (resolveDefinition(reference) instanceof PrintCalculationElement element && element.getCalculation() != null) {
        Calculation calculation = element.getCalculation();
        if (calculation.getComputationAlternatives().isEmpty()) {
          ComputationStep step = new ComputationStep();
          step.setId(nanoId());
          calculation.getComputationAlternatives().add(step);
        }
        calculation.getComputationAlternatives().get(0).setOperation(calculationOperationField.getText());
        showSelectedSegment();
        canvas.selectReference(reference);
        commitChange();
      }
    });
  }

  private interface ElementTextWriter {
    boolean write(PrintElementReference reference, String value);
  }

  private void wireTextField(TextField field, ElementTextWriter writer) {
    field.focusedProperty().addListener((observable, hadFocus, hasFocus) -> {
      PrintElementReference reference = canvas.getSelectedReference();
      if (hasFocus || updatingFromModel || reference == null) {
        return;
      }
      if (writer.write(reference, field.getText())) {
        showSelectedSegment();
        canvas.selectReference(reference);
        commitChange();
      }
    });
  }

  private interface MeasureAccessor {
    Measure get(PrintElementReference reference);
  }

  private void wireMeasureField(TextField field, MeasureAccessor accessor) {
    field.focusedProperty().addListener((observable, hadFocus, hasFocus) -> {
      PrintElementReference reference = canvas.getSelectedReference();
      if (hasFocus || updatingFromModel || reference == null) {
        return;
      }
      Measure measure = accessor.get(reference);
      if (measure == null) {
        return;
      }
      try {
        measure.setValue(new BigDecimal(field.getText().strip()));
        showSelectedSegment();
        canvas.selectReference(reference);
        commitChange();
      }
      catch (NumberFormatException e) {
        field.setText(measure.getValue() != null ? measure.getValue().toPlainString() : "");
      }
    });
  }

  @Override
  public void loadModel(@NonNull A12Model<?> model) {
    load((PrintModel) model);
    updateSettingsErrorBadge();
  }

  private void load(@NonNull PrintModel model) {
    this.model = model;

    updatingFromModel = true;
    try {
      rebuildSegmentTabs();
      rebuildStructureTree();
    }
    finally {
      updatingFromModel = false;
    }
    showSelectedSegment();
  }

  /** Segments ordered by general.structure; segments missing from the structure list come last. */
  private List<PrintSegmentDefinition> orderedSegments() {
    List<PrintSegmentDefinition> definitions = model.getContent().getSegments() != null
        ? model.getContent().getSegments().getDefinitions()
        : List.of();
    List<PrintSegmentDefinition> ordered = new ArrayList<>();
    if (model.getContent().getGeneral() != null && model.getContent().getGeneral().getStructure() != null) {
      for (PrintStructureEntry entry : model.getContent().getGeneral().getStructure()) {
        definitions.stream()
            .filter(definition -> definition.getId() != null && definition.getId().equals(entry.getId()))
            .findFirst()
            .ifPresent(ordered::add);
      }
    }
    for (PrintSegmentDefinition definition : definitions) {
      if (!ordered.contains(definition)) {
        ordered.add(definition);
      }
    }
    return ordered;
  }

  private void rebuildSegmentTabs() {
    segmentTabs.getTabs().clear();
    for (PrintSegmentDefinition segment : orderedSegments()) {
      Tab tab = new Tab(segment.getTitle() == null || segment.getTitle().isBlank() ? "Segment" : segment.getTitle());
      tab.setClosable(false);
      tab.setUserData(segment);
      segmentTabs.getTabs().add(tab);
    }
  }

  private void rebuildStructureTree() {
    TreeItem<Object> root = new TreeItem<>("Print Model");
    root.setExpanded(true);

    TreeItem<Object> segmentsItem = new TreeItem<>("Segments");
    segmentsItem.setExpanded(true);
    for (PrintSegmentDefinition segment : orderedSegments()) {
      TreeItem<Object> segmentItem = new TreeItem<>(segment);
      segmentItem.setExpanded(true);
      for (PrintElementReference reference : segment.getElementReferences()) {
        segmentItem.getChildren().add(new TreeItem<>(reference));
      }
      segmentsItem.getChildren().add(segmentItem);
    }
    root.getChildren().add(segmentsItem);

    structureTree.setRoot(root);
    structureTree.setShowRoot(false);
    structureTree.setCellFactory(tree -> new javafx.scene.control.TreeCell<>() {
      @Override
      protected void updateItem(Object value, boolean empty) {
        super.updateItem(value, empty);
        setText(empty || value == null ? null : describe(value));
      }
    });
  }

  private String describe(Object value) {
    if (value instanceof PrintSegmentDefinition segment) {
      return segment.getTitle() == null || segment.getTitle().isBlank() ? "Segment" : segment.getTitle();
    }
    if (value instanceof PrintElementReference reference) {
      PrintElementDefinition definition = resolveDefinition(reference);
      if (definition == null) {
        return "<missing definition>";
      }
      String type = definition.getType() != null ? definition.getType() : "?";
      if (definition instanceof PrintTextElement text && text.getText() != null && text.getText().getText() != null) {
        String plain = text.getText().getText().replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").strip();
        return type + (plain.isEmpty() ? "" : ": " + (plain.length() > 30 ? plain.substring(0, 30) + "…" : plain));
      }
      return type;
    }
    return String.valueOf(value);
  }

  private PrintSegmentDefinition selectedSegment() {
    Tab tab = segmentTabs.getSelectionModel().getSelectedItem();
    return tab != null && tab.getUserData() instanceof PrintSegmentDefinition segment ? segment : null;
  }

  private void selectSegmentTab(PrintSegmentDefinition segment) {
    for (Tab tab : segmentTabs.getTabs()) {
      if (tab.getUserData() == segment) {
        segmentTabs.getSelectionModel().select(tab);
        return;
      }
    }
  }

  private void showSelectedSegment() {
    PrintSegmentDefinition segment = selectedSegment();
    if (segment == null) {
      canvas.getPage().getChildren().clear();
      showSegmentProperties(null);
      return;
    }
    canvas.showSegment(model, segment);
    showSegmentProperties(segment);
  }

  private void showSegmentProperties(PrintSegmentDefinition segment) {
    updatingFromModel = true;
    try {
      segmentPropertiesBox.setVisible(segment != null);
      segmentPropertiesBox.setManaged(segment != null);
      elementPropertiesBox.setVisible(false);
      elementPropertiesBox.setManaged(false);
      if (segment != null) {
        segmentTitleField.setText(segment.getTitle() != null ? segment.getTitle() : "");
        String orientation = segment.getDefaultSegment() != null && segment.getDefaultSegment().getPageOrientation() != null
            ? segment.getDefaultSegment().getPageOrientation()
            : "Portrait";
        segmentOrientationField.setValue(orientation);
        orientationLabel.setText(orientation);
      }
    }
    finally {
      updatingFromModel = false;
    }
  }

  private void showElementProperties(PrintElementReference reference) {
    if (reference == null) {
      showSegmentProperties(selectedSegment());
      return;
    }

    updatingFromModel = true;
    try {
      segmentPropertiesBox.setVisible(false);
      segmentPropertiesBox.setManaged(false);
      elementPropertiesBox.setVisible(true);
      elementPropertiesBox.setManaged(true);

      PrintElementDefinition definition = resolveDefinition(reference);
      elementTypeLabel.setText(definition != null && definition.getType() != null ? definition.getType() : "<missing definition>");
      refreshPositionFields(reference);

      boolean isText = definition instanceof PrintTextElement;
      boolean isField = definition instanceof PrintFieldElement;
      boolean isCalculation = definition instanceof PrintCalculationElement;
      boolean isGeneric = definition instanceof GenericPrintElement;

      setSection(textPropertiesBox, isText);
      setSection(fieldPropertiesBox, isField);
      setSection(calculationPropertiesBox, isCalculation);
      setSection(genericPropertiesBox, isGeneric);

      if (isText) {
        PrintTextElement text = (PrintTextElement) definition;
        textHtmlField.setText(text.getText() != null && text.getText().getText() != null ? text.getText().getText() : "");
      }
      else if (isField) {
        PrintFieldElement field = (PrintFieldElement) definition;
        fieldModelField.setText(field.getField() != null && field.getField().getModel() != null ? field.getField().getModel() : "");
        fieldPathField.setText(field.getField() != null && field.getField().getPath() != null ? field.getField().getPath() : "");
      }
      else if (isCalculation) {
        Calculation calculation = ((PrintCalculationElement) definition).getCalculation();
        calculationNameField.setText(calculation != null && calculation.getName() != null ? calculation.getName() : "");
        calculationOperationField.setText(calculation != null && !calculation.getComputationAlternatives().isEmpty()
            && calculation.getComputationAlternatives().get(0).getOperation() != null
            ? calculation.getComputationAlternatives().get(0).getOperation()
            : "");
        calculationResultTypeField.setText(calculation != null && calculation.getResultType() != null ? calculation.getResultType() : "");
      }
      else if (isGeneric) {
        genericJsonField.setText(JsonSettings.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(definition));
      }
    }
    finally {
      updatingFromModel = false;
    }
  }

  private void setSection(VBox section, boolean visible) {
    section.setVisible(visible);
    section.setManaged(visible);
  }

  private void refreshPositionFields(PrintElementReference reference) {
    boolean wasUpdating = updatingFromModel;
    updatingFromModel = true;
    try {
      elementXField.setText(measureText(reference.getPosition() != null ? reference.getPosition().getX() : null));
      elementYField.setText(measureText(reference.getPosition() != null ? reference.getPosition().getY() : null));
      elementMinWidthField.setText(measureText(reference.getDimensions() != null ? reference.getDimensions().getMinWidth() : null));
      elementMinHeightField.setText(measureText(reference.getDimensions() != null ? reference.getDimensions().getMinHeight() : null));
    }
    finally {
      updatingFromModel = wasUpdating;
    }
  }

  private static String measureText(Measure measure) {
    return measure != null && measure.getValue() != null ? measure.getValue().toPlainString() : "";
  }

  private PrintElementDefinition resolveDefinition(PrintElementReference reference) {
    if (reference == null || reference.getRefId() == null) {
      return null;
    }
    return model.getContent().getElementDefinitions().stream()
        .filter(definition -> reference.getRefId().equals(definition.getId()))
        .findFirst()
        .orElse(null);
  }

  @FXML
  public void onAddSegment(ActionEvent e) {
    TextInputDialog dialog = new TextInputDialog("Segment " + (orderedSegments().size() + 1));
    dialog.setTitle("Add Segment");
    dialog.setHeaderText("New print segment");
    dialog.setContentText("Title:");
    String title = dialog.showAndWait().orElse(null);
    if (title == null || title.isBlank()) {
      return;
    }

    PrintSegmentDefinition segment = new PrintSegmentDefinition();
    segment.setId(nanoId());
    segment.setTitle(title);
    segment.setType("Default");
    DefaultSegment defaultSegment = new DefaultSegment();
    defaultSegment.setId(nanoId());
    defaultSegment.setPageOrientation("Portrait");
    segment.setDefaultSegment(defaultSegment);
    model.getContent().getSegments().getDefinitions().add(segment);

    if (model.getContent().getGeneral() != null) {
      if (model.getContent().getGeneral().getStructure() == null) {
        model.getContent().getGeneral().setStructure(new ArrayList<>());
      }
      PrintStructureEntry entry = new PrintStructureEntry();
      entry.setId(segment.getId());
      model.getContent().getGeneral().getStructure().add(entry);
    }

    rebuildSegmentTabs();
    rebuildStructureTree();
    segmentTabs.getSelectionModel().select(segmentTabs.getTabs().size() - 1);
    commitChange();
  }

  @FXML
  public void onRemoveSegment(ActionEvent e) {
    PrintSegmentDefinition segment = selectedSegment();
    if (segment == null) {
      return;
    }
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
        "Remove segment \"" + describe(segment) + "\" including its elements?", ButtonType.OK, ButtonType.CANCEL);
    if (alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
      return;
    }

    // Element definitions only used by this segment are removed along with it.
    for (PrintElementReference reference : segment.getElementReferences()) {
      removeDefinitionIfUnreferenced(reference.getRefId(), segment);
    }
    model.getContent().getSegments().getDefinitions().remove(segment);
    if (model.getContent().getGeneral() != null && model.getContent().getGeneral().getStructure() != null) {
      model.getContent().getGeneral().getStructure()
          .removeIf(entry -> segment.getId() != null && segment.getId().equals(entry.getId()));
    }

    rebuildSegmentTabs();
    rebuildStructureTree();
    showSelectedSegment();
    commitChange();
  }

  @FXML
  public void onAddTextElement(ActionEvent e) {
    PrintSegmentDefinition segment = selectedSegment();
    if (segment == null) {
      return;
    }

    PrintTextElement element = new PrintTextElement();
    element.setId(nanoId());
    element.setType("Text");
    RichText text = new RichText();
    text.setId(nanoId());
    text.setText("<p><span style=\"\">New Text</span></p>");
    element.setText(text);
    BorderProperties borderProperties = new BorderProperties();
    borderProperties.setId(nanoId());
    element.setBorderProperties(borderProperties);
    TextProperties textProperties = new TextProperties();
    textProperties.setId(nanoId());
    textProperties.setTextStyleId(overridable("INPUT", "no-text-style-fallback-id", "/content/elementDefinitions/textProperties/textStyleId/value/"));
    textProperties.setAlignment(overridable("DEFAULT", null, "/content/elementDefinitions/textProperties/alignment/value/"));
    element.setTextProperties(textProperties);
    model.getContent().getElementDefinitions().add(element);

    segment.getElementReferences().add(newReference(element.getId()));

    rebuildStructureTree();
    showSelectedSegment();
    commitChange();
  }

  @FXML
  public void onAddFieldElement(ActionEvent e) {
    PrintSegmentDefinition segment = selectedSegment();
    if (segment == null) {
      return;
    }

    PrintFieldElement element = new PrintFieldElement();
    element.setId(nanoId());
    element.setType("Field");
    FieldRef field = new FieldRef();
    field.setId(nanoId());
    field.setModel(model.getModelReferences().stream()
        .filter(reference -> reference.getModelType() == ModelType.DOCUMENT)
        .map(reference -> reference.getReference())
        .findFirst()
        .orElse(""));
    field.setPath("/");
    element.setField(field);
    model.getContent().getElementDefinitions().add(element);

    segment.getElementReferences().add(newReference(element.getId()));

    rebuildStructureTree();
    showSelectedSegment();
    commitChange();
  }

  @FXML
  public void onRemoveElement(ActionEvent e) {
    PrintElementReference reference = canvas.getSelectedReference();
    PrintSegmentDefinition segment = selectedSegment();
    if (reference == null || segment == null) {
      return;
    }

    segment.getElementReferences().remove(reference);
    removeDefinitionIfUnreferenced(reference.getRefId(), null);

    rebuildStructureTree();
    showSelectedSegment();
    commitChange();
  }

  /** Deletes the element definition unless it is still referenced from another segment or text entity. */
  private void removeDefinitionIfUnreferenced(String refId, PrintSegmentDefinition excludedSegment) {
    if (refId == null) {
      return;
    }
    boolean stillReferenced = model.getContent().getSegments().getDefinitions().stream()
        .filter(segment -> segment != excludedSegment)
        .flatMap(segment -> segment.getElementReferences().stream())
        .anyMatch(reference -> refId.equals(reference.getRefId()));
    boolean referencedAsEntity = model.getContent().getElementDefinitions().stream()
        .filter(definition -> definition instanceof PrintTextElement)
        .map(definition -> ((PrintTextElement) definition).getText())
        .filter(text -> text != null)
        .flatMap(text -> text.getEntities().stream())
        .anyMatch(entity -> refId.equals(entity.getRefId()));
    if (!stillReferenced && !referencedAsEntity) {
      model.getContent().getElementDefinitions().removeIf(definition -> refId.equals(definition.getId()));
    }
  }

  private PrintElementReference newReference(String refId) {
    PrintElementReference reference = new PrintElementReference();
    reference.setId(nanoId());
    reference.setRefId(refId);

    PrintPosition position = new PrintPosition();
    position.setId(nanoId());
    position.setX(measure(10));
    position.setY(measure(10));
    reference.setPosition(position);

    PrintDimensions dimensions = new PrintDimensions();
    dimensions.setId(nanoId());
    dimensions.setMinWidth(measure(40));
    dimensions.setMinHeight(measure(6));
    reference.setDimensions(dimensions);

    ScreenReadingOrder screenReadingOrder = new ScreenReadingOrder();
    screenReadingOrder.setId(nanoId());
    screenReadingOrder.setScreenReadingOrderWeight(0);
    reference.setScreenReadingOrder(screenReadingOrder);

    reference.setPageBreakBehavior(overridable("DEFAULT", null, "/content/segments/definitions/elementReferences/pageBreakBehavior/value/"));
    return reference;
  }

  private Measure measure(double valueMm) {
    Measure measure = new Measure();
    measure.setId(nanoId());
    measure.setValue(BigDecimal.valueOf(valueMm).stripTrailingZeros());
    measure.setUnit("Millimeter");
    return measure;
  }

  private OverridableValue overridable(String source, Object value, String path) {
    OverridableValue overridable = new OverridableValue();
    overridable.setId(nanoId());
    overridable.setSource(source);
    overridable.setValue(value);
    overridable.setPath(path);
    return overridable;
  }

  // Print model node ids follow the nanoid style of the SME print editor (21 url-safe characters).
  private static String nanoId() {
    String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-";
    StringBuilder id = new StringBuilder(21);
    ThreadLocalRandom random = ThreadLocalRandom.current();
    for (int i = 0; i < 21; i++) {
      id.append(alphabet.charAt(random.nextInt(alphabet.length())));
    }
    return id.toString();
  }

  private void commitChange() {
    projectItem.save();
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.PRINT;
  }
}
