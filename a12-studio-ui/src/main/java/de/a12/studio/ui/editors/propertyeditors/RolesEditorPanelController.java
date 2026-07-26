package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.A12Model;
import de.a12.studio.models.Annotation;
import de.a12.studio.models.ModelType;
import de.a12.studio.ui.editors.AnnotationHeaderRegistry;
import javafx.scene.Node;
import org.jspecify.annotations.NonNull;

import java.io.File;

/**
 * Edits the comma-separated {@code roles} header annotation (see {@code /header/annotations} in a model's
 * json), e.g. {@code "tester,reviewer"}. Not bound to a single {@link Element}
 * (roles live on the model header), so {@link #setElement} is never called and only {@link #setModel} is used.
 */
public class RolesEditorPanelController extends AbstractRolesPanelController {

  private static final String ROLES_ANNOTATION_NAME = "roles";

  private static final String MISSING_ROLES_FILE_WARNING =
      "The workspace should have a roles file if roles are specified in the model.";

  private A12Model<?> model;
  private ModelType currentModelType;

  public void setModel(@NonNull A12Model<?> model) {
    this.model = model;
    this.currentModelType = model.getModelType();
    Annotation rolesAnnotation = findRolesAnnotation(model);
    roles.clear();
    roles.addAll(parseRoles(rolesAnnotation == null ? null : rolesAnnotation.getValue()));
    rebuildRows();
    updateRolesFileWarning();
  }

  @Override
  protected Node createRoleField(int index) {
    return createRoleComboBox(index, "role-");
  }

  @Override
  protected void commitRolesChange() {
    if (model == null) {
      return;
    }

    String joined = joinRoles();

    Annotation rolesAnnotation = findRolesAnnotation(model);
    if (joined.isEmpty()) {
      if (rolesAnnotation != null) {
        model.getAnnotations().remove(rolesAnnotation);
        AnnotationHeaderRegistry.getInstance().removeName(currentModelType, ROLES_ANNOTATION_NAME);
      }
    } else if (rolesAnnotation != null) {
      rolesAnnotation.setValue(joined);
      AnnotationHeaderRegistry.getInstance().setValue(currentModelType, ROLES_ANNOTATION_NAME, joined);
    } else {
      Annotation annotation = new Annotation();
      annotation.setName(ROLES_ANNOTATION_NAME);
      annotation.setValue(joined);
      model.getAnnotations().add(annotation);
      AnnotationHeaderRegistry.getInstance().addName(currentModelType, ROLES_ANNOTATION_NAME, joined);
    }

    commitChange();
    updateRolesFileWarning();
  }

  /**
   * Mirrors SME's {@code shouldHaveRolesModelIfSpecifyingRoles} rule: roles typed on a model are free text
   * unless the workspace has a roles file to validate them against, so warn (rather than block) when roles
   * are specified but no such file exists.
   */
  private void updateRolesFileWarning() {
    boolean rolesSpecified = roles.stream().anyMatch(role -> !role.isBlank());
    if (rolesSpecified && !workspaceHasRolesFile()) {
      showError("WARNING", MISSING_ROLES_FILE_WARNING);
    } else {
      hideError();
    }
  }

  private static boolean workspaceHasRolesFile() {
    File rolesFile = resolveWorkspaceRolesFile();
    return rolesFile == null || rolesFile.isFile();
  }

  private static Annotation findRolesAnnotation(A12Model<?> model) {
    for (Annotation annotation : model.getAnnotations()) {
      if (ROLES_ANNOTATION_NAME.equals(annotation.getName())) {
        return annotation;
      }
    }
    return null;
  }
}
