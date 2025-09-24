package com.example;

import com.example.api.ElpriserAPI;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Main {
    public static final int VALID = 1;
    public static final int NOT_VALID = 0;
    public static final int EMPTY = 0;

    public static void main(String[] args) {
        ElpriserAPI api = new ElpriserAPI();

        boolean printPrices = true;

        ElpriserAPI.Prisklass zone = parseZone(args);
        LocalDate date = parseDate(args);
        List<ElpriserAPI.Elpris> todayPrices = api.getPriser(date, zone);
        List<ElpriserAPI.Elpris> tomorrowPrices = parseTomorrowPrices(api, zone);


        if (args.length == EMPTY) {
            printHelp();
            printPrices = false;
        }

        for (String arg : args){
            if  (arg.equals("--help")){
                printHelp();
                printPrices = false;
            }
        }

        for (String arg : args){
            if (arg.equals("--sorted")){

            }
        }

        parseCharging(args, todayPrices);

        if (printPrices) {
            if (todayPrices.isEmpty()){
                System.out.println("No data");

            } else
                printPrices(todayPrices);
        }

        sortedPrices(todayPrices);
    }

    private static List<ElpriserAPI.Elpris> parseTomorrowPrices(ElpriserAPI api, ElpriserAPI.Prisklass zone) {
        LocalTime now = LocalTime.now();
        LocalTime cutoff = LocalTime.of(13, 0);

        if (now.isBefore(cutoff)) {
            LocalDate today = LocalDate.now();
            LocalDate tomorrow = today.plusDays(1);
            return api.getPriser(tomorrow, zone);
        }
        return null;
    }

    private static ElpriserAPI.Prisklass parseZone(String[] args) {
        ElpriserAPI.Prisklass zone = ElpriserAPI.Prisklass.SE1;
        int missingZone = NOT_VALID;
        int validZone = NOT_VALID;

        if(args.length == EMPTY){
            missingZone = VALID;
            validZone = VALID;
        }

        for (String arg : args){
            if(arg.equals("--zone")){
                missingZone = VALID;
            }
            if(arg.equals("--help")){
                missingZone = VALID;
                validZone = VALID;
            }
        }
        if (missingZone == NOT_VALID){
            System.out.println("zone required");
//            throw new IllegalArgumentException("Zone required");
        }

        for (int i = 0; i < args.length; i++) {
            if  (args[i].equals("--zone")) {
                if (args[i+1].equals("SE1")) {zone = ElpriserAPI.Prisklass.SE1; validZone = VALID;}
                if (args[i+1].equals("SE2")) {zone = ElpriserAPI.Prisklass.SE2; validZone = VALID;}
                if (args[i+1].equals("SE3")) {zone = ElpriserAPI.Prisklass.SE3; validZone = VALID;}
                if (args[i+1].equals("SE4")) {zone = ElpriserAPI.Prisklass.SE4; validZone = VALID;}
                }
            }

        if (validZone == NOT_VALID) {
            System.out.println("Invalid zone");
        }

        return zone;
    }

    private static LocalDate parseDate(String[] args) {
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateString;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--date")) {
                dateString = args[++i];
                try {
                    date = LocalDate.parse(dateString, formatter);
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid date");
                }
            }
        }
        return date;
    }

    private static void parseCharging(String[] args, List<ElpriserAPI.Elpris> prices) {
        int validWindow = NOT_VALID;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--charging")){
                if (args[i+1].equals("2h")){optimalChargingWindow(prices, 2); validWindow = VALID;}
                if (args[i+1].equals("4h")){optimalChargingWindow(prices, 4); validWindow = VALID;}
                if (args[i+1].equals("8h")){optimalChargingWindow(prices, 8); validWindow = VALID;}
                if (validWindow == 0)
                    System.out.println("Invalid charging window");
            }
        }
        if (validWindow == VALID)
            printChargingWindow();
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

        SortedPrices[] sortedPrices = new SortedPrices[prices.size()];

        for (int i = 0; i < prices.size(); i++) {
            double price = prices.get(i).sekPerKWh();
            sortedPrices[i]= new SortedPrices(price, i);
        }

        Comparator<SortedPrices> comparator = Comparator.comparing(SortedPrices::prices, Comparator.reverseOrder());
        Arrays.sort(sortedPrices, comparator);
        System.out.print("[");
        printSortedPrices(prices, sortedPrices);


        //todo: figure out how to keep index so print can include corresponding time instead of just prices.
    }

    private static int optimalChargingWindow(List<ElpriserAPI.Elpris> prices, int duration) {
        double[] array =  new double[prices.size()];
        int index = prices.size();

        for (int i = 0; i < prices.size(); i++) {
            array[i] = prices.get(i).sekPerKWh()*100;
        }

        int optimalChargingWindow = 0;
        for (int i = 0; i < duration; i++) {
            optimalChargingWindow += (int) array[i];
        }

        int currentChargingWindow = optimalChargingWindow;
        for (int i = duration; i < index; i++) {
            currentChargingWindow += (int) (array[i] - array[index - duration]);
            optimalChargingWindow = Math.max(optimalChargingWindow, currentChargingWindow);
        }

        return optimalChargingWindow;
        //todo: figure out how to how to apply to finding optimal charging window.
    }

    private static void printSortedPrices(List<ElpriserAPI.Elpris> prices, SortedPrices[] sortedPrices) {
        for (int i = 0; i < sortedPrices.length; i++) {
            int index = sortedPrices[i].index();
            int start = prices.get(index).timeStart().getHour();
            int end = prices.get(index).timeEnd().getHour();
            int lineBreak = i+1;
            String stringFormat = String.format("%02d-%02d", start, end);

            System.out.printf("%s %2.2f öre",
                    stringFormat,
                    sortedPrices[i].prices()*100);

            if (i < sortedPrices.length - 1) {
                System.out.print(", \n");
            }
//            if (lineBreak %4 == 0 && i < sortedPrices.length - 1) {
//                System.out.println();
//            }
        }
        System.out.print("]");
        System.out.println();
    }

    private static void printHelp(){
        System.out.println("""
                --zone SE1|SE2|SE3|SE4 (required)
                --date YYYY-MM-DD (optional, defaults to current date) will only work after 1pm
                --sorted (optional, to display prices in descending order)
                --charging 2h|4h|8h (optional, to find optimal charging windows)
                --help (optional, to display usage information""");
    }

    private static void printPrices(List<ElpriserAPI.Elpris> prices) {
        int expensiveStart = prices.get(mostExpensiveHour(prices)).timeStart().getHour();
        int expensiveEnd = prices.get(mostExpensiveHour(prices)).timeEnd().getHour();
        int cheapStart = prices.get(leastExpensiveHour(prices)).timeStart().getHour();
        int cheapEnd = prices.get(leastExpensiveHour(prices)).timeEnd().getHour();

        System.out.printf("Högsta pris: %s, Price: %.2f öre/kWh\n",
                timeFormat(expensiveStart, expensiveEnd),
                prices.get(mostExpensiveHour(prices)).sekPerKWh()*100);
        System.out.printf("Lägsta pris: %s, Price: %.2f öre/kWh\n",
                timeFormat(cheapStart, cheapEnd),
                prices.get(leastExpensiveHour(prices)).sekPerKWh()*100);
        System.out.printf("Medelpris: %.2f öre/kwh\n", meanPrice(prices)*100);
    }

    private static void printChargingWindow(){

    }
    
    private static String timeFormat(int start, int end){
        return String.format("%02d-%02d", start, end);
    }
}


record SortedPrices(
        double prices,
        int index
){}
