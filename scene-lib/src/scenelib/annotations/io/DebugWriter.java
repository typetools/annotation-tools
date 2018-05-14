package scenelib.annotations.io;

import java.io.PrintWriter;
import java.util.logging.Level;

public class DebugWriter {
  private PrintWriter out = new PrintWriter(System.out);
  private Level level = Level.WARNING;

  public DebugWriter or(final DebugWriter other) {
    return new DebugWriter() {
      @Override
      public boolean isEnabled() {
        return super.isEnabled() || other.isEnabled();
      }
    };
  }

  public static boolean anyEnabled(DebugWriter... debugs) {
    for (DebugWriter debug : debugs) {
      if (debug.isEnabled()) { return true; }
    }
    return false;
  }

  public boolean isEnabled() {
    return level == Level.INFO;
  }

  public void setEnabled(boolean enabled) {
    level = enabled ? Level.INFO : Level.WARNING;
  }

  public void debug(String format, Object... args) {
    if (isEnabled()) {
      out.print(String.format(format, args));
      out.flush();
    }
  }
}
