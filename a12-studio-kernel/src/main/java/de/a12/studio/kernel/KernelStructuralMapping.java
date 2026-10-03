package de.a12.studio.kernel;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import com.mgmtp.a12.kernel.md.datatransfer.a12internal.mappingmodel.MappingModelService;
import com.mgmtp.a12.kernel.md.datatransfer.a12internal.mappingmodel.SourceAndTargetDMForSMM;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.serializer.model.a12internal.services.DocumentModelSerializer;
import com.mgmtp.a12.kernel.md.structuralmapping.a12internal.services.StructuralMappingMetaModelService;
import com.mgmtp.a12.kernel.md.structuralmapping.a12internal.services.StructuralMappingModelService;
import com.mgmtp.a12.kernel.md.structuralmapping.a12internal.util.SMMNotificationSource;
import com.mgmtp.a12.kernel.mmtypings.mm_mappingmodel_2.views.MM_MappingModel_2;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views.MM_StructuralMappingModel_1;
import com.mgmtp.a12.model.notification.RankedNotification;

/**
 * Facade over the kernel's Structural Mapping Model services, the in-process counterpart of the SME backend's
 * {@code StructuralMappingModelController}, {@code FieldMappingController}, {@code ResolutionStrategyController}
 * and the {@code compute-source-and-target-model} endpoint of its mapping model controller. Everything crosses
 * this boundary as JSON text (the {@code .json} content of the models), so no kernel type leaks out.
 *
 * <p>The structural mapping model is always edited <em>in the context of</em> a Mapping Model: the Document
 * Models the field mappings point into are not the project's Document Models but the <em>source</em> (all
 * Source Models joined, plus the precomputation fragment) and the <em>target</em> the kernel derives from the
 * Mapping Model ({@link #computeContext}).
 *
 * <p>Uses the kernel's {@code a12internal} services (no public equivalent exists), which is why it lives only here.
 */
public final class KernelStructuralMapping {

  /** The Document Models an SMM is checked and edited against, as computed by {@link #computeContext}. */
  public record Context(String sourceDocumentModelJson, String targetDocumentModelJson) {
  }

  /**
   * One way of adding a field mapping.
   *
   * @param mappingBlockIndex 1-based index of the mapping block the field mapping ends up in; one past the
   *                          current number of blocks means "a new mapping block"
   * @param modifiedModelJson the whole SMM with the field mapping added
   */
  public record AddOption(int mappingBlockIndex, String modifiedModelJson) {
  }

  private static final Locale LOCALE = Locale.US;

  private final DocumentModelServiceFactory factory = new DocumentModelServiceFactory();

  /** Reading the two Document Models is the expensive part of a service; one service per context, for as long as the context lives. */
  private final Map<Context, StructuralMappingModelService> services = Collections.synchronizedMap(new WeakHashMap<>());

  /**
   * Computes the source and target Document Model of a Mapping Model.
   *
   * @param mappingModelJson the Mapping Model's {@code .json}
   * @param source           lookup of Document Model json by id (the Mapping Model's Source, Target and
   *                         PreComputationFragment models and whatever they include)
   * @throws KernelException if the Mapping Model or one of its Document Models cannot be read or combined
   */
  public Context computeContext(String mappingModelJson, KernelModelSource source) {
    try {
      MM_MappingModel_2 mappingModel = MappingModelService.deserialize(new StringReader(mappingModelJson), failOnProblem("mapping model"));
      IDocumentModelResolver resolver = id -> {
        try {
          return factory.createDocumentModelSerializer().deserialize(new StringReader(source.load(id)));
        }
        catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      };
      SourceAndTargetDMForSMM result = MappingModelService.create(resolver)
          .createSourceAndTargetDMForSMM(mappingModel, failOnError("mapping model"), LOCALE);
      return new Context(serialize(result.sourceDM()), serialize(result.targetDM()));
    }
    catch (RuntimeException e) {
      throw new KernelException("Cannot compute the source and target model of the mapping model: " + e.getMessage(), e);
    }
  }

  /** Consistency check that needs no Document Models: the structure of the SMM only. */
  public List<KernelFinding> checkStandalone(String smmJson) {
    try {
      List<KernelFinding> findings = new ArrayList<>();
      StructuralMappingModelService.checkConsistencyStandalone(deserialize(smmJson), collect(findings), LOCALE);
      return findings;
    }
    catch (RuntimeException e) {
      throw new KernelException("Cannot check the structural mapping model: " + e.getMessage(), e);
    }
  }

  /**
   * The full consistency check of the SMM against its source and target Document Model: unknown entities,
   * incompatible types, resolution strategies that cross, field mappings without applicable resolution
   * strategy, ... Findings carry the document pointer of the offending element in
   * {@link KernelFinding#elementPath()}, e.g. {@code /content/MappingBlocks[1]/FieldMappings[3]}.
   */
  public List<KernelFinding> checkFull(String smmJson, Context context) {
    try {
      List<KernelFinding> findings = new ArrayList<>();
      service(context).checkConsistencyFull(deserialize(smmJson), collect(findings), LOCALE);
      return findings;
    }
    catch (RuntimeException e) {
      throw new KernelException("Cannot check the structural mapping model: " + e.getMessage(), e);
    }
  }

  /**
   * Adds a field mapping, creating the resolution strategies it needs (the kernel decides where it goes).
   *
   * @return the whole SMM with the field mapping added
   */
  public String addFieldMapping(String smmJson, Context context, String sourceFieldPath, String targetFieldPath) {
    try {
      return serialize(service(context).addFieldMapping(deserialize(smmJson), sourceFieldPath, targetFieldPath));
    }
    catch (RuntimeException e) {
      throw new KernelException("Cannot add the field mapping " + sourceFieldPath + " -> " + targetFieldPath + ": " + e.getMessage(), e);
    }
  }

