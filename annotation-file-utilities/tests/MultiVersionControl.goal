package plume;

import org.tmatesoft.svn.core.wc.*;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.*;
import org.ini4j.Ini;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.net.URL;

// annotation-tools note:
// Running insert-annotations-to-source on this code from plume-lib
// revealed a bug in annotations.io.classfile.CodeOffsetVisitor.
// Makefile now includes plume-lib in its classpath (via
// annotation-file-utilities.jar) so this test can be compiled.  The
// following stub definitions for annotations eliminate the original
// code's Checker Framework dependency.

import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.TYPE_USE;

@interface EnsuresNonNull { String value(); }
@interface Initialized {}
@Target({TYPE_USE})
@interface NonNull {}
@Target({TYPE_USE})
@interface Nullable {}
@interface Pure {}
@Target({TYPE_USE})
@interface Raw {}
@Target({TYPE_USE})
@interface Regex { int value() default 0; }
@interface RequiresNonNull { String value(); }
@interface SideEffectFree {}
@Target({TYPE_USE})
@interface UnknownInitialization {}

// A related program is the "mr" program (http://kitenet.net/~joey/code/mr/).
// To read its documentation:  pod2man mr | nroff -man
// Some differences are:
//  * mvc knows how to search for all repositories
//  * mvc uses a timeout
//  * mvc tries to improve tool output:
//     * mvc tries to be as quiet as possible.  The fact that it issues
//       output only if there is a problem makes "mvc status" appropriate
//       for running as a cron job, and reduces distraction.
//     * mvc rewrites paths from relative to absolute form or adds
//       pathnames, to make output comprehensible without knowing the
//       working directory of the command.
//  * mvc's configuration files tend to be smaller & simpler

/**
 * This program, mvc for Multiple Version Control, lets you run a version
 * control command, such as "status" or "update", on a <b>set</b> of
 * CVS/Git/Hg/SVN checkouts rather than just one.<p>
 *
 * This program simplifies managing your checkouts/clones.  You might
 * want to update/pull all of them, or you might want to know whether any
 * of them have uncommitted changes.  When setting up a new account,
 * you might want to clone or check them all out.  This program does any
 * of those tasks.  In particular, it accepts these arguments:
 * <pre>
 *   checkout  -- Check out (clone) all repositories.
 *   clone     -- Same as checkout.
 *   update    -- Update all checkouts.  For a distributed version control
 *                system such as Git or Mercurial, also does a pull.
 *   pull      -- Same as update.
 *   status    -- Show files that are changed but not committed, or committed
 *                but not pushed, or have shelved/stashed changes.
 *   list      -- List the checkouts/clones that this program is aware of.
 * </pre>
 * (The <tt>commit</tt> action is not supported, because that is not
 * something that should be done in an automated way -- it needs a user-
 * written commit message.)<p>
 *
 * You can specify the set of checkouts/clones for the program to manage, or
 * it can search your directory structure to find all of your checkouts, or
 * both.  To list all un-committed changed files under your home directory:
 * <pre>java plume.MultiVersionControl status --search=true</pre>
 *
 * <b>Command-line arguments</b><p>
 * The command-line options are as follows:
 * <!-- start options doc (DO NOT EDIT BY HAND) -->
 * <ul>
 *   <li id="option:home"><b>--home=</b><i>string</i>. User home directory</li>
 *   <li id="option:checkouts"><b>--checkouts=</b><i>string</i>. File with list of checkouts.  Set it to /dev/null to suppress reading.
 *  Defaults to <tt>$HOME/.mvc-checkouts</tt>. [default ~/.mvc-checkouts]</li>
 *   <li id="option:dir"><b>--dir=</b><i>string</i> <tt>[+]</tt>. Directory under which to search for checkouts; default=home dir</li>
 *   <li id="option:ignore-dir"><b>--ignore-dir=</b><i>string</i> <tt>[+]</tt>. Directory under which to NOT search for checkouts</li>
 *   <li id="option:search"><b>--search=</b><i>boolean</i>. Search for all checkouts, not just those listed in a file [default false]</li>
 *   <li id="option:show"><b>--show=</b><i>boolean</i>. Display commands as they are executed [default false]</li>
 *   <li id="option:print-directory"><b>--print-directory=</b><i>boolean</i>. Print the directory before executing commands [default false]</li>
 *   <li id="option:dry-run"><b>--dry-run=</b><i>boolean</i>. Do not execute commands; just print them.  Implies --show --redo-existing [default false]</li>
 *   <li id="option:redo-existing"><b>--redo-existing=</b><i>boolean</i>. Default is for checkout command to skip existing directories. [default false]</li>
 *   <li id="option:timeout"><b>--timeout=</b><i>int</i>. Terminating the process can leave the repository in a bad state, so
 *  set this rather high for safety.  Also, the timeout needs to account
 *  for the time to run hooks (that might recompile or run tests). [default 600]</li>
 *   <li id="option:quiet"><b>-q</b> <b>--quiet=</b><i>boolean</i>. Run quietly (e.g., no output about missing directories) [default true]</li>
 *   <li id="option:cvs-executable"><b>--cvs-executable=</b><i>string</i>. Path to the cvs program [default cvs]</li>
 *   <li id="option:git-executable"><b>--git-executable=</b><i>string</i>. Path to the git program [default git]</li>
 *   <li id="option:hg-executable"><b>--hg-executable=</b><i>string</i>. Path to the hg program [default hg]</li>
 *   <li id="option:svn-executable"><b>--svn-executable=</b><i>string</i>. Path to the svn program [default svn]</li>
 *   <li id="option:insecure"><b>--insecure=</b><i>boolean</i>. Pass --insecure argument to hg (and likewise for other programs) [default false]</li>
 *   <li id="option:cvs-arg"><b>--cvs-arg=</b><i>string</i> <tt>[+]</tt>. Extra argument to pass to the cvs program</li>
 *   <li id="option:git-arg"><b>--git-arg=</b><i>string</i> <tt>[+]</tt>. Extra argument to pass  to the git program</li>
 *   <li id="option:hg-arg"><b>--hg-arg=</b><i>string</i> <tt>[+]</tt>. Extra argument to pass  to the hg program</li>
 *   <li id="option:svn-arg"><b>--svn-arg=</b><i>string</i> <tt>[+]</tt>. Extra argument to pass  to the svn program</li>
 *   <li id="option:debug"><b>--debug=</b><i>boolean</i>. Print debugging output [default false]</li>
 *   <li id="option:debug-replacers"><b>--debug-replacers=</b><i>boolean</i>. Debug 'replacers' that filter command output [default false]</li>
 *   <li id="option:debug-process-output"><b>--debug-process-output=</b><i>boolean</i>. Lightweight debugging of 'replacers' that filter command output [default false]</li>
 * </ul>
 * <tt>[+]</tt> marked option can be specified multiple times
 * <!-- end options doc -->
 * <p>
 *
 * <b>File format for <tt>.mvc-checkouts</tt> file</b><p>
 *
 * The remainder of this document describes the file format for the
 * <tt>.mvc-checkouts</tt> file.<p>
 *
 * (Note:  because mvc can search for all checkouts in your directory, you
 * don't need a <tt>.mvc-checkouts</tt> file.  Using a
 * <tt>.mvc-checkouts</tt> file makes the program faster because it does not
 * have to search all of your directories.  It also permits you to
 * process only a certain set of checkouts.)<p>
 *
 * The <tt>.mvc-checkouts</tt> file contains a list of <em>sections</em>.
 * Each section names either a root from which a sub-part (e.g., a module
 * or a subdirectory) will be checked out, or a repository all of which
 * will be checked out.
 * Examples include:
 * <pre>
 * CVSROOT: :ext:login.csail.mit.edu:/afs/csail.mit.edu/u/m/mernst/.CVS/.CVS-mernst
 * SVNROOT: svn+ssh://tricycle.cs.washington.edu/cse/courses/cse403/09sp
 * SVNREPOS: svn+ssh://login.csail.mit.edu/afs/csail/u/a/akiezun/.SVN/papers/parameterization-paper/trunk
 * HGREPOS: https://jsr308-langtools.googlecode.com/hg</pre>
 *
 * Within each section is a list of directories that contain a checkout
 * from that repository.  If the section names a root, then a module or
 * subdirectory is needed.  By default, the directory's basename is used.
 * This can be overridden by specifying the module/subdirectory on the same
 * line, after a space.  If the section names a repository, then no module
 * information is needed or used.<p>
 *
 * When performing a checkout, the parent directories are created if
 * needed.<p>
 *
 * In the file, blank lines, and lines beginning with "#", are ignored.<p>
 *
 * Here are some example sections:
 * <pre>
 * CVSROOT: :ext:login.csail.mit.edu:/afs/csail.mit.edu/group/pag/projects/classify-tests/.CVS
 * ~/research/testing/symstra-eclat-paper
 * ~/research/testing/symstra-eclat-code
 * ~/research/testing/eclat
 *
 * SVNROOT: svn+ssh://login.csail.mit.edu/afs/csail/group/pag/projects/.SVNREPOS/
 * ~/research/typequals/annotations-papers
 *
 * SVNREPOS: svn+ssh://login.csail.mit.edu/afs/csail/group/pag/projects/abb/REPOS
 * ~/prof/grants/2008-06-abb/abb
 *
 * HGREPOS: https://checker-framework.googlecode.com/hg/
 * ~/research/types/checker-framework
 *
 * SVNROOT: svn+ssh://login.csail.mit.edu/afs/csail/u/d/dannydig/REPOS/
 * ~/research/concurrency/concurrentPaper
 * ~/research/concurrency/mit.edu.concurrencyRefactorings concurrencyRefactorings/project/mit.edu.concurrencyRefactorings</pre>
 *
 * Furthermore, these 2 sections have identical effects:
 * <pre>
 * SVNROOT: https://crashma.googlecode.com/svn/
 * ~/research/crashma trunk
 *
 * SVNREPOS: https://crashma.googlecode.com/svn/trunk
 * ~/research/crashma</pre>
 * and, all 3 of these sections have identical effects:
 * <pre>
 * SVNROOT: svn+ssh://login.csail.mit.edu/afs/csail/group/pag/projects/
 * ~/research/typequals/annotations
 *
 * SVNROOT: svn+ssh://login.csail.mit.edu/afs/csail/group/pag/projects/
 * ~/research/typequals/annotations annotations
 *
 * SVNREPOS: svn+ssh://login.csail.mit.edu/afs/csail/group/pag/projects/annotations
 * ~/research/typequals/annotations</pre>
 */

