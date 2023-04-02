package dev.arubik.realmcraft.Managers.Command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.Plugin;

import dev.arubik.realmcraft.Handlers.RealMessage;
import dev.arubik.realmcraft.Handlers.RealMessage.DebugType;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.HashMap;

public class CommandMapper {

    private final static Field COMMAND_MAP_FIELD;
    private final static CommandMap COMMAND_MAP;
    @Getter
    private static HashMap<String, Command> commands = new HashMap<String, Command>();
    private String FALLBACK_PREFIX;
    private Plugin plugin;

    public CommandMapper(String fallbackPrefix, Plugin plugin) {
        this.FALLBACK_PREFIX = fallbackPrefix;
        this.plugin = plugin;
    }

    static {
        try {
            COMMAND_MAP_FIELD = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            COMMAND_MAP_FIELD.setAccessible(true);

            COMMAND_MAP = (CommandMap) COMMAND_MAP_FIELD.get(Bukkit.getServer());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("CommandMap not found in Bukkit Server while loading Wordle plugin");
        }
    }

    public void register(RealCommand command) {
        COMMAND_MAP.register(FALLBACK_PREFIX, command.command);
        if (COMMAND_MAP.getCommand(command.command.getName()) != null) {
            RealMessage.sendConsoleMessage(DebugType.ERROR,
                    "Command " + command.command.getName() + "alredy registered");
            Bukkit.getPluginManager().registerEvents(command, plugin);
        } else {
            Bukkit.getPluginManager().registerEvents(command, plugin);
            commands.put(command.command.getName(), command.command);
        }
    }

    public void unregister(RealCommand command) {
        COMMAND_MAP.getCommand(command.command.getName()).unregister(COMMAND_MAP);
    }

}