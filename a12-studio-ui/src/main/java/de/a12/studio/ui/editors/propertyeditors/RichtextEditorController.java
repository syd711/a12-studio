package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Edits a single {@link Column}'s {@link Column#getExpression()} via a {@link CodeArea} (RichTextFX), giving
 * expression text a monospaced editor instead of a plain {@link javafx.scene.control.TextArea}. Not bound to a
 * single {@link de.a12.studio.models.documentmodel.Element} (the expression lives on the {@link Column} being
 * edited by {@link de.a12.studio.ui.editors.overviewmodel.dialogs.OverviewColumnDialogController}), so it
 * follows the same per-Column pattern as {@link de.a12.studio.ui.editors.propertyeditors.IconPanelController#setColumn}.
 */
public class RichtextEditorController extends AbstractPropertyEditor implements Initializable {

  // Quoted string literals in the a12 expression language, e.g. "* * *" in Invoice_OM.json's ExpressionColumn.
  private static final Pattern STRING_PATTERN = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"");

  @FXML
  private StackPane editorContainer;

  private final CodeArea codeArea = new CodeArea();

  private Column column;

  // Set while setColumn() is repopulating codeArea from the model, so the listener below doesn't mistake that
  // programmatic change for a user edit.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    codeArea.getStyleClass().add("expression-code-area");
    codeArea.setWrapText(true);
    editorContainer.getChildren().add(new VirtualizedScrollPane<>(codeArea));

    codeArea.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      column.setExpression(blankToNull(newValue));
      commitChange();
    });
    codeArea.textProperty().addListener((observable, oldValue, newValue) ->
        codeArea.setStyleSpans(0, computeHighlighting(newValue)));
  }

  private static StyleSpans<Collection<String>> computeHighlighting(String text) {
    Matcher matcher = STRING_PATTERN.matcher(text);
    StyleSpansBuilder<Collection<String>> builder = new StyleSpansBuilder<>();
    int lastEnd = 0;
    while (matcher.find()) {
      builder.add(Collections.emptyList(), matcher.start() - lastEnd);
      builder.add(Collections.singleton("string"), matcher.end() - matcher.start());
      lastEnd = matcher.end();
    }
    builder.add(Collections.emptyList(), text.length() - lastEnd);
    return builder.create();
  }

  public void setColumn(@NonNull Column column) {
    this.column = column;
    updatingFromModel = true;
    try {
      codeArea.replaceText(column.getExpression() != null ? column.getExpression() : "");
    }
    finally {
      updatingFromModel = false;
    }
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }
}
