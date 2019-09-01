package samples.sample2;

public class FiberSample {

    public static void main(String[] args) {
        print("main start");
        try (var scope1 = FiberScope.open()) {
            var fiber1 = scope1.schedule(() -> foo());
            var fiber2 = scope1.schedule(() -> bar());
        }
        print("main end");
    }

    static void foo() {
        print("foo start");
        try (var scope2 = FiberScope.open()) {
            scope2.schedule(() -> print("foo 1"));
            scope2.schedule(() -> print("foo 2"));
        }
        print("foo end");
    }

    static void bar() {
        print("bar start");
        try (var scope3 = FiberScope.open()) {
            scope3.schedule(() -> print("bar 1"));
            scope3.schedule(() -> print("bar 2"));
        }
        print("bar end");
    }

    static void print(String s){
        System.out.println(s);
    }
}
