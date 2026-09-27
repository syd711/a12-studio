package de.a12.studio.ui.editors.applicationmodel;

import de.a12.studio.models.applicationmodel.Region;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns an {@link de.a12.studio.models.applicationmodel.ApplicationModel}'s region tree into picker options for the region-valued fields
 * (Default Region, a {@link de.a12.studio.models.applicationmodel.Directive}'s own {@code region}) - mirrors
 * SME's {@code regionReferenceProvider}, which shows a breadcrumb-style path ({@code "› CONTENT › HIDDEN"})
 * for every (sub)region while the field's actual value stays the plain region name (the same flat-by-name
 * matching {@link de.a12.studio.modelsvalidation.validators.application.ApplicationSceneGraphValidator} uses,
 * not a hierarchical path). See "Application Model: gap review" gap 7 in {@code
 * docs/sme-reference-comparison.md}.
 */
public final class RegionReferenceOptions {

  private RegionReferenceOptions() {
  }

  /** Every (sub)region's name in {@code root}'s tree, pre-order (root first). */
  public static List<String> regionNames(Region root) {
    List<String> names = new ArrayList<>();
    collectNames(root, names);
    return names;
  }

  private static void collectNames(Region region, List<String> names) {
    if (region == null) {
      return;
    }
    if (region.getName() != null) {
      names.add(region.getName());
    }
    for (Region subRegion : region.getSubRegions()) {
      collectNames(subRegion, names);
    }
  }

  /**
   * The breadcrumb display for {@code regionName} within {@code root}'s tree, e.g. {@code "› CONTENT ›
   * HIDDEN"} - the root region itself is left out of the breadcrumb (it's the implicit top level every path
   * starts from), so a direct child of the root shows as just {@code "› <name>"}. Falls back to the plain
   * name if it isn't found in the tree at all (e.g. free-typed text that doesn't match any region yet).
   */
  public static String breadcrumb(Region root, String regionName) {
    List<String> path = findPath(root, regionName, new ArrayList<>());
    if (path == null || path.isEmpty()) {
      return regionName == null ? "" : regionName;
    }
    List<String> displayPath = path.size() > 1 ? path.subList(1, path.size()) : path;
    return "› " + String.join(" › ", displayPath);
  }

  private static List<String> findPath(Region region, String targetName, List<String> ancestors) {
    if (region == null) {
      return null;
    }
    List<String> path = new ArrayList<>(ancestors);
    path.add(region.getName());
    if (targetName != null && targetName.equals(region.getName())) {
      return path;
    }
    for (Region subRegion : region.getSubRegions()) {
      List<String> found = findPath(subRegion, targetName, path);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  /**
   * Populates {@code comboBox}'s items with every region name in {@code root}'s tree and shows each as its
   * breadcrumb in the dropdown, while the combo box's own editor text (and committed value) stays the plain
   * name a user typed or picked - so free-typing an arbitrary region name (SME: "any value may be entered")
   * still works.
   */
  public static void applyRegionOptions(ComboBox<String> comboBox, Region root) {
    comboBox.getItems().setAll(regionNames(root));
    comboBox.setCellFactory(listView -> createBreadcrumbCell(root));
  }

  private static ListCell<String> createBreadcrumbCell(Region root) {
    return new ListCell<>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : breadcrumb(root, item));
      }
    };
  }
}
