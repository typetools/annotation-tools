package scenelib.annotations.io;

/** <code>IOUtils</code> has some static methods useful to scene I/O code. */
class IOUtils {
  private IOUtils() {}

  static String packagePart(String className) {
    int lastdot = className.lastIndexOf('.');
    return (lastdot == -1) ? "" : className.substring(0, lastdot);
  }

  static String basenamePart(String className) {
    int lastdot = className.lastIndexOf('.');
    return (lastdot == -1) ? className : className.substring(lastdot + 1);
  }
  
  static String[] parseCommandLine(String[] args) {
    try {
      Method method = CommandLine.class.getDeclaredMethod(
              "parse", List.class);
      return ((List)method.invoke(null, Arrays.asList(args))).toArray(new String[0]);
    }
    catch (NoSuchMethodException e) {
      Method method = CommandLine.class.getDeclaredMethod(
              "parse", String[].class);
      return (String[])method.invoke(null, args);
    }
  }
}
