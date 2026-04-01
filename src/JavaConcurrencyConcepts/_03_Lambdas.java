import java.util.ArrayList;
import java.util.List;

@FunctionalInterface
interface MathOperation {
    public double execute(double a, double b);
}
public class Lambdas_3 {
    public static void main(String[] args) {
        List<MathOperation> operations = new ArrayList<>();
        operations.add((a, b) -> a + b);
        operations.add((a, b) -> a - b);
        operations.add((a, b) -> a * b);
        operations.add((a, b) -> a / b);

        double v1, v2;
        v1 = 28;
        v2 = 3;
        for (MathOperation operation : operations) {
            System.out.printf("Value of operation is %f\n", operation.execute(v1, v2));
        }
    }
}
