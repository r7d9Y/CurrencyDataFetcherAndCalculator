package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class ExchangeRateCalculator {

    public static void main(String[] args) {
        dialog();
    }

    final static String dir = "res/";

    /**
     * This method will display a dialog to the user to input the currency to convert from,
     * the amount to convert, and the currency to convert to
     * then it will calculate the converted value
     */
    public static void dialog() {
        System.out.printf("""
                        Exchange Rate Calculator, version %f\s
                        ------------------------------------------------------------------------------------------------
                                        
                        please enter the currency you want to convert from, the amount you'd like to convert,\s
                        and the currency you want to convert to (the currencies must be in a 3-letter format, e.g. USD)
                        just separate them with a space, e.g. "USD 100 EUR"\s
                                        
                        """,
                ExchangeRateFetcher.version);

        System.out.println("Please wait while exchange rates are updated (a network connection is needed)...");

        System.out.println(ExchangeRateFetcher.getExchangeRates(dir) ?
                "Exchange rates updated successfully" :
                "Failed to update exchange rates");


        do {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Input: ");
            String input = scanner.nextLine().strip().trim();

            if (input.toLowerCase().contains("ex")) break;
            
            String[] inputValue = input.split(" ");
            try {
                double amount = Double.parseDouble(inputValue[1]);
                String from = inputValue[0].trim().toUpperCase();
                String to = inputValue[2].trim().toUpperCase();

                System.out.println(getConvertedValue(from, to, amount));
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } while (true);
    }

    /**
     * @param from   the currency to convert from
     * @param to     the currency to convert to
     * @param amount the amount to convert
     * @return the converted value
     * @throws IOException if the file is not found
     */
    public static double getConvertedValue(String from, String to, double amount) throws IOException {
        List<String> s = Files.readAllLines(Path.of(dir + from + ".csv"));
        return s.stream().filter(line -> line.contains(from + "to" + to))
                .findFirst()
                .map(line -> Double.parseDouble(line.split(";")[1]) * amount)
                .orElse(0.0);
    }

}
