import com.example.simplecalculator.Calculator;

public class CalculatorTest {
    static int checks;
    static void expect(String a, String b, String op, String expected) {
        String actual = Calculator.calculate(a, b, op);
        if (!expected.equals(actual)) throw new AssertionError(actual + " != " + expected);
        checks++;
    }
    static void invalid(String a, String b, String op) {
        try { Calculator.calculate(a, b, op); }
        catch (IllegalArgumentException expected) { checks++; return; }
        throw new AssertionError("Expected invalid input");
    }
    public static void main(String[] args) {
        expect("2", "3", "+", "5");
        expect("2", "3", "−", "-1");
        expect("-2", "3", "×", "-6");
        expect("7", "2", "÷", "3.5");
        expect("0.1", "0.2", "+", "0.3");
        expect("1", "3", "÷", "0.3333333333333333");
        expect("0", "3", "÷", "0");
        expect("999999999999999999999999", "1", "+", "1000000000000000000000000");
        invalid("1", "0", "÷");
        invalid("1", "-0.0", "÷");
        invalid("", "2", "+");
        invalid("-", "2", "+");
        invalid("1.2.3", "2", "+");
        System.out.println(checks + " calculator checks passed");
    }
}
