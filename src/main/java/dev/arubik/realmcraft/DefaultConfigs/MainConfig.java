package dev.arubik.realmcraft.DefaultConfigs;

import java.util.HashMap;
import java.util.Map;

import dev.arubik.realmcraft.Handlers.RealMessage.DebugType;
import dev.arubik.realmcraft.realmcraft.Modules;

public class MainConfig implements RealLoader {

    @Override
    public Map<String, Object> getDefaultValues() {
        Map<String, Object> defaultValues = new HashMap<String, Object>();
        defaultValues.put("file.separator", "/");

        for (DebugType type : DebugType.values()) {
            defaultValues.put("debug." + type.name(), false);
        }
        defaultValues.put("IReplacer.Pickup-Event-Enabled", true);
        defaultValues.put("IReplacer.Click-Event-Enabled", true);
        defaultValues.put("IReplacer.InventoryMove-Event-Enabled", true);
        defaultValues.put("IReplacer.InventoryOpen-Event-Enabled", true);
        defaultValues.put("IReplacer.Crafting-Event-Enabled", true);
        defaultValues.put("IReplacer.Furnace-Event-Enabled", true);
        defaultValues.put("IReplacer.Smithing-Event-Enabled", false);
        for (Modules module : Modules.values()) {
            defaultValues.put("modules." + module.name().replace("_", "-"), true);
        }
        defaultValues.put("mode.api", false);
        return defaultValues;
    }

    @Override
    public Map<Integer, String> getComments() {
        return null;
    }

}
