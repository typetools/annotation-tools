package annotator.tests;

public class Receivers {
    public void m() {}

    public void m(int i) {}

    public void m(@Anno() String s) {}
}

class Receivers2 {
    public void m(Receivers2 this) {}

    public void m(Receivers2 this, int i) {}
}

class Receivers3<K, V> {
    public void m() {}

    public void m(int i) {}
}

class Receivers4<K, V> {
    public void m(Receivers4<K, V> this) {}

    public void m(Receivers4<K, V> this, int i) {}
}

interface Receivers5 {
    public void m();
}

enum Receivers6 {
    TEST;
    public void m() {}
}

class Receivers7<K extends Object> {
    public void m() {}
}

class Receivers8<K extends Object> {
    public void m(Receivers8<K> this) {}
}

@interface Anno {}
