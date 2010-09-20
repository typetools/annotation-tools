
:: Extract annotations from a class file and write them to an annotation file.
:: For usage information, run: extract-annotations.bat --help 
:: See the annotation file utilities documentation for more information.

set ANNOTATION_FILE_UTILS=%~d0
set ANNOTATION_FILE_UTILS=%ANNOTATION_FILE_UTILS%%~p0
set ANNOTATION_FILE_UTILS=%ANNOTATION_FILE_UTILS%annotation-file-utilities.jar
:: Contains new version of java.lang.annotation.ElementType.
set JAVAC_JAR=%~d0
set JAVAC_JAR=%ANNOTATION_FILE_UTILS%%~p0
set JAVAC_JAR=%JAVAC_JAR%..\..\..\jsr308-langtools\dist\lib\javac.jar

java -ea "-Xbootclasspath/p:%ANNOTATION_FILE_UTILS%;%JAVAC_JAR%" -cp "%ANNOTATION_FILE_UTILS%;%CLASSPATH%" annotations.io.classfile.ClassFileReader %*

