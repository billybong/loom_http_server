package samples.sample1;

public class ContinuationSample {

    public static void main(String[] args) {
        var scope = new ContinuationScope("sample");
        var continuation = new Continuation(scope, () -> {
            print("1");
            Continuation.yield(scope);
            print("2");
            Continuation.yield(scope);
            print("3");
        });

        while (!continuation.isDone()) {
            print("Before run");
            continuation.run();
            print("After run");
        }
        print("Done!");
    }

    private static void print(String s){
        System.out.println(s);
    }
}