// TODO:

// It might be nice to list all the "unexpected" checkouts -- those found
// on disk that are not in the checkouts file.  This permits the checkouts
// file to be updated and then used in preference to searching the whole
// filesystem, which may be slow.
// You can do this from the command line by comparing the output of these
// two commands:
//   mvc list --repositories /dev/null | sort > checkouts-from-directory
//   mvc list --search=false | sort > checkouts-from-file
// but it might be nicer for the "list" command to do that explicitly.

// The "list" command should be in the .mvc-checkouts file format, rather
// than requiring the user to munge it.

// In checkouts file, use of space delimiter for specifyng module interacts
// badly with file names that contain spaces.  This doesn't seem important
// enough to fix.

// When discovering checkouts from a directory structure, there is a
// problem when two modules from the same SVN repository are checked out,
// with one checkout inside the other at the top level.  The inner
// checkout's directory can be mis-reported as the outer one.  This isn't
// always a problem for nested checkouts (so it's hard to reproduce), and
// nested checkouts are bad style anyway, so I am deferring
// investigating/fixing it.

// Add "incoming" command that shows you need to do update and/or fetch?
//
// For Mercurial, I can do "hg incoming", but how to show that the current
// working directory is not up to date with respect to the local
// repository?  "hg prompt" with the "update" tag will do the trick, see
// http://bitbucket.org/sjl/hg-prompt/src/ .  Or don't bother:  it's rarely an
// issue if you always update via "hg fetch" as done by this program.
//
// For svn, "svn status -u":
//   The out-of-date information appears in the ninth column (with -u):
//       '*' a newer revision exists on the server
//       ' ' the working copy is up to date



public class MultiVersionControl {

  @Option(value="User home directory", noDocDefault=true)
  public static String home = System.getProperty ("user.home");

  /**
   * File with list of checkouts.  Set it to /dev/null to suppress reading.
   * Defaults to <tt>$HOME/.mvc-checkouts</tt>.
   */
  @Option("File with list of checkouts.  Set it to /dev/null to suppress reading.")
  public String checkouts = "~/.mvc-checkouts";

  @Option("Directory under which to search for checkouts; default=home dir")
  public List<String> dir = new ArrayList<String>();

  @Option("Directory under which to NOT search for checkouts")
  public List<String> ignore_dir = new ArrayList<String>();
  private List<File> ignoreDirs = new ArrayList<File>();

  // Default is false because searching whole directory structure is slow.
  @Option("Search for all checkouts, not just those listed in a file")
  public boolean search = false;

  // TODO: use consistent names: both "show" or both "print"

  @Option("Display commands as they are executed")
  public boolean show = false;

  @Option("Print the directory before executing commands")
  public boolean print_directory = false;

  @Option("Do not execute commands; just print them.  Implies --show --redo-existing")
  public boolean dry_run = false;

  /**  Default is for checkout command to skip existing directories. */
  @Option("Redo existing checkouts; relevant only to checkout command")
  public boolean redo_existing = false;

  /**
   * Terminating the process can leave the repository in a bad state, so
   * set this rather high for safety.  Also, the timeout needs to account
   * for the time to run hooks (that might recompile or run tests).
   */
  @Option("Timeout for each command, in seconds")
  public int timeout = 600;

  @Option("-q Run quietly (e.g., no output about missing directories)")
  public boolean quiet = true;

  // These *-executable command-line options are handy:
  //  * if you want to use a specific version of the program
  //  * if the program is not on your path
  //  * if there is a directory of the same name as the program, and . is on
  //    your path; in that case, the command would try to execute the directory.

  @Option("Path to the cvs program")
  public String cvs_executable = "cvs";

  @Option("Path to the git program")
  public String git_executable = "git";

  @Option("Path to the hg program")
  public String hg_executable = "hg";

  @Option("Path to the svn program")
  public String svn_executable = "svn";

  @Option("Pass --insecure argument to hg (and likewise for other programs)")
  public boolean insecure = false;

  // The {cvs,git,hg,svn}_arg options probably aren't very useful, because
  // there are few arguments that are applicable to every command; for
  // example, --insecure isn't applicable to "hg status".

  @Option("Extra argument to pass to the cvs program")
  public List<String> cvs_arg = new ArrayList<String>();

  @Option("Extra argument to pass  to the git program")
  public List<String> git_arg = new ArrayList<String>();

  @Option("Extra argument to pass  to the hg program")
  public List<String> hg_arg = new ArrayList<String>();

  @Option("Extra argument to pass  to the svn program")
  public List<String> svn_arg = new ArrayList<String>();

  // It would be good to be able to set this per-checkout.
  // This variable is static because it is used in static methods.
  @Option("Print debugging output")
  static public boolean debug = false;

  @Option("Debug 'replacers' that filter command output")
  public boolean debug_replacers = false;

  @Option("Lightweight debugging of 'replacers' that filter command output")
  public boolean debug_process_output = false;

  static enum Action {
    CHECKOUT,
    STATUS,
    UPDATE,
    LIST
    };
  // Shorter variants
  private static Action CHECKOUT = Action.CHECKOUT;
  private static Action STATUS = Action.STATUS;
  private static Action UPDATE = Action.UPDATE;
  private static Action LIST = Action.LIST;

  private Action action;

  // Replace "~" by the expansion of "$HOME".
  private static String expandTilde (String path) {
    return path.replaceFirst("^~", home);
  }

