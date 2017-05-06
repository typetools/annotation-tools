[Pull Request for this fix](https://github.com/typetools/annotation-tools/pull/129).

Reason of using a separate directory to test enum-imports:

The parent directory set 'abbreviate=false' when calling AFU to insert annotations,
this will cause AFU insert annotation fully-qualified name for each annotation location rather than
insert imports statements in the begin of a source code file and then insert annotation simple name
for each annotation location. Since we want to test the import insertions is correct for enum fields
of an annotation, thus I use this separate directory, in which it will calling AFU with 'abbreviate=true'
to test annotation enum field import insertions.
