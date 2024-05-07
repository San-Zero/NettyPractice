package org.example.message;

public class TierCheckMessage {
    private final String tier;

    public TierCheckMessage(String tier) {
        this.tier = tier;
    }

    public String getTier() {
        return tier;
    }
}
