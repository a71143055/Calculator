package kr.ac.kopo.calculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView resultTextView;
    private String currentInput = "";
    private Double firstOperand = null;
    private String operator = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultTextView = findViewById(R.id.resultTextView);

        int[] buttonIds = {
                R.id.button0, R.id.button1, R.id.button2, R.id.button3,
                R.id.button4, R.id.button5, R.id.button6, R.id.button7,
                R.id.button8, R.id.button9, R.id.buttonDot, R.id.buttonAdd,
                R.id.buttonSubtract, R.id.buttonMultiply, R.id.buttonDivide,
                R.id.buttonEquals
        };

        for (int id : buttonIds) {
            Button btn = findViewById(id);
            btn.setOnClickListener(this::onButtonClick);
        }
    }

    private void onButtonClick(View v) {
        Button button = (Button) v;
        String value = button.getText().toString();

        switch (value) {
            case "+":
            case "-":
            case "×":
            case "÷":
                if (!currentInput.isEmpty()) {
                    firstOperand = Double.parseDouble(currentInput);
                    operator = value;
                    currentInput = "";
                }
                break;
            case "=":
                if (operator != null && !currentInput.isEmpty()) {
                    double secondOperand = Double.parseDouble(currentInput);
                    double result = calculate(firstOperand, secondOperand, operator);
                    resultTextView.setText(String.valueOf(result));
                    currentInput = "";
                    operator = null;
                    firstOperand = null;
                }
                break;
            default:
                currentInput += value;
                resultTextView.setText(currentInput);
        }
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
}
