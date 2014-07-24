package annotations.toys;

@ClassTokenAnnotation(favoriteClasses = { String.class, int.class, void.class,
        int[].class, Object[][][].class })
public interface SimpleInterface {
    int myField = 1;
}
