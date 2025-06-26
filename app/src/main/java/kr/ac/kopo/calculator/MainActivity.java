package kr.ac.kopo.calculator;

import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class MainActivity extends AppCompatActivity {
    private final StringBuilder inputExpression = new StringBuilder();
    private TextView calcTextView;
    private TextView resultTextView;
    private Button buttonBackspace;

    private final Handler handler = new Handler();
    private boolean isPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calcTextView = findViewById(R.id.calcTextView);
        resultTextView = findViewById(R.id.resultTextView);
        buttonBackspace = findViewById(R.id.buttonBackspace);

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
                    id != R.id.buttonPi && id != R.id.buttonSquareBrackets &&
                    id != R.id.buttonParentheses && id != R.id.buttonBrace) {

                Button btn = findViewById(id);
                btn.setOnClickListener(this::onButtonClick);

            } else {
                Button btn = findViewById(id);

                final String[] symbols;
                final String defaultText;

                if (id == R.id.buttonParentheses) {
                    symbols = new String[]{"(", ")"};
                    defaultText = "( )";
                } else if (id == R.id.buttonBrace) {
                    symbols = new String[]{"{", "}"};
                    defaultText = "{ }";
                } else if (id == R.id.buttonSquareBrackets) {
                    symbols = new String[]{"[", "]"};
                    defaultText = "[ ]";
                } else {
                    continue; // 이외 버튼은 무시
                }

                final boolean[] toggle = {true};

                btn.setOnTouchListener((v, event) -> {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            isPressed = true;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (!isPressed) return;
                                    btn.setText(toggle[0] ? symbols[0] : symbols[1]);
                                    toggle[0] = !toggle[0];
                                    handler.postDelayed(this, 150);
                                }
                            });
                            break;
                        case MotionEvent.ACTION_UP:
                            isPressed = false;
                            handler.removeCallbacksAndMessages(null);
                            String chosen = toggle[0] ? symbols[1] : symbols[0];
                            insertSymbol(chosen);
                            btn.setText(defaultText);
                            toggle[0] = true;

                            v.performClick(); // 👈 이 줄 추가!
                            break;
                    }
                    return true;
                });
            }
        }



        buttonBackspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputExpression.length() > 0) {
                    // 입력값에서 마지막 문자 삭제
                    inputExpression.deleteCharAt(inputExpression.length() - 1);

                    // 화면에 표시된 텍스트도 업데이트
                    calcTextView.setText(inputExpression.toString());
                }
            }
        });
    }

    public void onButtonClick(View v) {
        Button b = (Button) v;
        String buttonText = b.getText().toString();
        int id = v.getId();

        if (id == R.id.buttonAdd || id == R.id.buttonSubtract ||
                id == R.id.buttonMultiply || id == R.id.buttonDivide) {
            inputExpression.append(buttonText);
            calcTextView.setText(inputExpression.toString());
        } else if (id == R.id.buttonEquals) {
            String result = calculateExpression(inputExpression.toString());
            resultTextView.setText(result);
            inputExpression.setLength(0);
        } else if (id == R.id.buttonBackspace) {
            if (inputExpression.length() > 0) {
                inputExpression.deleteCharAt(inputExpression.length() - 1);
            }
            calcTextView.setText(inputExpression.toString());
        } else {
            inputExpression.append(buttonText);
            calcTextView.setText(inputExpression.toString());
        }
    }

    private double calculate(double a, double b, String op) {
        switch (op) {
            case "+": return a + b;
            case "-": return a - b;
            case "*": return a * b;
            case "**": return Math.pow(a, b);
            case "/": return b != 0 ? a / b : Double.NaN;
            case "//": return b != 0 ? Math.floor(a / b) : Double.NaN;
            case "%": return b != 0 ? a % b : Double.NaN;
            default: return 0;
        }
    }

    private String calculateExpression(String expression) {
        try {
            // Python 스타일 연산자들 변환
            expression = expression.replace("**", "^");   // 제곱
            expression = expression.replace("//", "/");   // 정수 나눗셈은 일반 나눗셈으로 처리

            Expression exp = new ExpressionBuilder(expression).build();
            double result = exp.evaluate();
            return String.valueOf(result);
        } catch (Exception e) {
            return "오류";
        }
    }

    private void insertSymbol(String symbol) {
        int cursorPos = inputExpression.length();
        inputExpression.insert(cursorPos, symbol);
        calcTextView.setText(inputExpression.toString());
    }

}
