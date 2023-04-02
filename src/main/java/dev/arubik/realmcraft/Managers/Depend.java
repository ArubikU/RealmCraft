package dev.arubik.realmcraft.Managers;

import org.bukkit.Bukkit;

public interface Depend {
    String[] getDependatsPlugins();

    public default boolean isDependatsPluginsEnabled() {
        for (String pluginName : getDependatsPlugins()) {
            if (!isPluginEnabled(pluginName))
                return false;
        }
        return true;
    }

    public static boolean isPluginEnabled(String pluginName) {
        return Bukkit.getServer().getPluginManager().getPlugin(pluginName) != null;
    }

    public static boolean isPluginEnabled(Depend depend) {
        for (String pluginName : depend.getDependatsPlugins()) {
            if (!isPluginEnabled(pluginName))
                return false;
        }
        return true;
    }
}
