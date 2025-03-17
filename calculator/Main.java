package calculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Calculator calculator = new Calculator();

        while (true) {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }
            input = input
                    .replaceAll("=", " = ")
                    .replaceAll("\\+", " + ")
                    .replaceAll("-", " - ")
                    .trim();
            String[] tokens = input.split("\\s+");
            ArrayList<TokenType> tokenTypes = Main.classifyTokens(tokens);

            // COMMAND
            if (tokenTypes.getFirst() == TokenType.COMMAND) {
                boolean exit = Main.handleCommand(input);
                if (exit) {
                    break;
                }
                continue;
            }

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
                result = calculator.calculate(tokens, tokenTypes);
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
            if (token.charAt(0) == '/') {
                type = TokenType.COMMAND;
            } else if (token.matches("[A-Za-z]+")) {
                type = TokenType.VARIABLE;
            } else if (token.matches("[+=-]")) {
                switch (token) {
                    case "=" -> type = TokenType.EQUALS;
                    case "+" -> type = TokenType.PLUS;
                    case "-" -> type = TokenType.MINUS;
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

    Calculator() {
        this.store = new HashMap<>();
    }

    public Integer retrieve(String key) throws UnknownVariableException {
        if (this.store.containsKey(key)) {
            return this.store.get(key);
        }
        throw new UnknownVariableException("Unknown variable: " + key);
    }

    public Long calculate(String[] tokens, ArrayList<TokenType> tokenTypes) throws InvalidExpressionException, UnknownVariableException {
        boolean assignment = false;
        long result = 0;
        if (tokens.length >= 3
                && tokenTypes.getFirst() == TokenType.VARIABLE
                && tokenTypes.get(1) == TokenType.EQUALS
        ) {
            assignment = true;
        }

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
    }

}

enum TokenType {
    COMMAND,
    VARIABLE,
    PLUS,
    MINUS,
    EQUALS,
    NUMBER,
    UNKNOWN
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
