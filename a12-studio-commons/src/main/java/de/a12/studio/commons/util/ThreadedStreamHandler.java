package de.a12.studio.commons.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * Reads a process' stdout/stderr on a background thread so the process doesn't block
 * once its output buffer fills up. Used by SystemCommandExecutor.
 */
@Slf4j
class ThreadedStreamHandler extends Thread {

  private final InputStream inputStream;
  private final StringBuilder outputBuffer = new StringBuilder();
  private boolean enableLog = false;
  private boolean stopped = false;

  ThreadedStreamHandler(String name, InputStream inputStream) {
    super(name);
    this.inputStream = inputStream;
  }

  @Override
  public void run() {
    stopped = false;
    try (InputStreamReader isr = new InputStreamReader(inputStream);
         BufferedReader bufferedReader = new BufferedReader(isr)) {
      String line;
      while (!stopped && (line = bufferedReader.readLine()) != null) {
        outputBuffer.append(line).append("\n");
        if (enableLog) {
          log.info("System Command Output: {}", line);
        }
      }
    }
    catch (Exception ioe) {
      log.warn("Error reading process stream: {}", ioe.getMessage());
    }
  }

  public void stopThread() {
    this.stopped = true;
  }

  public void enableLog(boolean b) {
    enableLog = b;
  }

  public StringBuilder getOutputBuffer() {
    return outputBuffer;
  }
}
