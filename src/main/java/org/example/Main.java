package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.Probabilities;
import dto.RootObject;
import dto.StandardSymbol;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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
            // Create ObjectMapper for reading JSON
            ObjectMapper objectMapper = new ObjectMapper();

            // Read the JSON into the root object (RootObject is the class representing the JSON structure)
            RootObject rootObject = objectMapper.readValue(inputStream, RootObject.class);

            // Access and print some fields to verify it worked
            System.out.println("Columns: " + rootObject.getColumns());
            System.out.println("Rows: " + rootObject.getRows());

            // Access probabilities
            for (StandardSymbol symbol : rootObject.getProbabilities().getStandardSymbols()) {
                System.out.println("Column: " + symbol.getColumn() + ", Row: " + symbol.getRow());
                System.out.println("Symbols: " + symbol.getSymbols());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


//        Random random = new Random();
//        int randomValue = random.nextInt(10); // Generates 0 to (totalWeight - 1)
//        int currentSum = 0;
//
//        Map<String, Integer> probabilities = new HashMap<>();
//
//        for (Map.Entry<String, Integer> entry : probabilities.entrySet()) {
//            currentSum += entry.getValue(); // Add the weight of the current key
//            if (randomValue < currentSum) {
////                return entry.getKey();
//            }
//        }


        System.out.println("Hello world!");


    }



}