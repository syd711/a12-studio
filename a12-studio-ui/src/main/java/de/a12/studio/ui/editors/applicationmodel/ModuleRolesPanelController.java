package de.a12.studio.ui.editors.applicationmodel;

import de.a12.studio.models.applicationmodel.Menu;
import de.a12.studio.models.applicationmodel.Module;
import de.a12.studio.ui.editors.propertyeditors.AbstractRolesPanelController;
import javafx.scene.Node;
import org.jspecify.annotations.NonNull;

/**
 * Edits the comma-separated {@code permission} (roles) of a single {@link Module}'s {@link Menu}, e.g.
 * {@code "tester,reviewer"}. Not bound to a single Element (roles live on the module's menu), so it follows the
 * model-header pattern used by {@link de.a12.studio.ui.editors.propertyeditors.RolesEditorPanelController}, minus the roles-file warning (which only
 * makes sense for a whole model's header roles annotation).
 */
public class ModuleRolesPanelController extends AbstractRolesPanelController {

  private Module module;

  public void setModule(@NonNull Module module) {
    this.module = module;
    Menu menu = module.getMenu();
    roles.clear();
    roles.addAll(parseRoles(menu == null ? null : menu.getPermission()));
    rebuildRows();
  }

  @Override
  protected Node createRoleField(int index) {
    return createRoleComboBox(index, "module-role-");
  }

  @Override
  protected void commitRolesChange() {
    if (module == null) {
      return;
    }

    String joined = joinRoles();
    module.getOrCreateMenu().setPermission(joined.isEmpty() ? null : joined);

    commitChange();
  }
}
