
:: Extract annotations from a class file and write them to an annotation file.
:: For usage information, run: extract-annotations.bat --help 
:: See the annotation file utilities documentation for more information.

set ANNOTATION_FILE_UTILS=%~d0
set ANNOTATION_FILE_UTILS=%ANNOTATION_FILE_UTILS%%~p0
set ANNOTATION_FILE_UTILS=%ANNOTATION_FILE_UTILS%annotation-file-utilities.jar

java -cp "%ANNOTATION_FILE_UTILS%;%CLASSPATH%" annotations.io.classfile.ClassFileReader %*

