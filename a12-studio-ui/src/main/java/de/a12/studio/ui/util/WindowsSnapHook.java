package de.a12.studio.ui.util;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.HHOOK;
import com.sun.jna.platform.win32.WinUser.KBDLLHOOKSTRUCT;
import com.sun.jna.platform.win32.WinUser.LowLevelKeyboardProc;
import com.sun.jna.platform.win32.WinUser.MSG;
import com.sun.jna.ptr.IntByReference;

import java.util.function.Consumer;

/**
 * Windows-only global low-level keyboard hook (WH_KEYBOARD_LL) that intercepts Win+Left/Right/
 * Up/Down before the shell's own reserved Snap Assist handling consumes them - the shell grabs
 * those combinations (especially Left/Right) ahead of normal window messages, so a regular
 * JavaFX/Swing key listener on the app's own window never sees them. A low-level hook installed
 * by this process runs earlier in the system-wide hook chain and can swallow the keystroke
 * (by not forwarding it to {@link User32#CallNextHookEx}) before the shell acts on it.
 *
 * <p>Since the hook is system-wide, it only swallows the keystroke while one of this process's
 * own windows is the foreground window - see {@link #isOwnProcessForeground()} - so it never
 * hijacks Win+Arrow snapping for other applications.
 *
 * <p>The hook callback fires on the dedicated thread that installed it, which must pump a
 * Windows message loop for the callback to be delivered at all - see {@link #runHookLoop()}.
 */
public class WindowsSnapHook {

  public enum SnapKey { LEFT, RIGHT, UP, DOWN }

  private static final int VK_LWIN = 0x5B;
  private static final int VK_RWIN = 0x5C;
  private static final int VK_LEFT = 0x25;
  private static final int VK_UP = 0x26;
  private static final int VK_RIGHT = 0x27;
  private static final int VK_DOWN = 0x28;

  private final Consumer<SnapKey> onSnapKey;

  private Thread hookThread;
  private volatile HHOOK hook;
  private volatile boolean winDown;

  // kept as a field so it isn't garbage collected while the native side still holds a
  // reference to it - JNA callbacks are only pinned by Java-side references
  private LowLevelKeyboardProc callback;

  public WindowsSnapHook(Consumer<SnapKey> onSnapKey) {
    this.onSnapKey = onSnapKey;
  }

  public void install() {
    if (!OSUtil.isWindows()) {
      return;
    }
    hookThread = new Thread(this::runHookLoop, "windows-snap-hook");
    hookThread.setDaemon(true);
    hookThread.start();
  }

  public void uninstall() {
    HHOOK installedHook = hook;
    if (installedHook != null) {
      User32.INSTANCE.UnhookWindowsHookEx(installedHook);
      hook = null;
    }
    if (hookThread != null) {
      hookThread.interrupt();
    }
  }

  private void runHookLoop() {
    callback = this::hookCallback;
    HMODULE moduleHandle = Kernel32.INSTANCE.GetModuleHandle(null);
    hook = User32.INSTANCE.SetWindowsHookEx(WinUser.WH_KEYBOARD_LL, callback, moduleHandle, 0);
    if (hook == null) {
      return;
    }

    // WH_KEYBOARD_LL callbacks are only delivered while this thread pumps messages
    MSG msg = new MSG();
    while (User32.INSTANCE.GetMessage(msg, null, 0, 0) > 0) {
      User32.INSTANCE.TranslateMessage(msg);
      User32.INSTANCE.DispatchMessage(msg);
    }
  }

  private LRESULT hookCallback(int nCode, WPARAM wParam, KBDLLHOOKSTRUCT info) {
    if (nCode >= 0) {
      int vkCode = info.vkCode;
      boolean isKeyDown = wParam.intValue() == WinUser.WM_KEYDOWN || wParam.intValue() == WinUser.WM_SYSKEYDOWN;

      if (vkCode == VK_LWIN || vkCode == VK_RWIN) {
        winDown = isKeyDown;
      }
      else if (isKeyDown && winDown && isOwnProcessForeground()) {
        SnapKey key = switch (vkCode) {
          case VK_LEFT -> SnapKey.LEFT;
          case VK_RIGHT -> SnapKey.RIGHT;
          case VK_UP -> SnapKey.UP;
          case VK_DOWN -> SnapKey.DOWN;
          default -> null;
        };
        if (key != null) {
          onSnapKey.accept(key);
          // swallow it so the shell's own Snap Assist doesn't also act on it
          return new LRESULT(1);
        }
      }
    }
    LPARAM lParam = new LPARAM(Pointer.nativeValue(info.getPointer()));
    return User32.INSTANCE.CallNextHookEx(hook, nCode, wParam, lParam);
  }

  private boolean isOwnProcessForeground() {
    IntByReference foregroundPid = new IntByReference();
    User32.INSTANCE.GetWindowThreadProcessId(User32.INSTANCE.GetForegroundWindow(), foregroundPid);
    return foregroundPid.getValue() == Kernel32.INSTANCE.GetCurrentProcessId();
  }
}
