package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dto.OutPut;
import dto.RootObject;
import dto.StandardSymbol;

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
//            System.out.println("Column: " + symbol.getColumn() + ", Row: " + symbol.getRow());
            Map<String, Integer> symbolMap =symbol.getSymbols();

            Random random = new Random();
            int totalWeight =0;

            // Sum all values in the map
            totalWeight = symbolMap.values().stream().mapToInt(Integer::intValue).sum();
            int randomValue = random.nextInt(totalWeight - 1); // Generates 0 to (totalWeight - 1)
//            System.out.println("randomValue: " + randomValue);

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
        }
        return standardSymbolMatrix;
    }

    public static String[][] addBonusSymbolToMatrix(RootObject rootObject, String[][] standardSymbolMetrix, StringBuilder bonusKey) {
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

    public static Map<String, Integer> getDuplicateSymbols(String[][] matrixWithBonusSymbols ) {
        Map<String, Integer> countMap = new HashMap<>();

        // Traverse the 2D array and count occurrences of each string
        for (String[] row : matrixWithBonusSymbols) {
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

    public static Map<String, Double> calculateWinningAmountWithoutBonus(Map<String, Integer> rewardMultiplierSymbolsMap, RootObject rootObject, int betAmount ){

        Map<String, Double> countMap = new HashMap<>();


        double total= 0;
        for (Map.Entry<String, Integer> entry : rewardMultiplierSymbolsMap.entrySet()) {
            // bet_amount x reward(symbol_A) x reward(same_symbol_5_times)

            double symbolValue = (rootObject.getSymbols().get(entry.getKey()).getRewardMultiplier());
            int sameSymbolCount = entry.getValue();
            double winCombinationsCount = getWinCombinationCount(sameSymbolCount, rootObject);
            double reward = betAmount * symbolValue * winCombinationsCount ;
            countMap.put(entry.getKey(),reward );
//            total = total+ reward ;
        }
//        rewardMultiplierSymbolsMap = countMap
        return countMap;
    }

    // sub methode of CalculateWinningAmount
    public static double getWinCombinationCount(int sameSymbolCount, RootObject rootObject){
        String winCombinationsKey = "same_symbol_" + sameSymbolCount + "_times";
        double winCombinationsCount = rootObject.getWinCombinations().get(winCombinationsKey).getRewardMultiplier();
//        double winCombinationsCount = rootObject.getWinCombinations().get(winCombinationsKey).getCount();
        return winCombinationsCount;
    }

    public static double addSymbolBonus(StringBuilder bonusKey, double winningAmountWithoutBonus, RootObject rootObject ){

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

    public static double  addPatternBonus(String[][] matrixWithBonusSymbols, HashMap<String, Integer> winningDuplicateSymbols,
                                          RootObject rootObject,  Map<String, Double> winingSymbolAndRewardMap ){



        Map<String, Double> countMap = new HashMap<>();

        // HashMap to store indexes
//        HashMap<String, int[][]> indexMap = new HashMap<>();
        HashMap<String, List<Integer>> allHorizontalIndexesMap = new HashMap<>();
        HashMap<String, List<Integer>> allVerticalIndexesMap = new HashMap<>();

        findAllWinningIndices(  allHorizontalIndexesMap, allVerticalIndexesMap , matrixWithBonusSymbols, winningDuplicateSymbols,  rootObject);


        List<String> keysWithHorizontalSequence = hasHorizontalSequence(allHorizontalIndexesMap,  rootObject);
        List<String> keysWithVerticalSequence = hasVerticalSequence(allVerticalIndexesMap,  rootObject);


        double winningAmountWithPatternBonus = 0;


        double winningAmountWithPatternBonusHorizontally = 0;

        if(keysWithHorizontalSequence.size() > 0){
            for (int i = 0; i < keysWithHorizontalSequence.size(); i++) {
                double winningAmountWithSymbolBonus =winingSymbolAndRewardMap.get(keysWithHorizontalSequence.get(i));
                double rewardMultiplier = rootObject.getWinCombinations().get("same_symbols_horizontally").getRewardMultiplier();
                double total = winningAmountWithSymbolBonus * rewardMultiplier * keysWithHorizontalSequence.size();

                winningAmountWithPatternBonusHorizontally = winningAmountWithPatternBonusHorizontally + total;
            }
        }

        double winningAmountWithPatternBonusVertically =0;
        if (keysWithVerticalSequence.size() > 0) {
            for (int i = 0; i < keysWithVerticalSequence.size(); i++) {

                double winningAmountWithSymbolBonus =winingSymbolAndRewardMap.get(keysWithVerticalSequence.get(i));
                double rewardMultiplier = rootObject.getWinCombinations().get("same_symbols_vertically").getRewardMultiplier();
                double total = winningAmountWithSymbolBonus * rewardMultiplier * keysWithVerticalSequence.size();

                winningAmountWithPatternBonusVertically = winningAmountWithPatternBonusVertically +   total ;
            }
        }

        winningAmountWithPatternBonus = winningAmountWithPatternBonusHorizontally + winningAmountWithPatternBonusVertically;

        return winningAmountWithPatternBonus;
    }
    public static void findAllWinningIndices( HashMap<String, List<Integer>> allXindexesMap,HashMap<String, List<Integer>> allYindexesMap ,
                                                 String[][] matrixWithBonusSymbols, HashMap<String, Integer> winningDuplicateSymbols, RootObject rootObject) {


        // Finding indexes for each winning symbol
        for (Map.Entry<String, Integer> entry : winningDuplicateSymbols.entrySet()) {
            String key = entry.getKey();
            List<int[]> positions = new ArrayList<>();
            List<Integer> allXindexes = new ArrayList<>();
            List<Integer> allYindexes = new ArrayList<>();

            // Searching in the matrix
            for (int i = 0; i < matrixWithBonusSymbols.length; i++) {
                for (int j = 0; j < matrixWithBonusSymbols[i].length; j++) {
                    if (matrixWithBonusSymbols[i][j].equals(key)) {
                        positions.add(new int[]{i, j}); // Store (row, column) index
                        allXindexes.add(i);
                        allYindexes.add(j);
                    }
                }
            }
            // Convert List<int[]> to int[][] and put in indexMap
//            indexMap.put(key, positions.toArray(new int[0][]));
            allXindexesMap.put(key, allXindexes);
            allYindexesMap.put(key, allYindexes);
        }

        // Printing the indexMap
//        for (Map.Entry<String, int[][]> entry : indexMap.entrySet()) {
//            System.out.println("Key: " + entry.getKey());
//            for (int[] pos : entry.getValue()) {
//                System.out.println("  (" + pos[0] + ", " + pos[1] + ")");
//            }
//        }

        // Printing the map
        for (Map.Entry<String, List<Integer>> entry : allXindexesMap.entrySet()) {
            System.out.println("X Key: " + entry.getKey() + ", Values: " + entry.getValue());
        }

        // Printing the map
        for (Map.Entry<String, List<Integer>> entry : allYindexesMap.entrySet()) {
            System.out.println("Y Key: " + entry.getKey() + ", Values: " + entry.getValue());
        }
    }

    public static  List<String> hasHorizontalSequence( HashMap<String, List<Integer>> firstIndexMap, RootObject rootObject){

        List<String> keysWithFrequentNumbers = new ArrayList<>();
        int columnLength = rootObject.getColumns();

        for (Map.Entry<String, List<Integer>> entry : firstIndexMap.entrySet()) {
            String key = entry.getKey();
            List<Integer> values = entry.getValue();

            // Count occurrences of each number
            HashMap<Integer, Integer> frequencyMap = new HashMap<>();
            for (Integer num : values) {
                frequencyMap.put(num, frequencyMap.getOrDefault(num, 0) + 1);
            }

            // Check if any number appears more than 'n' times
            for (Integer count : frequencyMap.values()) {
                if (count >= columnLength ) {
                    keysWithFrequentNumbers.add(key);
                    break; // No need to check further for this key
                }
            }
            System.out.println("Horizontal Sequence, symbol appearing more than " + columnLength + " times: " + keysWithFrequentNumbers);
        }
        return keysWithFrequentNumbers;
    }

    public static  List<String> hasVerticalSequence( HashMap<String, List<Integer>> firstIndexMap, RootObject rootObject){

        List<String> keysWithFrequentNumbers = new ArrayList<>();
        int rowLength = rootObject.getRows();

        for (Map.Entry<String, List<Integer>> entry : firstIndexMap.entrySet()) {
            String key = entry.getKey();
            List<Integer> values = entry.getValue();

            // Count occurrences of each number
            HashMap<Integer, Integer> frequencyMap = new HashMap<>();
            for (Integer num : values) {
                frequencyMap.put(num, frequencyMap.getOrDefault(num, 0) + 1);
            }
            // Check if any number appears more than 'n' times
            for (Integer count : frequencyMap.values()) {
                if (count >= rowLength ) {
                    keysWithFrequentNumbers.add(key);
                    break; // No need to check further for this key
                }
            }
            System.out.println("Vertical Sequence, symbol appearing more than " + rowLength + " times: " + keysWithFrequentNumbers);
        }
        return keysWithFrequentNumbers;
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

        int betAmount = 100;
        System.out.println("betAmount = "+  betAmount );

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
            String[][]  matrixWithBonusSymbols2 = addBonusSymbolToMatrix(rootObject,standardSymbolMatrix, bonusKey);


             bonusKey = new StringBuilder("+1000");  // todo remove

            String[][] matrixWithBonusSymbols = {
                    {"A", "A", "B"},
                    {"A", "+1000", "B"},
                    {"A", "A", "B"}
            };

            System.out.println("bonusKey  @@@@@@@@@@  " + bonusKey);

            // Print the 2D array
            for (String[] row : matrixWithBonusSymbols) {
                for (String col : row) {
                    System.out.print(col + " ");
                }
                System.out.println();
            }

            System.out.println("getDuplicateSymbols  " + getDuplicateSymbols(matrixWithBonusSymbols));
            Map<String, Integer>  winningDuplicateSymbols = getDuplicateSymbols(matrixWithBonusSymbols);

            Map<String, Double> winingSymbolAndRewardMap = new HashMap<String, Double>(); //

            winingSymbolAndRewardMap  = calculateWinningAmountWithoutBonus(winningDuplicateSymbols, rootObject, betAmount );
            System.out.println("winningAmountWithoutBonus  " + winingSymbolAndRewardMap);


            // pattern based Bonus calculation
            double winningAmountWithSymbolBonusAndPatternBonus = addPatternBonus( matrixWithBonusSymbols, (HashMap<String, Integer>) winningDuplicateSymbols
                    ,rootObject , winingSymbolAndRewardMap );

            double finalReword = addSymbolBonus( bonusKey, winningAmountWithSymbolBonusAndPatternBonus,  rootObject );
            System.out.println("winningAmountWithSymbolBonus  " + finalReword);


            System.out.println("finalReword  " + finalReword);


            // final output
            OutPut outPut = new OutPut(
                    matrixWithBonusSymbols,
                    finalReword,
                    null,
                    bonusKey.toString()
            );

            // Create ObjectMapper to serialize to JSON
            ObjectMapper objectMapperOutput = new ObjectMapper();
            // Enable pretty printing
            objectMapperOutput.enable(SerializationFeature.INDENT_OUTPUT);

            // Convert the object to JSON and print it
            String json = objectMapperOutput.writeValueAsString(outPut);
            System.out.println(json);

            } catch (IOException e) {
            e.printStackTrace();
        }
    }



}