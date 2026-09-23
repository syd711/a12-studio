package de.a12.studio.models.composeddocumentmodel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One relationship traversed by a {@link ComposedDocumentModel}, from its parent (source) Document Model to a
 * child (target) Document Model - the a12-studio-side in-memory shape of one SME "Relationship Element" (BA
 * docs: {@code cdm.relationship}/{@code cdm.sourceRole}/{@code cdm.targetRole}/{@code cdm.targetDocumentModel}).
 * Not a JSON structure of its own - {@link ComposedDocumentModelResolver} projects it to/from the model
 * header's raw {@code Annotation} list.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CdmRelationshipStep {

  private String relationshipName;
  private String sourceRole;
  private String targetRole;
  private String targetDocumentModel;
}