  public static void main (String[] args) {
    setupSVNKIT();
    MultiVersionControl mvc = new MultiVersionControl(args);

    Set<Checkout> checkouts = new LinkedHashSet<Checkout>();

    try {
      readCheckouts(new File(mvc.checkouts), checkouts);
    } catch (IOException e) {
      System.err.println("Problem reading file " + mvc.checkouts + ": " + e.getMessage());
    }

    if (mvc.search) {
      // Postprocess command-line arguments
      for (String adir : mvc.ignore_dir) {
        File afile = new File(expandTilde(adir));
        if (! afile.exists()) {
            System.err.printf("Warning: Directory to ignore while searching for checkouts does not exist:%n  %s%n", adir);
        } else if (! afile.isDirectory()) {
            System.err.printf("Warning: Directory to ignore while searching for checkouts is not a directory:%n  %s%n", adir);
        } else {
          mvc.ignoreDirs.add(afile);
        }
      }

      for (String adir : mvc.dir) {
        adir = expandTilde(adir);
        if (debug) {
          System.out.println("Searching for checkouts under " + adir);
        }
        if (! new File(adir).isDirectory()) {
          System.err.printf("Directory in which to search for checkouts is not a directory: %s%n", adir);
          System.exit(2);
        }
        findCheckouts(new File(adir), checkouts, mvc.ignoreDirs);
      }
    }

    if (debug) {
      System.out.println("Processing checkouts read from " + checkouts);
    }
    mvc.process(checkouts);
  }

  private static void setupSVNKIT() {
    DAVRepositoryFactory.setup();
    SVNRepositoryFactoryImpl.setup();
    FSRepositoryFactory.setup();
  }

  // OptionsDoclet requires a nullary constructor (but a private one is OK).
  /*@SuppressWarnings("nullness")*/ // initialization warning in unused constructor
  private MultiVersionControl() {
  }

  public MultiVersionControl(String[] args) {
    parseArgs(args);
  }

  /*@RequiresNonNull("dir")*/
  /*@EnsuresNonNull("action")*/
  public void parseArgs(/*>>> @UnknownInitialization @Raw MultiVersionControl this,*/ String[] args) {
    @SuppressWarnings("initialization") // "new MyClass(underInitialization)" yields @UnderInitialization even when @Initialized would be safe
    /*@Initialized*/ Options options = new Options ("mvc [options] {checkout,status,update,list}", this);
    String[] remaining_args = options.parse_or_usage (args);
    if (remaining_args.length != 1) {
      options.print_usage("Please supply exactly one argument (found %d)%n%s", remaining_args.length, UtilMDE.join(remaining_args, " "));
      System.exit(1);
    }
    String action_string = remaining_args[0];
    if ("checkout".startsWith(action_string)) {
      action = CHECKOUT;
    } else if ("clone".startsWith(action_string)) {
      action = CHECKOUT;
    } else if ("list".startsWith(action_string)) {
      action = LIST;
    } else if ("pull".startsWith(action_string)) {
      action = UPDATE;
    } else if ("status".startsWith(action_string)) {
      action = STATUS;
    } else if ("update".startsWith(action_string)) {
      action = UPDATE;
    } else {
      options.print_usage("Unrecognized action \"%s\"", action_string);
      System.exit(1);
    }

    // clean up options

    checkouts = expandTilde(checkouts);

    if (dir.isEmpty()) {
      dir.add(home);
    }

    if (action == CHECKOUT) {
      search = false;
      show = true;
      // Checkouts can be much slower than other operations.
      timeout = timeout * 10;

      // Set dry_run to true unless it was explicitly specified
      boolean explicit_run_dry = false;
      for (String arg : args) {
        if (arg.startsWith("--dry-run") || arg.startsWith("--dry_run")) {
          explicit_run_dry = true;
        }
      }
      if (! explicit_run_dry) {
        if (! quiet) {
          System.out.println("No --dry-run argument, so using --dry-run=true; override with --dry-run=false");
        }
        dry_run = true;
      }

    }

    if (dry_run) {
      show = true;
      redo_existing = true;
    }

    if (debug) {
      show = true;
    }

  }

  static enum RepoType {
    BZR,
    CVS,
    GIT,
    HG,
    SVN };

  // TODO: have subclasses of Checkout for the different varieties, perhaps.
  static class Checkout {
    RepoType repoType;
    /** Local directory */
    // actually the parent directory?
    File directory;
    /**
     * Non-null for CVS and SVN.
     * May be null for distributed version control systems (Bzr, Git, Hg).
     * For distributed systems, refers to the parent repository from which
     * this was cloned, not the one here in this directory.
     * <p>
     * Most operations don't need this.  it is needed for checkout, though.
     */
    /*@Nullable*/ String repository;
    /**
     * Null if no module, just whole thing.
     * Non-null for CVS and, optionally, for SVN.
     * Null for distributed version control systems (Bzr, Git, Hg).
     */
    /*@Nullable*/ String module;


    Checkout(RepoType repoType, File directory) {
      this(repoType, directory, null, null);
    }

    Checkout(RepoType repoType, File directory, /*@Nullable*/ String repository, /*@Nullable*/ String module) {
      // Directory might not exist if we are running the checkout command.
      // If it exists, it must be a directory.
      assert (directory.exists() ? directory.isDirectory() : true)
        : "Not a directory: " + directory;
      this.repoType = repoType;
      this.directory = directory;
      this.repository = repository;
      this.module = module;
      // These asserts come at the end so that the error message can be better.
      switch (repoType) {
      case BZR:
        assertSubdirExists(directory, ".bzr");
        assert module == null;
        break;
      case CVS:
        assertSubdirExists(directory, "CVS");
        assert module != null : "No module for CVS checkout at: " + directory;
        break;
      case GIT:
        assertSubdirExists(directory, ".git");
        assert module == null;
        break;
      case HG:
        assertSubdirExists(directory, ".hg");
        assert module == null;
        break;
      case SVN:
        assertSubdirExists(directory, ".svn");
        assert module == null;
        break;
      default:
        assert false;
      }
    }

    /** If the directory exists, then the subdirectory must exist too. */
    private static void assertSubdirExists(File directory, String subdirName) {
      if (directory.exists()
          && ! new File(directory, subdirName).isDirectory()) {
        System.err.printf("Directory %s exists but %s subdirectory does not exist%n",
                          directory, subdirName);
        System.exit(2);
      }
    }


    @Override
    @SuppressWarnings("interning")
    /*@Pure*/ public boolean equals(/*@Nullable*/ Object other) {
      if (! (other instanceof Checkout))
        return false;
      Checkout c2 = (Checkout) other;
      return ((repoType == c2.repoType)
              && directory.equals(c2.directory)
              && ((repository == null)
                  ? (c2.repository == null)
                  : repository.equals(c2.repository))
              && ((module == null)
                  ? (c2.module == null)
                  : module.equals(c2.module)));
    }

    @Override
    /*@Pure*/ public int hashCode() {
      return (repoType.hashCode()
              + directory.hashCode()
              + (repository == null ? 0 : repository.hashCode())
              + (module == null ? 0 : module.hashCode()));
    }

    @Override
      /*@SideEffectFree*/ public String toString() {
      return repoType
        + " " + directory
        + " " + repository
        + " " + module;
    }

  }


  ///////////////////////////////////////////////////////////////////////////
  /// Read checkouts from a file
  ///

