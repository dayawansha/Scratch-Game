package dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class RootObject {
    private int columns;
    private int rows;
    private Map<String, SymbolDetails> symbols;
    private Probabilities probabilities;

    @JsonProperty("win_combinations")
    private Map<String, WinCombination> winCombinations;

    // Getters and Setters
    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public Map<String, SymbolDetails> getSymbols() {
        return symbols;
    }

    public void setSymbols(Map<String, SymbolDetails> symbols) {
        this.symbols = symbols;
    }

    public Probabilities getProbabilities() {
        return probabilities;
    }

    public void setProbabilities(Probabilities probabilities) {
        this.probabilities = probabilities;
    }

    public Map<String, WinCombination> getWinCombinations() {
        return winCombinations;
    }

    public void setWinCombinations(Map<String, WinCombination> winCombinations) {
        this.winCombinations = winCombinations;
    }
}

