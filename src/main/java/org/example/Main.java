package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.RootObject;
import dto.StandardSymbol;
import dto.SymbolDetails;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static String[][] generateStandardSymbolMatrix(RootObject rootObject){

        List<StandardSymbol> standardSymbolList = rootObject.getProbabilities().getStandardSymbols();

//        String[][] standardSymbolMatrix = new String[3][3];
        String[][] standardSymbolMatrix = new String[rootObject.getRows()][rootObject.getColumns()];

        // Access probabilities
        for (StandardSymbol symbol : standardSymbolList) {
            System.out.println("Column: " + symbol.getColumn() + ", Row: " + symbol.getRow());
            Map<String, Integer> symbolMap =symbol.getSymbols();

            Random random = new Random();
            int totalWeight =0;

            // Sum all values in the map
            totalWeight = symbolMap.values().stream().mapToInt(Integer::intValue).sum();
            int randomValue = random.nextInt(totalWeight - 1); // Generates 0 to (totalWeight - 1)
            System.out.println("randomValue: " + randomValue);

            int currentSum = 0;
            String cell = null;
            ArrayList<String> ansList = new ArrayList<>();

            for (Map.Entry<String, Integer> entry : symbolMap.entrySet()) {
                currentSum += entry.getValue(); // Add the weight of the current key
                if (currentSum > randomValue) {
                    cell = entry.getKey();
                    ansList.add(cell);
                    standardSymbolMatrix[symbol.getRow()][symbol.getColumn()] = cell;
                    break;
                }
            }
            System.out.println("########");
        }
        return standardSymbolMatrix;
    }

    public static String[][] addBonusTOMatrix(RootObject rootObject, String[][] standardSymbolMetrix, StringBuilder bonusKey) {
        Map<String, Integer> symbolMap = rootObject.getProbabilities().getBonusSymbols().getSymbols();
        int totalWeight = symbolMap.values().stream().mapToInt(Integer::intValue).sum();
        Random random = new Random();
        int randomValue = random.nextInt(totalWeight - 1);

        int currentSum = 0;
        String bouns = null;

        for (Map.Entry<String, Integer> entry : symbolMap.entrySet()) {
            currentSum += entry.getValue(); // Add the weight of the current key
            if (currentSum > randomValue) {
                bouns = entry.getKey();
                break;
            }
        }
        int row = random.nextInt(rootObject.getRows() -1); // Random row index
        int col = random.nextInt(rootObject.getColumns() -1 ); // Random column index
        standardSymbolMetrix[row][col] = bouns;
        bonusKey.replace(0, bonusKey.length(), bouns);
        return standardSymbolMetrix;
    }

    public static Map<String, Integer> getRewardMultiplierSymbols(String[][] matrixWithBonus ) {
        Map<String, Integer> countMap = new HashMap<>();

        // Traverse the 2D array and count occurrences of each string
        for (String[] row : matrixWithBonus) {
            for (String str : row) {
                countMap.put(str, countMap.getOrDefault(str, 0) + 1);
            }
        }
        Map<String, Integer> filteredMap = countMap.entrySet().stream()
                .filter(entry -> entry.getValue() > 2)  // Filter by value
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)); // Collect back to a Map

        return filteredMap;
    }

    //  Calculations: (bet_amount x reward(symbol_A) x reward(same_symbol_5_times) x reward(same_symbols_vertically))
    //               + (bet_amount x reward(symbol_B) x reward(same_symbol_3_times) x reward(same_symbols_vertically)) (+/x) reward(+1000)


    // = (100 x5 x5 x2) + (100 x3 x1 x2) + 1000
    // = 5000 + 600 + 1000
    // = 6600
    //Examples (with a winning combination [same symbols should be repeated at least 3 / reward x2]):

    public static double CalculateWinningAmount(Map<String, Integer> rewardMultiplierSymbolsMap, RootObject rootObject, int betAmount ){

        double total= 0;
        for (Map.Entry<String, Integer> entry : rewardMultiplierSymbolsMap.entrySet()) {
            // bet_amount x reward(symbol_A) x reward(same_symbol_5_times)

            double symbolValue = (rootObject.getSymbols().get(entry.getKey()).getRewardMultiplier());
            int sameSymbolCount = entry.getValue();
            double winCombinationsCount = getWinCombinationCount(sameSymbolCount, rootObject);
            double reward = betAmount * symbolValue * sameSymbolCount * winCombinationsCount ;
            total = total+ reward ;
        }
        return total;
    }

    public static double getWinCombinationCount(int sameSymbolCount, RootObject rootObject){
        String winCombinationsKey = "same_symbol_" + sameSymbolCount + "_times";
        double winCombinationsCount = rootObject.getWinCombinations().get(winCombinationsKey).getCount();
        return winCombinationsCount;
    }

    public static double addBonus(StringBuilder bonusKey, double winningAmountWithoutBonus, RootObject rootObject ){

        double finalValue = 0;
        String key = String.valueOf(bonusKey);
        String bonusType = rootObject.getSymbols().get(key).getImpact();

        if(bonusType.equals("multiply_reward")){
            double bonusRewardMultiplier = rootObject.getSymbols().get(key).getRewardMultiplier();
            finalValue = winningAmountWithoutBonus*bonusRewardMultiplier;

        } else if (bonusType.equals("extra_bonus")){
            double bonusRewardMultiplier = rootObject.getSymbols().get(key).getExtra();
            finalValue = winningAmountWithoutBonus + bonusRewardMultiplier;

        }else if (bonusType.equals("miss")){
            finalValue = winningAmountWithoutBonus;
        }
        return finalValue;
    }

    public static void main(String[] args) {

//            // Read file name from VM arguments
//            String configFile = System.getProperty("config");  // Read -Dconfig argument
//
//            if (configFile == null) {
//                System.out.println("Error: Please provide the config file using -Dconfig=<filename>");
//                return;
//            }else{
//
//            }

        int betAmount = 10;
        StringBuilder bonusKey = new StringBuilder("");

        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("config.json");

        if (inputStream == null) {
            System.out.println("File not found!");
            return;
        }
        try {

            // Create ObjectMapper for reading JSON
            ObjectMapper objectMapper = new ObjectMapper();
            // Read the JSON into the root object (RootObject is the class representing the JSON structure)
            RootObject rootObject = objectMapper.readValue(inputStream, RootObject.class);


            String[][] standardSymbolMatrix= generateStandardSymbolMatrix(rootObject);
            String[][]  matrixWithBonus = addBonusTOMatrix(rootObject,standardSymbolMatrix, bonusKey);

            System.out.println("bonusKey  @@@@@@@@@@  " + bonusKey);

            // Print the 2D array
            for (String[] row : matrixWithBonus) {
                for (String col : row) {
                    System.out.print(col + " ");
                }
                System.out.println();
            }

            System.out.println("checkSameSymbolCount  " + getRewardMultiplierSymbols(matrixWithBonus));
            Map<String, Integer>  rewardMultiplierSymbolsMap = getRewardMultiplierSymbols(matrixWithBonus);

            double winningAmountWithoutBonus = CalculateWinningAmount(rewardMultiplierSymbolsMap, rootObject, betAmount );
            System.out.println("winningAmountWithoutBonus  " + winningAmountWithoutBonus);

            double winningAmountWithBonus = addBonus( bonusKey, winningAmountWithoutBonus,  rootObject );
            System.out.println("winningAmountWithBonus  " + winningAmountWithBonus);

            } catch (IOException e) {
            e.printStackTrace();
        }
    }



}