  /**
   * Read checkouts from the file (in .mvc-checkouts format), and add
   * them to the set.
   */
  static void readCheckouts(File file, Set<Checkout> checkouts) throws IOException {
    RepoType currentType = RepoType.BZR; // arbitrary choice
    String currentRoot = null;
    boolean currentRootIsRepos = false;

    EntryReader er = new EntryReader(file);
    for (String line : er) {
      if (debug) {
        System.out.println("line: " + line);
      }
      line = line.trim();
      // Skip comments and blank lines
      if (line.equals("") || line.startsWith("#")) {
        continue;
      }

      String[] splitTwo = line.split("[ \t]+");
      if (debug) {
        System.out.println("split length: " + splitTwo.length);
      }
      if (splitTwo.length == 2) {
        String word1 = splitTwo[0];
        String word2 = splitTwo[1];
        if (word1.equals("BZRROOT:") || word1.equals("BZRREPOS:")) {
          currentType = RepoType.BZR;
          currentRoot = word2;
          currentRootIsRepos = word1.equals("BZRREPOS:");
          continue;
        } else if (word1.equals("CVSROOT:")) {
          currentType = RepoType.CVS;
          currentRoot = word2;
          currentRootIsRepos = false;
          // If the CVSROOT is remote, try to make it local.
          if (currentRoot.startsWith(":ext:")) {
            String[] rootWords = currentRoot.split(":");
            String possibleRoot = rootWords[rootWords.length-1];
            if (new File(possibleRoot).isDirectory()) {
              currentRoot = possibleRoot;
            }
          }
          continue;
        } else if (word1.equals("HGROOT:") || word1.equals("HGREPOS:")) {
          currentType = RepoType.HG;
          currentRoot = word2;
          currentRootIsRepos = word1.equals("HGREPOS:");
          continue;
        } else if (word1.equals("GITROOT:") || word1.equals("GITREPOS:")) {
          currentType = RepoType.GIT;
          currentRoot = word2;
          currentRootIsRepos = word1.equals("GITREPOS:");
          continue;
        } else if (word1.equals("SVNROOT:") || word1.equals("SVNREPOS:")) {
          currentType = RepoType.SVN;
          currentRoot = word2;
          currentRootIsRepos = word1.equals("SVNREPOS:");
          continue;
        }
      }

      if (currentRoot == null) {
        System.err.printf("need root before directory at line %d of file %s%n",
                          er.getLineNumber(), er.getFileName());
        System.exit(1);
      }

      String dirname;
      String root = currentRoot;
      if (root.endsWith("/")) root = root.substring(0,root.length()-1);
      String module = null;

      int spacePos = line.lastIndexOf(' ');
      if (spacePos == -1) {
        dirname = line;
      } else {
        dirname = line.substring(0, spacePos);
        module = line.substring(spacePos+1);
      }

      // The directory may not yet exist if we are doing a checkout.
      File dir = new File(expandTilde(dirname));

      if (module == null) {
          module = dir.getName();
      }
      if (currentType != RepoType.CVS) {
        if (! currentRootIsRepos) {
          root = root + "/" + module;
        }
        module = null;
      }

      Checkout checkout = new Checkout(currentType, dir, root, module);
      checkouts.add(checkout);
    }
  }


  ///////////////////////////////////////////////////////////////////////////
  /// Find checkouts in a directory
  ///

  /// Note:  this can be slow, because it examines every directory in your
  /// entire home directory.

  // Find checkouts.  These are indicated by directories named .bzr, CVS,
  // .hg, or .svn.
  //
  // With some version control systems, this task is easy:  there is
  // exactly one .bzr or .hg directory per checkout.  With CVS and SVN,
  // there is one CVS/.svn directory per directory of the checkout.  It is
  // permitted for one checkout to be made inside another one (though that
  // is bad style), so we must examine every CVS/.svn directory to find all
  // the distinct checkouts.

  // An alternative implementation would use Files.walkFileTree, but that
  // is available only in Java 7.



//   /** Find all checkouts under the given directory. */
//   static Set<Checkout> findCheckouts(File dir) {
//     assert dir.isDirectory();
//
//     Set<Checkout> checkouts = new LinkedHashSet<Checkout>();
//
//     findCheckouts(dir, checkouts);
//
//     return checkouts;
//   }


  /**
   * Find all checkouts at or under the given directory (or, as a special
   * case, also its parent -- could rewrite to avoid that case), and adds
   * them to checkouts.  Works by checking whether dir or any of its
   * descendants is a version control directory.
   */
  private static void findCheckouts(File dir, Set<Checkout> checkouts, List<File> ignoreDirs) {
    if (! dir.isDirectory()) {
      // This should never happen, unless the directory is deleted between
      // the call to findCheckouts and the test of isDirectory.
      return;
    }
    if (ignoreDirs.contains(dir)) {
      return;
    }

    String dirName = dir.getName().toString();
    File parent = dir.getParentFile();
    if (parent != null) {
      // The "return" statements below cause the code not to look for
      // checkouts inside version control directories.  (But it does look
      // for checkouts inside other checkouts.)  If someone checks in (say)
      // a .svn file into a Mercurial repository, then removes it, the .svn
      // file remains in the repository even if not in the working copy.
      // That .svn file will cause an exception in dirToCheckoutSvn,
      // because it is not associated with a working copy.
      if (dirName.equals(".bzr")) {
        checkouts.add(new Checkout(RepoType.BZR, parent, null, null));
        return;
      } else if (dirName.equals("CVS")) {
        addCheckoutCvs(dir, parent, checkouts);
        return;
      } else if (dirName.equals(".hg")) {
        checkouts.add(dirToCheckoutHg(dir, parent));
        return;
      } else if (dirName.equals(".svn")) {
        Checkout c = dirToCheckoutSvn(parent);
        if (c != null)
          checkouts.add(c);
        return;
      }
    }

    @SuppressWarnings("nullness") // dependent: listFiles => non-null because dir is a directory, and we don't know that checkouts.add etc do not affect dir
    File /*@NonNull*/ [] childdirs = dir.listFiles(idf);
    if (childdirs == null) {
      System.err.printf("childdirs is null (permission or other I/O problem?) for %s%n", dir.toString());
      return;
    }
    for (File childdir : childdirs) {
      findCheckouts(childdir, checkouts, ignoreDirs);
    }
  }


  /** Accept only directories that are not symbolic links. */
  static class IsDirectoryFilter implements FileFilter {
    public boolean accept(File pathname) {
      try {
        return pathname.isDirectory()
          && pathname.getPath().equals(pathname.getCanonicalPath());
      } catch (IOException e) {
        System.err.printf("Exception in IsDirectoryFilter.accept(%s): %s%n", pathname, e);
        throw new Error(e);
        // return false;
      }
    }
  }

  static IsDirectoryFilter idf = new IsDirectoryFilter();


  /**
   * Given a directory named "CVS", create a corresponding Checkout object
   * for its parent, and add it to the given set.  (Google Web Toolkit does
   * that, for example.)
   */
  static void addCheckoutCvs(File cvsDir, File dir, Set<Checkout> checkouts) {
    assert cvsDir.getName().toString().equals("CVS") : cvsDir.getName();
    // relative path within repository
    File repositoryFile = new File(cvsDir, "Repository");
    File rootFile = new File(cvsDir, "Root");
    if (! (repositoryFile.exists() && rootFile.exists())) {
      // apparently it wasn't a version control directory
      return;
    }
    String pathInRepo = UtilMDE.readFile(repositoryFile).trim();
    String repoRoot = UtilMDE.readFile(rootFile).trim();
    /*@NonNull*/ File repoFileRoot = new File(pathInRepo);
    while (repoFileRoot.getParentFile() != null) {
      @SuppressWarnings("nullness") // just checked that parent is non-null
      /*@NonNull*/ File newRepoFileRoot = repoFileRoot.getParentFile();
      repoFileRoot = newRepoFileRoot;
    }

    // strip common suffix off of local dir and repo url
    Pair</*@Nullable*/ File, /*@Nullable*/ File> stripped
      = removeCommonSuffixDirs(dir, new File(pathInRepo),
                               repoFileRoot, "CVS");
    File cDir = stripped.a;
    if (cDir == null) {
      System.out.printf("dir (%s) is parent of path in repo (%s)",
                        dir, pathInRepo);
      System.exit(1);
    }
    String pathInRepoAtCheckout;
    if (stripped.b != null) {
      pathInRepoAtCheckout = stripped.b.toString();
    } else {
      pathInRepoAtCheckout = cDir.getName();
    }

    checkouts.add(new Checkout(RepoType.CVS, cDir, repoRoot, pathInRepoAtCheckout));
  }

  /**
   * Given a directory named ".hg" , create a corresponding Checkout object
   * for its parent.
   */
  static Checkout dirToCheckoutHg(File hgDir, File dir) {
    String repository = null;

    File hgrcFile = new File(hgDir, "hgrc");
    Ini ini;
    // There also exist Hg commands that will do this same thing.
    if (hgrcFile.exists()) {
      try {
        ini = new Ini(new FileReader(hgrcFile));
      } catch (IOException e) {
        throw new Error("Problem reading file " + hgrcFile);
      }

      Ini.Section pathsSection = ini.get("paths");
      if (pathsSection != null) {
        repository = pathsSection.get("default");
        if (repository != null && repository.endsWith("/")) {
          repository = repository.substring(0, repository.length()-1);
        }
      }
    }

    return new Checkout(RepoType.HG, dir, repository, null);
  }


