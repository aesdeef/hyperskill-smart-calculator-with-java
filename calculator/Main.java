package calculator;

import java.util.HashMap;
import java.util.Scanner;
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

            // CHECK PARENTHESES
            boolean valid = checkParentheses(input);
            if (!valid) {
                System.out.println("Invalid expression");
                continue;
            }

            // COMMAND
            if (input.charAt(0) == '/') {
                boolean exit = Main.handleCommand(input);
                if (exit) {
                    break;
                }
                continue;
            }

            // HANDLING MULTIPLE EQUALS
            Matcher multipleEqualsMatcher = Pattern.compile("=.*=").matcher(input);
            if (multipleEqualsMatcher.find()) {
                System.out.println("Invalid assignment");
                continue;
            }


            // VALUE ASSIGNMENT AND CALCULATION
            long result;
            try {
                Matcher equalsMatcher = Pattern.compile("(?<left>[^=]*)=(?<right>[^=]*)").matcher(input);
                if (equalsMatcher.matches()) {
                    String left = equalsMatcher.group("left").trim();
                    String right = equalsMatcher.group("right").trim();
                    calculator.assign(left, right);
                    continue;
                }
                result = Long.parseLong(calculator.eval(input));
            } catch (UnknownVariableException e) {
                System.out.println("Unknown variable");
                continue;
            } catch (InvalidIdentifierException e) {
                System.out.println("Invalid identifier");
                continue;
            } catch (InvalidExpressionException e) {
                System.out.println("Invalid expression");
                continue;
            } catch (InvalidAssignmentException e) {
                System.out.println("Invalid assignment");
                continue;
            }

            System.out.println(result);
        }
    }

    private static boolean checkParentheses(String input) {
        int openCount = 0;
        for (char c : input.toCharArray()) {
            if (c == '(') {
                openCount++;
            } else if (c == ')') {
                if (openCount == 0) {
                    return false;
                }
                openCount--;
            }
        }
        return openCount == 0;
    }

    private static boolean handleCommand(String input) {
        switch (input) {
            case "/help" -> System.out.println("The program calculates the sum of numbers");
            case "/exit" -> {
                System.out.println("Bye!");
                return true;
            }
            default -> System.out.println("Unknown command");
        }
        return false;
    }

}

class Calculator {
    HashMap<String, Integer> store;
    static Pattern removeSpacesAfterPlusesAndMinuses = Pattern.compile("(?<operator>[+-])\\s+");
    static Pattern consecutivePlusMinus = Pattern.compile("\\s*(?<first>[+-])\\s*(?<second>[+-])\\s*");
    static Pattern brackets = Pattern.compile("\\((?<contents>[^)]*)\\)");
    static Pattern power = Pattern.compile("(?<base>\\d+)\\s*\\^\\s*(?<exponent>\\d+)");
    static Pattern multiply = Pattern.compile("(?<value1>-?\\d+)\\s*(?<operator>[*/])\\s*(?<value2>-?\\d+)");
    static Pattern add = Pattern.compile("(?<value1>-?\\d+)\\s*(?<operator>[+-])\\s*(?<value2>-?\\d+)");
    static Pattern variable = Pattern.compile("[a-z]+");
    static Pattern invalidToken = Pattern.compile("[a-z][0-9]|[0-9][a-z]|[*/]{2,}");

    Calculator() {
        this.store = new HashMap<>();
    }

    public Integer retrieve(String key) throws UnknownVariableException {
        if (this.store.containsKey(key)) {
            return this.store.get(key);
        }
        throw new UnknownVariableException("Unknown variable: " + key);
    }

    String eval(String input) throws UnknownVariableException, InvalidExpressionException {
        Matcher matcher = invalidToken.matcher(input);
        if (matcher.find()) {
            throw new InvalidExpressionException("Invalid expression: " + input);
        }

        matcher = removeSpacesAfterPlusesAndMinuses.matcher(input);
        if (matcher.find()) {
            return eval(input.replace(matcher.group(), matcher.group("operator")));
        }

        matcher = consecutivePlusMinus.matcher(input);
        if (matcher.find()) {
            String first = matcher.group("first");
            String second = matcher.group("second");
            if (first.equals(second)) {
                return eval(input.replace(matcher.group(), "+"));
            } else {
                return eval(input.replace(matcher.group(), "-"));
            }
        }

        Matcher variableMatcher = variable.matcher(input);
        if (variableMatcher.find()) {
            String value = this.retrieve(variableMatcher.group()).toString();
            return eval(input.replace(variableMatcher.group(), value));
        }

        Matcher bracketsMatcher = brackets.matcher(input);
        if (bracketsMatcher.find()) {
            String value = eval(bracketsMatcher.group("contents").trim());
            return eval(input.replace(bracketsMatcher.group(), value));
        }

        Matcher powerMatcher = power.matcher(input);
        if (powerMatcher.find()) {
            long base = Long.parseLong(powerMatcher.group("base").trim());
            long exponent = Long.parseLong(powerMatcher.group("exponent").trim());
            long value = 1;
            for (long i = 0; i < exponent; i++) {
                value *= base;
            }
            return eval(input.replace(powerMatcher.group(), Long.toString(value)));
        }

        Matcher multiplyMatcher = multiply.matcher(input);
        if (multiplyMatcher.find()) {
            long value1 = Long.parseLong(multiplyMatcher.group("value1").trim());
            long value2 = Long.parseLong(multiplyMatcher.group("value2").trim());
            String operator = multiplyMatcher.group("operator").trim();
            long result;
            switch (operator) {
                case "*" -> result = value1 * value2;
                case "/" -> result = value1 / value2;
                default -> throw new InvalidExpressionException("");
            }
            return eval(input.replace(multiplyMatcher.group(), Long.toString(result)));
        }

        Matcher addMatcher = add.matcher(input);
        if (addMatcher.find()) {
            long value1 = Long.parseLong(addMatcher.group("value1").trim());
            long value2 = Long.parseLong(addMatcher.group("value2").trim());
            String operator = addMatcher.group("operator").trim();
            long result;
            switch (operator) {
                case "+" -> result = value1 + value2;
                case "-" -> result = value1 - value2;
                default -> throw new InvalidExpressionException("");
            }
            return eval(input.replace(addMatcher.group(), Long.toString(result)));
        }

        return input;
    }

    public void assign(String left, String right) throws InvalidIdentifierException, InvalidExpressionException, UnknownVariableException, InvalidAssignmentException {
        if (!variable.matcher(left).matches()) {
            throw new InvalidIdentifierException(left);
        }
        long rightValue;
        try {
            rightValue = Long.parseLong(this.eval(right));
        } catch (InvalidExpressionException e) {
            throw new InvalidAssignmentException(e.getMessage());
        }
        this.store.put(left, (int) rightValue);
    }
}

class UnknownVariableException extends Exception {
    UnknownVariableException(String message) {
        super(message);
    }
}

class InvalidIdentifierException extends Exception {
    InvalidIdentifierException(String message) {
        super(message);
    }
}

class InvalidExpressionException extends Exception {
    public InvalidExpressionException(String message) {
        super(message);
    }
}

class InvalidAssignmentException extends Throwable {
    public InvalidAssignmentException(String message) {
        super(message);
    }
}
