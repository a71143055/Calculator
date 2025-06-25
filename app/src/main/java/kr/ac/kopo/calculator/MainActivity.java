package kr.ac.kopo.calculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private final StringBuilder inputExpression = new StringBuilder();
    private TextView calcTextView;
    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calcTextView = findViewById(R.id.calcTextView);

        int[] buttonIds = {
                R.id.button0, R.id.button1, R.id.button2, R.id.button3,
                R.id.button4, R.id.button5, R.id.button6, R.id.button7,
                R.id.button8, R.id.button9, R.id.buttonDot, R.id.buttonAdd,
                R.id.buttonSubtract, R.id.buttonMultiply, R.id.buttonDivide,
                R.id.buttonEquals, R.id.buttonComma, R.id.buttonBackspace,
                R.id.buttonPipe, R.id.buttonRecord, R.id.buttonAnd,
                R.id.buttonSquareBrackets, R.id.buttonPercent, R.id.buttonBrace,
                R.id.buttonPi, R.id.buttonParentheses, R.id.buttonColon
        };

        for (int id : buttonIds) {
            if (id != R.id.buttonBackspace && id != R.id.buttonRecord &&
            id != R.id.buttonPi) {
                Button btn = findViewById(id);
                btn.setOnClickListener(this::onButtonClick);
            }
        }
    }

    public void onButtonClick(View v) {
        Button b = (Button) v;
        String buttonText = b.getText().toString();
        int id = v.getId();

        if (id == R.id.buttonAdd || id == R.id.buttonSubtract ||
                id == R.id.buttonMultiply || id == R.id.buttonDivide) {
            inputExpression.append(buttonText);
        } else if (id == R.id.buttonEquals) {
            String result = calculateExpression(inputExpression.toString());
            resultTextView.setText(result);
            inputExpression.setLength(0);
        } else if (id == R.id.buttonBackspace) {
            if (inputExpression.length() > 0) {
                inputExpression.deleteCharAt(inputExpression.length() - 1);
            }
        } else {
            inputExpression.append(buttonText);
        }
        calcTextView.setText(inputExpression.toString());
    }

    private double calculate(double a, double b, String op) {
        switch (op) {
            case "+": return a + b;
            case "-": return a - b;
            case "×": return a * b;
            case "÷": return b != 0 ? a / b : Double.NaN;
            default: return 0;
        }
    }

    private String calculateExpression(String expression) {
        try {
            String[] parts;
            double a, b;

            if (expression.contains("+")) {
                parts = expression.split("\\+");
                a = Double.parseDouble(parts[0]);
                b = Double.parseDouble(parts[1]);
                return String.valueOf(calculate(a, b, "+"));
            } else if (expression.contains("-")) {
                parts = expression.split("-");
                a = Double.parseDouble(parts[0]);
                b = Double.parseDouble(parts[1]);
                return String.valueOf(calculate(a, b, "-"));
            } else if (expression.contains("×")) {
                parts = expression.split("×");
                a = Double.parseDouble(parts[0]);
                b = Double.parseDouble(parts[1]);
                return String.valueOf(calculate(a, b, "×"));
            } else if (expression.contains("÷")) {
                parts = expression.split("÷");
                a = Double.parseDouble(parts[0]);
                b = Double.parseDouble(parts[1]);
                return String.valueOf(calculate(a, b, "÷"));
            } else {
                return "계산 불가";
            }
        } catch (Exception e) {
            return "오류";
        }
    }
}