  /**
   * Given a directory named ".git" , create a corresponding Checkout object
   * for its parent.
   */
  static Checkout dirToCheckoutGit(File gitDir, File dir) {
    String repository = UtilMDE.backticks("git", "config", "remote.origin.url");

    return new Checkout(RepoType.GIT, dir, repository, null);
  }


  /**
   * Given a directory that contains a .svn subdirectory, create a
   * corresponding Checkout object.
   * Returns null if this is not possible.
   */
  static /*@Nullable*/ Checkout dirToCheckoutSvn(File dir) {

    // For SVN, do
    //   svn info
    // and grep out these lines:
    //   URL: svn+ssh://login.csail.mit.edu/afs/csail/group/pag/projects/reCrash/repository/trunk/www
    //   Repository Root: svn+ssh://login.csail.mit.edu/afs/csail/group/pag/projects/reCrash/repository

    // Use SVNKit?
    // Con: introduces dependency on external library.
    // Pro: no need to re-implement or to call external process (which
    //   might be slow for large checkouts).

    @SuppressWarnings("nullness") // unannotated library: SVNKit
    SVNWCClient wcClient = new SVNWCClient((/*@Nullable*/ ISVNAuthenticationManager) null, null);
    SVNInfo info;
    try {
      info = wcClient.doInfo(new File(dir.toString()), SVNRevision.WORKING);
    } catch (SVNException e) {
      // throw new Error("Problem in dirToCheckoutSvn(" + dir + "): ", e);
      System.err.println("Problem in dirToCheckoutSvn(" + dir + "): " + e.getMessage());
      if (e.getMessage() != null && e.getMessage().contains("This client is too old")) {
        System.err.println("plume-lib needs a newer version of SVNKit.");
      }
      return null;
    }
    // getFile is null when operating on a working copy, as I am
    // String relativeFile = info.getPath(); // relative to repository root -- can use to determine root of checkout
    // getFile is just the (absolute) local file name for local items -- same as "dir"
    // File relativeFile = info.getFile();
    SVNURL url = info.getURL();
    // This can be null (example: dir /afs/csail.mit.edu/u/m/mernst/.snapshot/class/6170/2006-spring/3dphysics).  I don't know under what circumstances.
    SVNURL repoRoot = info.getRepositoryRootURL();
    if (repoRoot == null) {
      System.err.println("Problem:  old svn working copy in " + dir.toString());
      System.err.println("Check it out again to get a 'Repository Root' entry in the svn info output.");
      System.err.println("  repoUrl = " + url);
      System.exit(2);
    }
    if (debug) {
      System.out.println();
      System.out.println("repoRoot = " + repoRoot);
      System.out.println(" repoUrl = " + url);
      System.out.println("     dir = " + dir.toString());
    }

    // Strip common suffix off of local dir and repo url.
    Pair</*@Nullable*/ File, /*@Nullable*/ File> stripped
      = removeCommonSuffixDirs(dir, new File(url.getPath()),
                               new File(repoRoot.getPath()), ".svn");
    File cDir = stripped.a;
    if (cDir == null) {
      System.out.printf("dir (%s) is parent of repository URL (%s)",
                         dir, url.getPath());
      System.exit(1);
    }
    if (stripped.b == null) {
      System.out.printf("dir (%s) is child of repository URL (%s)",
                        dir, url.getPath());
      System.exit(1);
    }
    String pathInRepoAtCheckout = stripped.b.toString();
    try {
      url = url.setPath(pathInRepoAtCheckout, false);
    } catch (SVNException e) {
      throw new Error(e);
    }

    if (debug) {
      System.out.println("stripped: " + stripped);
      System.out.println("repoRoot = " + repoRoot);
      System.out.println(" repoUrl = " + url);
      System.out.println("    cDir = " + cDir.toString());
    }

    assert url.toString().startsWith(repoRoot.toString())
      : "repoRoot="+repoRoot+", url="+url;
    return new Checkout(RepoType.SVN, cDir, url.toString(), null);

    /// Old implementation
    // String module = url.toString().substring(repoRoot.toString().length());
    // if (module.startsWith("/")) {
    //   module = module.substring(1);
    // }
    // if (module.equals("")) {
    //   module = null;
    // }
    // return new Checkout(RepoType.SVN, cDir, repoRoot.toString(), module);



  }

  /**
   * Strip identical elements off the end of both paths, and then return
   * what is left of each.  Returned elements can be null!  If p2_limit is
   * non-null, then it should be a parent of p2, and the stripping stops
   * when p2 becomes p2_limit.  If p1_contains is non-null, then p1 must
   * contain a subdirectory of that name.
   */
  static Pair</*@Nullable*/ File,/*@Nullable*/ File> removeCommonSuffixDirs(File p1, File p2, File p2_limit, String p1_contains) {
    if (debug) {
      System.out.printf("removeCommonSuffixDirs(%s, %s, %s, %s)%n", p1, p2, p2_limit, p1_contains);
    }
    // new names for results, because we will be side-effecting them
    File r1 = p1;
    File r2 = p2;
    while (r1 != null
           && r2 != null
           && (p2_limit == null || ! r2.equals(p2_limit))
           && r1.getName().equals(r2.getName())) {
      if (p1_contains != null
          && ! new File(r1.getParentFile(), p1_contains).isDirectory()) {
        break;
      }
      r1 = r1.getParentFile();
      r2 = r2.getParentFile();
    }
    if (debug) {
      System.out.printf("removeCommonSuffixDirs => %s %s%n", r1, r2);
    }
    return Pair.of(r1,r2);
  }


  ///////////////////////////////////////////////////////////////////////////
  /// Process checkouts
  ///

  /** Change pb's command by adding the given argument at the end. */
  private void addArg(ProcessBuilder pb, String arg) {
    List<String> command = pb.command();
    command.add(arg);
    pb.command(command);
  }

  /** Change pb's command by adding the given arguments at the end. */
  private void addArgs(ProcessBuilder pb, List<String> args) {
    List<String> command = pb.command();
    command.addAll(args);
    pb.command(command);
  }

  private static class Replacer {
    /*@Regex*/ String regexp;
    String replacement;
    public Replacer(/*@Regex*/ String regexp, String replacement) {
      this.regexp = regexp;
      this.replacement = replacement;
    }
    public String replaceAll(String s) {
      return s.replaceAll(regexp, replacement);
    }
  }

