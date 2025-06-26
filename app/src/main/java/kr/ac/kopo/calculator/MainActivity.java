package kr.ac.kopo.calculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private final StringBuilder inputExpression = new StringBuilder();
    private TextView calcTextView;
    private TextView resultTextView;
    private Button buttonBackspace;

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
            id != R.id.buttonPi && id != R.id.buttonSquareBrackets && id != R.id.buttonParentheses && id != R.id.buttonBrace) {
                Button btn = findViewById(id);
                btn.setOnClickListener(this::onButtonClick);
            }else if (id == R.id.buttonSquareBrackets) {
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
            } else if (id == R.id.buttonBrace) {
                Button btn = findViewById(id);
                btn.setOnClickListener(v -> insertSymbol("{")); // 또는 "}"
                btn.setOnLongClickListener(v -> {
                    insertSymbol("}"); // 길게 누르면 오른쪽 괄호
                    return true; // 이벤트 소비
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

    private String calculateExpression(String expression) {
        try {
            // Python 스타일 연산자들 변환
            expression = expression.replace("**", "^");   // 제곱
            expression = expression.replace("//", "/");   // 정수 나눗셈은 일반 나눗셈으로 처리

            // 행렬 연산 감지
            if (expression.startsWith("[[") && expression.endsWith("]]")) {
                return evaluateMatrix(expression);
            }

            // 집합 연산 감지
            if (expression.startsWith("{") && expression.endsWith("}")) {
                return evaluateSet(expression);
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
            // 입력: "x^2 - 3x + 2 @ [[1,2],[0,1]]"
            String[] parts = expr.split("@");
            if (parts.length != 2) return "잘못된 형식";

            String poly = parts[0].trim();
            String matrixStr = parts[1].trim();

            double[][] A = parseMatrix(matrixStr);
            double[] coeffs = parsePolynomial(poly); // 계수 추출

            double[][] result = applyPolynomial(A, coeffs);
            return Arrays.deepToString(result);

        } catch (Exception e) {
            e.printStackTrace();
            return "다항 행렬 오류";
        }
    }

    private double[] parsePolynomial(String poly) {
        poly = poly.replaceAll("-", "+-").replaceAll("\\s", "");
        String[] terms = poly.split("\\+");
        List<Double> coeffList = new ArrayList<>();

        for (String term : terms) {
            if (term.isEmpty()) continue;
            if (term.contains("x^")) {
                int degree = Integer.parseInt(term.split("\\^")[1]);
                double coeff = parseCoeff(term.split("x")[0]);
                while (coeffList.size() <= degree) coeffList.add(0.0);
                coeffList.set(degree, coeff);
            } else if (term.contains("x")) {
                double coeff = parseCoeff(term.split("x")[0]);
                while (coeffList.size() <= 1) coeffList.add(0.0);
                coeffList.set(1, coeff);
            } else {
                double constant = Double.parseDouble(term);
                while (coeffList.size() <= 0) coeffList.add(0.0);
                coeffList.set(0, constant);
            }
        }

        return coeffList.stream().mapToDouble(Double::doubleValue).toArray();
    }

    private double[][] applyPolynomial(double[][] A, double[] coeffs) {
        int n = A.length;
        double[][] result = zeroMatrix(n);
        double[][] power = identityMatrix(n);

        for (double coeff : coeffs) {
            double[][] term = scalarMultiply(power, coeff);
            result = addMatrices(result, term);
            power = multiplyMatrices(power, A);
        }

        return result;
    }

    private double[][] zeroMatrix(int n) {
        return new double[n][n];
    }

    private double[][] identityMatrix(int n) {
        double[][] I = new double[n][n];
        for (int i = 0; i < n; i++) I[i][i] = 1;
        return I;
    }

    private double[][] scalarMultiply(double[][] A, double scalar) {
        int n = A.length;
        double[][] result = new double[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                result[i][j] = A[i][j] * scalar;
        return result;
    }

    private double[][] addMatrices(double[][] A, double[][] B) {
        int n = A.length;
        double[][] result = new double[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                result[i][j] = A[i][j] + B[i][j];
        return result;
    }

    private double[][] multiplyMatrices(double[][] A, double[][] B) {
        int n = A.length;
        double[][] result = new double[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                for (int k = 0; k < n; k++)
                    result[i][j] += A[i][k] * B[k][j];
        return result;
    }


    private double parseCoeff(String s) {
        if (s.equals("") || s.equals("+")) return 1;
        if (s.equals("-")) return -1;
        return Double.parseDouble(s);
    }


    private double[][] parseMatrix(String input) {
        input = input.trim().replaceAll("\\s", "");

        if (input.startsWith("[") && !input.startsWith("[[")) {
            String[] elements = input.replaceAll("[\\[\\]]", "").split(",");
            double[][] matrix = new double[1][elements.length];
            for (int i = 0; i < elements.length; i++) {
                matrix[0][i] = Double.parseDouble(elements[i]);
            }
            return matrix;
        }

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

        return matrix;
    }






    private String evaluateSet(String expr) {
        try {
            String operator = null;
            int opIndex = -1;

            if ((opIndex = expr.indexOf("|")) != -1) operator = "|";
            else if ((opIndex = expr.indexOf("&")) != -1) operator = "&";
            else if ((opIndex = expr.indexOf("-")) != -1) operator = "-";

            if (operator == null) return "지원하지 않는 연산";

            String left = expr.substring(0, opIndex).trim();
            String right = expr.substring(opIndex + 1).trim();

            Set<String> A = new LinkedHashSet<>(Arrays.asList(left.replaceAll("[{}\\s]", "").split(",")));
            Set<String> B = new LinkedHashSet<>(Arrays.asList(right.replaceAll("[{}\\s]", "").split(",")));

            Set<String> result = new LinkedHashSet<>(A); // 순서 유지

            switch (operator) {
                case "|": result.addAll(B); break;           // 합집합
                case "&": result.retainAll(B); break;        // 교집합
                case "-": result.removeAll(B); break;        // 차집합
            }

            return "{" + String.join(", ", result) + "}";

        } catch (Exception e) {
            return "집합 오류";
        }
    }

    private void insertSymbol(String symbol) {
        int cursorPos = inputExpression.length();
        inputExpression.insert(cursorPos, symbol);
        calcTextView.setText(inputExpression.toString());
    }

}