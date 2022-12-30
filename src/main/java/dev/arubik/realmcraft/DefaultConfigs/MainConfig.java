package dev.arubik.realmcraft.DefaultConfigs;

import java.util.HashMap;
import java.util.Map;

public class MainConfig implements RealLoader {

    @Override
    public Map<String, Object> getDefaultValues() {
        Map<String, Object> defaultValues = new HashMap<String, Object>();
        defaultValues.put("file.separator", "/");
        return defaultValues;
    }

}
