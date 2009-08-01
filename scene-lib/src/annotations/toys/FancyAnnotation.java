package annotations.toys;

public @interface FancyAnnotation {
    int myInt();

    String left();

    SimplerAnnotation[] friends();
}
