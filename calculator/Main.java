package calculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Calculator calculator = new Calculator();

        mainLoop:
        while (true) {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }
            // CHECK PARENTHESES
            int openCount = 0;
            for (char c : input.toCharArray()) {
                if (c == '(') {
                    openCount++;
                } else if (c == ')') {
                    if (openCount == 0) {
                        System.out.println("Invalid expression");
                        continue mainLoop;
                    }
                    openCount--;
                }
            }
            if (openCount != 0) {
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

            input = input
                    .replaceAll("=", " = ")
                    .replaceAll("\\+", " + ")
                    .replaceAll("-", " - ")
                    // .replaceAll("\\*", " * ")
                    // .replaceAll("/", " / ")
                    // .replaceAll("\\^", " ^ ")
                    .replaceAll("\\(", " ( ")
                    .replaceAll("\\)", " ) ")
                    .trim();
            String[] tokens = input.split("\\s+");
            ArrayList<TokenType> tokenTypes = Main.classifyTokens(tokens);

            // HANDLING UNKNOWN TOKENS
            if (tokenTypes.contains(TokenType.UNKNOWN)) {
                String errorMessage = null;
                for (TokenType tokenType : tokenTypes) {
                    switch (tokenType) {
                        case UNKNOWN -> errorMessage = "Invalid identifier";
                        case EQUALS -> errorMessage = "Invalid assignment";
                    }
                    if (errorMessage != null) {
                        break;
                    }
                }
                System.out.println(errorMessage);
                continue;
            }

            // HANDLING MULTIPLE EQUALS
            if (tokenTypes.stream().filter(token -> token == TokenType.EQUALS).count() > 1) {
                System.out.println("Invalid assignment");
                continue;
            }

            // VALUE RETRIEVAL
            if (tokens.length == 1 && tokenTypes.getFirst() == TokenType.VARIABLE) {
                try {
                    System.out.println(calculator.retrieve(input));
                } catch (UnknownVariableException e) {
                    System.out.println("Unknown variable");
                }
                continue;
            }

            // ECHOING A NUMBER
            if (tokens.length == 1 && tokenTypes.getFirst() == TokenType.NUMBER) {
                System.out.println(input);
                continue;
            }

            // VALUE ASSIGNMENT AND CALCULATION
            Long result;
            try {
                result = calculator.calculate(tokens, tokenTypes, input);
            } catch (UnknownVariableException e) {
                System.out.println("Unknown variable");
                continue;
            } catch (InvalidExpressionException e) {
                System.out.println("Invalid expression");
                continue;
            }

            if (result != null) {
                System.out.println(result);
            }
        }
    }

    private static ArrayList<TokenType> classifyTokens(String[] tokens) {
        ArrayList<TokenType> tokenTypes = new ArrayList<>();
        for (String token : tokens) {
            TokenType type = TokenType.UNKNOWN;
            if (token.matches("[A-Za-z]+")) {
                type = TokenType.VARIABLE;
            } else if (token.matches("[+\\-*/^=()]")) {
                switch (token) {
                    case "=" -> type = TokenType.EQUALS;
                    case "+" -> type = TokenType.PLUS;
                    case "-" -> type = TokenType.MINUS;
                    case "*" -> type = TokenType.TIMES;
                    case "/" -> type = TokenType.DIVIDE;
                    case "^" -> type = TokenType.POWER;
                    case "(" -> type = TokenType.OPEN_BRACKETS;
                    case ")" -> type = TokenType.CLOSE_BRACKETS;
                }
            } else {
                try {
                    Integer.parseInt(token);
                    type = TokenType.NUMBER;
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
            tokenTypes.add(type);
        }
        return tokenTypes;
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

    Calculator() {
        this.store = new HashMap<>();
    }

    public Integer retrieve(String key) throws UnknownVariableException {
        if (this.store.containsKey(key)) {
            return this.store.get(key);
        }
        throw new UnknownVariableException("Unknown variable: " + key);
    }

    private String eval(String input) throws UnknownVariableException, InvalidExpressionException {
        Matcher matcher = removeSpacesAfterPlusesAndMinuses.matcher(input);
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

    public Long calculate(String[] tokens, ArrayList<TokenType> tokenTypes, String input) throws InvalidExpressionException, UnknownVariableException {
        boolean assignment = false;
        long result = 0;
        if (tokens.length >= 3
                && tokenTypes.getFirst() == TokenType.VARIABLE
                && tokenTypes.get(1) == TokenType.EQUALS
        ) {
            assignment = true;
            int equals = input.indexOf('=');
            long value = Long.parseLong(this.eval(input.substring(equals + 1).trim()));
            this.store.put(tokens[0], (int) value);
            return null;
        }
        return Long.parseLong(this.eval(input));

        /*
        TokenType currentOperator = TokenType.PLUS;
        for (int i = assignment ? 2 : 0; i < tokens.length; i++) {
            Integer toAdd = null;
            switch (tokenTypes.get(i)) {
                case NUMBER -> toAdd = Integer.parseInt(tokens[i]);
                case VARIABLE -> toAdd = this.retrieve(tokens[i]);
                case PLUS -> {
                    if (currentOperator == null) {
                        currentOperator = TokenType.PLUS;
                    }
                }
                case MINUS -> {
                    if (currentOperator == TokenType.MINUS) {
                        currentOperator = TokenType.PLUS;
                    } else {
                        currentOperator = TokenType.MINUS;
                    }
                }
                default -> throw new InvalidExpressionException("");
            }

            if (toAdd != null) {
                switch (currentOperator) {
                    case PLUS -> result += toAdd;
                    case MINUS -> result -= toAdd;
                    case null, default -> throw new InvalidExpressionException("");
                }
                currentOperator = null;
            }
        }

        if (assignment) {
            this.store.put(tokens[0], (int) result);
            return null;
        }
        return result;
         */
    }
}

enum TokenType {
    COMMAND,
    VARIABLE,
    PLUS,
    MINUS,
    TIMES,
    DIVIDE,
    POWER,
    EQUALS,
    NUMBER,
    OPEN_BRACKETS, CLOSE_BRACKETS, UNKNOWN
}

class UnknownVariableException extends Exception {
    UnknownVariableException(String message) {
        super(message);
    }
}

class InvalidExpressionException extends Exception {
    public InvalidExpressionException(String message) {
        super(message);
    }
}
