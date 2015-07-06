package annotations.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.sun.tools.javac.main.CommandLine;

import plume.FileIOException;
import annotations.Annotation;
import annotations.Annotations;
import annotations.el.AScene;
import annotations.el.AnnotationDef;
import annotations.el.DefException;
import annotations.field.AnnotationFieldType;
import annotations.io.IndexFileParser;
import annotations.io.IndexFileWriter;

/**
 * Utility for merging index files, including multiple versions for the
 *  same class.
 * 
 * @author dbro
 */
public class IndexFileMerger {
  public static void main(String[] args) {
    if (args.length < 1) { System.exit(0); }

    SetMultimap<String, String> annotatedFor = HashMultimap.create();
    String[] inputArgs;

    // TODO: document assumptions
    // collect annotations into scene
    try {
      try {
        inputArgs = CommandLine.parse(args);
      } catch (IOException ex) {
        System.err.println(ex);
        System.err.println("(For non-argfile beginning with \"@\", use \"@@\" for initial \"@\".");
        System.err.println("Alternative for filenames: indicate directory, e.g. as './@file'.");
        System.err.println("Alternative for flags: use '=', as in '-o=@Deprecated'.)");
        System.exit(1);
        return;  // so compiler knows inputArgs defined after try/catch
      }

      String basePath = new File(inputArgs[0]).getCanonicalPath();
      AScene scene = new AScene();

      for (int i = 1; i < inputArgs.length; i++) {
        File inputFile = new File(inputArgs[i]);
        String inputPath = inputFile.getCanonicalPath();
        String filename = inputFile.getName();

        if (!(filename.endsWith(".jaif") || filename.endsWith("jann"))) {
          System.err.println("WARNING: ignoring non-JAIF " + filename);
          continue;
        }
        if (!inputPath.startsWith(basePath)) {
          System.err.println("WARNING: ignoring file outside base directory "
              + filename);
          continue;
        }

        // note which base subdirectory JAIF came from
        String relPath = inputPath.substring(basePath.length()+1);  // +1 for /
        int ix = relPath.indexOf(File.separator);
        String subdir = ix < 0 ? relPath : relPath.substring(0, ix);
        // trim .jaif or .jann and subdir, convert directory to package id
        String classname = relPath.substring(0, relPath.lastIndexOf('.'))
            .substring(relPath.indexOf('/')+1).replace(File.separator, ".");
        annotatedFor.put(classname, "\"" + subdir + "\"");

        try {
          IndexFileParser.parseFile(inputPath, scene);
        } catch (FileNotFoundException e) {
          System.err.println("IndexFileMerger: can't read "
              + inputPath);
          System.exit(1);
        } catch (FileIOException e) {
          e.printStackTrace();  // TODO
          System.exit(1);
        }
      }

      // add AnnotatedFor to each annotated class
      AnnotationFieldType stringArray =
          AnnotationFieldType.fromClass(new String[0].getClass(),
              Collections.<String, AnnotationDef>emptyMap());
      AnnotationDef afDef =
          Annotations.createValueAnnotationDef("AnnotatedFor",
              Collections.<Annotation>emptySet(), stringArray);
      for (Map.Entry<String, Collection<String>> entry :
          annotatedFor.asMap().entrySet()) {
        String key = entry.getKey();
        Collection<String> values = entry.getValue();
        Annotation afAnno = new Annotation(afDef, Collections
                .<String, Collection<String>>singletonMap("value", values));
        scene.classes.vivify(key).tlAnnotationsHere.add(afAnno);
      }
      annotatedFor = null;  // for gc

      try {
        IndexFileWriter.write(scene, new PrintWriter(System.out, true));
      } catch (SecurityException e) {
        e.printStackTrace();
        System.exit(1);
      } catch (DefException e) {
        e.printStackTrace();
        System.exit(1);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
