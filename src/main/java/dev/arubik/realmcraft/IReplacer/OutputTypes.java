package dev.arubik.realmcraft.IReplacer;

public enum OutputTypes {
    VANILLA,
    MMOITEMS,
    MYTHICMOBS,
    REALSTACK,
    COMMAND;

    public static OutputTypes fromString(String string) {
        switch (string.toUpperCase()) {
            case "VANILLA": {
                return VANILLA;
            }
            case "MMOITEMS": {
                return MMOITEMS;
            }
            case "MYTHICMOBS": {
                return MYTHICMOBS;
            }
            case "COMMAND": {
                return COMMAND;
            }
            case "REALSTACK": {
                return REALSTACK;
            }
            default: {
                return VANILLA;
            }
        }
    }
}
