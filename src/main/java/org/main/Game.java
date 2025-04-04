package org.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dto.OutPut;
import dto.RootObject;
import dto.StandardSymbol;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class Game {


    // this method will generate Standard Symbol Matrix based on the Probabilities of config Map
    public static String[][] generateStandardSymbolMatrix(RootObject rootObject){

        List<StandardSymbol> standardSymbolList = rootObject.getProbabilities().getStandardSymbols();

        String[][] standardSymbolMatrix = new String[rootObject.getRows()][rootObject.getColumns()];

        // Access probabilities
        for (StandardSymbol symbol : standardSymbolList) {
            Map<String, Integer> symbolMap =symbol.getSymbols();

            Random random = new Random();
            int totalWeight =0;

            // Sum all values in the map
            totalWeight = symbolMap.values().stream().mapToInt(Integer::intValue).sum();
            int randomValue = random.nextInt(totalWeight - 1); // Generates 0 to (totalWeight - 1)

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

    // this method will addB onus Symbol To the generated Matrix based on the Probabilities of config Map
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

    // this method will return Duplicate Symbols count with key
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


    // this method will calculate Winning Amount Without Bonus // betAmount * symbolValue * winCombinationsCount
    public static Map<String, Double> addSymbolValueAndSymbolDuplicationBonus(Map<String, Integer> rewardMultiplierSymbolsMap, RootObject rootObject,
                                                                         double betAmount ){

        Map<String, Double> countMap = new HashMap<>();

        for (Map.Entry<String, Integer> entry : rewardMultiplierSymbolsMap.entrySet()) {

            double symbolValue = (rootObject.getSymbols().get(entry.getKey()).getRewardMultiplier());
            int sameSymbolCount = entry.getValue();

            double winCombinationsCount = getWinCombinationCount(sameSymbolCount, rootObject);

            double reward = betAmount * symbolValue * winCombinationsCount ;
            countMap.put(entry.getKey(),reward );

        }
        return countMap;
    }


    // sub methode of calculateWinningAmountWithoutBonus
    public static double getWinCombinationCount(int sameSymbolCount, RootObject rootObject){
        String winCombinationsKey = "same_symbol_" + sameSymbolCount + "_times";
        double winCombinationsCount = rootObject.getWinCombinations().get(winCombinationsKey).getRewardMultiplier();
        return winCombinationsCount;
    }


    // this method help to update "Output object , this updates "applied_winning_combinations" field
    public static HashMap<String, List<String>> updateWinningCombinationsForOutput(Map<String, Integer> winningDuplicateSymbols, HashMap<String, List<String>> appliedWinningCombinationsForOutPut){

        HashMap<String, List<String>> newAppliedWinningCombinations = new HashMap<>();

        for (Map.Entry<String, Integer> entry : winningDuplicateSymbols.entrySet()) {

            int sameSymbolCount = entry.getValue();
            String key = entry.getKey();
            String winCombinationsKey = "same_symbol_" + sameSymbolCount + "_times";
            List<String> newList = new ArrayList<>();
            newList.add(winCombinationsKey);
            List<String> list = appliedWinningCombinationsForOutPut.get(key);
            newList.addAll(list != null ? list : Collections.emptyList());
            newAppliedWinningCombinations.put(key,  newList);
        }
        return newAppliedWinningCombinations;
    }


    // this method add the bonus symbol based amount to the finalReword
    public static double addSymbolBonus(StringBuilder bonusKey, double winningAmountWithoutBonus, RootObject rootObject, Map<String, Integer>  winningDuplicateSymbols ){

        double finalValue = 0;
        String key = String.valueOf(bonusKey);
        String bonusType = rootObject.getSymbols().get(key).getImpact();

        // Bonus symbols are only effective when there are at least one winning combination
        if(winningDuplicateSymbols.size() == 0){
            return winningAmountWithoutBonus;
        }

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

    // this method add Pattern based Bonus to the Reword. it identified whether it is  HorizontalSequence, VerticalSequence. then add the Reword
    public static double  addPatternBonus(String[][] matrixWithBonusSymbols, HashMap<String, Integer> winningDuplicateSymbols, RootObject rootObject,
                                          Map<String, Double> symbolValueAndSymbolDuplicationBonusMap , HashMap<String, List<String>> appliedWinningCombinationsForOutPut){

        HashMap<String, List<Integer>> allXIndexesMap = new HashMap<>();
        HashMap<String, List<Integer>> allYIndexesMap = new HashMap<>();
        HashMap<String, int[][]> allXYIndexesMap = new HashMap<>();

        findAllWinningIndices(  allXIndexesMap, allYIndexesMap , allXYIndexesMap, matrixWithBonusSymbols, winningDuplicateSymbols);

        List<String> keysWithHorizontalSequence = hasHorizontalSequence(allXIndexesMap,  rootObject);
        List<String> keysWithVerticalSequence = hasVerticalSequence(allYIndexesMap,  rootObject);

       diagonallyLeftToRight( allXYIndexesMap,symbolValueAndSymbolDuplicationBonusMap,matrixWithBonusSymbols,rootObject, appliedWinningCombinationsForOutPut);
       diagonallyRightToLeft( allXYIndexesMap,symbolValueAndSymbolDuplicationBonusMap,matrixWithBonusSymbols,rootObject, appliedWinningCombinationsForOutPut );

        double winningAmountWithPatternBonus = 0;

        double winningAmountWithPatternBonusHorizontally = 0;
        if(keysWithHorizontalSequence.size() > 0){
            for (int i = 0; i < keysWithHorizontalSequence.size(); i++) {

                double winningAmountWithSymbolBonus =symbolValueAndSymbolDuplicationBonusMap.get(keysWithHorizontalSequence.get(i));
                double rewardMultiplier = rootObject.getWinCombinations().get("same_symbols_horizontally").getRewardMultiplier();
                double total = winningAmountWithSymbolBonus * rewardMultiplier * keysWithHorizontalSequence.size();
                winningAmountWithPatternBonusHorizontally = winningAmountWithPatternBonusHorizontally + total;

                // adding values to outPut object field appliedWinningCombinations
                List<String> newList = new ArrayList<>();
                newList.add("same_symbols_horizontally");
                appliedWinningCombinationsForOutPut.computeIfAbsent(keysWithHorizontalSequence.get(i), k -> new ArrayList<>()).addAll(newList);
            }
        }

        double winningAmountWithPatternBonusVertically =0;
        if (keysWithVerticalSequence.size() > 0) {

            for (int i = 0; i < keysWithVerticalSequence.size(); i++) {
                double winningAmountWithSymbolBonus =symbolValueAndSymbolDuplicationBonusMap.get(keysWithVerticalSequence.get(i));
                double rewardMultiplier = rootObject.getWinCombinations().get("same_symbols_vertically").getRewardMultiplier();
                double total = winningAmountWithSymbolBonus * rewardMultiplier * keysWithVerticalSequence.size();
                winningAmountWithPatternBonusVertically = winningAmountWithPatternBonusVertically +   total ;

                // adding values to outPut object field appliedWinningCombinations
                List<String> newList = new ArrayList<>();
                newList.add("same_symbols_vertically");
                appliedWinningCombinationsForOutPut.computeIfAbsent(keysWithVerticalSequence.get(i), k -> new ArrayList<>()).addAll(newList);
            }
        }

        double totalWithoutPatternBonus = 0.0;
        if(keysWithVerticalSequence.size() == 0 && keysWithHorizontalSequence.size() == 0){
            totalWithoutPatternBonus = symbolValueAndSymbolDuplicationBonusMap.values()
                    .stream()
                    .mapToDouble(Double::doubleValue)
                    .sum();
        }

        winningAmountWithPatternBonus = winningAmountWithPatternBonusHorizontally + winningAmountWithPatternBonusVertically + totalWithoutPatternBonus;

        return winningAmountWithPatternBonus;
    }

    // this methode will calculate and update the symbolValueAndSymbolDuplicationBonusMap if there is a diagonallyLeftToRight
    public static HashMap<String, List<String>> diagonallyLeftToRight( HashMap<String, int[][]> allXYIndexesMap,  Map<String, Double> symbolValueAndSymbolDuplicationBonusMap ,
                                                String[][] matrixWithBonusSymbols, RootObject rootObject, HashMap<String, List<String>> appliedWinningCombinationsForOutPut ){
        boolean status = true;

        if( rootObject.getRows() != rootObject.getColumns()){
            return appliedWinningCombinationsForOutPut;
        }

        if(symbolValueAndSymbolDuplicationBonusMap == null){
            return appliedWinningCombinationsForOutPut;
        }

        if(symbolValueAndSymbolDuplicationBonusMap.size() == 0){
            return appliedWinningCombinationsForOutPut;
        }

        int matrixSize = rootObject.getRows();
        int[][] primaryDiagonal = new int[matrixSize][2];

        // Storing primary diagonal indices
        for (int i = 0; i < matrixSize ; i++) {
            primaryDiagonal[i][0] = i;
            primaryDiagonal[i][1] = i;
        }

        /// check the diagonally Left To Right status
        // Use a HashSet for fast lookup


        // Convert all indexes from HashMap into a Set
        // based on the key, indexSet should be compared
        for (int[][] indexArray : allXYIndexesMap.values()) {

            Set<String> indexSet = new HashSet<>();
            for (int[] pair : indexArray) {
                indexSet.add(pair[0] + "," + pair[1]); // Store as "row,col"
            }

            for (int[] target : primaryDiagonal) {
                if (!indexSet.contains(target[0] + "," + target[1])) {
                    status = false; // If any index is missing, return false
                    break;
                }
            }
        }
        // Check if all primaryDiagonal indexes exist in the Set

        if(status){
             double rewardMultiplier = rootObject.getWinCombinations().get("same_symbols_diagonally_left_to_right").getRewardMultiplier();
             String symbolOfDiagonallyLeftToRight = matrixWithBonusSymbols[0][0];

            double currentValueForSymbolAndDuplicationBonus = symbolValueAndSymbolDuplicationBonusMap.get(symbolOfDiagonallyLeftToRight);
            double diagonallyValueForSymbolAndDuplicationBonus = currentValueForSymbolAndDuplicationBonus * rewardMultiplier;

            symbolValueAndSymbolDuplicationBonusMap.forEach((key, value) -> {
                if (key.equals(symbolOfDiagonallyLeftToRight)) {
                    symbolValueAndSymbolDuplicationBonusMap.put(key, diagonallyValueForSymbolAndDuplicationBonus);
                }
            });
            List<String> newList = new ArrayList<>();
            newList.add("same_symbols_diagonally_left_to_right");
            appliedWinningCombinationsForOutPut.computeIfAbsent(symbolOfDiagonallyLeftToRight, k -> new ArrayList<>()).addAll(newList);
        }
        return appliedWinningCombinationsForOutPut;
    }


    // this methode will calculate and update the symbolValueAndSymbolDuplicationBonusMap if there is a diagonallyRightToLeft
    public static HashMap<String, List<String>> diagonallyRightToLeft( HashMap<String, int[][]> allXYIndexesMap,  Map<String, Double> symbolValueAndSymbolDuplicationBonusMap ,
                                                             String[][] matrixWithBonusSymbols, RootObject rootObject , HashMap<String, List<String>> appliedWinningCombinationsForOutPut){
        boolean status = true;

        if( rootObject.getRows() != rootObject.getColumns()){
            return appliedWinningCombinationsForOutPut;
        }

        if(symbolValueAndSymbolDuplicationBonusMap == null){
            return appliedWinningCombinationsForOutPut;
        }

        if(symbolValueAndSymbolDuplicationBonusMap.size() == 0){
            return appliedWinningCombinationsForOutPut;
        }

        int matrixSize = rootObject.getRows();
        int[][] secondaryDiagonal = new int[matrixSize][2];

        for (int i = 0; i < matrixSize ; i++) {
            secondaryDiagonal[i][0] = i;
            secondaryDiagonal[i][1] = matrixSize - 1 - i;
        }

        /// check the diagonally Left To Right status
        // Convert all indexes from HashMap into a Set
        for (int[][] indexArray : allXYIndexesMap.values()) {
            // Use a HashSet for fast lookup
            Set<String> indexSet = new HashSet<>();
            for (int[] pair : indexArray) {
                indexSet.add(pair[0] + "," + pair[1]); // Store as "row,col"
            }
            // Check if all primaryDiagonal indexes exist in the Set
            for (int[] target : secondaryDiagonal) {
                if (!indexSet.contains(target[0] + "," + target[1])) {
                    status = false; // If any index is missing, return false
                }
            }
        }

        if(status){
            double rewardMultiplier = rootObject.getWinCombinations().get("same_symbols_diagonally_right_to_left").getRewardMultiplier();
            String symbolOfDiagonallyRightToLeft = matrixWithBonusSymbols[0][matrixSize-1];

            double currentValueForSymbolAndDuplicationBonus = symbolValueAndSymbolDuplicationBonusMap.get(symbolOfDiagonallyRightToLeft);
            double diagonallyValueForSymbolAndDuplicationBonus = currentValueForSymbolAndDuplicationBonus * rewardMultiplier;

            symbolValueAndSymbolDuplicationBonusMap.forEach((key, value) -> {
                if (key.equals(symbolOfDiagonallyRightToLeft)) {
                    symbolValueAndSymbolDuplicationBonusMap.put(key, diagonallyValueForSymbolAndDuplicationBonus);
                }
            });
            List<String> newList = new ArrayList<>();
            newList.add("same_symbols_diagonally_right_to_left");
            appliedWinningCombinationsForOutPut.computeIfAbsent(symbolOfDiagonallyRightToLeft, k -> new ArrayList<>()).addAll(newList);
        }
        return appliedWinningCombinationsForOutPut;
    }



    // sub method of addPatternBonus, this can find the all X,Y All Winning Indices
    public static void findAllWinningIndices( HashMap<String, List<Integer>> allXindexesMap,HashMap<String, List<Integer>> allYindexesMap , HashMap<String, int[][]> allXYIndexesMap,
                                                 String[][] matrixWithBonusSymbols, HashMap<String, Integer> winningDuplicateSymbols) {

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
            allXYIndexesMap.put(key, positions.toArray(new int[0][]));
            allXindexesMap.put(key, allXindexes);
            allYindexesMap.put(key, allYindexes);
        }

        // Printing the indexMap
/*        for (Map.Entry<String, int[][]> entry : allXYIndexesMap.entrySet()) {
            System.out.println("Key: " + entry.getKey());
            for (int[] pos : entry.getValue()) {
                System.out.println("  (" + pos[0] + ", " + pos[1] + ")");
            }
        }

        // Printing the map
        for (Map.Entry<String, List<Integer>> entry : allXindexesMap.entrySet()) {
            System.out.println("X Key: " + entry.getKey() + ", Values: " + entry.getValue());
        }

        // Printing the map
        for (Map.Entry<String, List<Integer>> entry : allYindexesMap.entrySet()) {
            System.out.println("Y Key: " + entry.getKey() + ", Values: " + entry.getValue());
        }*/

    }


    // sub method of addPatternBonus, this method can identify if sequence has a Horizontal Sequence
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
//            System.out.println("Horizontal Sequence, symbol appearing more than " + columnLength + " times: " + keysWithFrequentNumbers);
        }
        return keysWithFrequentNumbers;
    }

    // sub method of addPatternBonus, this method can identify if sequence has a Vertical Sequence
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
//            System.out.println("Vertical Sequence, symbol appearing more than " + rowLength + " times: " + keysWithFrequentNumbers);
        }
        return keysWithFrequentNumbers;
    }



     public static void main(String[] args) throws FileNotFoundException {

         String configPath = null;
         double betAmount = 0; // Default value

         for (int i = 0; i < args.length; i++) {
             if (args[i].equals("--config") && i + 1 < args.length) {
                 configPath = args[i + 1];
             } else if (args[i].equals("--betting-amount") && i + 1 < args.length) {
                 betAmount = Double.parseDouble(args[i + 1]);
             }
         }

         if(betAmount == 0){
             throw new RuntimeException("The betting amount should be greater than 0.");
         }

         InputStream configFileInputStream = null;

         if (configPath != null) {
             configFileInputStream = new FileInputStream(configPath);
//             throw new RuntimeException("Config file path is required!");
         }else {
             configFileInputStream = Game.class.getClassLoader().getResourceAsStream("config.json");
         }

        StringBuilder bonusKey = new StringBuilder("");
        HashMap<String, List<String>> appliedWinningCombinationsForOutPut = new HashMap<>();

        if (configFileInputStream == null) {
            System.out.println("File not found! The config.json file should be provided.");
            return;
        }
        try {

            // Create ObjectMapper for reading JSON
            ObjectMapper objectMapper = new ObjectMapper();
            // Read the JSON into the root object (RootObject is the class representing the JSON structure)
            RootObject rootObject = objectMapper.readValue(configFileInputStream, RootObject.class);


            String[][] standardSymbolMatrix= generateStandardSymbolMatrix(rootObject);
            String[][]  matrixWithBonusSymbols = addBonusSymbolToMatrix(rootObject,standardSymbolMatrix, bonusKey);

            // Print the 2D array
/*            for (String[] row : matrixWithBonusSymbols) {
                for (String col : row) {
                    System.out.print(col + "   ");
                }
                System.out.println();
            }*/

            Map<String, Integer>  winningDuplicateSymbols = getDuplicateSymbols(matrixWithBonusSymbols);

            Map<String, Double> symbolValueAndSymbolDuplicationBonusMap  = addSymbolValueAndSymbolDuplicationBonus(winningDuplicateSymbols, rootObject, betAmount );

            // pattern based Bonus calculation
            double winningAmountWithSymbolBonusAndPatternBonus = addPatternBonus( matrixWithBonusSymbols, (HashMap<String, Integer>) winningDuplicateSymbols
                    ,rootObject, symbolValueAndSymbolDuplicationBonusMap, appliedWinningCombinationsForOutPut );

            double finalReword = addSymbolBonus( bonusKey, winningAmountWithSymbolBonusAndPatternBonus,  rootObject, winningDuplicateSymbols );

            appliedWinningCombinationsForOutPut = updateWinningCombinationsForOutput(winningDuplicateSymbols,  appliedWinningCombinationsForOutPut);

            // final output
            OutPut outPut = new OutPut(
                    matrixWithBonusSymbols,
                    finalReword,
                    appliedWinningCombinationsForOutPut,
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