package de.a12.studio.plugin.access;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Table;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Reads Microsoft Access databases (.accdb / .mdb) using Jackcess (pure Java, no ODBC required)
 * and exposes table names and column metadata for import into a Document Model.
 */
@Slf4j
public class AccessImportService {

  public record ColumnInfo(String name, ColumnFieldType fieldType) {
  }

  /**
   * The subset of Document Model field types we map Access column types to.
   * We deliberately keep this narrow – exotic or rarely-used Access types fall back to String.
   */
  public enum ColumnFieldType {
    STRING,
    NUMBER,
    BOOLEAN,
    DATE,
    DATE_TIME
  }

  /**
   * Returns the names of all user-visible tables in the given Access database file.
   *
   * @param accessFile an .accdb or .mdb file
   * @return sorted list of table names
   * @throws IOException if the file cannot be read
   */
  public List<String> readTableNames(@NonNull File accessFile) throws IOException {
    try (Database db = DatabaseBuilder.open(accessFile)) {
      Set<String> tableNames = db.getTableNames();
      List<String> result = new ArrayList<>(tableNames);
      result.sort(String::compareToIgnoreCase);
      return result;
    }
  }

  /**
   * Returns the columns of the given table in the Access database, in their natural order.
   *
   * @param accessFile an .accdb or .mdb file
   * @param tableName  the exact table name as returned by {@link #readTableNames}
   * @return ordered list of column infos
   * @throws IOException if the file cannot be read or the table does not exist
   */
  public List<ColumnInfo> readColumns(@NonNull File accessFile, @NonNull String tableName) throws IOException {
    try (Database db = DatabaseBuilder.open(accessFile)) {
      Table table = db.getTable(tableName);
      if (table == null) {
        throw new IOException("Table not found: " + tableName);
      }
      List<ColumnInfo> result = new ArrayList<>();
      for (Column col : table.getColumns()) {
        result.add(new ColumnInfo(col.getName(), mapDataType(col.getType())));
      }
      return result;
    }
  }

  /**
   * Maps a Jackcess {@link DataType} to one of our simplified {@link ColumnFieldType} values.
   * Unknown / unsupported types default to {@link ColumnFieldType#STRING}.
   */
  private ColumnFieldType mapDataType(@NonNull DataType dataType) {
    return switch (dataType) {
      case BOOLEAN -> ColumnFieldType.BOOLEAN;
      case BYTE, INT, LONG, FLOAT, DOUBLE, NUMERIC, MONEY -> ColumnFieldType.NUMBER;
      case SHORT_DATE_TIME -> ColumnFieldType.DATE_TIME;
      default -> ColumnFieldType.STRING;
    };
  }
}
