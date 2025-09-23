package com.example;

import com.example.api.ElpriserAPI;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ElpriserAPI api = new ElpriserAPI();

        ElpriserAPI.Prisklass zone = parseZone(args);
        LocalDate date = parseDate(args);




        List<ElpriserAPI.Elpris> todayPrices = api.getPriser(date, zone);

        System.out.println(cheapestHour(todayPrices));
        System.out.println(mostExpensiveHour(todayPrices));
        System.out.println(meanPrice(todayPrices));
        System.out.println(todayPrices);

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

    private static int cheapestHour(List<ElpriserAPI.Elpris> prices) {
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

    private static int mostExpensiveHour(List<ElpriserAPI.Elpris> prices) {
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


//    private static int optimalChargingWindow(int duration, int chargingWindow, List<ElpriserAPI.Elpris> prices) {
//        double[] window = new double[prices.size()];
//        for (int i = 0; i < prices.size(); i++) {
//            window[i] = prices.get(i).sekPerKWh();
//        }
//
//        for (int i = 0; i < prices.size() - chargingWindow; i++) {
//            int currentSum = 0;
//            for (int j = 0; j < chargingWindow; j++) {
//                currentSum = currentSum + window[i+j];
//            }
//        }
//
//        return -1;
//    }

}
