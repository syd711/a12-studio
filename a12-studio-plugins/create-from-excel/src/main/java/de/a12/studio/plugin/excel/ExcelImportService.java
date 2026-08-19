package de.a12.studio.plugin.excel;

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
   * The subset of Document Model field types we map Excel column types to.
   */
  public enum ColumnFieldType {
    STRING,
    NUMBER,
    BOOLEAN,
    DATE,
    DATE_TIME
  }

  /**
   * Column name + inferred Document Model field type.
   */
  public record ColumnInfo(@NonNull String name, @NonNull ColumnFieldType fieldType) {
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
   * Reads the columns of the given sheet. The first non-empty row is treated as the header; cells
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
    Row headerRow = findHeaderRow(sheet);
    if (headerRow == null) {
      return List.of();
    }

    int firstCol = headerRow.getFirstCellNum();
    int lastCol = headerRow.getLastCellNum(); // exclusive

    List<String> headers = new ArrayList<>();
    for (int c = firstCol; c < lastCol; c++) {
      Cell cell = headerRow.getCell(c);
      String name = cell != null ? cell.toString().trim() : "";
      headers.add(name.isBlank() ? "Column" + (c + 1) : name);
    }

    ColumnFieldType[] inferred = new ColumnFieldType[headers.size()];
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
        if (inferred[idx] == ColumnFieldType.STRING) {
          continue;
        }
        Cell cell = row.getCell(c);
        ColumnFieldType cellType = inferCellType(cell);
        inferred[idx] = mergeTypes(inferred[idx], cellType);
      }
    }

    List<ColumnInfo> result = new ArrayList<>(headers.size());
    for (int i = 0; i < headers.size(); i++) {
      ColumnFieldType type = inferred[i] != null ? inferred[i] : ColumnFieldType.STRING;
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

  @Nullable
  private ColumnFieldType inferCellType(@Nullable Cell cell) {
    if (cell == null) {
      return null;
    }
    CellType type = cell.getCellType() == CellType.FORMULA
        ? cell.getCachedFormulaResultType()
        : cell.getCellType();

    return switch (type) {
      case BOOLEAN -> ColumnFieldType.BOOLEAN;
      case NUMERIC -> DateUtil.isCellDateFormatted(cell)
          ? ColumnFieldType.DATE_TIME
          : ColumnFieldType.NUMBER;
      case STRING -> ColumnFieldType.STRING;
      default -> null;
    };
  }

  @NonNull
  private ColumnFieldType mergeTypes(
      @Nullable ColumnFieldType accumulated,
      @Nullable ColumnFieldType observed) {

    if (observed == null) {
      return accumulated != null ? accumulated : ColumnFieldType.STRING;
    }
    if (accumulated == null) {
      return observed;
    }
    if (accumulated == observed) {
      return accumulated;
    }
    return ColumnFieldType.STRING;
  }
}
