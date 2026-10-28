package com.example.simplecalculator;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
    private static final int TEXT = Color.rgb(241, 246, 239);
    private EditText first, second;
    private TextView result, expression;

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private GradientDrawable surface(String color, int radius) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.parseColor(color));
        background.setCornerRadius(dp(radius));
        return background;
    }
    private TextView label(String value, int size, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }
    private void space(LinearLayout layout, int height) {
        layout.addView(new View(this), new LinearLayout.LayoutParams(1, dp(height)));
    }
    private EditText number(LinearLayout layout, String title, int id) {
        TextView caption = label(title, 14, Color.parseColor("#A5B2A7"));
        layout.addView(caption);
        space(layout, 8);
        EditText field = new EditText(this);
        field.setId(id);
        caption.setLabelFor(id);
        field.setSingleLine(true);
        field.setTextSize(26);
        field.setTextColor(TEXT);
        field.setHintTextColor(Color.parseColor("#708175"));
        field.setHint("숫자 입력");
        field.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        field.setFilters(new InputFilter[] {new InputFilter.LengthFilter(24)});
        field.setPadding(dp(18), dp(12), dp(18), dp(12));
        field.setBackground(surface("#202B24", 16));
        layout.addView(field, new LinearLayout.LayoutParams(-1, dp(68)));
        return field;
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(24), dp(28), dp(24), dp(24));
        page.setFocusableInTouchMode(true);
        scroll.addView(page);
        scroll.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets.consumeSystemWindowInsets();
        });
        setContentView(scroll);
        page.addView(label("SIMPLE CALCULATOR", 12, Color.parseColor("#B8E986")));
        space(page, 10);
        TextView title = label("간단 계산기", 30, TEXT);
        title.setTypeface(null, Typeface.BOLD);
        page.addView(title);
        space(page, 8);
        page.addView(label("숫자 두 개를 입력하고 연산을 선택하세요.", 14, Color.parseColor("#A5B2A7")));
        space(page, 28);
        LinearLayout answer = new LinearLayout(this);
        answer.setOrientation(LinearLayout.VERTICAL);
        answer.setPadding(dp(20), dp(20), dp(20), dp(20));
        answer.setBackground(surface("#B8E986", 22));
        answer.addView(label("계산 결과", 14, Color.parseColor("#324728")));
        space(answer, 12);
        result = label("0", 36, Color.parseColor("#172511"));
        result.setTypeface(null, Typeface.BOLD);
        result.setTextIsSelectable(true);
        result.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        answer.addView(result);
        expression = label("계산을 시작해 보세요", 14, Color.parseColor("#405733"));
        expression.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        space(answer, 8);
        answer.addView(expression);
        page.addView(answer, new LinearLayout.LayoutParams(-1, -2));
        space(page, 26);
        first = number(page, "첫 번째 숫자", 1001);
        space(page, 18);
        second = number(page, "두 번째 숫자", 1002);
        space(page, 22);
        LinearLayout operations = new LinearLayout(this);
        String[] symbols = {"+", "−", "×", "÷"};
        String[] names = {"더하기", "빼기", "곱하기", "나누기"};
        for (int i = 0; i < symbols.length; i++) {
            final String op = symbols[i];
            Button button = new Button(this);
            button.setText(op);
            button.setTextSize(28);
            button.setTextColor(TEXT);
            button.setContentDescription(names[i]);
            button.setBackground(surface("#2C3C30", 16));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(64), 1);
            if (i > 0) params.leftMargin = dp(10);
            operations.addView(button, params);
            button.setOnClickListener(v -> compute(op));
        }
        page.addView(operations);
        space(page, 14);
        Button reset = new Button(this);
        reset.setText("초기화");
        reset.setTextSize(17);
        reset.setTextColor(TEXT);
        reset.setBackground(surface("#202B24", 16));
        reset.setOnClickListener(v -> {
            first.setText(""); second.setText(""); result.setText("0");
            expression.setText("계산을 시작해 보세요");
            page.requestFocus();
            android.view.inputmethod.InputMethodManager keyboard = (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            keyboard.hideSoftInputFromWindow(first.getWindowToken(), 0);
        });
        page.addView(reset, new LinearLayout.LayoutParams(-1, dp(56)));
        if (state != null) {
            result.setText(state.getString("result", "0"));
            expression.setText(state.getString("expression", "계산을 시작해 보세요"));
        }
    }
    private void compute(String op) {
        try {
            String a = first.getText().toString();
            String b = second.getText().toString();
            result.setText(Calculator.calculate(a, b, op));
            expression.setText(a + " " + op + " " + b + " =");
        } catch (IllegalArgumentException error) {
            result.setText("—");
            expression.setText(error.getMessage());
        }
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putString("result", result.getText().toString());
        state.putString("expression", expression.getText().toString());
    }
}
