package dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SymbolDetails {



    @JsonProperty("reward_multiplier")
    private double rewardMultiplier;

    @JsonProperty("type")
    private String type;

    @JsonProperty("impact")
    private String impact;

    @JsonProperty("extra")
    private Integer extra;  // Add this field for the "extra" property

    // Getters and Setters
    public double getRewardMultiplier() {
        return rewardMultiplier;
    }

    public void setRewardMultiplier(double rewardMultiplier) {
        this.rewardMultiplier = rewardMultiplier;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImpact() {
        return impact;
    }

    public void setImpact(String impact) {
        this.impact = impact;
    }

    public Integer getExtra() {
        return extra;
    }

    public void setExtra(Integer extra) {
        this.extra = extra;
    }
}

