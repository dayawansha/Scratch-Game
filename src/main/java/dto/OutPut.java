package dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;

public class OutPut {

    private String[][]  matrix;

    private double reward;

    @JsonProperty("applied_winning_combinations")
    private HashMap<String, List<String>> appliedWinningCombinations;

    @JsonProperty("applied_bonus_symbol")
    private String appliedBonusSymbol;


    // Constructor
    public OutPut(String[][] matrix, double reward,
                  HashMap<String, List<String>> appliedWinningCombinations,
                  String appliedBonusSymbol) {
        this.matrix = matrix;
        this.reward = reward;
        this.appliedWinningCombinations = appliedWinningCombinations;
        this.appliedBonusSymbol = appliedBonusSymbol;
    }

//    // Override toString() method to print the DTO object
//    @Override
//    public String toString() {
//        return "OutPut{matrix='" + matrix + "', " +
//                "reward=" + reward +
//                "appliedWinningCombinations=" + appliedWinningCombinations +
//                "appliedBonusSymbol=" + appliedBonusSymbol +
//                "}";
//    }

    public String[][] getMatrix() {
        return matrix;
    }

    public void setMatrix(String[][] matrix) {
        this.matrix = matrix;
    }

    public double getReward() {
        return reward;
    }

    public void setReward(double reward) {
        this.reward = reward;
    }

    public HashMap<String, List<String>> getAppliedWinningCombinations() {
        return appliedWinningCombinations;
    }

    public void setAppliedWinningCombinations(HashMap<String, List<String>> appliedWinningCombinations) {
        this.appliedWinningCombinations = appliedWinningCombinations;
    }

    public String getAppliedBonusSymbol() {
        return appliedBonusSymbol;
    }

    public void setAppliedBonusSymbol(String appliedBonusSymbol) {
        this.appliedBonusSymbol = appliedBonusSymbol;
    }
}
