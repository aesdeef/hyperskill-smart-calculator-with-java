package calculator;

import java.util.Scanner;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String input = scanner.nextLine();
            switch (input) {
                case "" -> {
                    // do nothing
                }
                case "/help" -> System.out.println("The program calculates the sum of numbers");
                case "/exit" -> {
                    System.out.println("Bye!");
                    System.exit(0);
                }
                default -> {
                    // clean up the input
                    String trimmedInput = input.trim().replaceAll("\\s+", " ");
                    Pattern consecutiveSigns = Pattern.compile("[+-] ?[+-]");
                    while (consecutiveSigns.matcher(trimmedInput).find()) {
                        trimmedInput = trimmedInput.replaceAll("+ ?+", "+");
                        trimmedInput = trimmedInput.replaceAll("+ ?-", "-");
                        trimmedInput = trimmedInput.replaceAll("- ?+", "-");
                        trimmedInput = trimmedInput.replaceAll("- ?-", "+");
                    }
                    trimmedInput = trimmedInput.replaceAll("+ ", "+");
                    trimmedInput = trimmedInput.replaceAll("- ", "-");

                    String[] inputParts = trimmedInput.split(" ");
                    long sum = 0;
                    for (String part : inputParts) {
                        sum += Long.parseLong(part);
                    }
                    System.out.println(sum);
                }
            }
        }
    }
}
