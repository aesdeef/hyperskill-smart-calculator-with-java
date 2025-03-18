package calculator;

import java.util.HashMap;
import java.util.Scanner;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Calculator calculator = new Calculator();

        while (true) {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }

            if (input.charAt(0) == '/') {
                switch (input) {
                    case "/help" -> System.out.println("The program calculates the sum of numbers");
                    case "/exit" -> {
                        System.out.println("Bye!");
                        System.exit(0);
                    }
                    default -> System.out.println("Unknown command");
                }
                continue;
            }

            try {
                calculator.process(input);
            } catch (CalculatorException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}

class Calculator {
    HashMap<String, Integer> store;
    static Pattern assignment = Pattern.compile("(?<left>[^=]*)=(?<right>[^=]*)");
    static Pattern variable = Pattern.compile("[a-z]+");
    static Pattern invalidToken = Pattern.compile("[a-z][0-9]|[0-9][a-z]|[*/]{2,}");
    static Pattern longPlus = Pattern.compile("\\+{2,}|(--)+");
    static Pattern plusMinus = Pattern.compile("\\+-");
    static Pattern brackets = Pattern.compile("\\((?<contents>[^)]*)\\)");
    static Pattern power = Pattern.compile("(?<base>\\d+)\\s*\\^\\s*(?<exponent>\\d+)");
    static Pattern multiply = Pattern.compile("(?<value1>-?\\d+)\\s*(?<operator>[*/])\\s*(?<value2>-?\\d+)");
    static Pattern add = Pattern.compile("(?<value1>-?\\d+)\\s*(?<operator>[+-])\\s*(?<value2>-?\\d+)");

    Calculator() {
        this.store = new HashMap<>();
    }

    public void process(String input) throws InvalidExpressionException, InvalidAssignmentException, UnknownVariableException, InvalidIdentifierException {
        this.validateInput(input);
        Matcher matcher = assignment.matcher(input);
        if (matcher.matches()) {
            String left = matcher.group("left").trim();
            String right = matcher.group("right").trim();
            this.assign(left, right);
        } else {
            System.out.println(this.eval(input));
        }
    }

    private void validateInput(String input) throws InvalidAssignmentException, InvalidExpressionException {
        // CHECK PARENTHESES
        boolean valid = checkParentheses(input);
        if (!valid) {
            throw new InvalidExpressionException();
        }

        // HANDLING MULTIPLE EQUALS
        Matcher multipleEqualsMatcher = Pattern.compile("=.*=").matcher(input);
        if (multipleEqualsMatcher.find()) {
            throw new InvalidAssignmentException();
        }
    }

    private void assign(String left, String right) throws InvalidIdentifierException, UnknownVariableException, InvalidAssignmentException {
        if (!variable.matcher(left).matches()) {
            throw new InvalidIdentifierException();
        }
        long rightValue;
        try {
            rightValue = Long.parseLong(this.eval(right));
        } catch (InvalidExpressionException e) {
            throw new InvalidAssignmentException();
        }
        this.store.put(left, (int) rightValue);
    }

    private String eval(String input) throws UnknownVariableException, InvalidExpressionException {
        input = this.prepareInput(input);
        Matcher matcher;

        matcher = brackets.matcher(input);
        if (matcher.find()) {
            String value = eval(matcher.group("contents").trim());
            return eval(input.replace(matcher.group(), value));
        }

        matcher = power.matcher(input);
        while (matcher.find()) {
            long base = Long.parseLong(matcher.group("base").trim());
            long exponent = Long.parseLong(matcher.group("exponent").trim());
            long value = 1;
            for (long i = 0; i < exponent; i++) {
                value *= base;
            }
            input = eval(input.replace(matcher.group(), Long.toString(value)));
            matcher = power.matcher(input);
        }

        matcher = multiply.matcher(input);
        while (matcher.find()) {
            long value1 = Long.parseLong(matcher.group("value1").trim());
            long value2 = Long.parseLong(matcher.group("value2").trim());
            String operator = matcher.group("operator").trim();
            long result;
            switch (operator) {
                case "*" -> result = value1 * value2;
                case "/" -> result = value1 / value2;
                default -> throw new InvalidExpressionException();
            }
            input = eval(input.replace(matcher.group(), Long.toString(result)));
            matcher = multiply.matcher(input);
        }

        matcher = add.matcher(input);
        while (matcher.find()) {
            long value1 = Long.parseLong(matcher.group("value1").trim());
            long value2 = Long.parseLong(matcher.group("value2").trim());
            String operator = matcher.group("operator").trim();
            long result;
            switch (operator) {
                case "+" -> result = value1 + value2;
                case "-" -> result = value1 - value2;
                default -> throw new InvalidExpressionException();
            }
            input = eval(input.replace(matcher.group(), Long.toString(result)));
            matcher = add.matcher(input);
        }

        return input;
    }

    private String prepareInput(String input) throws UnknownVariableException {
        input = this.replaceAll(input, invalidToken, (match) -> {throw new InvalidExpressionException();});
        input = this.replaceAll(input, longPlus, "+");
        input = this.replaceAll(input, plusMinus, "-");
        input = this.replaceAll(input, variable, (match) -> this.retrieve(match).toString());

        return input;
    }

    private String replaceAll(String input, Pattern pattern, String replacement) {
        return this.replaceAll(input, pattern, (match) -> replacement);
    }

    private String replaceAll(String input, Pattern pattern, UnaryOperator<String> operator) {
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String match = matcher.group();
            input = input.replace(match, operator.apply(match));
            matcher = pattern.matcher(input);
        }
        return input;
    }

    private Integer retrieve(String key) throws UnknownVariableException {
        if (this.store.containsKey(key)) {
            return this.store.get(key);
        }
        throw new UnknownVariableException();
    }

    private static boolean checkParentheses(String input) {
        int openCount = 0;
        for (char c : input.toCharArray()) {
            switch (c) {
                case '(' -> openCount++;
                case ')' -> openCount--;
            }
            if (openCount < 0) {
                return false;
            }
        }
        return openCount == 0;
    }
}

class CalculatorException extends RuntimeException {
    CalculatorException(String message) {
        super(message);
    }
}

class UnknownVariableException extends CalculatorException {
    UnknownVariableException() {
        super("Unknown variable");
    }
}

class InvalidIdentifierException extends CalculatorException {
    InvalidIdentifierException() {
        super("Invalid identifier");
    }
}

class InvalidExpressionException extends CalculatorException {
    InvalidExpressionException() {
        super("Invalid expression");
    }
}

class InvalidAssignmentException extends CalculatorException {
    InvalidAssignmentException() {
        super("Invalid assignment");
    }
}
