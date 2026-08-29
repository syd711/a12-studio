package de.a12.studio.ui.util;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

/**
 * Reflects a property editor panel's own error container onto the label of every enclosing
 * "sub-tab-panel" {@link Tab} (see {@code stylesheet-tab-pane.css}) as a small red/amber dot, so
 * switching away from a tab that still has an unresolved validation problem doesn't hide the fact
 * that one exists.
 * <p>
 * Driven purely by the live scene graph rather than any registry of panels: {@link
 * de.a12.studio.ui.editors.AbstractPropertyEditor} calls {@link #refresh(Node)} with its own error
 * container's root whenever that container's visibility/severity changes. This walks upward from
 * there, and for every "sub-tab-panel" {@link TabPane} crossed along the way, re-scans the whole
 * content subtree of the {@link Tab} that owns the calling node for the worst severity still
 * present - necessary since a sibling panel's error elsewhere in the same tab must still keep the
 * badge visible even after the panel that triggered this particular refresh has cleared its own.
 */
@Slf4j
public final class TabErrorBadge {

  private static final String SUB_TAB_PANEL_STYLE_CLASS = "sub-tab-panel";
  private static final String ERROR_CONTAINER_STYLE_CLASS = "error-container";
  private static final String BADGE_STYLE_CLASS = "tab-error-badge";
  private static final PseudoClass WARNING_PSEUDO_CLASS = PseudoClass.getPseudoClass("warning");

  private TabErrorBadge() {
  }

  public static void refresh(@NonNull Node fromNode) {
    log.info("[TabErrorBadge] refresh() from {} (parent={}, scene={})", fromNode.getClass().getSimpleName(),
        fromNode.getParent() != null ? fromNode.getParent().getClass().getSimpleName() : "null",
        fromNode.getScene() != null ? "attached" : "null");

    if (fromNode.getScene() == null) {
      // Validation can run (e.g. during the initial model load) before this panel's tree is attached to the
      // app's live Scene, at which point ScrollPane/TabPane haven't reparented their content into their Skin
      // yet (that only happens once a Skin is created, which requires Scene attachment) - so the ancestor walk
      // below would dead-end early and never find the owning Tab. Wait for attachment, then retry one pulse
      // later so that follow-up layout/skin pass has actually run.
      log.info("[TabErrorBadge]   not yet attached to a Scene - deferring refresh until it is");
      fromNode.sceneProperty().addListener(new ChangeListener<Scene>() {
        @Override
        public void changed(ObservableValue<? extends Scene> observable, Scene oldScene, Scene newScene) {
          if (newScene != null) {
            fromNode.sceneProperty().removeListener(this);
            Platform.runLater(() -> refresh(fromNode));
          }
        }
      });
      return;
    }

    // Force any pending CSS/skin pass to run synchronously (e.g. right after the sceneProperty listener above
    // fires), so skin-driven reparenting - ScrollPane/TabPane only add their content as an actual scene-graph
    // child once their Skin exists - is guaranteed to have already happened by the time we walk below, rather
    // than racing whatever the next automatic pulse would have done.
    fromNode.getScene().getRoot().applyCss();
    fromNode.getScene().getRoot().layout();

    Node current = fromNode.getParent();
    boolean foundSubTabPanel = false;
    while (current != null) {
      log.info("[TabErrorBadge]   ancestor: {} styleClass={}", current.getClass().getSimpleName(), current.getStyleClass());
      if (current instanceof TabPane tabPane && tabPane.getStyleClass().contains(SUB_TAB_PANEL_STYLE_CLASS)) {
        foundSubTabPanel = true;
        boolean matchedTab = false;
        for (Tab tab : tabPane.getTabs()) {
          boolean descendant = isDescendant(tab.getContent(), fromNode);
          log.info("[TabErrorBadge]     tab '{}': content={}, isDescendant={}", tabLabel(tab),
              tab.getContent() != null ? tab.getContent().getClass().getSimpleName() : "null", descendant);
          if (descendant) {
            matchedTab = true;
            String severity = worstSeverity(tab.getContent());
            log.info("[TabErrorBadge]     -> matched tab '{}', worstSeverity={}", tabLabel(tab), severity);
            updateBadge(tab, severity);
            break;
          }
        }
        if (!matchedTab) {
          log.info("[TabErrorBadge]   sub-tab-panel TabPane found but no owning Tab matched fromNode");
        }
      }
      current = current.getParent();
    }
    if (!foundSubTabPanel) {
      log.info("[TabErrorBadge]   no enclosing 'sub-tab-panel' TabPane found while walking up from {}", fromNode.getClass().getSimpleName());
    }
  }

