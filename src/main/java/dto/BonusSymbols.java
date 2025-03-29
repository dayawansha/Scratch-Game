package dto;

import java.util.Map;

public class BonusSymbols {

    private Map<String, Integer> symbols; // Maps symbol names to their values (e.g., "A": 1)

    public Map<String, Integer> getSymbols() {
        return symbols;
    }

    public void setSymbols(Map<String, Integer> symbols) {
        this.symbols = symbols;
    }
}
