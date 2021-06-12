package scenelib.annotations.util;

import com.sun.tools.javac.main.CommandLine;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class CommandLineUtils  {
  @SuppressWarnings("unchecked")
  public static String[] parseCommandLine(String[] args) throws Exception {
    try {
      Method method = CommandLine.class.getDeclaredMethod(
              "parse", List.class);
      return ((List<?>)method.invoke(null, Arrays.asList(args))).toArray(new String[0]);
    }
    catch (NoSuchMethodException e) {
      Method method = CommandLine.class.getDeclaredMethod(
              "parse", String[].class);
      return (String[])method.invoke(null, (Object) args);
    }
  }
}
