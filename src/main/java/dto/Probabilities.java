package dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import java.util.List;
import java.util.Map;

public class Probabilities {


    @JsonProperty("standard_symbols")
    private List<StandardSymbol> standardSymbols;  // Correct field name in camelCase

    @JsonProperty("bonus_symbols")
    private BonusSymbols bonusSymbols;


    public BonusSymbols getBonusSymbols() {
        return bonusSymbols;
    }

    public void setBonusSymbols(BonusSymbols bonusSymbols) {
        this.bonusSymbols = bonusSymbols;
    }


    // Getters and Setters
    public List<StandardSymbol> getStandardSymbols() {
        return standardSymbols;
    }

    public void setStandardSymbols(List<StandardSymbol> standardSymbols) {
        this.standardSymbols = standardSymbols;
    }


}
