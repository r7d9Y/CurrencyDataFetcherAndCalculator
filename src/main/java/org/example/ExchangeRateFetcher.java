package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;


public class ExchangeRateFetcher {
    public static final float version = 1.0f;
    private static final ArrayList<String> responseCodes = new ArrayList<>();
    private static final String[] currencies = new String[]{
            "EUR", "GBP", "INR", "AUD", "CAD", "SGD", "CHF", "MYR", "JPY", "CNY",
            "ARS", "BHD", "BWP", "BRL", "BND", "BGN", "CLP", "COP", "CZK", "DKK",
            "AED", "HKD", "HUF", "ISK", "IDR", "IRR", "ILS", "KZT", "KWD", "LYD",
            "MUR", "MXN", "NPR", "NZD", "NOK", "OMR", "PKR", "PHP", "PLN", "QAR",
            "RON", "RUB", "SAR", "ZAR", "KRW", "LKR", "SEK", "TWD", "THB", "TTD",
            "TRY", "VEF", "USD"
    };

    /**
     * @return an array of available currencies in a 3-letter format
     */
    public String[] getAvailableCurrencies() {
        return currencies;
    }

    private static String outDir = "res/";

    /**
     * @param outputDirectory the directory where the exchange rates will be saved
     *                        this method will get the exchange rates of the currencies in the currencies array
     *                        and save them in the outputDirectory in CSV file per currency
     */
    public static boolean getExchangeRates(String outputDirectory) {
        long time = System.currentTimeMillis();
        Path pathOfOutput = Path.of(outputDirectory);
        if (!Files.exists(pathOfOutput) || !Files.isDirectory(pathOfOutput)) {
            try {
                Files.createDirectory(pathOfOutput);
            } catch (IOException e) {
                e.getMessage();
                return false;
            }
        }
        outDir = outputDirectory;
        for (String currency : currencies) {
            Fetcher f = new Fetcher(currency, outputDirectory);
            f.run();
        }
        System.out.println("Response codes from currencies scrapper: " + responseCodes);
        System.out.println("Time taken to get exchange rates: " + (System.currentTimeMillis() - time) + "ms");
        return true;
    }

    /**
     * this method will reset the exchange rates of the currencies in the currencies array
     * by deleting the CSV files of the exchange rates and re-fetching them
     */
    public static void resetExchangeRates() {
        try {
            for (String currency : currencies) {
                Files.delete(Path.of(outDir + "/" + currency + ".csv"));
            }
            getExchangeRates(outDir);
        } catch (IOException e) {
            e.getMessage();
        }
    }

    /**
     * @param url the url of the page to be fetched
     * @return the content of the html page as a string
     * @throws IOException if the page is not found or cannot be fetched
     */
    private static String getPage(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        responseCodes.add(String.valueOf(con.getResponseCode()));
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    /**
     * this method will go through the content of the given html page (<a href="https://www.x-rates.com">...</a>)
     * and extract the exchange rates
     *
     * @param html      the content of the html page
     * @param currency  the currency to get the exchange rates for
     * @param outputDir the directory where the exchange rates will be saved in a CSV file
     * @throws IOException if the CSV file cannot be created successfully
     */
    private static void getContentIntoCSV(String html, String currency, String outputDir) throws IOException {
        StringBuilder outputContent = new StringBuilder();
        currency = currency.toUpperCase();
        Document doc = Jsoup.parse(html);
        Elements links = doc.getElementsByClass("rtRates");
        for (Element link : links) {
            String href = link.attr("href").contains("from=" + currency + "&amp") ?
                    link.children().get(1).text() : link.children().get(0).text();
            String out = (!href.isEmpty() ? link : "") + href;
            if (out.contains("from=" + currency)) {
                String conversion = out.substring(out.indexOf('&') - 3, out.indexOf("&")) + "to" +
                        out.substring(out.indexOf("to=") + 3, out.indexOf("to=") + 6);
                double rate = Double.parseDouble(out.substring(out.lastIndexOf('>') + 1, out.length() - 1).trim());
                outputContent.append(conversion).append(";").append(rate).append("\n");
            }
        }

        Path pathOfCSV = Path.of(outputDir + "/" + currency + ".csv");
        Files.deleteIfExists(pathOfCSV);
        Files.createFile(pathOfCSV);
        BufferedWriter writer = new BufferedWriter(new FileWriter(String.valueOf(pathOfCSV)));
        writer.write(outputContent.toString());

        writer.close();
    }

    /**
     * this class is used to fetch the exchange rates of a given currency
     *
     * @param currency        the currency to get the exchange rates for
     * @param outputDirectory the directory where the exchange rates will be saved in a CSV file
     */
    private record Fetcher(String currency, String outputDirectory) implements Runnable {
        public void run() {
            try {
                String html = getPage("https://www.x-rates.com/table/?from=" + currency + "&amount=1");
                getContentIntoCSV(html, currency, outputDirectory);
            } catch (IOException e) {
                e.getMessage();
            }

        }
    }
}