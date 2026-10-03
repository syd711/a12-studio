package de.a12.studio.models.transformermodel;

import de.a12.studio.models.A12Model;

/**
 * A Transformer Model ({@code modelType: "transformer"}, SME's "Transformed Document Model"): the configuration
 * that turns an XSD into a Document Model. Only the configuration is stored; the Document Model it describes is
 * generated on demand (it carries the Transformer Model's id) and is never persisted. See
 * {@link TransformerModelContent}.
 */
public class TransformerModel extends A12Model<TransformerModelContent> {
}
