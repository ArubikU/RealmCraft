package dev.arubik.realmcraft.IReplacer;

public enum ReplacementTypes {
    NAME,
    VANILLA,
    NBTTAGMATCH,
    CUSTOMMODELDATA,
    SKULLOWNER,
    LORECONTAINS,
    NBTCONTAINS;

    public static ReplacementTypes fromString(String string) {
        switch (string.toUpperCase()) {
            case "NAME":
                return NAME;
            case "VANILLA":
                return VANILLA;
            case "NBTTAGMATCH":
                return NBTTAGMATCH;
            case "CUSTOMMODELDATA":
                return CUSTOMMODELDATA;
            case "SKULLOWNER":
                return SKULLOWNER;
            case "LORECONTAINS":
                return LORECONTAINS;
            case "NBTCONTAINS":
                return NBTCONTAINS;
            default:
                return NAME;
        }
    }
}
