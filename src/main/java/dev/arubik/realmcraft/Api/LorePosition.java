package dev.arubik.realmcraft.Api;

public enum LorePosition {
    TOP, BOTTOM, MID, REPLACE;

    private LorePosition() {
        replace = "";
    }

    private LorePosition(String replace) {
        this.replace = replace;
    }

    public String replace;

    public void setReplace(LorePosition pos, String s) {
        pos.replace = s;
    }

    public static LorePosition genReplace(String s) {
        LorePosition a = LorePosition.REPLACE;
        a.setReplace(a, s);
        return a;
    }
}
