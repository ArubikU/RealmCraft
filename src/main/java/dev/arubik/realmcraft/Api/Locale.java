package dev.arubik.realmcraft.Api;

public enum Locale {
    EN,
    ES,
    FR,
    DE,
    IT,
    PT,
    RU,
    JA,
    KO,
    ZH,
    ZH_TW,
    PL,
    NL,
    HU,
    SV,
    CS,
    TR,
    DA,
    FI;

    public String getLang() {
        return this.name().toLowerCase();
    }
}
