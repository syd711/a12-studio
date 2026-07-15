package de.a12.studio.dataservices.services.printmodel;

import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.model.a12internal.FieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.CustomFieldType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.StringType;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.DocumentModelWrapper;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.ExternalDocumentModelSerializerService;
import com.mgmtp.a12.print.engine.api.PdfBoxPrintEngineConfig;
import com.mgmtp.a12.print.engine.api.PdfPrintResult;
import com.mgmtp.a12.print.engine.api.PrintEngineConfig;
import com.mgmtp.a12.print.engine.api.PrintJob;
import com.mgmtp.a12.print.engine.api.PrintJobConfig;
import com.mgmtp.a12.print.engine.api.a12.DocumentDependencyDescriptor;
import com.mgmtp.a12.print.engine.api.constant.ConfigConstants;
import com.mgmtp.a12.print.engine.runtime.KernelDocumentProvider;
import com.mgmtp.a12.print.engine.runtime.PrintEngine;
import com.mgmtp.a12.print.engine.runtime.PrintJobManager;
import com.mgmtp.a12.print.engine.runtime.TypesettingModelProvider;
import com.mgmtp.a12.print.engine.runtime.pdf.PdfPrintEngine;
import com.mgmtp.a12.print.engine.runtime.pdfBox.PdfBoxPrintEngine;
import de.a12.studio.dataservices.services.printmodel.exceptions.PreCompileException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class PrintService {

  private final ExecutorService executorService;

  public PrintService(ExecutorService executorService) {
    this.executorService = executorService;
  }

  public PdfPrintResult print(PrintParameters printParameters) {
    String printModel = printParameters.getPrintModel();
    Map<String, String> printModelMap = printParameters.getPrintModelMap();
    Map<String, String> documentModelMap = printParameters.getDocumentModelMap();
    Map<String, String> typesettingModelMap = printParameters.getTypesettingModelMap();
    var document = printParameters.getDocument();
    Map<String, String> fonts = printParameters.getFonts();
    var locale = printParameters.getLocale();
    var timeZone = printParameters.getTimeZone();
    boolean useLegacyRendering = printParameters.isUseLegacyRendering();

    if (documentModelMap.size() > 1) {
      throw new RuntimeException("Currently a maximum of one model is supported");
    }

    DocumentModelResolver documentModelResolver = new DocumentModelResolver(documentModelMap);
    PrintJob printJob = precompilePrintModel(printModel, printModelMap, documentModelResolver, useLegacyRendering);

    if (printJob != null) {
      printJob.withLocale(locale);
      printJob.withTimeZone(timeZone);
    }

    PrintEngine<PdfPrintResult> pdfPrintEngine = setupPrintEngine(useLegacyRendering, fonts);

    Iterator<String> documentModelIds = documentModelMap.keySet().iterator();
    if (documentModelIds.hasNext() && document.isPresent()) {
      String documentModelId = documentModelIds.next();
      if (printJob == null) {
        throw new IllegalStateException("Print job could not be precompiled");
      }
      printJob.withProvider(
          new KernelDocumentProvider() {
            @Override
            public boolean supports(DocumentDependencyDescriptor documentDependencyDescriptor) {
              return documentDependencyDescriptor.getModelReference().getReference().equals(documentModelId);
            }

            @Override
            public IDocument loadDocument(DocumentDependencyDescriptor descriptor) {
              return new DocumentDeserializer().provide(documentModelId, document.get(), documentModelResolver);
            }
          });
    }

    if (!typesettingModelMap.isEmpty()) {
      if (printJob == null) {
        throw new IllegalStateException("Print job could not be precompiled");
      }
      printJob.withProvider(
          TypesettingModelProvider.fromLoader(id -> {
            String typesetting = typesettingModelMap.get(id);
            if (typesetting == null) {
              throw new RuntimeException("Typesetting for id " + id + " not found");
            }
            return new TypesettingDeserializer().validateAndMarshallTypesettingDto(typesetting);
          }));
    }

    return pdfPrintEngine.execute(printJob);
  }

  public PrintJob precompilePrintModel(
      String printModel, Map<String, String> printModelMap, DocumentModelResolver documentModelResolver, boolean useLegacyRendering) {
    try {
      PrintJobManager printJobManager = getPrintJobManager(printModelMap, documentModelResolver, useLegacyRendering);
      var printModelId = printJobManager.prepare(printModel);
      return printJobManager.createNewJob(printModelId);
    } catch (Exception exception) {
      throw new PreCompileException(exception);
    }
  }

  private PrintJobManager getPrintJobManager(
      Map<String, String> printModelMap, DocumentModelResolver documentModelResolver, boolean useLegacyRendering) {
    return new PrintJobManager(
        executorService,
        new PrintJobManager.PrintJobManagerApi() {
          @Override
          public String loadPrintModel(String id) {
            if (printModelMap.containsKey(id)) {
              return printModelMap.get(id);
            }
            throw new RuntimeException("No model for " + id);
          }

          @Override
          public IDocumentModel loadDocumentModel(String id) {
            return deserializeDocumentModel(id, documentModelResolver);
          }
        },
        PrintJobConfig.DEFAULT,
        !useLegacyRendering);
  }

  private IDocumentModel deserializeDocumentModel(String documentModelId, DocumentModelResolver documentModelResolver) {
    var internDocumentModel =
        ExternalDocumentModelSerializerService.getInternDocumentModel(documentModelResolver.getDocumentModelById(documentModelId));
    for (FieldTypeDefinition fieldTypeDefinition : internDocumentModel.getContent().getTypeDefinitions()) {
      if (fieldTypeDefinition.getFieldType() instanceof CustomFieldType) {
        fieldTypeDefinition.setFieldType(new StringType());
      }
      fieldTypeDefinition.setName(String.format("%s_%s", documentModelId, fieldTypeDefinition.getName()));
    }
    return new DocumentModelWrapper(internDocumentModel);
  }

  private Map<String, String> tagFontsAsAttachments(Map<String, String> fonts) {
    Map<String, String> resultFonts = new HashMap<>();
    for (Map.Entry<String, String> font : fonts.entrySet()) {
      resultFonts.put(font.getKey(), ConfigConstants.ATTACHMENT_SUFFIX + font.getValue());
    }
    return resultFonts;
  }

  private PrintEngine<PdfPrintResult> setupPrintEngine(boolean useLegacyRendering, Map<String, String> fonts) {
    if (useLegacyRendering) {
      return new PdfPrintEngine(
          executorService, PrintEngineConfig.DEFAULT.toBuilder().availableFonts(tagFontsAsAttachments(fonts)).build());
    } else {
      return new PdfBoxPrintEngine(executorService, new PdfBoxPrintEngineConfig(tagFontsAsAttachments(fonts)));
    }
  }
}
