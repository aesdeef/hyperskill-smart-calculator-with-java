package calculator;

import java.util.Scanner;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

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
                    default -> {
                        System.out.println("Unknown command");
                    }
                }
                continue;
            }

            long result;
            try {
                result = Main.parseExpression(input);
                System.out.println(result);
            } catch (InvalidExpressionException e) {
                System.out.println("Invalid expression");
            }
        }
    }

    private static long parseExpression(String input) throws InvalidExpressionException {
        // clean up the input
        String sanitisedInput = input.replaceAll("\\s+", " ");
        Pattern consecutiveSigns = Pattern.compile("[+-] ?[+-]");
        while (consecutiveSigns.matcher(sanitisedInput).find()) {
            sanitisedInput = sanitisedInput.replaceAll("\\+ ?\\+", "+");
            sanitisedInput = sanitisedInput.replaceAll("\\+ ?-", "-");
            sanitisedInput = sanitisedInput.replaceAll("- ?\\+", "-");
            sanitisedInput = sanitisedInput.replaceAll("- ?-", "+");
        }
        sanitisedInput = sanitisedInput.replaceAll("\\+ ", "+");
        sanitisedInput = sanitisedInput.replaceAll("- ", "-");

        String[] inputParts = sanitisedInput.split(" ");
        for (int i = 1; i < inputParts.length; i++) {
            if (!inputParts[i].matches("[+-]\\d+")) {
                throw new InvalidExpressionException("Invalid expression");
            }
        }
        long sum = 0;
        for (String part : inputParts) {
            try {
                sum += Long.parseLong(part);
            } catch (NumberFormatException e) {
                throw new InvalidExpressionException(e.getMessage());
            }
        }
        return sum;
    }
}

class InvalidExpressionException extends Exception {
    public InvalidExpressionException(String message) {
        super(message);
    }
}
