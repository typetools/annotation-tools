package annotator.tests;

public class InnerReceivers {

    InnerReceivers i = new InnerReceivers() {

        void m() {}

        class Inner {
            public Inner() {}

            void m(Inner this) {}
        }
    };

    public InnerReceivers() {}

    void m(InnerReceivers this) {}

    void m2(annotator.tests.InnerReceivers this) {}

    class Inner1<Y, Z> {

        public Inner1() {}

        void m(InnerReceivers.Inner1<Y, Z> this) {}

        void m2(annotator.tests.InnerReceivers.Inner1<Y, Z> this) {}

        class Inner2 {

            public Inner2() {}

            void m(InnerReceivers.Inner1<Y, Z>.Inner2 this) {}
        }
    }

    static class StaticInner1 {

        public StaticInner1() {}

        void m(InnerReceivers.StaticInner1 this) {}

        void m2(annotator.tests.InnerReceivers.StaticInner1 this) {}
    }
}

class Outer<K> {
    static class StaticInner2 {

        public StaticInner2() {}

        void m(Outer.StaticInner2 this) {}

        void m2(annotator.tests.Outer.StaticInner2 this) {}

        static class StaticInner3 {

            public StaticInner3() {}
        }
    }
}
