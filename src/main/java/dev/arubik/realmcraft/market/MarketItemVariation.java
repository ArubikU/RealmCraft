package dev.arubik.realmcraft.market;

import dev.arubik.realmcraft.FileManagement.InteractiveSection;

public class MarketItemVariation {

    public int basePrice = 0;

    /*
     * The rangue is from -30% to 40% of midSales
     * if lower thant the rangue, the price of sale will be up
     * and buy will be down
     * if higher thant the rangue, the price of sale will be down
     * and buy will be up
     */
    public int midSales = 0;

    public boolean isBuyEnabled = true;

    public static MarketItemVariation fromConfigurationSection(InteractiveSection ItemSection) {

        MarketItemVariation variation = new MarketItemVariation();
        variation.basePrice = ItemSection.getOrDefault("basePrice", variation.basePrice);
        variation.midSales = ItemSection.getOrDefault("midSales", variation.midSales);

        variation.isBuyEnabled = ItemSection.getOrDefault("isBuyEnabled", variation.isBuyEnabled);

        return variation;
    }
}
