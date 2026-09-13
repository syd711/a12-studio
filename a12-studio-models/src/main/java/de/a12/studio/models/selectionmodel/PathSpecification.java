package de.a12.studio.models.selectionmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * One entry of a {@link SelectionCategory}'s {@code Selected}/{@code Unselected} list: an absolute path
 * into the reference Document Model's element tree (e.g. {@code "/Status/AcquiredAt"}), optionally ending
 * in a group with a trailing {@code "/"} or a {@code "*"} wildcard segment. Matches SME's {@code {path:
 * string}} shape (see {@code SelectionContent} in {@code selectionModel.ts}).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class PathSpecification {

  private String path;
}
