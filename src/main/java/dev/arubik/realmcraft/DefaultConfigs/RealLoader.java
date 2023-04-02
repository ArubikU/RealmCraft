package dev.arubik.realmcraft.DefaultConfigs;

import java.util.Map;

public interface RealLoader {
    Map<String, Object> getDefaultValues();

    Map<Integer, String> getComments();
}
