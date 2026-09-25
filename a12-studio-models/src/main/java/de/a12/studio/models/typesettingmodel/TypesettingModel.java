package de.a12.studio.models.typesettingmodel;

import de.a12.studio.models.A12Model;

/**
 * A Print Typesetting Model ({@code modelType "typesetting"}, filename suffix {@code TSM}): typography rules the
 * print engine applies to text, referenced from a Print Model's Text Styles. Mirrors SME's
 * {@code printTypesettingModel} module, whose editor is the {@code print-typesetting} library's
 * {@code TypesettingModelEditor}.
 *
 * <p>Unlike most model types it has no {@code locales}/{@code labels}/{@code modelReferences} in its header - the
 * only header setting it carries is the {@code roles} annotation (see {@code RolesEditorPanelController}), which
 * is why its Model Settings dialog shows just the Roles panel.
 */
public class TypesettingModel extends A12Model<TypesettingModelContent> {
}
