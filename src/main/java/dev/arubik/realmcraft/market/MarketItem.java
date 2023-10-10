package dev.arubik.realmcraft.market;

import dev.arubik.realmcraft.FileManagement.InteractiveFile;
import lombok.Setter;

public class MarketItem {

    private String id;

    /**
     * The modification to the price of the item
     * from -85% to 85%. applied to the base price
     */

    @Setter
    private double modification;
    @Setter
    private double buyModification;
    @Setter
    private int buys;
    @Setter
    private int sales;
    @Setter
    private int playersBuyCount;
    @Setter
    private int playersSellCount;

    public MarketItem(String id, double modification, double buyModification, int buys, int sales, int playersBuyCount,
            int playersSellCount) {
        this.id = id;
        this.modification = modification;
        this.buyModification = buyModification;
        this.buys = buys;
        this.sales = sales;
        this.playersBuyCount = playersBuyCount;
        this.playersSellCount = playersSellCount;

    }

    public MarketItem(String id) {
        this.id = id;
        this.modification = 0;
        this.buyModification = 0;
        this.buys = 0;
        this.sales = 0;
        this.playersBuyCount = 0;
        this.playersSellCount = 0;
    }

    public void serialize(String path, InteractiveFile file) {
        file.set(path + "." + this.id + ".modification", this.modification);
        file.set(path + "." + this.id + ".buyModification", this.buyModification);
        file.set(path + "." + this.id + ".buys", this.buys);
        file.set(path + "." + this.id + ".sales", this.sales);
        file.set(path + "." + this.id + ".playersBuyCount", this.playersBuyCount);
        file.set(path + "." + this.id + ".playersSellCount", this.playersSellCount);
    }

    public MarketItem deserialize(String path, InteractiveFile file) {
        this.modification = file.getDouble(path + "." + this.id + ".modification");
        this.buyModification = file.getDouble(path + "." + this.id + ".buyModification");
        this.buys = file.getInteger(path + "." + this.id + ".buys");
        this.sales = file.getInteger(path + "." + this.id + ".sales");
        this.playersBuyCount = file.getInteger(path + "." + this.id + ".playersBuyCount");
        this.playersSellCount = file.getInteger(path + "." + this.id + ".playersSellCount");
        return this;
    }
}
