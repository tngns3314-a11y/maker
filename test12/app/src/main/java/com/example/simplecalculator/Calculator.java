package com.example.simplecalculator;

import java.math.BigDecimal;
import java.math.MathContext;

public final class Calculator {
    private Calculator() {}

    public static String calculate(String first, String second, String operation) {
        if (first.trim().isEmpty() || second.trim().isEmpty()) {
            throw new IllegalArgumentException("숫자 두 개를 모두 입력해 주세요.");
        }
        final BigDecimal a;
        final BigDecimal b;
        try {
            a = new BigDecimal(first.trim());
            b = new BigDecimal(second.trim());
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("올바른 숫자를 입력해 주세요.");
        }
        BigDecimal result;
        switch (operation) {
            case "+": result = a.add(b); break;
            case "−": result = a.subtract(b); break;
            case "×": result = a.multiply(b); break;
            case "÷":
                if (b.signum() == 0) throw new IllegalArgumentException("0으로 나눌 수 없습니다.");
                result = a.divide(b, MathContext.DECIMAL64);
                break;
            default: throw new IllegalArgumentException("연산을 선택해 주세요.");
        }
        return result.stripTrailingZeros().toPlainString();
    }
}