  private static boolean isDescendant(Node ancestor, Node node) {
    if (ancestor == null) {
      return false;
    }
    Node current = node;
    while (current != null) {
      if (current == ancestor) {
        return true;
      }
      current = current.getParent();
    }
    return false;
  }

  /** {@code null} if no error/warning is currently visible anywhere under {@code node}, else "ERROR" or "WARNING". */
  private static String worstSeverity(@NonNull Node node) {
    String worst = node.getStyleClass().contains(ERROR_CONTAINER_STYLE_CLASS) && node.isVisible()
        ? (node.getPseudoClassStates().contains(WARNING_PSEUDO_CLASS) ? "WARNING" : "ERROR")
        : null;
    if ("ERROR".equals(worst) || !(node instanceof Parent parent)) {
      return worst;
    }
    for (Node child : parent.getChildrenUnmodifiable()) {
      String childSeverity = worstSeverity(child);
      if ("ERROR".equals(childSeverity)) {
        return "ERROR";
      }
      if (childSeverity != null) {
        worst = childSeverity;
      }
    }
    return worst;
  }

  private static void updateBadge(@NonNull Tab tab, String severity) {
    if (severity == null) {
      Object existing = tab.getProperties().get(BADGE_STYLE_CLASS);
      if (existing instanceof Circle badge) {
        badge.setVisible(false);
        log.info("[TabErrorBadge]     badge hidden for tab '{}'", tabLabel(tab));
      }
      return;
    }
    Circle badge = ensureBadge(tab);
    badge.setVisible(true);
    badge.pseudoClassStateChanged(WARNING_PSEUDO_CLASS, "WARNING".equals(severity));
    log.info("[TabErrorBadge]     badge shown for tab '{}' severity={}, badge.parent={}, badge.scene={}, badge.layoutBounds={}",
        tabLabel(tab), severity,
        badge.getParent() != null ? badge.getParent().getClass().getSimpleName() : "null",
        badge.getScene() != null ? "attached" : "null", badge.getLayoutBounds());
  }

  private static Circle ensureBadge(@NonNull Tab tab) {
    Object existing = tab.getProperties().get(BADGE_STYLE_CLASS);
    if (existing instanceof Circle badge) {
      return badge;
    }

    String originalText = tab.getText();
    log.info("[TabErrorBadge]     creating badge graphic for tab '{}' (had graphic={})", originalText, tab.getGraphic() != null);

    Circle badge = new Circle(4);
    badge.getStyleClass().add(BADGE_STYLE_CLASS);
    badge.setMouseTransparent(true);
    badge.setTranslateX(6);
    badge.setTranslateY(-8);

    Label label = new Label(originalText);

    StackPane graphic = new StackPane(label, badge);
    StackPane.setAlignment(badge, Pos.TOP_RIGHT);

    tab.setText(null);
    tab.setGraphic(graphic);
    tab.getProperties().put(BADGE_STYLE_CLASS, badge);
    // Added only now, after our own setText(null) above, so it doesn't immediately fire and overwrite the
    // label with null; from here on it just keeps the label in sync with any future (external) text change.
    tab.textProperty().addListener((observable, oldValue, newValue) -> label.setText(newValue));
    return badge;
  }

  private static String tabLabel(@NonNull Tab tab) {
    if (tab.getText() != null) {
      return tab.getText();
    }
    if (tab.getGraphic() instanceof StackPane stackPane) {
      for (Node child : stackPane.getChildren()) {
        if (child instanceof Label label) {
          return label.getText();
        }
      }
    }
    return "(unknown)";
  }
}