  public void process(Set<Checkout> checkouts) {
    // Always run at least one command, but sometimes up to three.
    ProcessBuilder pb = new ProcessBuilder("");
    ProcessBuilder pb2 = new ProcessBuilder(new ArrayList<String>());
    ProcessBuilder pb3 = new ProcessBuilder(new ArrayList<String>());
    pb.redirectErrorStream(true);
    pb2.redirectErrorStream(true);
    pb3.redirectErrorStream(true);
    // I really want to be able to redirect output to a Reader, but that
    // isn't possible.  I have to send it to a file.
    // I can't just use the InputStream directly, because if the process is
    // killed because of a timeout, the stream is inaccessible.

    CHECKOUTLOOP:
    for (Checkout c : checkouts) {
      if (debug) {
        System.out.println(c);
      }
      File dir = c.directory;

      List<Replacer> replacers = new ArrayList<Replacer>();
      List<Replacer> replacers3 = new ArrayList<Replacer>();

      switch (c.repoType) {
      case BZR:
        break;
      case CVS:
        replacers.add(new Replacer("(^|\\n)([?]) ", "$1$2 " + dir + "/"));
        break;
      case GIT:
        replacers.add(new Replacer("(^|\\n)fatal:", "$1fatal in " + dir + ":"));
        replacers.add(new Replacer("(^|\\n)warning:", "$1warning in " + dir + ":"));
        replacers.add(new Replacer("(^|\\n)(There is no tracking information for the current branch\\.)", "$1" + dir + ": " + "$2"));
        replacers.add(new Replacer("(^|\\n)(Your configuration specifies to merge)", dir + ": $1$2"));
        break;
      case HG:
        // "real URL" is for bitbucket.org.  (Should be early in list.)
        replacers.add(new Replacer("(^|\\n)real URL is .*\\n", "$1"));
        replacers.add(new Replacer("(^|\\n)(abort: .*)", "$1$2: " + dir));
        replacers.add(new Replacer("(^|\\n)([MARC!?I]) ", "$1$2 " + dir + "/"));
        replacers.add(new Replacer("(^|\\n)(\\*\\*\\* failed to import extension .*: No module named demandload\\n)", "$1"));
        // Hack, should be replaced when googlecode certificate problems are fixed.
        replacers.add(new Replacer("(^|\\n)warning: .* certificate not verified \\(check web.cacerts config setting\\)\\n", "$1"));
        // May appear twice in output with overlapping matches, so repeat the replacer
        replacers.add(new Replacer("(^|\\n)warning: .* certificate not verified \\(check web.cacerts config setting\\)\\n", "$1"));
        // Does this mask too many errors?
        replacers.add(new Replacer("(^|\\n)((comparing with default-push\\n)?abort: repository default(-push)? not found!: .*\\n)", "$1"));
        break;
      case SVN:
        replacers.add(new Replacer("(svn: Network connection closed unexpectedly)", "$1 for " + dir));
        replacers.add(new Replacer("(svn: Repository) (UUID)", "$1 " + dir + " $2"));
        replacers.add(new Replacer("(svn: E155037: Previous operation has not finished; run 'cleanup' if it was interrupted)", "$1; for " + dir));
        break;
      default:
        assert false;
      }
      // The \r* is necessary here; (somtimes?) there are two carriage returns.
      replacers.add(new Replacer("(remote: )?Warning: untrusted X11 forwarding setup failed: xauth key data not generated\r*\n(remote: )?Warning: No xauth data; using fake authentication data for X11 forwarding\\.\r*\n", ""));
      replacers.add(new Replacer("(working copy ')", "$1" + dir));

      pb.command("echo", "command", "not", "set");
      pb.directory(dir);
      pb2.command(new ArrayList<String>());
      pb2.directory(dir);
      pb3.command(new ArrayList<String>());
      pb3.directory(dir);
      boolean show_normal_output = false;
      // Set pb.command() to be the command to be executed.
      switch (action) {
      case LIST:
        System.out.println(c);
        continue CHECKOUTLOOP;
      case CHECKOUT:
        pb.directory(dir.getParentFile());
        String dirbase = dir.getName();
        if (c.repository == null) {
          System.out.printf("Skipping checkout with unknown repository:%n  %s%n",
                            dir);
          continue CHECKOUTLOOP;
        }
        switch (c.repoType) {
        case BZR:
          System.out.println("bzr handling not yet implemented: skipping " + c.directory);
          break;
        case CVS:
          assert c.module != null : "@AssumeAssertion(nullness): dependent type CVS";
          pb.command(cvs_executable, "-d", c.repository, "checkout",
                     "-P", // prune empty directories
                     "-ko", // no keyword substitution
                     c.module);
          addArgs(pb, cvs_arg);
          break;
        case GIT:
          pb.command(git_executable, "clone", c.repository, dirbase);
          addArgs(pb, git_arg);
          break;
        case HG:
          pb.command(hg_executable, "clone", c.repository, dirbase);
          addArgs(pb, hg_arg);
          if (insecure) addArg(pb, "--insecure");
          break;
        case SVN:
          if (c.module != null) {
            pb.command(svn_executable, "checkout", c.repository, c.module);
          } else {
            pb.command(svn_executable, "checkout", c.repository);
          }
          addArgs(pb, svn_arg);
          break;
        default:
          assert false;
        }
        break;
      case STATUS:
        // I need a replacer for other version control systems, to add
        // directory names.
        show_normal_output = true;
        switch (c.repoType) {
        case BZR:
          System.out.println("bzr handling not yet implemented: skipping " + c.directory);
          break;
        case CVS:
          assert c.repository != null;
          pb.command(cvs_executable, "-q",
                     // Including "-d REPOS" seems to give errors when a
                     // subdirectory is in a different CVS repository.
                     // "-d", c.repository,
                     "diff",
                     "-b",      // compress whitespace
                     "--brief", // report only whether files differ, not details
                     "-N");     // report new files
          addArgs(pb, cvs_arg);
          //         # For the last perl command, this also works:
          //         #   perl -p -e 'chomp(\$cwd = `pwd`); s/^Index: /\$cwd\\//'";
          //         # but the one we use is briefer and uses the abbreviated directory name.
          //         $filter = "grep -v \"unrecognized keyword 'UseNewInfoFmtStrings'\" | grep \"^Index:\" | perl -p -e 's|^Index: |$dir\\/|'";
          String removeRegexp
            = ("\n=+"
               + "\nRCS file: .*" // no trailing ,v for newly-created files
               + "(\nretrieving revision .*)?" // no output for newly-created files
               + "\ndiff .*"
               + "(\nFiles .* and .* differ)?" // no output if only whitespace differences
               );
          replacers.add(new Replacer(removeRegexp, ""));
          replacers.add(new Replacer("(^|\\n)Index: ", "$1" + dir + "/"));
          replacers.add(new Replacer("(^|\\n)(cvs \\[diff aborted)(\\]:)", "$1$2 in " + dir + "$3"));
          replacers.add(new Replacer("(^|\\n)(Permission denied)", "$1$2 in " + dir));
          replacers.add(new Replacer("(^|\\n)(cvs diff: )(cannot find revision control)", "$1$2 in " + dir + ": $3"));
          replacers.add(new Replacer("(^|\\n)(cvs diff: cannot find )", "$1$2" + dir));
          replacers.add(new Replacer("(^|\\n)(cvs diff: in directory )", "$1$2" + dir + "/"));
          replacers.add(new Replacer("(^|\\n)(cvs diff: ignoring )", "$1$2" + dir + "/"));
          break;
        case GIT:
          pb.command(git_executable, "status");
          addArgs(pb, git_arg);
          // Why was I using this option??
          // addArg(pb, "--untracked-files=no");
          addArg(pb, "--porcelain"); // experimenting with porcelain output
          replacers.add(new Replacer("(^|\\n)On branch master\\nYour branch is up-to-date with 'origin/master'.\\n\\n?", "$1"));
          replacers.add(new Replacer("(^|\\n)nothing to commit,? working directory clean\\n", "$1"));
          replacers.add(new Replacer("(^|\\n)no changes added to commit \\(use \"git add\" and/or \"git commit -a\"\\)\\n", "$1"));
          replacers.add(new Replacer("(^|\\n)nothing added to commit but untracked files present \\(use \"git add\" to track\\)\\n", "$1"));
          replacers.add(new Replacer("(^|\\n)nothing to commit \\(use -u to show untracked files\\)\n", "$1"));

          replacers.add(new Replacer("(^|\\n)#\\n", "$1"));
          replacers.add(new Replacer("(^|\\n)# On branch master\\n", "$1"));
          replacers.add(new Replacer("(^|\\n)nothing to commit \\(working directory clean\\)\\n", "$1"));
          replacers.add(new Replacer("(^|\\n)# Changed but not updated:\\n", "$1"));
          replacers.add(new Replacer("(^|\\n)#   \\(use \"git add <file>...\" to update what will be committed\\)\\n", "$1"));
          replacers.add(new Replacer("(^|\\n)#   \\(use \"git checkout -- <file>...\" to discard changes in working directory\\)\\n", "$1"));
          replacers.add(new Replacer("(^|\\n)# Untracked files:\\n", "$1"));
          replacers.add(new Replacer("(^|\\n)#   \\(use \"git add <file>...\" to include in what will be committed\\)\\n", "$1"));

          replacers.add(new Replacer("(^|\\n)(#\tmodified:   )", "$1" + dir + "/"));
          // This must come after the above, since it matches a prefix of the above
          replacers.add(new Replacer("(^|\\n)(#\t)", "$1untracked: " + dir + "/"));
          replacers.add(new Replacer("(^|\\n)# Your branch is ahead of .*\\n", "$1unpushed changesets: " + pb.directory() + "\n"));
          replacers.add(new Replacer("(^|\\n)([?][?]) ", "$1$2 " + dir + "/"));
          replacers.add(new Replacer("(^|\\n)([ ACDMRU][ACDMRTU]) ", "$1$2 " + dir + "/"));
          replacers.add(new Replacer("(^|\\n)([ACDMRU][ ACDMRTU]) ", "$1$2 " + dir + "/"));

          // Useful info, but don't bother to report it, for consistency with other VCSes
          replacers.add(new Replacer("(^|\\n)# Your branch is behind .*\\n", "$1unpushed changesets: " + pb.directory() + "\n"));

          // Could remove all other output, but this could suppress messages
          // replacers.add(new Replacer("(^|\\n)#.*\\n", "$1"));

          // Necessary because "git status --porcelain" does not report:
          //   # Your branch is ahead of 'origin/master' by 1 commit.
          // If you have pushed but not pulled, then this will report
          pb2.command(git_executable, "log", "--branches", "--not", "--remotes");
          addArgs(pb2, git_arg);
          replacers.add(new Replacer("^commit .*(.*\\n)+", "unpushed commits: " + pb2.directory() + "\n"));

          // TODO: look for stashes

          break;
        case HG:
          pb.command(hg_executable, "status");
          addArgs(pb, hg_arg);
          if (debug) {
            System.out.printf("invalidCertificate(%s) => %s%n", c.directory, invalidCertificate(c.directory));
          }
          if (invalidCertificate(c.directory)) {
            pb2.command(hg_executable, "outgoing", "-l", "1", "--config", "web.cacerts=");
          } else {
            pb2.command(hg_executable, "outgoing", "-l", "1");
          }
          addArgs(pb2, hg_arg);
          if (insecure) addArg(pb2, "--insecure");
          // The third line is either "no changes found" or "changeset".
          replacers.add(new Replacer("^comparing with .*\\nsearching for changes\\nchangeset[^\001]*", "unpushed changesets: " + pb.directory() + "\n"));
          replacers.add(new Replacer("^\\n?comparing with .*\\nsearching for changes\\nno changes found\n", ""));
          // TODO:  Shelve is an optional extension, and so this should make no report if it is not installed.
          pb3.command(hg_executable, "shelve", "-l");
          addArgs(pb3, hg_arg);
          replacers3.add(new Replacer("^hg: unknown command 'shelve'\\n(.*\\n)+", ""));
          replacers3.add(new Replacer("^(.*\\n)+", "shelved changes: " + pb.directory() + "\n"));
          break;
        case SVN:
          // Handle some changes.
          // "svn status" also outputs an eighth column, only if you pass the --show-updates switch: [* ]
          replacers.add(new Replacer("(^|\\n)([ACDIMRX?!~ ][CM ][L ][+ ][$ ]) *", "$1$2 " + dir + "/"));
          pb.command(svn_executable, "status");
          addArgs(pb, svn_arg);
          break;
        default:
          assert false;
        }
        break;
      case UPDATE:
        switch (c.repoType) {
        case BZR:
          System.out.println("bzr handling not yet implemented: skipping " + c.directory);
          break;
        case CVS:
          replacers.add(new Replacer("(^|\\n)(cvs update: ((in|skipping) directory|conflicts found in )) +", "$1$2 " + dir + "/"));
          replacers.add(new Replacer("(^|\\n)(Merging differences between 1.16 and 1.17 into )", "$1$2 " + dir + "/"));
          assert c.repository != null;
          pb.command(cvs_executable,
                     // Including -d causes problems with CVS repositories
                     // that are embedded inside other repositories.
                     // "-d", c.repository,
                     "-Q", "update", "-d");
          addArgs(pb, cvs_arg);
          //         $filter = "grep -v \"config: unrecognized keyword 'UseNewInfoFmtStrings'\"";
          replacers.add(new Replacer("(cvs update: move away )", "$1" + dir + "/"));
          replacers.add(new Replacer("(cvs \\[update aborted)(\\])", "$1 in " + dir + "$2"));
          break;
        case GIT:
          replacers.add(new Replacer("(^|\\n)Already up-to-date\\.\\n", "$1"));
          replacers.add(new Replacer("(^|\\n)error:", "$1error in " + dir + ":"));
          replacers.add(new Replacer("(^|\\n)Please, commit your changes or stash them before you can merge.\\nAborting\\n", "$1"));
          replacers.add(new Replacer("((^|\\n)CONFLICT \\(content\\): Merge conflict in )", "$1" + dir + "/"));
          replacers.add(new Replacer("(^|\\n)([ACDMRU]\t)", "$1$2" + dir + "/"));
          pb.command(git_executable, "pull", "-q");
          addArgs(pb, git_arg);
          break;
        case HG:
          replacers.add(new Replacer("(^|\\n)([?!AMR] ) +", "$1$2 " + dir + "/"));
          replacers.add(new Replacer("(^|\\n)abort: ", "$1"));
          pb.command(hg_executable, "-q", "update");
          addArgs(pb, hg_arg);
          if (invalidCertificate(c.directory)) {
            pb2.command(hg_executable, "-q", "fetch", "--config", "web.cacerts=");
          } else {
            pb2.command(hg_executable, "-q", "fetch");
          }
          addArgs(pb2, hg_arg);
          if (insecure) addArg(pb2, "--insecure");
          break;
        case SVN:
          replacers.add(new Replacer("(^|\\n)([?!AMR] ) +", "$1$2 " + dir + "/"));
          replacers.add(new Replacer("(svn: Failed to add file ')(.*')", "$1" + dir + "/" + "$2"));
          assert c.repository != null;
          pb.command(svn_executable, "-q", "update");
          addArgs(pb, svn_arg);
        //         $filter = "grep -v \"Killed by signal 15.\"";
          break;
        default:
          assert false;
        }
        break;
      default:
        assert false;
      }

      // Check that the directory exists (OK if it doesn't for checkout).
      if (debug) {
        System.out.println(dir + ":");
      }
      if (dir.exists()) {
        if (action == CHECKOUT && ! redo_existing && ! quiet) {
          System.out.println("Skipping checkout (dir already exists): " + dir);
          continue;
        }
      } else {
        // Directory does not exist
        File parent = dir.getParentFile();
        if (parent == null) {
          // This happens when dir is the root directory.
          // It doesn't happen merely when the parent doesn't yet exist.
          System.err.printf("Directory %s does not exist, and it has no parent%n", dir);
          continue;
        }
        switch (action) {
        case CHECKOUT:
          if (! parent.exists()) {
            if (show) {
              if (! dry_run) {
                System.out.printf("Parent directory %s does not exist%s%n",
                                  parent, (dry_run ? "" : " (creating)"));
              } else {
                System.out.printf("  mkdir -p %s%n", parent);
              }
            }
            if (! dry_run) {
              if (! parent.mkdirs()) {
                System.err.println("Could not create directory: " + parent);
                System.exit(1);
              }
            }
          }
          break;
        case STATUS:
        case UPDATE:
          if (! quiet) {
            System.out.println("Cannot find directory: " + dir);
          }
          continue CHECKOUTLOOP;
        case LIST:
        default:
          assert false;
        }
      }

      if (print_directory) {
        System.out.println(dir + " :");
      }
      perform_command(pb, replacers, show_normal_output);
      if (pb2.command().size() > 0) perform_command(pb2, replacers, show_normal_output);
      if (pb3.command().size() > 0) perform_command(pb3, replacers3, show_normal_output);
    }
  }

