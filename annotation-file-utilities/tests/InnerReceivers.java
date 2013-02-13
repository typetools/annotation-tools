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

    class Inner1<Y, Z> {

        public Inner1() {}

        void m(InnerReceivers.Inner1<Y, Z> this) {}

        class Inner2 {

            public Inner2() {}

            void m(InnerReceivers.Inner1<Y, Z>.Inner2 this) {}
        }
    }
}
