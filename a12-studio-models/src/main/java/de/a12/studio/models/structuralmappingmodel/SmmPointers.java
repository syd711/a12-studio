package de.a12.studio.models.structuralmappingmodel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The document pointers the kernel (and a12-studio's validation) address the elements of a Structural Mapping
 * Model with, e.g. {@code /content/MappingBlocks[1]/ResolutionStrategies[2]}. Repetitions are 1-based.
 */
public final class SmmPointers {

  private static final Pattern PART = Pattern.compile("/([A-Za-z]+)(?:\\[(\\d+)])?");

  private SmmPointers() {
  }

  public static String groupToClear(int index) {
    return "/content/GroupsToClearOnFirstFill[" + (index + 1) + "]";
  }

  public static String mappingBlock(int blockIndex) {
    return "/content/MappingBlocks[" + (blockIndex + 1) + "]";
  }

  public static String resolutionStrategy(int blockIndex, int index) {
    return mappingBlock(blockIndex) + "/ResolutionStrategies[" + (index + 1) + "]";
  }

  public static String fieldMapping(int blockIndex, int index) {
    return mappingBlock(blockIndex) + "/FieldMappings[" + (index + 1) + "]";
  }

  /**
   * Cuts a kernel pointer down to the element of the model it is about: {@code /content[1]/MappingBlocks[1]/FieldMappings[3]/targetFieldFullName[1]}
   * to {@code /content/MappingBlocks[1]/FieldMappings[3]}. Anything that is not about a group-to-clear, mapping
   * block, resolution strategy or field mapping gives {@code /content}.
   */
  public static String normalize(String kernelPointer) {
    StringBuilder pointer = new StringBuilder("/content");
    if (kernelPointer == null) {
      return pointer.toString();
    }
    Matcher matcher = PART.matcher(kernelPointer);
    // The first part is /content[1] itself; of the rest only the mapping block and its strategy/mapping are kept.
    for (int part = 0; matcher.find() && part <= 2; part++) {
      if (part == 0) {
        continue;
      }
      String name = matcher.group(1);
      if (part == 2 && !name.equals("ResolutionStrategies") && !name.equals("FieldMappings")) {
        break;
      }
      pointer.append('/').append(name).append('[').append(matcher.group(2) != null ? matcher.group(2) : "1").append(']');
    }
    return pointer.toString();
  }
}