  private /*@Regex(1)*/ Pattern defaultPattern = Pattern.compile("^default[ \t]*=[ \t]*(.*)");

  /**
   * Given a directory containing a Mercurial checkout, return its default
   * path.  Return null otherwise.
   */
  // This implementation is not quite right because we didn't look for the
  // [path] section.  We could fix this by using a real ini reader or
  // calling "hg showconfig".  This hack is good enough for now.
  private /*@Nullable*/ String defaultPath(File dir) {
    File hgrc = new File(new File(dir, ".hg"), "hgrc");
    try (EntryReader er = new EntryReader(hgrc, "^#.*", null)) {
      for (String line : er) {
        Matcher m = defaultPattern.matcher(line);
        if (m.matches()) {
          return m.group(1);
        }
      }
    } catch (IOException e) {
      // System.out.printf("IOException: " + e);
      return null;
    }
    return null;
  }

  private Pattern invalidCertificatePattern = Pattern.compile("^https://[^.]*[.][^.]*[.]googlecode[.]com/hg$");

  private boolean invalidCertificate(File dir) {
    String defaultPath = defaultPath(dir);
    if (debug) { System.out.printf("defaultPath=%s for %s%n", defaultPath, dir); }
    if (defaultPath == null) {
      return false;
    }
    return (defaultPath.startsWith("https://hg.codespot.com/")
            || invalidCertificatePattern.matcher(defaultPath).matches());
  }


