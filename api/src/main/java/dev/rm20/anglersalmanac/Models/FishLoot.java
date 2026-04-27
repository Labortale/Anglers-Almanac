package dev.rm20.anglersalmanac.Models;

public class FishLoot {
    protected String id;
    protected String itemID;
    protected String name;
    protected String rarity;
    protected int weight;

    public FishLoot() {}

    // Getters
    public String getId() { return id; }
    public String getItemID() { return itemID; }
    public String getName() { return name; }
    public String getRarity() { return rarity; }


    public static String getRarityColour(String Rarity) {
        if (Rarity == null) return "#5A5B5B";

        return switch (Rarity.toLowerCase()) {
            case "common" -> "#5A5B5B";
            case "uncommon" -> "#005205";
            case "rare" -> "#5481EB";
            case "epic" -> "#5C337B";
            case "legendary" -> "#FFDB4B";
            default -> "#5A5B5B"; // Default weight for unknown rarities
        };
    }
}