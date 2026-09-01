package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.A12Model;
import de.a12.studio.models.Annotation;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.util.ProjectDocumentModels;
import javafx.scene.Node;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

  /**
   * Seeds this panel's rows without binding to a model, for {@link
   * de.a12.studio.ui.projecttree.dialogs.NewModelDialogController}, which only creates the model once its
   * dialog is confirmed. Edits made here are read back via {@link #getRoles()} on submit -- {@link
   * #commitRolesChange} stays a no-op the whole time since {@link #model} is never set.
   */
  public void initializeRoles(@NonNull List<String> initialRoles) {
    roles.clear();
    roles.addAll(initialRoles);
    rebuildRows();
  }

  /**
   * The current, trimmed, non-blank roles -- the counterpart to {@link #initializeRoles} for callers that
   * never bind this panel to a model via {@link #setModel}.
   */
  public List<String> getRoles() {
    return roles.stream().map(String::trim).filter(value -> !value.isEmpty()).toList();
  }

  /**
   * The roles declared on the project's Application Model's own {@code roles} header annotation, used to
   * seed a new model's roles panel (see {@link #initializeRoles}) so it defaults to the roles the app is
   * already scoped to. Empty if the project has no Application Model, or it declares no roles.
   */
  public static List<String> findApplicationModelRoles(@NonNull ProjectItem contextItem) {
    return ProjectDocumentModels.getOtherModelsOfType(contextItem, ModelType.APPLICATION).stream()
        .map(RolesEditorPanelController::findRolesAnnotation)
        .filter(Objects::nonNull)
        .flatMap(annotation -> parseRoles(annotation.getValue()).stream())
        .distinct()
        .sorted()
        .toList();
  }

  /**
   * Sets the {@code roles} header annotation on a freshly created model (see {@link #initializeRoles}'s
   * caller). Unlike {@link #commitRolesChange}, this is a one-shot write for a model that cannot already
   * carry the annotation, so there's nothing to update or remove.
   */
  public static void applyRoles(@NonNull A12Model<?> model, @NonNull List<String> roles) {
    String joined = roles.stream().map(String::trim).filter(value -> !value.isEmpty()).collect(Collectors.joining(","));
    if (joined.isEmpty()) {
      return;
    }
    Annotation annotation = new Annotation();
    annotation.setName(ROLES_ANNOTATION_NAME);
    annotation.setValue(joined);
    model.getAnnotations().add(annotation);
    Studio.getCurrentProject().getAnnotationHeaderRegistry().addName(model.getModelType(), ROLES_ANNOTATION_NAME, joined);
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
        Studio.getCurrentProject().getAnnotationHeaderRegistry().removeName(currentModelType, ROLES_ANNOTATION_NAME);
      }
    } else if (rolesAnnotation != null) {
      rolesAnnotation.setValue(joined);
      Studio.getCurrentProject().getAnnotationHeaderRegistry().setValue(currentModelType, ROLES_ANNOTATION_NAME, joined);
    } else {
      Annotation annotation = new Annotation();
      annotation.setName(ROLES_ANNOTATION_NAME);
      annotation.setValue(joined);
      model.getAnnotations().add(annotation);
      Studio.getCurrentProject().getAnnotationHeaderRegistry().addName(currentModelType, ROLES_ANNOTATION_NAME, joined);
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
