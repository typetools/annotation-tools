package annotations.toys;

import java.util.*;

public @interface SimpleAnnotation {
    BalanceEnum be();

    int height();

    int[] wrappedHeight();

    Class<? super HashMap<String, String>> favoriteClass();
}
