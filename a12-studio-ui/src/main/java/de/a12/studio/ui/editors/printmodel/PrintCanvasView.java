package de.a12.studio.ui.editors.printmodel;

import de.a12.studio.models.printmodel.Calculation;
import de.a12.studio.models.printmodel.FieldRef;
import de.a12.studio.models.printmodel.GenericPrintElement;
import de.a12.studio.models.printmodel.Measure;
import de.a12.studio.models.printmodel.PrintCalculationElement;
import de.a12.studio.models.printmodel.PrintElementDefinition;
import de.a12.studio.models.printmodel.PrintElementReference;
import de.a12.studio.models.printmodel.PrintFieldElement;
import de.a12.studio.models.printmodel.PrintModel;
import de.a12.studio.models.printmodel.PrintSegmentDefinition;
import de.a12.studio.models.printmodel.PrintTextElement;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renders one print model segment as an approximate A4 page: a white {@link Pane} in millimeter
 * scale with one absolutely positioned {@link StackPane} per element reference. Elements are
 * selectable (blue border) and draggable; positions snap to half millimeters and are written back
 * to the model through {@link #onPositionChanged}.
 */
class PrintCanvasView {

  /** 96 dpi screen pixels per millimeter; the page is laid out in physical page coordinates. */
  static final double PX_PER_MM = 96.0 / 25.4;

  private static final double A4_WIDTH_MM = 210;
  private static final double A4_HEIGHT_MM = 297;
  private static final double SNAP_MM = 0.5;

  private static final Color SELECTION_COLOR = Color.web("#3d8fd1");
  private static final Color CHIP_BACKGROUND = Color.web("#e8f0fe");
  private static final Color CHIP_BORDER = Color.web("#a5c4e8");

  private final Pane page = new Pane();

  private Consumer<PrintElementReference> onSelected = reference -> {
  };
  private Consumer<PrintElementReference> onPositionChanged = reference -> {
  };

  private StackPane selectedNode;
  private PrintElementReference selectedReference;

  PrintCanvasView() {
    page.setBackground(Background.fill(Color.WHITE));
    page.setEffect(new DropShadow(12, Color.gray(0.3)));
    // Clicking empty page space clears the element selection (back to segment properties).
    page.setOnMouseClicked(event -> {
      if (event.getTarget() == page) {
        select(null, null);
      }
    });
  }

  Pane getPage() {
    return page;
  }

  void setOnSelected(Consumer<PrintElementReference> onSelected) {
    this.onSelected = onSelected;
  }

  void setOnPositionChanged(Consumer<PrintElementReference> onPositionChanged) {
    this.onPositionChanged = onPositionChanged;
  }

  /** Rebuilds the page for the given segment; landscape swaps the A4 dimensions. */
  void showSegment(PrintModel model, PrintSegmentDefinition segment) {
    page.getChildren().clear();
    selectedNode = null;
    selectedReference = null;

    boolean landscape = segment.getDefaultSegment() != null
        && "Landscape".equalsIgnoreCase(segment.getDefaultSegment().getPageOrientation());
    double widthMm = landscape ? A4_HEIGHT_MM : A4_WIDTH_MM;
    double heightMm = landscape ? A4_WIDTH_MM : A4_HEIGHT_MM;
    page.setMinSize(widthMm * PX_PER_MM, heightMm * PX_PER_MM);
    page.setPrefSize(widthMm * PX_PER_MM, heightMm * PX_PER_MM);
    page.setMaxSize(widthMm * PX_PER_MM, heightMm * PX_PER_MM);

    int fontSize = model.getContent().getGeneral() != null
        && model.getContent().getGeneral().getSegmentDefaults() != null
        && model.getContent().getGeneral().getSegmentDefaults().getFontSize() != null
        ? model.getContent().getGeneral().getSegmentDefaults().getFontSize()
        : 12;

    for (PrintElementReference reference : segment.getElementReferences()) {
      PrintElementDefinition definition = resolve(model, reference.getRefId());
      StackPane node = buildElementNode(model, reference, definition, fontSize);
      page.getChildren().add(node);
    }
  }

  /** Re-selects the node of the given reference, e.g. after the canvas was rebuilt. */
  void selectReference(PrintElementReference reference) {
    for (var child : page.getChildren()) {
      if (child instanceof StackPane node && node.getUserData() == reference) {
        select(node, reference);
        return;
      }
    }
  }

  private static PrintElementDefinition resolve(PrintModel model, String refId) {
    if (refId == null) {
      return null;
    }
    return model.getContent().getElementDefinitions().stream()
        .filter(definition -> refId.equals(definition.getId()))
        .findFirst()
        .orElse(null);
  }

  private StackPane buildElementNode(PrintModel model, PrintElementReference reference,
                                     PrintElementDefinition definition, int fontSize) {
    StackPane node = new StackPane();
    node.setUserData(reference);
    node.setAlignment(Pos.TOP_LEFT);
    node.setCursor(Cursor.MOVE);
    node.setLayoutX(mm(reference.getPosition() != null ? reference.getPosition().getX() : null) * PX_PER_MM);
    node.setLayoutY(mm(reference.getPosition() != null ? reference.getPosition().getY() : null) * PX_PER_MM);
    node.setMinWidth(mm(reference.getDimensions() != null ? reference.getDimensions().getMinWidth() : null) * PX_PER_MM);
    node.setMinHeight(mm(reference.getDimensions() != null ? reference.getDimensions().getMinHeight() : null) * PX_PER_MM);
    node.setBorder(dashedBorder(Color.gray(0.75)));
    node.setPadding(new Insets(1));

    node.getChildren().add(renderContent(model, definition, fontSize));

    // Drag start state: sceneX, sceneY, layoutX, layoutY at mouse-press.
    double[] dragStart = new double[4];
    node.setOnMousePressed(event -> {
      select(node, reference);
      dragStart[0] = event.getSceneX();
      dragStart[1] = event.getSceneY();
      dragStart[2] = node.getLayoutX();
      dragStart[3] = node.getLayoutY();
      event.consume();
    });
    node.setOnMouseDragged(event -> {
      // Scene deltas are in screen pixels; divide by the zoom scale to get page pixels.
      double scale = page.getLocalToSceneTransform().getMxx();
      node.setLayoutX(dragStart[2] + (event.getSceneX() - dragStart[0]) / scale);
      node.setLayoutY(dragStart[3] + (event.getSceneY() - dragStart[1]) / scale);
      event.consume();
    });
    node.setOnMouseReleased(event -> {
      if (node.getLayoutX() != dragStart[2] || node.getLayoutY() != dragStart[3]) {
        commitPosition(node, reference);
      }
      event.consume();
    });

    return node;
  }

  /** Snaps the dragged position to half millimeters, writes it into the model and re-aligns the node. */
  private void commitPosition(StackPane node, PrintElementReference reference) {
    double xMm = snap(Math.max(0, node.getLayoutX() / PX_PER_MM));
    double yMm = snap(Math.max(0, node.getLayoutY() / PX_PER_MM));
    node.setLayoutX(xMm * PX_PER_MM);
    node.setLayoutY(yMm * PX_PER_MM);

    if (reference.getPosition() != null) {
      setMm(reference.getPosition().getX(), xMm);
      setMm(reference.getPosition().getY(), yMm);
    }
    onPositionChanged.accept(reference);
  }

  private static double snap(double mm) {
    return Math.round(mm / SNAP_MM) * SNAP_MM;
  }

  private static double mm(Measure measure) {
    return measure != null && measure.getValue() != null ? measure.getValue().doubleValue() : 0;
  }

  private static void setMm(Measure measure, double valueMm) {
    if (measure != null) {
      BigDecimal value = BigDecimal.valueOf(valueMm).setScale(1, RoundingMode.HALF_UP).stripTrailingZeros();
      // Keep integral values integral ("74", not "74.0") to match the print engine's notation.
      measure.setValue(value.scale() <= 0 ? value.setScale(0, RoundingMode.UNNECESSARY) : value);
    }
  }

  private void select(StackPane node, PrintElementReference reference) {
    if (selectedNode != null) {
      selectedNode.setBorder(dashedBorder(Color.gray(0.75)));
    }
    selectedNode = node;
    selectedReference = reference;
    if (node != null) {
      node.setBorder(new Border(new BorderStroke(SELECTION_COLOR, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1.5))));
    }
    onSelected.accept(reference);
  }

  PrintElementReference getSelectedReference() {
    return selectedReference;
  }

  private static Border dashedBorder(Color color) {
    return new Border(new BorderStroke(color, BorderStrokeStyle.DASHED, CornerRadii.EMPTY, BorderWidths.DEFAULT));
  }

  private javafx.scene.Node renderContent(PrintModel model, PrintElementDefinition definition, int fontSize) {
    if (definition instanceof PrintTextElement text) {
      return renderText(model, text, fontSize);
    }
    if (definition instanceof PrintFieldElement field) {
      return chip("Field", describeField(field.getField()));
    }
    if (definition instanceof PrintCalculationElement calculation) {
      return chip("Calculation", calculation.getCalculation() != null ? calculation.getCalculation().getName() : "?");
    }
    if (definition instanceof GenericPrintElement generic) {
      return renderGeneric(generic, fontSize);
    }
    Label missing = new Label("Missing element definition");
    missing.setTextFill(Color.RED);
    return missing;
  }

  private static String describeField(FieldRef field) {
    if (field == null) {
      return "?";
    }
    return (field.getModel() != null ? field.getModel() : "?") + (field.getPath() != null ? field.getPath() : "");
  }

  private static final Pattern PARAGRAPH_PATTERN = Pattern.compile("<p[^>]*>(.*?)</p>", Pattern.DOTALL);
  private static final Pattern ENTITY_SPAN_PATTERN =
      Pattern.compile("<span[^>]*entity-id=\"([^\"]+)\"[^>]*entity-type=\"([^\"]+)\"[^>]*>(.*?)</span></span>", Pattern.DOTALL);
  private static final Pattern TAG_PATTERN = Pattern.compile("<[^>]+>");

  /**
   * Lightweight HTML rendering: paragraphs become lines, embedded entity spans become inline chips,
   * every other tag is stripped. Sufficient as a layout preview; not a faithful HTML renderer.
   */
  private javafx.scene.Node renderText(PrintModel model, PrintTextElement element, int fontSize) {
    VBox paragraphs = new VBox(2);
    String html = element.getText() != null && element.getText().getText() != null ? element.getText().getText() : "";
    Font font = Font.font(null, fontSize * 96.0 / 72.0);

    Matcher paragraphMatcher = PARAGRAPH_PATTERN.matcher(html);
    boolean any = false;
    while (paragraphMatcher.find()) {
      any = true;
      paragraphs.getChildren().add(renderParagraph(model, paragraphMatcher.group(1), font));
    }
    if (!any) {
      paragraphs.getChildren().add(renderParagraph(model, html, font));
    }
    return paragraphs;
  }

  private TextFlow renderParagraph(PrintModel model, String paragraphHtml, Font font) {
    TextFlow flow = new TextFlow();
    int position = 0;
    Matcher entityMatcher = ENTITY_SPAN_PATTERN.matcher(paragraphHtml);
    while (entityMatcher.find()) {
      addPlainText(flow, paragraphHtml.substring(position, entityMatcher.start()), font);
      flow.getChildren().add(entityChip(model, entityMatcher.group(2), entityMatcher.group(1), entityMatcher.group(3)));
      position = entityMatcher.end();
    }
    addPlainText(flow, paragraphHtml.substring(position), font);
    return flow;
  }

  private static void addPlainText(TextFlow flow, String html, Font font) {
    String plain = unescape(TAG_PATTERN.matcher(html).replaceAll(""));
    if (!plain.isEmpty()) {
      Text text = new Text(plain);
      text.setFont(font);
      flow.getChildren().add(text);
    }
  }

  private javafx.scene.Node entityChip(PrintModel model, String entityType, String entityId, String innerHtml) {
    PrintElementDefinition definition = resolve(model, entityId);
    String label;
    if (definition instanceof PrintFieldElement field) {
      label = describeField(field.getField());
    }
    else if (definition instanceof PrintCalculationElement calculationElement) {
      Calculation calculation = calculationElement.getCalculation();
      label = calculation != null && calculation.getName() != null ? calculation.getName() : "Calculation";
    }
    else {
      label = unescape(TAG_PATTERN.matcher(innerHtml).replaceAll(""));
    }
    return chip(entityType, label);
  }

  private static javafx.scene.Node chip(String type, String label) {
    Label chip = new Label(type + ": " + label);
    chip.setFont(Font.font(null, FontWeight.NORMAL, 11));
    chip.setPadding(new Insets(0, 4, 0, 4));
    chip.setBackground(new Background(new BackgroundFill(CHIP_BACKGROUND, new CornerRadii(4), Insets.EMPTY)));
    chip.setBorder(new Border(new BorderStroke(CHIP_BORDER, BorderStrokeStyle.SOLID, new CornerRadii(4), BorderWidths.DEFAULT)));
    return chip;
  }

  /** Best-effort preview for element types the studio has no typed DTO for (Table, Image, ...). */
  private javafx.scene.Node renderGeneric(GenericPrintElement generic, int fontSize) {
    String type = generic.getType() != null ? generic.getType() : "?";
    Label label = new Label(type);
    label.setFont(Font.font(null, FontWeight.BOLD, fontSize * 96.0 / 72.0));
    label.setTextFill(Color.gray(0.45));

    if ("Image".equalsIgnoreCase(type)) {
      Object alternativeText = generic.getExtras().get("alternativeText");
      if (alternativeText != null) {
        label.setText("Image: " + alternativeText);
      }
    }
    StackPane box = new StackPane(label);
    box.setAlignment(Pos.CENTER);
    box.setBackground(Background.fill(Color.gray(0.96)));
    return box;
  }

  private static String unescape(String text) {
    return text
        .replace("&nbsp;", " ")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&amp;", "&")
        .strip();
  }
}
