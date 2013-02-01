public class InnerReceivers {

    InnerReceivers i = new InnerReceivers() {

        void m() {}

        class Inner {
            public Inner() {}

            void m(Inner this) {}
        }
    };
}
