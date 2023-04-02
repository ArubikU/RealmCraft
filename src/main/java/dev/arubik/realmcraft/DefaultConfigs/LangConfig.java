package dev.arubik.realmcraft.DefaultConfigs;

import java.util.HashMap;
import java.util.Map;

public class LangConfig implements RealLoader {

    @Override
    public Map<String, Object> getDefaultValues() {
        Map<String, Object> defaultValues = new HashMap<String, Object>();
        defaultValues.put("lore.durability", "<white>Durability: {durability} / {max_durability}");
        return defaultValues;
    }

    @Override
    public Map<Integer, String> getComments() {
        Map<Integer, String> comments = new HashMap<Integer, String>();
        // comments.put(0, "This is the language file for RealmCraft");
        // comments.put(1, "You can change the messages here");
        // comments.put(2, "You can use MiniMessage here");
        // comments.put(3, "You can use the following placeholders:");
        // comments.put(4, "{durability} - current durability");
        // comments.put(5, "{max_durability} - max durability");
        return comments;
    }
}
