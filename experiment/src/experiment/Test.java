package experiment;

import experiment.Annotations.*;

@A public class Test<@B T1 extends Super1> extends @C Super2<@D T2> {
}

class Super1 { }
class Super2<E> { }
class T2 { }