package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.Probabilities;
import dto.RootObject;
import dto.StandardSymbol;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Main {



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

        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("config.json");

        if (inputStream == null) {
            System.out.println("File not found!");
            return;
        }

        try {

//            {
//                "column": 0,
//                    "row": 0,
//                    "symbols": {
//                        "A": 1,
//                        "B": 2,
//                        "C": 3,
//                        "D": 4,
//                        "E": 5,
//                        "F": 6
//            }
//            },

            // Create ObjectMapper for reading JSON
            ObjectMapper objectMapper = new ObjectMapper();

            // Read the JSON into the root object (RootObject is the class representing the JSON structure)
            RootObject rootObject = objectMapper.readValue(inputStream, RootObject.class);

            // Access and print some fields to verify it worked
            System.out.println("Columns: " + rootObject.getColumns());
            System.out.println("Rows: " + rootObject.getRows());

            List<StandardSymbol> standardSymbolList = rootObject.getProbabilities().getStandardSymbols();

            // Access probabilities
            for (StandardSymbol symbol : standardSymbolList) {
                System.out.println("Column: " + symbol.getColumn() + ", Row: " + symbol.getRow());
                System.out.println("Symbols: " + symbol.getSymbols());

                Map<String, Integer> symbolMap =symbol.getSymbols();


                Random random = new Random();
                int totalWeight =0;

                // Sum all values in the map
                totalWeight = symbolMap.values().stream().mapToInt(Integer::intValue).sum();
                int randomValue = random.nextInt(totalWeight - 1); // Generates 0 to (totalWeight - 1)

                System.out.println("totalWeight: " + totalWeight);
                System.out.println("randomValue: " + randomValue);


                int currentSum = 0;
                String cell = null;
                ArrayList<String> ansList = new ArrayList<>();

                for (Map.Entry<String, Integer> entry : symbolMap.entrySet()) {
                    currentSum += entry.getValue(); // Add the weight of the current key
                    if (currentSum > randomValue) {
                      cell = entry.getKey();
                      ansList.add(cell);
                      break;
                    }

                }

                System.out.println("ansList  " + ansList);
                System.out.println("########");

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}