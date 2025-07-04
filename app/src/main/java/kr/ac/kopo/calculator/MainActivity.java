package kr.ac.kopo.calculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    private final StringBuilder inputExpression = new StringBuilder();
    private TextView calcTextView;
    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calcTextView = findViewById(R.id.calcTextView);
        resultTextView = findViewById(R.id.resultTextView);
        Button buttonBackspace = findViewById(R.id.buttonBackspace);

        int[] buttonIds = {
                R.id.button0, R.id.button1, R.id.button2, R.id.button3,
                R.id.button4, R.id.button5, R.id.button6, R.id.button7,
                R.id.button8, R.id.button9, R.id.buttonDot, R.id.buttonAdd,
                R.id.buttonSubtract, R.id.buttonMultiply, R.id.buttonDivide,
                R.id.buttonEquals, R.id.buttonComma, R.id.buttonBackspace,
                R.id.buttonSquareBrackets, R.id.buttonPercent, R.id.buttonParentheses
        };

        for (int id : buttonIds) {
            if (id != R.id.buttonBackspace && id != R.id.buttonSquareBrackets && id != R.id.buttonParentheses ) {
                Button btn = findViewById(id);
                btn.setOnClickListener(this::onButtonClick);
            } else if (id == R.id.buttonSquareBrackets) {
                Button btn = findViewById(id);
                btn.setOnClickListener(v -> insertSymbol("[")); // 또는 "]"
                btn.setOnLongClickListener(v -> {
                    insertSymbol("]"); // 길게 누르면 오른쪽 괄호
                    return true; // 이벤트 소비
                });
            } else if (id == R.id.buttonParentheses) {
                Button btn = findViewById(id);
                btn.setOnClickListener(v -> insertSymbol("(")); // 또는 ")"
                btn.setOnLongClickListener(v -> {
                    insertSymbol(")"); // 길게 누르면 오른쪽 괄호
                    return true; // 이벤트 소비
                });
            }
        }

        buttonBackspace.setOnClickListener(v -> {
            if (inputExpression.length() > 0) {
                // 입력값에서 마지막 문자 삭제
                inputExpression.deleteCharAt(inputExpression.length() - 1);

                // 화면에 표시된 텍스트도 업데이트
                calcTextView.setText(inputExpression.toString());
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

    private String calculateExpression(String expression) {
        try {
            // Python 스타일 연산자들 변환
            expression = expression.replace("**", "^");   // 제곱
            expression = expression.replace("//", "/");   // 정수 나눗셈은 일반 나눗셈으로 처리

            // 행렬 연산 감지
            if (expression.startsWith("[") && expression.endsWith("]")) {
                return evaluateMatrix(expression);
            }

            Expression exp = new ExpressionBuilder(expression).build();
            double result = exp.evaluate();
            return String.valueOf(result);
        } catch (Exception e) {
            return "오류";
        }
    }

    private String evaluateMatrix(String expr) {
        try {
            List<Object> matrices = new ArrayList<>();
            List<String> operators = new ArrayList<>();

            Matcher m = Pattern.compile("(\\[\\[.*?]]|\\[.*?])|([+\\-])").matcher(expr.replaceAll("\\s", ""));
            while (m.find()) {
                String token = m.group();
                if (token.equals("+") || token.equals("-")) {
                    operators.add(token);
                } else {
                    matrices.add(parseMatrix(token)); // Object: double[] or double[][]
                }
            }

            if (matrices.size() < 2 || matrices.size() != operators.size() + 1) return "잘못된 형식";

            Object result = matrices.get(0);

            for (int i = 0; i < operators.size(); i++) {
                Object next = matrices.get(i + 1);
                String op = operators.get(i);

                if (result instanceof double[] && next instanceof double[]) {
                    double[] A = (double[]) result;
                    double[] B = (double[]) next;
                    if (A.length != B.length) return "크기 불일치";

                    double[] temp = new double[A.length];
                    for (int j = 0; j < A.length; j++) {
                        temp[j] = op.equals("+") ? A[j] + B[j] : A[j] - B[j];
                    }
                    result = temp;

                } else if (result instanceof double[][] && next instanceof double[][]) {
                    double[][] A = (double[][]) result;
                    double[][] B = (double[][]) next;
                    if (A.length != B.length || A[0].length != B[0].length) return "크기 불일치";

                    double[][] temp = new double[A.length][A[0].length];
                    for (int r = 0; r < A.length; r++)
                        for (int c = 0; c < A[0].length; c++)
                            temp[r][c] = op.equals("+") ? A[r][c] + B[r][c] : A[r][c] - B[r][c];
                    result = temp;

                } else {
                    return "차원 불일치";
                }
            }

            return (result instanceof double[])
                    ? Arrays.toString((double[]) result)
                    : Arrays.deepToString((double[][]) result);

        } catch (Exception e) {
            return "행렬 오류";
        }
    }

    private Object parseMatrix(String input) {
        input = input.trim().replaceAll("\\s", ""); // 공백 제거

        // 1차원 벡터 감지: [1,2,3]
        if (input.startsWith("[") && !input.startsWith("[[")) {
            String[] elements = input.replaceAll("[\\[\\]]", "").split(",");
            double[] vector = new double[elements.length];
            for (int i = 0; i < elements.length; i++) {
                vector[i] = Double.parseDouble(elements[i]);
            }
            return vector; // double[]
        }

        // 2차원 행렬 처리: [[1,2],[3,4]]
        input = input.replaceAll("^\\[\\[", "").replaceAll("]]$", "");
        String[] rows = input.split("],\\[");
        double[][] matrix = new double[rows.length][];

        for (int i = 0; i < rows.length; i++) {
            String[] elements = rows[i].split(",");
            matrix[i] = new double[elements.length];
            for (int j = 0; j < elements.length; j++) {
                matrix[i][j] = Double.parseDouble(elements[j]);
            }
        }

        return matrix; // double[][]
    }

    private void insertSymbol(String symbol) {
        int cursorPos = inputExpression.length();
        inputExpression.insert(cursorPos, symbol);
        calcTextView.setText(inputExpression.toString());
    }
}