  // If show_normal_output is true, then display the output even if the process
  // completed normally.  Ordinarily, output is displayed only if the
  // process completed erroneously.
  void perform_command(ProcessBuilder pb, List<Replacer> replacers, boolean show_normal_output) {
    /// The redirectOutput method only exists in Java 1.7.  Sigh.
    /// The workaround is to make TimeLimitProcess buffer its output.
    // File tempFile;
    // try {
    //   tempFile = File.createTempFile("mvc", null);
    // } catch (IOException e) {
    //   throw new Error("File.createTempFile can't create temporary file.", e);
    // }
    // tempFile.deleteOnExit();
    // pb.redirectOutput(tempFile);

    if (show) {
      System.out.println(command(pb));
    }
    if (dry_run) {
      return;
    }
    try {
      // Perform the command

      // For debugging
      //  my $command_cwd_sanitized = $command_cwd;
      //  $command_cwd_sanitized =~ s/\//_/g;
      //  $tmpfile = "/tmp/cmd-output-$$-$command_cwd_sanitized";
      // my $command_redirected = "$command > $tmpfile 2>&1";
      TimeLimitProcess p = new TimeLimitProcess(pb.start(), timeout * 1000, true);
      p.waitFor();
      // For reasons that are mysterious to me, this is necessary in order to
      // reliably capture the process's output.  I don't know why.  Calling
      // waitFor on the result of pb.start() didn't help -- only this did.
      Thread.sleep(10);
      if (p.timed_out()) {
        System.out.printf("Timed out (limit: %ss):%n", timeout);
        System.out.println(command(pb));
        // Don't return; also show the output
      }

      // Under what conditions should the output be printed?
      //  * for status, always
      //  * whenever the process exited non-normally
      //  * when debugging
      //  * other circumstances?
      // Try printing always, to better understand this question.
      if (show_normal_output || p.exitValue() != 0 || debug_replacers || debug_process_output) {
        // Filter then print the output.
        // String output = UtilMDE.readerContents(new BufferedReader(new InputStreamReader(p.getInputStream())));
        // String output = UtilMDE.streamString(p.getInputStream());
        String output = UtilMDE.streamString(p.getInputStream());
        if (debug_replacers || debug_process_output) {
          System.out.println("preoutput=<<<" + output + ">>>");
        }
        for (Replacer r : replacers) {
          if (debug_replacers) { System.out.println("midoutput_pre[" + r.regexp + "]=<<<" + output + ">>>"); }
          // Don't loop, because some regexps will continue to match repeatedly
          output = r.replaceAll(output);
          if (debug_replacers) { System.out.println("midoutput_post[" + r.regexp + "]=<<<" + output + ">>>"); }
        }
        if (debug_replacers || debug_process_output) {
          System.out.println("postoutput=<<<" + output + ">>>");
        }
        if (debug_replacers) {
          for (int i=0; i<Math.min(100,output.length()); i++) {
            System.out.println(i + ": " + (int) output.charAt(i) + "\n        \"" + output.charAt(i) + "\"");
          }
        }
        System.out.print(output);
      }

    } catch (IOException e) {
      throw new Error(e);
    } catch (InterruptedException e) {
      throw new Error(e);
    }
  }


  String command(ProcessBuilder pb) {
    return "  cd " + pb.directory() + "\n"
      + "  " + UtilMDE.join(pb.command(), " ");
  }


//     # Show the command.
//     if ($show) {
//       if (($action eq "checkout")
//           # Better would be to change the printed (but not executed) command
//           # || (($action eq "update") && defined($svnroot))
//           || ($action eq "update")) {
//         print "cd $command_cwd\n";
//       }
//       print "command: $command\n";
//     }
//
//     # Perform the command
//     if (! $dry_run) {
//       my $tmpfile = "/tmp/cmd-output-$$";
//       # For debugging
//       # my $command_cwd_sanitized = $command_cwd;
//       # $command_cwd_sanitized =~ s/\//_/g;
//       # my $tmpfile = "/tmp/cmd-output-$$-$command_cwd_sanitized";
//       my $command_redirected = "$command > $tmpfile 2>&1";
//       if ($debug) { print "About to execute: $command_redirected\n"; }
//       my $result = system("$command_redirected");
//       if ($debug) { print "Executed: $command_redirected\n"; }
//       if ($debug) { print "raw result = $result\n"; }
//       if ($result == -1) {
//         print "failed to execute: $command_redirected: $!\n";
//       } elsif ($result & 127) {
//         printf "child died with signal %d, %s coredump%n",
//         ($result & 127),  ($result & 128) ? 'with' : 'without';
//       } else {
//         # Problem:  diff returns failure status if there were differences
//         # or if there was an error, so ther's no good way to detect errors.
//         $result = $result >> 8;
//         if ($debug) { print "shifted result = $result\n"; }
//         if ((($action eq "status") && ($result != 0) && ($result != 1))
//             || (($action ne "status") && ($result != 0))) {
//           print "exit status $result for:\n  cd $command_cwd;\n  $command_redirected\n";
//           system("cat $tmpfile");
//         }
//       }
//       # Filter the output
//       if (defined($filter)) {
//         system("cat $tmpfile | $filter > $tmpfile-2");
//         rename("$tmpfile-2", "$tmpfile");
//       }
//       if ($debug && $show_directory) {
//         print "show-directory: $dir:\n";
//         printf "tmpfile size: %d, zeroness: %d, non-zeroness %d%n", (-s $tmpfile), (-z $tmpfile), (! -z $tmpfile);
//       }
//       if ((! -z $tmpfile) && $show_directory) {
//         print "$dir:\n";
//       }
//       system("cat $tmpfile");
//       unlink($tmpfile);
//     }
//     next;
//   }
// }

  /**
   * A stream of newlines.  Used for processes that want input, when we
   * don't want to give them input but don't want them to simply hang. */
  static class StreamOfNewlines extends InputStream {
    public int read() {
      return (int) '\n';
    }
  }

//   static interface BufferedReaderFilter {
//     void process(Stream s);
//   }
//
//   public static class CvsDiffFilter implements BufferedReaderFilter {
//
//     BufferedReader reader;
//     String directory;
//
//     public CvsDiffFilter(BufferedReader reader, String directory) {
//       this.reader = reader;
//       this.directory = directory;
//     }
//
//     public void close() {
//       reader.close();
//     }
//
//     public void mark(int readAheadLimit) {
//       reader.mark(readAheadLimit);
//     }
//
//     public boolean markSupported() {
//       reader.markSupported();
//     }
//
//     public int read() {
//       throw new UnsupportedOperationException();
//       // reader.read();
//     }
//
//     public int read(char[] cbuf, int off, int len) {
//       throw new UnsupportedOperationException();
//       // reader.read(char[] cbuf, int off, int len);
//     }
//
//     public String readLine() {
//       String result = reader.readLine();
//       if (result == null) {
//         return result;
//       } else if (result.startsWith("Index: ")) {
//         return directory + result.substring(7);
//       } else {
//         return "";
//       }
//     }
//
//     public boolean ready() {
//       reader.ready();
//     }
//
//     public void reset() {
//       reader.reset();
//     }
//
//     public long skip(long n) {
//       reader.skip(n);
//     }
//
//   }

}
