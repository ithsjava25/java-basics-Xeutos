package com.example;

import com.example.api.ElpriserAPI;

import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ElpriserAPI api = new ElpriserAPI();

        LocalDate today = LocalDate.now();
        List<ElpriserAPI.Elpris> todayPrices = api.getPriser(today, ElpriserAPI.Prisklass.SE3);



        System.out.println(cheapestHour(todayPrices));
        System.out.println(mostExpensiveHour(todayPrices));
        System.out.println(meanPrice(todayPrices));

        System.out.println(todayPrices);

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

}
