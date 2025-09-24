package com.example;

import com.example.api.ElpriserAPI;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ElpriserAPI api = new ElpriserAPI();

        ElpriserAPI.Prisklass zone = parseZone(args);
        LocalDate date = parseDate(args);
        List<ElpriserAPI.Elpris> todayPrices = api.getPriser(date, zone);

        for (String arg : args){
            if  (arg.equals("--help")){
                printHelp();
            }
        }

        for (String arg : args){
            if (arg.equals("--sorted")){
                sortedPrices(todayPrices);
            }
        }

        printPrices(todayPrices);

    }

    private static ElpriserAPI.Prisklass parseZone(String[] args) {
        ElpriserAPI.Prisklass zone = ElpriserAPI.Prisklass.SE1;
        if (args.length < 2) {
            throw new IllegalArgumentException("Invalid arguments");
        }

        for (int i = 0; i < args.length; i++) {
            if  (args[i].equals("--zone")) {
                if (args[i+1].equals("SE1")) {zone = ElpriserAPI.Prisklass.SE1;}
                if (args[i+1].equals("SE2")) {zone = ElpriserAPI.Prisklass.SE2;}
                if (args[i+1].equals("SE3")) {zone = ElpriserAPI.Prisklass.SE3;}
                if (args[i+1].equals("SE4")) {zone = ElpriserAPI.Prisklass.SE4;}
                }
            }
        return zone;
    }

    private static LocalDate parseDate(String[] args) {
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateString = "";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--date")) {
                dateString = args[++i];
                try {
                    date = LocalDate.parse(dateString, formatter);
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Invalid date format. Use yyyy-MM-dd");
                }
            }
        }
        return date;
    }


    private static double meanPrice(List<ElpriserAPI.Elpris> prices) {
        double sum = 0;
        for (ElpriserAPI.Elpris price : prices){
            sum += price.sekPerKWh();
        }
        return sum/prices.size();
    }

    private static int mostExpensiveHour(List<ElpriserAPI.Elpris> prices) {
        double largest = prices.getFirst().sekPerKWh();
        int index = 0;

        for (int i = 1; i < prices.size(); i++) {
            ElpriserAPI.Elpris price = prices.get(i);

            if (price.sekPerKWh() == largest) {
                continue;
            }

            if (price.sekPerKWh() > largest ) {
                largest = price.sekPerKWh();
                index = i;
            }
        }
        return index;
    }

    private static int leastExpensiveHour(List<ElpriserAPI.Elpris> prices) {
        double smallest = prices.getFirst().sekPerKWh();
        int index = 0;

        for (int i = 1; i < prices.size(); i++) {
            ElpriserAPI.Elpris price = prices.get(i);

            if  (price.sekPerKWh() == smallest ) {
                continue;
            }

            if (smallest > price.sekPerKWh() ) {
                smallest = price.sekPerKWh();
                index = i;
            }
        }
        return index;
    }

    private static void sortedPrices(List<ElpriserAPI.Elpris> prices) {
        Double[] array =  new Double[prices.size()];
        for (int i = 0; i < prices.size(); i++) {
            array[i] = prices.get(i).sekPerKWh();
        };
        Arrays.sort(array, Collections.reverseOrder());
        System.out.println(Arrays.toString(array));
    }


    private static int optimalChargingWindow() {
        return 0;
    }

    private static void printHelp(){
        System.out.println("--zone SE1|SE2|SE3|SE4 (required) \n"
        + "--date YYYY-MM-DD (optional, defaults to current date) will only work after 1pm\n"
        + "--sorted (optional, to display prices in descending order)\n"
        + "--charging 2h|4h|8h (optional, to find optimal charging windows)\n"
        + "--help (optional, to display usage information");
    }

    private static void printPrices(List<ElpriserAPI.Elpris> prices) {

        System.out.printf("Most expensive: %s, Price: %4f SEK/kWh\n",
                prices.get(mostExpensiveHour(prices)).timeStart().toLocalTime(), prices.get(mostExpensiveHour(prices)).sekPerKWh());
        System.out.printf("Least expensive: %s, Price: %4f SEK/kWh\n",
                prices.get(leastExpensiveHour(prices)).timeStart().toLocalTime(), prices.get(leastExpensiveHour(prices)).sekPerKWh());
        System.out.printf("Medelpris: %4f SEK/kwh\n", meanPrice(prices));
    }
}