  /** Every mapping block an (existing or new) field mapping could be moved to, each with the resulting SMM. */
  public List<AddOption> addOptions(String smmJson, Context context, String sourceFieldPath, String targetFieldPath) {
    try {
      return service(context).optionsForAddFieldMapping(deserialize(smmJson), sourceFieldPath, targetFieldPath).stream()
          .map(option -> new AddOption(option.repetitionOfModifiedMappingBlock(), serialize(option.modifiedSMM())))
          .toList();
    }
    catch (RuntimeException e) {
      throw new KernelException("Cannot determine the mapping blocks for " + sourceFieldPath + " -> " + targetFieldPath + ": " + e.getMessage(), e);
    }
  }

  /**
   * @param resolutionStrategyPointer document pointer of the strategy, e.g. {@code /content/MappingBlocks[1]/ResolutionStrategies[2]}
   * @return the full names of the groups that are valid as the strategy's source group
   */
  public Set<String> validSourceGroups(String smmJson, Context context, String resolutionStrategyPointer) {
    try {
      return new TreeSet<>(service(context).findValidSourceGroups(deserialize(smmJson), DocumentPointer.of(resolutionStrategyPointer)));
    }
    catch (RuntimeException e) {
      throw new KernelException("Cannot determine the source groups: " + e.getMessage(), e);
    }
  }

  /** The full names of the fields that are valid as the slice source field of the strategy. */
  public Set<String> validSliceSourceFields(String smmJson, Context context, String resolutionStrategyPointer) {
    try {
      return new TreeSet<>(service(context).findValidSliceSourceFields(deserialize(smmJson), DocumentPointer.of(resolutionStrategyPointer)));
    }
    catch (RuntimeException e) {
      throw new KernelException("Cannot determine the slice source fields: " + e.getMessage(), e);
    }
  }

  /** The full names of the fields that are valid as the slice target field of the strategy. */
  public Set<String> validSliceTargetFields(String smmJson, Context context, String resolutionStrategyPointer) {
    try {
      return new TreeSet<>(service(context).findValidSliceTargetFields(deserialize(smmJson), DocumentPointer.of(resolutionStrategyPointer)));
    }
    catch (RuntimeException e) {
      throw new KernelException("Cannot determine the slice target fields: " + e.getMessage(), e);
    }
  }

  /** The strategy types ({@code Fold}, {@code Slice}) that are valid for the strategy. */
  public Set<String> validResolutionStrategyTypes(String smmJson, Context context, String resolutionStrategyPointer) {
    try {
      Set<String> types = new TreeSet<>();
      service(context).findValidResolutionStrategyTypes(deserialize(smmJson), DocumentPointer.of(resolutionStrategyPointer))
          .forEach(type -> types.add(type.name().equalsIgnoreCase("Slice") ? "Slice" : "Fold"));
      return types;
    }
    catch (RuntimeException e) {
      throw new KernelException("Cannot determine the resolution strategy types: " + e.getMessage(), e);
    }
  }

  private StructuralMappingModelService service(Context context) {
    return services.computeIfAbsent(context, key -> {
      DocumentModelSerializer serializer = new DocumentModelSerializer();
      DocumentModel sourceModel = serializer.deserialize(new StringReader(key.sourceDocumentModelJson()));
      DocumentModel targetModel = serializer.deserialize(new StringReader(key.targetDocumentModelJson()));
      return StructuralMappingModelService.create(sourceModel, targetModel);
    });
  }

  private static MM_StructuralMappingModel_1 deserialize(String smmJson) {
    return StructuralMappingMetaModelService.deserialize(new StringReader(smmJson), failOnProblem("structural mapping model"));
  }

  private static String serialize(MM_StructuralMappingModel_1 smm) {
    StringWriter writer = new StringWriter();
    StructuralMappingMetaModelService.serialize(smm, writer);
    return writer.toString();
  }

  private static String serialize(DocumentModel documentModel) {
    StringWriter writer = new StringWriter();
    try {
      new DocumentModelSerializer().serialize(documentModel, writer, problem -> {
      });
    }
    catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return writer.toString();
  }

  /** {@code /content/MappingBlocks[1]/FieldMappings[2]}: every path part with its 1-based repetition. */
  private static String pointerOf(DocumentPointer pointer) {
    StringBuilder path = new StringBuilder();
    pointer.getPathParts().forEach(part -> path.append('/').append(part.name()).append('[').append(part.repetitionIndex()).append(']'));
    return path.toString();
  }

  private static Consumer<RankedNotification> collect(List<KernelFinding> findings) {
    return notification -> {
      KernelFinding finding = KernelFindings.of(notification);
      // The source of an SMM notification is the document pointer of the offending element.
      // (its toString() drops the repetition indices; the pointer keeps them).
      String path = notification.getSource() instanceof SMMNotificationSource<?> source ? pointerOf(source.pointer()) : null;
      findings.add(path == null ? finding : new KernelFinding(finding.severity(), finding.message(), path));
    };
  }

  /** Informational notifications (e.g. "no precomputation fragment specified") are normal and ignored. */
  private static Consumer<RankedNotification> failOnError(String what) {
    return notification -> {
      if (KernelFindings.of(notification).isError()) {
        throw new IllegalArgumentException("The " + what + " cannot be used: " + notification.getMessage());
      }
    };
  }

  private static Consumer<RankedNotification> failOnProblem(String what) {
    return notification -> {
      throw new IllegalArgumentException("The " + what + " cannot be read: " + notification.getMessage());
    };
  }
}
