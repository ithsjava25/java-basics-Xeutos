package com.example;

import com.example.api.ElpriserAPI;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class Main {
    public static final int VALID = 1;
    public static final int NOT_VALID = 0;
    public static final int EMPTY = 0;

    public static void main(String[] args) {
        Locale.setDefault(Locale.of("sv", "SE"));
        ElpriserAPI api = new ElpriserAPI();

        boolean printPrices = true;

        ElpriserAPI.Prisklass zone = parseZone(args);
        LocalDate date = parseDate(args);
        List<ElpriserAPI.Elpris> todayPrices = api.getPriser(date, zone);
        List<ElpriserAPI.Elpris> tomorrowPrices = api.getPriser(date.plusDays(1), zone);

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
                sortedPrices(todayPrices, tomorrowPrices);
            }
        }

        parseCharging(args, todayPrices, tomorrowPrices);

        if (printPrices) {
            if (todayPrices.isEmpty()){
                System.out.println("No data");

            } else if (todayPrices.size() == 96) {
                hourlyPrices(convertToHourlyPrices(todayPrices));
                meanPrice(todayPrices);
            } else{
                mostExpensiveHour(todayPrices);
                leastExpensiveHour(todayPrices);
                meanPrice(todayPrices);
                }
        }
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

    private static void parseCharging(String[] args, List<ElpriserAPI.Elpris> prices, List<ElpriserAPI.Elpris> tomorrowPrices) {
        int validWindow = NOT_VALID;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--charging")) {
                if (args[i + 1].equals("2h")) {
                    optimalChargingWindow(prices, tomorrowPrices, 2);
                    validWindow = VALID;
                }
                if (args[i + 1].equals("4h")) {
                    optimalChargingWindow(prices, tomorrowPrices, 4);
                    validWindow = VALID;
                }
                if (args[i + 1].equals("8h")) {
                    optimalChargingWindow(prices, tomorrowPrices, 8);
                    validWindow = VALID;
                }
                if (validWindow == NOT_VALID) {
                    System.out.println("Invalid charging window");
                }
            }
        }
    }

    private static List<Double> convertToHourlyPrices(List<ElpriserAPI.Elpris> prices){
        List<Double> hourlyPrices = new ArrayList<>();
        int hourIndex = 0;

        if (prices.size() == 96){
            for (int i = 0; i < prices.size()/4; i++) {
                double hourPrice = prices.get(hourIndex).sekPerKWh()+
                        prices.get(hourIndex+1).sekPerKWh()+
                        prices.get(hourIndex+2).sekPerKWh()+
                        prices.get(hourIndex+3).sekPerKWh();
                hourPrice = hourPrice/4;
                hourlyPrices.add(hourPrice);
                hourIndex += 4;
            }
        }
        return hourlyPrices;
    }

    private static void hourlyPrices(List<Double> hourlyPrices) {
        double largest = hourlyPrices.getFirst();
        double smallest = hourlyPrices.getFirst();
        int indexLarge = 0;
        int indexSmallest = 0;

        for (int i = 0; i < hourlyPrices.size(); i++) {
            double priceLarge = hourlyPrices.get(i);

            if (priceLarge == largest){
                continue;
            }
            if (priceLarge > largest){
                largest = priceLarge;
                indexLarge = i;
            }
        }
        for (int i = 0; i < hourlyPrices.size(); i++) {
        double priceSmall = hourlyPrices.get(i);

        if (priceSmall == smallest){
            continue;
        }
        if (priceSmall < smallest){
            smallest = priceSmall;
            indexSmallest = i;
        }
    }
        printPrice(largest,indexLarge,"Högsta");
        printPrice(smallest,indexSmallest,"Lägsta");
}

    private static void meanPrice(List<ElpriserAPI.Elpris> prices) {
        double sum = 0;
        for (ElpriserAPI.Elpris price : prices){
            sum += price.sekPerKWh();
        }
        sum = sum/prices.size();
        System.out.printf("Medelpris: %.2f öre/kwh%n", sum*100);
        //return sum/prices.size();
    }

    private static void mostExpensiveHour(List<ElpriserAPI.Elpris> prices) {
        double largest = prices.getFirst().sekPerKWh();
        int index = 0;

            for (int i = 1; i < prices.size(); i++) {
                double price = prices.get(i).sekPerKWh();

                if (price == largest) {
                    continue;
                }

                if (price > largest) {
                    largest = price;
                    index = i;
                }
            }
        printPrice(largest,index,"Högsta");
        //return index;
        //todo handle hourly max when api is updated for every quarter hour. on 1/10/25
    }

    private static void leastExpensiveHour(List<ElpriserAPI.Elpris> prices) {
        double smallest = prices.getFirst().sekPerKWh();
        int index = 0;

            for (int i = 1; i < prices.size(); i++) {
                double price = prices.get(i).sekPerKWh();

                if (price == smallest) {
                    continue;
                }

                if (smallest > price) {
                    smallest = price;
                    index = i;
                }
            }
        printPrice(smallest,index,"Lägsta");
        //return index;
        //todo handle hourly min when api is updated for every quarter hour. on 1/10/25
    }

    private static void sortedPrices(List<ElpriserAPI.Elpris> prices, List<ElpriserAPI.Elpris> tomorrowPrices ) {
        prices.addAll(tomorrowPrices);
        SortedPrices[] sortedPrices =  new SortedPrices[prices.size()];


        for (int i = 0; i < prices.size(); i++) {
            double price = prices.get(i).sekPerKWh();
            sortedPrices[i]= new SortedPrices(price, i);
        }


        Comparator<SortedPrices> comparator = Comparator.comparing(SortedPrices::prices, Comparator.reverseOrder()).thenComparing(SortedPrices::index);
        Arrays.sort(sortedPrices, comparator);
        printSortedPrices(prices, sortedPrices);
    }

    private static void optimalChargingWindow(List<ElpriserAPI.Elpris> prices, List<ElpriserAPI.Elpris> tomorrowPrices, int duration) {
         if (prices.isEmpty()) {
            System.out.println("no data");
            return;
         }
        prices.addAll(tomorrowPrices);
        double[] array =  new double[prices.size()];
        int index = prices.size();
        int priceIndex = 0;

        for (int i = 0; i < prices.size(); i++) {
            array[i] = prices.get(i).sekPerKWh();
        }

        double minSum = 0;
        for (int i = 0; i < duration; i++) {
            minSum += array[i];
        }

        double windowSum = minSum;
        for (int i = 0; i < index-duration; i++) {
            windowSum += array[i+duration] - array[i];

            if (minSum > windowSum){
                minSum = windowSum;
                priceIndex++;
            }
        }

        minSum = minSum / duration;
        minSum = minSum * 10000;
        minSum = Math.round(minSum);
        minSum = minSum / 100;
        printChargingWindow(prices, priceIndex, minSum);
    }

    private static void printSortedPrices(List<ElpriserAPI.Elpris> prices, SortedPrices[] sortedPrices) {
        for (SortedPrices sortedPrice : sortedPrices) {
            int index = sortedPrice.index();
            int start = prices.get(index).timeStart().getHour();
            int end = prices.get(index).timeEnd().getHour();
            String stringFormat = String.format("%02d-%02d", start, end);

            System.out.printf("%s %2.2f öre%n",
                    stringFormat,
                    sortedPrice.prices() * 100);
        }
    }

    private static void printHelp(){
        System.out.println("""
                --zone SE1|SE2|SE3|SE4 (required)
                --date YYYY-MM-DD (optional, defaults to current date) will only work after 1pm
                --sorted (optional, to display prices in descending order)
                --charging 2h|4h|8h (optional, to find optimal charging windows)
                --help (optional, to display usage information""");
    }

    private static void printPrice(double price, int index,String type){
        List<Integer> startHour = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23);
        List<Integer> endHour = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 0);

        System.out.printf("%s pris: %s, Price: %.2f öre/kWh%n",
                type, timeFormat(startHour.get(index),endHour.get(index)), price*100);
    }

    private static void printChargingWindow(List<ElpriserAPI.Elpris> prices, int index, double meanPrice) {
        System.out.println("Påbörja laddning: kl " + prices.get(index).timeStart().toLocalTime());
        System.out.printf("Medelpris för fönster: %2.2f öre%n", meanPrice);
    }

    private static String timeFormat(int start, int end) {
        return String.format("%02d-%02d", start, end);
    }
}


record SortedPrices(double prices, int index) {
}
