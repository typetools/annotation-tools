
:: Insert annotations (from an annoation file) into a class file.
:: For usage information, run: insert-annotations.bat --help
:: See the annotation file utilities documentation for more information.

set ANNOTATION_FILE_UTILS=%~d0
set ANNOTATION_FILE_UTILS=%ANNOTATION_FILE_UTILS%%~p0
set ANNOTATION_FILE_UTILS=%ANNOTATION_FILE_UTILS%\..\annotation-file-utilities.jar
set JAVAC_JAR=%~d0
set JAVAC_JAR=%ANNOTATION_FILE_UTILS%%~p0
set JAVAC_JAR=%JAVAC_JAR%..\..\..\jsr308-langtools\dist\lib\javac.jar

java -ea -cp "%JAVAC_JAR%;%ANNOTATION_FILE_UTILS%;%CLASSPATH%" scenelib.annotations.io.classfile.ClassFileWriter %*
