package calculator;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            if (input.equals("/exit")) {
                break;
            } else if (input.isEmpty()) {
                continue;
            } else if (!input.contains(" ")) {
                System.out.println(input);
            } else {
                String[] inputParts = input.split(" ");
                int first = Integer.parseInt(inputParts[0]);
                int second = Integer.parseInt(inputParts[1]);
                System.out.println(first + second);
            }
        }
        System.out.println("Bye!");
    }
}
