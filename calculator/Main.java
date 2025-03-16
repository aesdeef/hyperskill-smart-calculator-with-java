package calculator;

import java.util.Scanner;

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
                    String[] inputParts = input.split(" ");
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
