package scenelib.annotations.toys;

@ClassTokenAnnotation(favoriteClasses = { String.class, int.class, void.class,
        int[].class, Object[][][].class })
public interface SimplerInterface {
    int myField = 1;
}
