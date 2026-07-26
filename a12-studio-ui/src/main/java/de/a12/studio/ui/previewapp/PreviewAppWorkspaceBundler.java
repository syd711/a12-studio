package de.a12.studio.ui.previewapp;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.GZIPOutputStream;

/**
 * Writes {@code <workspace>/bundled/seed.tar.gz}: a gzip-compressed tar of the whole workspace
 * (project) folder, excluding the {@code bundled} folder itself. This is what the real
 * preview-app-server jar expects as its data seed, mirroring {@code bundleWorkspace()} in the
 * "A12 Preview App Control" Electron tool.
 *
 * <p>Hand-rolled rather than pulling in a tar library (e.g. commons-compress), consistent with
 * this codebase's existing {@code ZipUtil}, which likewise hand-rolls zip handling on top of
 * {@code java.util.zip}. Only regular files and directories are written (no symlinks/devices),
 * which is all a project workspace ever contains.
 */
@Slf4j
public class PreviewAppWorkspaceBundler {

  private static final String BUNDLED_FOLDER_NAME = "bundled";

  private static final int BLOCK_SIZE = 512;

  private PreviewAppWorkspaceBundler() {
  }

  public static File bundle(File workspaceFolder) throws IOException {
    File bundledDir = new File(workspaceFolder, BUNDLED_FOLDER_NAME);
    if (!bundledDir.isDirectory() && !bundledDir.mkdirs()) {
      throw new IOException("Failed to create directory " + bundledDir.getAbsolutePath());
    }

    File seedFile = new File(bundledDir, "seed.tar.gz");
    try (OutputStream fos = new FileOutputStream(seedFile);
         GZIPOutputStream gzos = new GZIPOutputStream(fos)) {
      writeEntry(gzos, workspaceFolder, workspaceFolder, bundledDir);
      writeEndOfArchive(gzos);
    }

    log.info("Bundled workspace \"{}\" into \"{}\"", workspaceFolder.getAbsolutePath(), seedFile.getAbsolutePath());
    return seedFile;
  }

  /**
   * Recurses into {@code currentDir}, writing each entry with a path relative to the fixed
   * {@code workspaceRoot} (not to {@code currentDir}) so nested files keep their full relative
   * path in the archive instead of being flattened to just their own name.
   */
  private static void writeEntry(OutputStream out, File workspaceRoot, File currentDir, File excluded) throws IOException {
    File[] children = currentDir.listFiles();
    if (children == null) {
      return;
    }

    for (File child : children) {
      if (child.equals(excluded)) {
        continue;
      }

      String relativePath = workspaceRoot.toPath().relativize(child.toPath()).toString().replace('\\', '/');
      if (child.isDirectory()) {
        writeHeader(out, relativePath + "/", 0, true);
        writeEntry(out, workspaceRoot, child, excluded);
      }
      else {
        long size = child.length();
        writeHeader(out, relativePath, size, false);
        writeFileContent(out, child, size);
      }
    }
  }

  private static void writeFileContent(OutputStream out, File file, long size) throws IOException {
    try (FileInputStream in = new FileInputStream(file)) {
      byte[] buffer = new byte[8192];
      int len;
      while ((len = in.read(buffer)) > 0) {
        out.write(buffer, 0, len);
      }
    }
    writePadding(out, size);
  }

  private static void writePadding(OutputStream out, long size) throws IOException {
    int remainder = (int) (size % BLOCK_SIZE);
    if (remainder != 0) {
      out.write(new byte[BLOCK_SIZE - remainder]);
    }
  }

  private static void writeEndOfArchive(OutputStream out) throws IOException {
    out.write(new byte[BLOCK_SIZE * 2]);
  }

  /**
   * Writes a 512-byte USTAR header. Paths longer than 100 bytes are split across the "prefix" (up
   * to 155 bytes) and "name" (up to 100 bytes) fields at a '/' boundary, as the ustar format
   * requires; this is generous enough for any realistic project folder depth.
   */
  private static void writeHeader(OutputStream out, String path, long size, boolean directory) throws IOException {
    byte[] header = new byte[BLOCK_SIZE];

    String name = path;
    String prefix = "";
    byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
    if (nameBytes.length > 100) {
      int splitIndex = findUstarSplitIndex(path);
      if (splitIndex < 0) {
        throw new IOException("Path too long to fit in a USTAR header: " + path);
      }
      prefix = path.substring(0, splitIndex);
      name = path.substring(splitIndex + 1);
    }

    writeAsciiField(header, 0, 100, name);
    writeOctalField(header, 100, 8, 0644);
    writeOctalField(header, 108, 8, 0);
    writeOctalField(header, 116, 8, 0);
    writeOctalField(header, 124, 12, size);
    writeOctalField(header, 136, 12, System.currentTimeMillis() / 1000);
    // checksum field itself is treated as 8 spaces while computing the checksum
    for (int i = 148; i < 156; i++) {
      header[i] = ' ';
    }
    header[156] = (byte) (directory ? '5' : '0');
    writeAsciiField(header, 257, 6, "ustar");
    writeAsciiField(header, 263, 2, "00");
    writeAsciiField(header, 345, 155, prefix);

    long checksum = 0;
    for (byte b : header) {
      checksum += (b & 0xFF);
    }
    // Standard USTAR checksum layout: 6 octal digits, then NUL, then space.
    byte[] checksumBytes = String.format("%06o", checksum).getBytes(StandardCharsets.UTF_8);
    System.arraycopy(checksumBytes, 0, header, 148, 6);
    header[154] = 0;
    header[155] = ' ';

    out.write(header);
  }

  private static int findUstarSplitIndex(String path) {
    // The suffix ("name" field, max 100 bytes) needs i >= path.length() - 101; scanning upward
    // from there picks the split that fills as much of the name field as the limit allows.
    int minIndex = Math.max(0, path.length() - 101);
    for (int i = minIndex; i < path.length(); i++) {
      int prefixLen = i;
      int suffixLen = path.length() - i - 1;
      if (path.charAt(i) == '/' && prefixLen <= 155 && suffixLen <= 100 && suffixLen > 0) {
        return i;
      }
    }
    return -1;
  }

  private static void writeAsciiField(byte[] header, int offset, int length, String value) {
    byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
    int copyLength = Math.min(bytes.length, length);
    System.arraycopy(bytes, 0, header, offset, copyLength);
  }

  private static void writeOctalField(byte[] header, int offset, int length, long value) {
    // length - 1 to leave room for the trailing NUL; the field is otherwise left space-padded.
    String octal = Long.toOctalString(value);
    int padded = length - 1 - octal.length();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < padded; i++) {
      sb.append('0');
    }
    sb.append(octal);
    writeAsciiField(header, offset, length - 1, sb.toString());
    header[offset + length - 1] = 0;
  }
}
