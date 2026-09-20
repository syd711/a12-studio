package de.a12.studio.ui.editors.documentmodel.commands;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.TypeDefinition;
import de.a12.studio.modelsvalidation.documentinsertion.DocumentModelInsertion;
import de.a12.studio.ui.util.commandstack.Command;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Applies a {@link DocumentModelInsertion.Plan} as one undoable step: the copied groups at {@code index} of
 * {@code siblings}, plus the type definitions and Type Definition Model imports the copied fields depend on.
 */
public class InsertModelContentCommand implements Command {

  private final DocumentModel model;

  private final List<Element> siblings;

  private final int index;

  private final List<GroupElement> groups;

  private final List<TypeDefinition> typeDefinitions;

  private final List<ModelReference> importReferences;

  public InsertModelContentCommand(@NonNull DocumentModel model, @NonNull List<Element> siblings, int index,
                                   @NonNull List<GroupElement> groups, @NonNull List<TypeDefinition> typeDefinitions,
                                   @NonNull List<ModelReference> importReferences) {
    this.model = model;
    this.siblings = siblings;
    this.index = index;
    this.groups = List.copyOf(groups);
    this.typeDefinitions = List.copyOf(typeDefinitions);
    this.importReferences = List.copyOf(importReferences);
  }

  @Override
  public void execute() {
    siblings.addAll(Math.min(index, siblings.size()), groups);
    model.getContent().getTypeDefinitions().addAll(typeDefinitions);
    model.getModelReferences().addAll(importReferences);
  }

  @Override
  public void undo() {
    siblings.removeAll(groups);
    model.getContent().getTypeDefinitions().removeAll(typeDefinitions);
    model.getModelReferences().removeAll(importReferences);
  }
}
