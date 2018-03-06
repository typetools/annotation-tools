:: Insert annotations (from an annotation file) into a Java source file.
:: For usage information, run: insert-annotations-to-source.bat --help
:: See the annotation file utilities documentation for more information.

set ANNOTATION_FILE_UTILS=%~d0
set ANNOTATION_FILE_UTILS=%ANNOTATION_FILE_UTILS%%~p0
set ANNOTATION_FILE_UTILS=%ANNOTATION_FILE_UTILS%\..\annotation-file-utilities.jar
set TOOLS_JAR=%JAVA_HOME%%~p0
set TOOLS_JAR=%TOOLS_JAR%\lib\tools.jar

java -ea -cp "%TOOLS_JAR%;%ANNOTATION_FILE_UTILS%;%CLASSPATH%" annotator.Main %*
