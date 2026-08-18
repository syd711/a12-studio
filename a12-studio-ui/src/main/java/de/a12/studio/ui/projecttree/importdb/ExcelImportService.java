package de.a12.studio.ui.projecttree.importdb;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads Microsoft Excel workbooks (.xlsx / .xls) using Apache POI and exposes sheet names and
 * column metadata suitable for importing into a Document Model.
 *
 * <p>The service assumes the <em>first non-empty row</em> of a sheet is its header row. Column
 * names are taken from the header cells. Column field types are inferred by scanning up to
 * {@value #MAX_SAMPLE_ROWS} data rows below the header and choosing the most specific type that
 * fits all sampled values (STRING is the fallback when types conflict or the column is empty).
 */
@Slf4j
public class ExcelImportService {

  /** Maximum number of data rows to examine when inferring a column's field type. */
  private static final int MAX_SAMPLE_ROWS = 100;

  // -------------------------------------------------------------------------
  // Public API
  // -------------------------------------------------------------------------

  /**
   * Column name + inferred Document Model field type, mirroring {@link AccessImportService.ColumnInfo}
   * so that the rest of the import pipeline ({@code ProjectTreeMenuActions}) can handle both
   * sources uniformly.
   */
  public record ColumnInfo(@NonNull String name, @NonNull AccessImportService.ColumnFieldType fieldType) {
  }

  /**
   * Reads the columns of the <em>first</em> sheet in the workbook. This is the entry point used
   * by the import dialog, which does not ask the user to select a sheet.
   *
   * @param excelFile an {@code .xlsx} or {@code .xls} file
   * @return ordered list of column infos; empty if the first sheet has no header row
   * @throws IOException if the file cannot be opened or contains no sheets
   */
  public List<ColumnInfo> readFirstSheetColumns(@NonNull File excelFile) throws IOException {
    try (Workbook wb = WorkbookFactory.create(excelFile, null, true)) {
      if (wb.getNumberOfSheets() == 0) {
        throw new IOException("The workbook contains no sheets.");
      }
      Sheet sheet = wb.getSheetAt(0);
      return readColumnsFromSheet(sheet);
    }
  }

  /**
   * Returns the names of all sheets in the workbook, in their natural order.
   *
   * @param excelFile an {@code .xlsx} or {@code .xls} file
   * @return ordered list of sheet names (never {@code null}, may be empty)
   * @throws IOException if the file cannot be opened or is not a recognised Excel format
   */
  public List<String> readSheetNames(@NonNull File excelFile) throws IOException {
    try (Workbook wb = WorkbookFactory.create(excelFile, null, true)) {
      List<String> names = new ArrayList<>(wb.getNumberOfSheets());
      for (int i = 0; i < wb.getNumberOfSheets(); i++) {
        names.add(wb.getSheetName(i));
      }
      return names;
    }
  }

  /**
   * Reads the columns of the given sheet.  The first non-empty row is treated as the header; cells
   * below it are sampled to infer the best-fitting Document Model field type for each column.
   *
   * @param excelFile an {@code .xlsx} or {@code .xls} file
   * @param sheetName the exact sheet name as returned by {@link #readSheetNames}
   * @return ordered list of column infos; empty if the sheet has no header row
   * @throws IOException if the file cannot be opened or the sheet does not exist
   */
  public List<ColumnInfo> readColumns(@NonNull File excelFile, @NonNull String sheetName) throws IOException {
    try (Workbook wb = WorkbookFactory.create(excelFile, null, true)) {
      Sheet sheet = wb.getSheet(sheetName);
      if (sheet == null) {
        throw new IOException("Sheet not found: " + sheetName);
      }
      return readColumnsFromSheet(sheet);
    }
  }

  // -------------------------------------------------------------------------
  // Shared column-reading logic
  // -------------------------------------------------------------------------

  private List<ColumnInfo> readColumnsFromSheet(@NonNull Sheet sheet) {
    // Locate the first non-empty row (the header).
    Row headerRow = findHeaderRow(sheet);
    if (headerRow == null) {
      return List.of();
    }

    int firstCol = headerRow.getFirstCellNum();
    int lastCol = headerRow.getLastCellNum(); // exclusive

    // Collect header names and initialise inferred type tracking per column.
    List<String> headers = new ArrayList<>();
    for (int c = firstCol; c < lastCol; c++) {
      Cell cell = headerRow.getCell(c);
      String name = cell != null ? cell.toString().trim() : "";
      headers.add(name.isBlank() ? "Column" + (c + 1) : name);
    }

    // Infer column types from subsequent data rows.
    AccessImportService.ColumnFieldType[] inferred =
        new AccessImportService.ColumnFieldType[headers.size()];
    int headerRowIdx = headerRow.getRowNum();
    int sampledRows = 0;
    for (int r = headerRowIdx + 1;
         r <= sheet.getLastRowNum() && sampledRows < MAX_SAMPLE_ROWS; r++) {
      Row row = sheet.getRow(r);
      if (row == null) {
        continue;
      }
      sampledRows++;
      for (int c = firstCol; c < lastCol; c++) {
        int idx = c - firstCol;
        if (inferred[idx] == AccessImportService.ColumnFieldType.STRING) {
          continue; // already fallen back to STRING – no point scanning further
        }
        Cell cell = row.getCell(c);
        AccessImportService.ColumnFieldType cellType = inferCellType(cell);
        inferred[idx] = mergeTypes(inferred[idx], cellType);
      }
    }

    // Build results – default to STRING for columns that had no data.
    List<ColumnInfo> result = new ArrayList<>(headers.size());
    for (int i = 0; i < headers.size(); i++) {
      AccessImportService.ColumnFieldType type =
          inferred[i] != null ? inferred[i] : AccessImportService.ColumnFieldType.STRING;
      result.add(new ColumnInfo(headers.get(i), type));
    }
    return result;
  }

  // -------------------------------------------------------------------------
  // Internal helpers
  // -------------------------------------------------------------------------

  @Nullable
  private Row findHeaderRow(@NonNull Sheet sheet) {
    for (int r = sheet.getFirstRowNum(); r <= sheet.getLastRowNum(); r++) {
      Row row = sheet.getRow(r);
      if (row != null && row.getFirstCellNum() >= 0) {
        return row;
      }
    }
    return null;
  }

  /**
   * Maps a single POI {@link Cell} to the most specific {@link AccessImportService.ColumnFieldType}
   * that describes its value. Blank / null cells return {@code null} (no information).
   */
  @Nullable
  private AccessImportService.ColumnFieldType inferCellType(@Nullable Cell cell) {
    if (cell == null) {
      return null;
    }
    CellType type = cell.getCellType() == CellType.FORMULA
        ? cell.getCachedFormulaResultType()
        : cell.getCellType();

    return switch (type) {
      case BOOLEAN -> AccessImportService.ColumnFieldType.BOOLEAN;
      case NUMERIC -> DateUtil.isCellDateFormatted(cell)
          ? AccessImportService.ColumnFieldType.DATE_TIME
          : AccessImportService.ColumnFieldType.NUMBER;
      case STRING -> AccessImportService.ColumnFieldType.STRING;
      default -> null; // BLANK, ERROR, _NONE – no information
    };
  }

  /**
   * Combines the current accumulated type for a column with the type inferred from one more cell.
   *
   * <p>Type precedence (most to least specific):
   * <ol>
   *   <li>{@code null} – no data yet → defer to the new observation</li>
   *   <li>{@code BOOLEAN} – only stays BOOLEAN if all sampled values are also BOOLEAN</li>
   *   <li>{@code DATE_TIME} / {@code DATE} – only stays if numeric cell types are date-formatted</li>
   *   <li>{@code NUMBER} – loses to STRING, DATE/DATE_TIME on type mismatch</li>
   *   <li>{@code STRING} – the universal fallback; once reached, stays STRING</li>
   * </ol>
   */
  @NonNull
  private AccessImportService.ColumnFieldType mergeTypes(
      @Nullable AccessImportService.ColumnFieldType accumulated,
      @Nullable AccessImportService.ColumnFieldType observed) {

    if (observed == null) {
      // blank cell → no new information, keep whatever we have
      return accumulated != null ? accumulated : AccessImportService.ColumnFieldType.STRING;
    }
    if (accumulated == null) {
      return observed;
    }
    if (accumulated == observed) {
      return accumulated;
    }
    // Any mismatch between distinct types → fall back to STRING
    return AccessImportService.ColumnFieldType.STRING;
  }
}
